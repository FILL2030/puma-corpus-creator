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

/**
 * Internal class to encapsulate the running of a Task.
 *
 * When a task has terminated this object performs callbacks on success and failure allowing the TaskManager
 * to be notified and perform necessary work.
 */
class TaskWrapper implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(TaskWrapper.class);

	private Task task;
	private TaskMonitor taskMonitor;
	private TaskCompletionHandler taskCompletionHandler;
	private long executionNumber;


	public TaskWrapper(Task task, TaskMonitor taskMonitor) {
		this.task = task;
		this.taskMonitor = taskMonitor;
	}

	public TaskWrapper(Task task, TaskMonitor taskMonitor, TaskCompletionHandler taskCompletionHandler) {
		this.task = task;
		this.taskMonitor = taskMonitor;
		this.taskCompletionHandler = taskCompletionHandler;
	}

	public TaskPriority getPriority() {
		return this.task.getPriority();
	}


	/**
	 * Called by the Tread Pool (via TaskDispatcher) when a thread is available.
	 */
	@Override
	public void run() {
		try {
			// Callback on task completed
			if (this.taskMonitor != null) {
				this.taskMonitor.onTaskWillStart(this.task);
			}

			// Do the call
			this.task.run();

			// Update monitor
			if (this.taskMonitor != null) {
				this.taskMonitor.onTaskCompleted(this.task);
			}

			// Callback on task completed
			if (this.taskCompletionHandler != null) {
				this.taskCompletionHandler.onTaskCompleted(this.task);
			}

		} catch (Exception exception) {
			log.error(exception.getMessage(), exception);

			PumaError pumaError = new PumaError("Exception occurred during task execution (" + exception.getMessage() + ")");
			// Update monitor
			if (this.taskMonitor != null) {
				this.taskMonitor.onTaskError(this.task, pumaError);
			}

			// Callback on task completed
			if (this.taskCompletionHandler != null) {
				this.taskCompletionHandler.onTaskError(this.task, pumaError);
			}
		}
	}

	/**
	 * Returns the encapsulated task
	 * @return the task
	 */
	public Task getTask() {
		return task;
	}

	public void onAddedToThreadPool(long executionNumber) {
		this.executionNumber = executionNumber;

		this.task.onAddedToThreadPool();
	}

	public long getExecutionNumber() {
		return executionNumber;
	}
}
