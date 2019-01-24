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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BulkIndexationTask extends IndexerTask {
	private static final Logger log = LoggerFactory.getLogger(BulkIndexationTask.class);

	private List<Long> documentVersionsIdToIndex;

	private IndexerManager indexerManager;

	public BulkIndexationTask(List<Long> documentVersionsIdToIndex, IndexerManager indexerManager) {
		this.documentVersionsIdToIndex = documentVersionsIdToIndex;
		this.indexerManager = indexerManager;
		this.setPriority(TaskPriority.DEFAULT);
	}

	@Override
	public Object execute() throws Exception {

		//index
		log.info("Starting bulk indexation task (" + this.getIndexerTaskId() + ") with " + this.documentVersionsIdToIndex.size() + " document version ids");
		this.indexerManager.index(this.documentVersionsIdToIndex);
		log.info("Finished bulk indexation task (" + this.getIndexerTaskId() + ") with " + this.documentVersionsIdToIndex.size() + " document version ids");
		return null;
	}

	@Override
	public List<Long> getIds() {
		return documentVersionsIdToIndex;
	}
}
