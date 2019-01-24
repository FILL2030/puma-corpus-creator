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

import java.util.concurrent.ThreadFactory;

public class TaskThreadFactory implements ThreadFactory {

	private static class TaskThread extends Thread {

		private static final Logger log = LoggerFactory.getLogger(TaskThreadFactory.class);

		private static ThreadLocal<String> threadName = new ThreadLocal<String>();

		public TaskThread(Runnable target) {
			super(target);
		}

		@Override
		public void run() {
			// Initialise any necessary things for the thread here (keep them available for all later usage)
			getThreadName();

			super.run();
		}

		public static String getThreadName() {
			if (threadName.get() == null) {
				threadName.set("TaskThread-" + Thread.currentThread().getId());
				log.debug("Creating new thread " + threadName.get());
			}

			return threadName.get();
		}
	}

	@Override
	public Thread newThread(Runnable runnable) {
		return new TaskThread(runnable);
	}
}
