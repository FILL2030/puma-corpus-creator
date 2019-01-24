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
package eu.ill.puma.taskmanager.threadpool.tasks;

import eu.ill.puma.taskmanager.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SleepingWithNextTestTask extends Task<Long> {

	private static final Logger log = LoggerFactory.getLogger(SleepingWithNextTestTask.class);

	public interface TestTaskCallback {
		void onTaskTerminated(long data);
	}

	private long index;
	private long sleepTimeMillis;
	private SleepingWithNextTestTask nextTask = null;
	private TestTaskCallback callback;

	public SleepingWithNextTestTask(long index, TestTaskCallback callback) {
		this.callback = callback;
		this.index = index;
		this.sleepTimeMillis  = this.index * 1000;
		if (this.index > 1) {
			this.nextTask = new SleepingWithNextTestTask(this.index - 1, this.callback);
		}
	}

	@Override
	public Long execute() {
		// Call web service


		// wait for documents returned


		// Store documents


		try {
			Thread.sleep(this.sleepTimeMillis);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		String message = "Slept for " + this.sleepTimeMillis + "ms";
		log.info(message);

		this.callback.onTaskTerminated(this.index);

		return this.index;
	}

	public Task getNextTask() {
		return this.nextTask;
	}

}
