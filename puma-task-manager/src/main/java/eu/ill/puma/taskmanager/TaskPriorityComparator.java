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

import java.util.Comparator;

public class TaskPriorityComparator<T extends TaskWrapper> implements Comparator<T> {

	@Override
	public int compare(T t1, T t2) {

		// Compare priorities initially
		int priorityComparison = t1.getPriority().compareTo(t2.getPriority());
		if (priorityComparison != 0) {
			return priorityComparison;
		}

		// Compare execution number if equal priority to maintain FIFO
		int execComparison = (int)(t1.getExecutionNumber() - t2.getExecutionNumber());

		return execComparison;
	}

}
