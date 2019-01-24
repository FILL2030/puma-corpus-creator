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
package eu.ill.puma.indexer.task;

import eu.ill.puma.taskmanager.Task;

import java.util.List;

public abstract class IndexerTask extends Task {
	private static long counter;
	private long indexerTaskId;

	public abstract List<Long> getIds();

	public IndexerTask() {
		this.indexerTaskId = this.getNextId();
	}

	private synchronized long getNextId() {
		return counter++;
	}

	public long getIndexerTaskId() {
		return indexerTaskId;
	}
}
