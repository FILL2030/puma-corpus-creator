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

//@Component
public class TaskManager {

	private boolean isActive = true;

	public TaskManager() {
	}

	public void executeTask(Task task) {
		try {
			task.run();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void executeTask(Task task, TaskCompletionHandler taskCompletionHandler) {
		if (this.isActive) {
			try {
				task.run();

				taskCompletionHandler.onTaskCompleted(task);

			} catch (Exception e) {
				taskCompletionHandler.onTaskError(task, new PumaError("Task Failed (" + e.getMessage() + ")"));
			}
		}
	}

	public void cancelTask(Task task) {

	}

	public int getPoolSize() {
		return 1;
	}

	public void disable() {
		this.isActive = false;
	}

	public void enable() {
		this.isActive = true;
	}
}