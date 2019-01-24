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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The TaskDispatched manages a thread pool and provided an interface to execute tasks.
 */
public class TaskDispatcher {

	private static final Logger log = LoggerFactory.getLogger(TaskDispatcher.class);

	private static long TASK_EXECUTION_COUNTER = 0;

	private ThreadPoolExecutor threadPoolExecutor;

	private int minimumQueueSizeForPriorities = 64;
	private int maximumQueueSizeForPriorities = 512;
	private boolean prioritiesActive = true;

	/**
	 * Initialises the the tread pool with a fixed size
	 *
	 * @param threadPoolSize The fixed thread pool size
	 */
	public void init(int threadPoolSize) {
		log.info("Starting the TaskDispatcher with " + threadPoolSize + " threads") ;
		PriorityBlockingQueue<Runnable> priorityBlockingQueue = new PriorityBlockingQueue<Runnable>(20, new TaskPriorityComparator());
		this.threadPoolExecutor = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 10, TimeUnit.SECONDS, priorityBlockingQueue, new TaskThreadFactory());
	}

	/**
	 * Stops the thread pool and any running tasks. All pending tasks are removed.
	 */
	public void shutdown() {
		// Shutdown the threadpool immediately : all waiting tasks are removed and running ones are stopped
		log.info("Shutting down TaskDispatcher");
		this.threadPoolExecutor.shutdownNow();

		// Wait for shutdown to finish
		try {
			this.threadPoolExecutor.awaitTermination(5000, TimeUnit.MILLISECONDS);

		} catch (InterruptedException e) {
			log.error("Failed to shut down thread pool correctly : " + e.getMessage(), e);
		}
	}


	/**
	 * Stops and starts the tread pool with a given number of threads.
	 * @param threadPoolSize The fixed thread pool size
	 */
	public void reset(int threadPoolSize) {
		// Shutdown
		this.shutdown();

		// Re-init with thread-pool size
		this.init(threadPoolSize);
	}

	/**
	 * Executes a task in the thread pool
	 * @param taskWrapper The task to be run
	 */
	public void executeTask(TaskWrapper taskWrapper) {
		long numberOfPendingTasks = this.getNumberOfPendingTasks();
		if (this.prioritiesActive && numberOfPendingTasks > this.maximumQueueSizeForPriorities) {
			log.info("Pending Task queue size = " + numberOfPendingTasks + " : deactivating priorities");
			TaskPriority.deactivatePriority();
			this.prioritiesActive = false;

		} else if (!this.prioritiesActive && numberOfPendingTasks < this.minimumQueueSizeForPriorities) {
			log.info("Pending Task queue size = " + numberOfPendingTasks + " : activating priorities");
			TaskPriority.activatePriority();
			this.prioritiesActive = true;
		}

		// Set the task status
		taskWrapper.onAddedToThreadPool(TASK_EXECUTION_COUNTER++);

		// Add the task to the tread pool
		this.threadPoolExecutor.execute(taskWrapper);
	}

	/**
	 * Cancels a task
	 * @param taskWrapper The task to be cancelled
	 */
	public void cancelTask(TaskWrapper taskWrapper) {
		// Remove from queue if queued
		this.threadPoolExecutor.remove(taskWrapper);
	}

	/**
	 * Returns the size of the thread pool
	 * @return The size of the thread pool
	 */
	public int getPoolSize() {
		return this.threadPoolExecutor.getCorePoolSize();
	}

	/**
	 * Get the current number of threads
	 * @return The current number of threads
	 */
	public int getCurrentNumberOfThreads() {
		return this.threadPoolExecutor.getPoolSize();
	}

	/**
	 * Get the number of threads that are actively executing tasks
	 * @return The active number of threads
	 */
	public int getActiveNumberOfThreads() {
		return this.threadPoolExecutor.getActiveCount();
	}

	/**
	 * Get the number of pending tasks
	 * @return The number of pending tasks
	 */
	public long getNumberOfPendingTasks() {
		return  this.threadPoolExecutor.getQueue().size();
	}

}
