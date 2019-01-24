/*
 * Copyright 2019 Institut Laue–Langevin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.ill.puma.taskmanager;

import eu.ill.puma.core.error.PumaError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The TaskManager centralises the execution of Tasks in a multi-threaded environment.
 *
 * All tasks are sent to the Task Dispatcher for execution.
 *
 */
@Component
public class TaskManager implements TaskMonitor {

	private static final Logger log = LoggerFactory.getLogger(TaskManager.class);

	@Value("${puma.taskManager.threadPoolSize}")
	private int threadPoolSize;

	private TaskDispatcher taskDispatcher = null;
	private List<Task> pendingTasks = new ArrayList<>();
	private List<Task> runningTasks = new ArrayList<>();
	private Map<Task, TaskWrapper> taskWrappers = new HashMap<>();

	public TaskManager() {
		// Create a new Task Dispatcher
		this.taskDispatcher = new TaskDispatcher();
	}

	/**
	 * Called when the system is up. Initialises the Task Dispatcher and puts any pending tasks on the queue.
	 */
	@PostConstruct
	public void init() {
		log.info("TaskManager Thread Pool Size = " + this.threadPoolSize);

		// Initialise TaskDispatcher
		this.taskDispatcher.init(this.threadPoolSize);
	}

	/**
	 * Resets the Task Manager and dispatcher
	 */
	public void reset() {
		// Reset the TaskDispatcher
		this.taskDispatcher.reset(this.threadPoolSize);

		// Interrupt all tasks
		this.interruptAllTasks();
	}


	/**
	 * Resets the Task Manager and Task Dispatcher with a given number of threads
	 * @param numberOfThreads The number of threads for the pool
	 */
	public void reset(int numberOfThreads) {
		// Reset the TaskDispatcher
		this.taskDispatcher.reset(numberOfThreads);

		// Interrupt all tasks
		this.interruptAllTasks();
	}

	/**
	 * Shuts down the Task Manager and dispatcher
	 */
	public void shutdown() {
		// Shutdown the TaskDispatcher
		this.taskDispatcher.shutdown();

		// Interrupt all tasks
		this.interruptAllTasks();
	}

	/**
	 * Interrupts all tasks and resets the arrays of running and pending tasks
	 */
	private synchronized void interruptAllTasks() {

		// Interrupt all pending and running tasks
		for (Task task : this.pendingTasks) {
			task.interrupt();
		}

		for (Task task : this.runningTasks) {
			task.interrupt();
		}

		this.pendingTasks = new ArrayList<>();
		this.runningTasks = new ArrayList<>();
		this.taskWrappers = new HashMap<>();
	}

	/*
	 * Called to execute a task. The task is send to the TaskDispatcher to be run asynchronously (when possible)
	 * and a callback is given to perform post-execution procedures.
	 */
	public synchronized  void executeTask(Task task) {
		// Add to list of pending tasks
		this.pendingTasks.add(task);

		// Encapsulate task in a task wrapper and register monitor
		TaskWrapper taskWrapper = new TaskWrapper(task, this);
		this.taskWrappers.put(task, taskWrapper);

		// Push task to the task dispatcher
		this.taskDispatcher.executeTask(taskWrapper);
	}

	/*
	 * Called to execute a task. The task is send to the TaskDispatcher to be run asynchronously (when possible)
	 * and a callback is given to perform post-execution procedures.
	 *
	 * A custom completion handler is passed as an argument that is called when the task completes/fails.
	 */
	public synchronized void executeTask(Task task, TaskCompletionHandler taskCompletionHandler) {
		// Add to list of pending tasks
		this.pendingTasks.add(task);

		// Encapsulate task in a task wrapper and register monitor and completion handler
		TaskWrapper taskWrapper = new TaskWrapper(task, this, taskCompletionHandler);
		this.taskWrappers.put(task, taskWrapper);

		// Push task to the task dispatcher
		this.taskDispatcher.executeTask(taskWrapper);
	}

	/**
	 * Removes a task from any pending queues and signals task to be cancelled:
	 * NOTE: cancelling of running tasks must be implemented on a per-task basis by examining the task.state.
	 * @param task The task to be cancelled
	 */
	public synchronized void cancelTask(Task task) {
		TaskWrapper taskWrapper = this.taskWrappers.get(task);
		if (taskWrapper != null) {
			this.taskDispatcher.cancelTask(taskWrapper);
			this.taskWrappers.remove(task);
		}

		// Notify task that is has been cancelled in case it is running
		task.cancel();

		this.pendingTasks.remove(task);
		this.runningTasks.remove(task);
	}

	/**
	 * Called when a task will execute
	 * @param task The task that will execute
	 */
	public synchronized void onTaskWillStart(Task task) {
		// Remove from pending tasks
		this.pendingTasks.remove(task);

		// Add to running tasks
		this.runningTasks.add(task);
	}

	/**
	 * Called when a task executes successfully.
	 * @param task The Task that has terminated
	 */
	public synchronized void onTaskCompleted(Task task) {
		// Remove from running tasks
		this.runningTasks.remove(task);

		// Remove task wrapped
		TaskWrapper taskWrapper = this.taskWrappers.get(task);
		if (taskWrapper != null) {
			this.taskWrappers.remove(task);
		}

		// Run task again or get chained task
		if (task.doRepeatRun()) {
			// Repeat the same task
			this.executeTask(task);

		} else if (task.hasNextTask()) {
			// Chain tasks
			this.executeTask(task.getNextTask());
		}
	}

	/**
	 * Called when a task fails.
	 * @param task The task that failed
	 * @param pumaError The error
	 */
	public synchronized void onTaskError(Task task, PumaError pumaError) {
		// Remove from running tasks
		this.runningTasks.remove(task);

		// Remove task wrapped
		TaskWrapper taskWrapper = this.taskWrappers.get(task);
		if (taskWrapper != null) {
			this.taskWrappers.remove(task);
		}
	}

	/**
	 * Returns the size of the thread pool
	 * @return The size of the thread pool
	 */
	public int getPoolSize() {
		return this.taskDispatcher.getPoolSize();
	}

	/**
	 * Get the current number of threads
	 * @return The current number of threads
	 */
	public int getCurrentNumberOfThreads() {
		return this.taskDispatcher.getCurrentNumberOfThreads();
	}

	/**
	 * Get the number of threads that are actively executing tasks
	 * @return The active number of threads
	 */
	public int getActiveNumberOfThreads() {
		return this.taskDispatcher.getActiveNumberOfThreads();
	}

	/**
	 * Get the number of pending tasks
	 * @return The number of pending tasks
	 */
	public long getNumberOfPendingTasks() {
		return this.taskDispatcher.getNumberOfPendingTasks();
	}
}
