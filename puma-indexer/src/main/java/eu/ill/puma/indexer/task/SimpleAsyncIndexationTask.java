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

import java.util.ArrayList;
import java.util.List;

public class SimpleAsyncIndexationTask extends IndexerTask {
	private static final Logger log = LoggerFactory.getLogger(BulkIndexationTask.class);

	private Long documentVersionIdToIndex;

	private IndexerManager indexerManager;

	public SimpleAsyncIndexationTask(Long documentVersionIdToIndex, IndexerManager indexerManager) {
		this.documentVersionIdToIndex = documentVersionIdToIndex;
		this.indexerManager = indexerManager;
		this.setPriority(TaskPriority.DEFAULT);
	}

	@Override
	public Object execute() throws Exception {

		//index
		log.info("Starting simple indexation task (" + this.getIndexerTaskId() + ") with document version id " + this.documentVersionIdToIndex);
		this.indexerManager.index(this.documentVersionIdToIndex);
		log.info("Finished simple indexation task (" + this.getIndexerTaskId() + ") with document version id " + this.documentVersionIdToIndex);
		return null;
	}

	@Override
	public List<Long> getIds() {
		List<Long> ids = new ArrayList();
		ids.add(documentVersionIdToIndex);
		return ids;
	}
}
