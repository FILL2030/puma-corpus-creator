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

import eu.ill.puma.persistence.repository.PumaDataSource;
import eu.ill.puma.taskmanager.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SleepingTestTask extends Task<Long> {

	private static final Logger log = LoggerFactory.getLogger(SleepingTestTask.class);

	private long index;
	private long sleepTimeMillis;

	public SleepingTestTask(long index) {
		this.index = index;
		this.sleepTimeMillis  = this.index * 1000;
	}

	@Override
	public Long execute() throws Exception {
		try {
			Thread.sleep(this.sleepTimeMillis);

		} catch (InterruptedException e) {
			e.printStackTrace();
			throw e;
		}

		String message = "Slept for " + this.sleepTimeMillis + "ms";
		log.info(message);

		return this.index;
	}
}
