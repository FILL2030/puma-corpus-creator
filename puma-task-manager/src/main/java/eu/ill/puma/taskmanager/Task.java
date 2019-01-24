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

import eu.ill.puma.core.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * The Task class encapsulates unitary tasks of work that are handled by the TaskManager. This class is overloaded to
 * perform unique pieces of work.
 *
 * Tasks are run on the TaskDispatcher which handles the multi-threaded environment.
 *
 * Tasks can be synchronised by calling the "get" method, which also returns any data obtained once the task has
 * completed.
 *
 * Tasks can be chained meaning that once the task has run it can produce a new task to be put into the task queue.
 *
 * Tasks can be re-run meaning that the same task can be automatically placed onto the task queue once it has
 * terminated.
 *
 * @param <V>
 */
public abstract class Task<V> {

	private static final Logger log = LoggerFactory.getLogger(Task.class);

	private V data = null;
	private TaskState state = TaskState.CREATED;
	private Exception caughtException = null;
	private Date creationDate = new Date();
	private Date startDate;
	private long lifeInMillis = 0;
	private long durationInMillis = 0;
	private TaskPriority priority = TaskPriority.DEFAULT;


	/**
	 * Package private method to execute main functionality of the task in a threaded environment
	 */
	final synchronized void run() throws Exception {
		// Re-initialise the state of the task
		this.state = TaskState.RUNNING;
		this.data = null;
		this.caughtException = null;

		try {
			// Set the start time
			this.startDate = new Date();

			// Execute the task functionality
			this.data = this.execute();

			// Calculate duration
			this.durationInMillis = DateUtils.getMillisSince(this.startDate);
			this.lifeInMillis = DateUtils.getMillisSince(this.creationDate);

			// Update the state and notify all blocked threads waiting for the data
			this.state = TaskState.COMPLETED;
			this.notifyAll();

		} catch (Exception e) {
			log.error("Caught exception during execution of task: " + e.getMessage(), e);
			this.caughtException = e;

			// Calculate duration
			this.durationInMillis = DateUtils.getMillisSince(this.startDate);
			this.lifeInMillis = DateUtils.getMillisSince(this.creationDate);

			// Update the state and notify all blocked threads waiting for the data
			this.state = TaskState.FAILED;
			this.notifyAll();

			// Rethrow exception
			throw e;
		}
	}

	/**
	 * Called when a task is added to the task queue. Initialises the state of the task.
	 */
	final synchronized void onAddedToThreadPool() {
		// Update creation date : gives time since added to queue which could be useful for monitoring
		this.creationDate = new Date();

		// If being run for the first time set the state to PENDING, otherwise PAUSED if re-running.
		if (this.state.equals(TaskState.CREATED)) {
			this.state = TaskState.PENDING;

		} else {
			this.state = TaskState.PAUSED;
		}
	}

	/**
	 * Blocking call to obtain the task results data. If the task has run then the data is returned
	 * immediately. If the task is still running then the calling thread is blocked until the
	 * task has terminated before returning the data
	 * @return
	 */
	public synchronized final V get() throws Exception {
		V returnData = null;
		if (!this.state.equals(TaskState.COMPLETED) && !this.state.equals(TaskState.FAILED) && !this.state.equals(TaskState.INTERRUPTED)) {
			try {
				// Wait until task completion
				this.wait();

			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);

				throw e;
			}
		}

		// Test for exception
		if (this.caughtException != null) {
			// Re-throw exception
			throw this.caughtException;
		}

		returnData = this.data;

		return returnData;
	}

	/**
	 * Interrupts any waiting threads and sets the state to interrupted
	 */
	synchronized void interrupt() {
		// Update the state and notify all blocked threads waiting for the data
		this.state = TaskState.INTERRUPTED;
		this.notifyAll();
	}


	/**
	 * Interrupts any waiting threads and sets the state to interrupted
	 */
	synchronized void cancel() {
		// Update the state and notify all blocked threads waiting for the data
		this.state = TaskState.CANCELLED;
		this.notifyAll();
	}

	/**
	 * This method implements the main functionality of the task
	 *
	 * Override this method
	 */
	public abstract V execute() throws Exception;


	/**
	 * Returns true if another task follows
	 * @return True if another task follows
	 */
	public final boolean hasNextTask() {
		return this.getNextTask() != null;
	}

	/**
	 * Returns the next task in a task chain
	 *
	 * Override this method
	 * @return The following task
	 */
	public Task getNextTask() {
		return null;
	}

	/**
	 * Returns true if the task should run again
	 *
	 * Override this method
	 * @return True if task should run again
	 */
	public boolean doRepeatRun() {
		return false;
	}

	/*
	 * Returns the state of the task
	 * @return the Task state
	 */
	public TaskState getState() {
		return this.state;
	}

	/**
	 * Returns the caught exception if one exists
	 * @return The caught exception
	 */
	public Exception getCaughtException() {
		return caughtException;
	}

	/**
	 * Returns the date the task has been put onto the queue
	 * @return The date when the task has been put onto the queue
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * Returns the date the task started
	 * @return The date when the task started
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * Returns the task execution duration in milliseconds
	 * @returnthe task execution duration in milliseconds
	 */
	public long getDurationInMillis() {
		return durationInMillis;
	}

	/**
	 * Returns the task life time in milliseconds (execution duration plus time on queue)
	 * @returnthe task life time in milliseconds
	 */
	public long geLifeInMillis() {
		return lifeInMillis;
	}

	/**
	 * Returns the task priority
	 * @return The task priority
	 */
	public TaskPriority getPriority() {
		return priority;
	}

	/**
	 * Sets the task priority
	 * @param priority The priority for the task
	 */
	public void setPriority(TaskPriority priority) {
		this.priority = priority;
	}
}
