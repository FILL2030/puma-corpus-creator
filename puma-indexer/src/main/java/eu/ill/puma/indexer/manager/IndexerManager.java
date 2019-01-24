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
package eu.ill.puma.indexer.manager;

import eu.ill.puma.core.error.PumaError;
import eu.ill.puma.indexer.task.*;
import eu.ill.puma.persistence.service.document.DocumentVersionService;
import eu.ill.puma.taskmanager.Task;
import eu.ill.puma.taskmanager.TaskCompletionHandler;
import eu.ill.puma.taskmanager.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class IndexerManager implements TaskCompletionHandler {

	private static final Logger log = LoggerFactory.getLogger(IndexerManager.class);
	private static final int batchNumberOfDocuments = 50;

	@Autowired
	private DocumentVersionService documentVersionService;

	@Autowired
	private TaskManager taskManager;

	private List<Task> activeTasks = new CopyOnWriteArrayList<>();

	private List<Long> pendingDocumentIds = new CopyOnWriteArrayList();

	private IndexationState state = IndexationState.Pending;

	public IndexerManager() {
	}

	public void indexAsync(Long documentVersionId) {

		if (this.state == IndexationState.Pending) {
			this.state = IndexationState.Running;
		}

		if (this.state == IndexationState.Running) {

			// Start async
			SimpleAsyncIndexationTask indexationTask = new SimpleAsyncIndexationTask(documentVersionId, this);

			// Keep active tasks
			activeTasks.add(indexationTask);

			// execute task
			this.taskManager.executeTask(indexationTask, this);

		} else {
			log.warn("Cannot index document " + documentVersionId + " because indexer is not pending/running");
		}
	}

	public void indexAsync(List<Long> documentVersionIds) {

		if (documentVersionIds.size() > 0) {
			if (this.state == IndexationState.Pending) {
				this.state = IndexationState.Running;
			}

			if (this.state == IndexationState.Running) {

				//add id to queue
				this.pendingDocumentIds.addAll(documentVersionIds);

				// Start async
				AsyncIndexationTask asyncIndexationTask = new AsyncIndexationTask(this);

				// execute task
				this.taskManager.executeTask(asyncIndexationTask);

			} else {
				log.warn("Cannot index document " + documentVersionIds.toString() + " because indexer is not pending/running");
			}
		}
	}

	public void indexAllAsync() {
		if (this.pendingDocumentIds.size() == 0 && this.activeTasks.size() == 0) {
			List<Long> idsToIndex = documentVersionService.getAllIdsForIndexation();

			if (idsToIndex.size() > 0) {
				this.state = IndexationState.Running;

				//add id to queue
				this.pendingDocumentIds.addAll(idsToIndex);

				// Start async
				AsyncReIndexationTask asyncIndexationTask = new AsyncReIndexationTask(this);

				// execute task
				this.taskManager.executeTask(asyncIndexationTask);

			}
		} else {
			this.state = IndexationState.ReIndex;
		}
	}

	public void indexRemainingForIndexationAsync() {
		List<Long> idsToIndex = documentVersionService.getRemainingIdsForIndexation();

		this.indexAsync(idsToIndex);
	}

	public void index(Long documentVersionId) {
		try {
			// index single document
			if (documentVersionService.canDocumentWithIdBeIndexed(documentVersionId)) {
				documentVersionService.indexDocumentVersionWithId(documentVersionId);

			} else {
				log.info("DocumentVersion with id " + documentVersionId + " is not valid for indexation");
			}

		} catch (DocumentVersionService.DocumentVersionPersistenceException e) {
			log.error("Failed to index document with Id " + documentVersionId + " : " + e.getMessage());
		}
	}

	public void index(List<Long> documentVersionIds) {
		try {
			// index batch of documents
			documentVersionService.indexDocumentVersionsWithIds(documentVersionIds);

		} catch (DocumentVersionService.DocumentVersionPersistenceException e) {
			log.error("Failed to index batch of documents : " + e.getMessage());
		}
	}

	public void pause() {
		if (this.state == IndexationState.Running || this.state == IndexationState.Pending) {
			this.state = IndexationState.Paused;
			log.info("Pausing indexation");
		} else {
			log.warn("Cannot pause indexation because indexer is not pending/running");
		}
	}

	public void resume() {
		if (this.state == IndexationState.Paused) {
			this.state = IndexationState.Running;
			log.info("resume indexation");

			// Continue async asap
			AsyncIndexationTask resumeIndexationTask = new AsyncIndexationTask(this);
			this.taskManager.executeTask(resumeIndexationTask);
		} else {
			log.warn("Cannot restart indexation because indexer is not paused");
		}
	}

	public synchronized void cancelAll() {
		if (this.state == IndexationState.Running) {
			this.state = IndexationState.Pending;
			log.info("Stopping indexation");
			pendingDocumentIds.clear();
		} else {
			log.warn("Cannot stop indexation because indexer is not running");
		}
	}

	public void removeIndex() {
		documentVersionService.removeIndex();
	}

	/**
	 * Creates the next N tasks until max reached or no more document to index are available
	 */
	public synchronized void createNextTasks() {
		// Add as many tasks as possible while we are running, we have a stage runner and stage runner is not terminated
		while (this.state == IndexationState.Running && this.activeTasks.size() < this.taskManager.getPoolSize() && this.pendingDocumentIds.size() > 0) {

			//build id list
			List<Long> idsToIndex = new ArrayList<Long>();

			while (idsToIndex.size() < batchNumberOfDocuments && this.pendingDocumentIds.size() > 0) {
				idsToIndex.add(this.pendingDocumentIds.get(0));
				this.pendingDocumentIds.remove(0);
			}

			//create task
			BulkIndexationTask indexationTask = new BulkIndexationTask(idsToIndex, this);

			// Keep active tasks
			this.activeTasks.add(indexationTask);

			// execute task
			this.taskManager.executeTask(indexationTask, this);
		}
	}

	@Override
	public synchronized void onTaskCompleted(Task task) {
		this.activeTasks.remove(task);

		IndexerTask completedTask = (IndexerTask) task;
		if(completedTask.getIds().size() > 0){
			log.info("complete indexation task with " + ((IndexerTask) task).getIds().size() + " document versions");
		}

		if (this.pendingDocumentIds.size() == 0 && this.activeTasks.size() == 0 && this.state != IndexationState.ReIndex) {
			//indexation is over
			this.state = IndexationState.Pending;
		} else if (this.state != IndexationState.ReIndex) {
			//continue indexation
			AsyncIndexationTask resumeIndexationTask = new AsyncIndexationTask(this);
			this.taskManager.executeTask(resumeIndexationTask);
		} else if (this.pendingDocumentIds.size() == 0 && this.activeTasks.size() == 0 && this.state == IndexationState.ReIndex) {
			//indexation is over and state = reindex
			this.state = IndexationState.Pending;
			this.indexAllAsync();
		}

	}

	@Override
	public synchronized void onTaskError(Task task, PumaError error) {
		this.activeTasks.remove(task);

		IndexerTask completedTask = (IndexerTask) task;
		if(completedTask.getIds().size() > 0){
			log.info("failed indexation task with " + ((IndexerTask) task).getIds().size() + " document versions");
		}

		if (this.pendingDocumentIds.size() == 0 && this.activeTasks.size() == 0 && this.state != IndexationState.ReIndex) {
			//indexation is over
			this.state = IndexationState.Pending;
		} else if (this.pendingDocumentIds.size() == 0 && this.activeTasks.size() == 0 && this.state == IndexationState.ReIndex) {
			//indexation is over and state = reindex
			this.state = IndexationState.Pending;
			this.indexAllAsync();
		}
	}

	/**
	 * metrics
	 */
	public IndexationState getState() {
		return state;
	}

	public synchronized int getActiveTaskCount() {
		return this.activeTasks.size();
	}

	public long getNumberRemainingForIndexation() {
		return this.documentVersionService.getNumberRemainingForIndexation();
	}

	public long getNumberIndexed() {
		return this.documentVersionService.getNumberIndexed();
	}
}
