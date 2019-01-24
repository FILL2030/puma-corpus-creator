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

public class TaskPriority implements Comparable<TaskPriority> {

	private static int defaultDefaultPriority = 0;
	private static int defaultLowPriority = -100;
	private static int defaultHighPriority = 100;
	private static int defaultUrgentPriority = 1000;

	public static TaskPriority DEFAULT = new TaskPriority(defaultDefaultPriority);
	public static TaskPriority LOW = new TaskPriority(defaultLowPriority);
	public static TaskPriority HIGH = new TaskPriority(defaultHighPriority);
	public static TaskPriority URGENT = new TaskPriority(defaultUrgentPriority);
	public static TaskPriority ASAP = new TaskPriority(999999);

	private int priority;

	public TaskPriority(int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public static void activatePriority() {
		DEFAULT.setPriority(defaultDefaultPriority);
		LOW.setPriority(defaultLowPriority);
		HIGH.setPriority(defaultHighPriority);
		URGENT.setPriority(defaultUrgentPriority);
	}

	public static void deactivatePriority() {
		DEFAULT.setPriority(0);
		DEFAULT.setPriority(0);
		DEFAULT.setPriority(0);
		DEFAULT.setPriority(0);
	}

	@Override
	public int compareTo(TaskPriority other) {
		if (this.priority < other.priority) {
			return 1;

		} else if (this.priority > other.priority) {
			return -1;
		}

		return 0;
	}
}
