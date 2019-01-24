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

import eu.ill.puma.indexer.manager.IndexerManager;
import eu.ill.puma.taskmanager.TaskPriority;

import java.util.ArrayList;
import java.util.List;

public class AsyncReIndexationTask extends IndexerTask {

	private IndexerManager indexerManager;

	public AsyncReIndexationTask(IndexerManager indexerManager) {
		this.indexerManager = indexerManager;
		this.setPriority(TaskPriority.ASAP);
	}

	@Override
	public Object execute() throws Exception {
		this.indexerManager.removeIndex();
		this.indexerManager.createNextTasks();
		return null;
	}

	@Override
	public List<Long> getIds() {
		return new ArrayList();
	}
}
