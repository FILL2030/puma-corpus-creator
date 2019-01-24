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
package eu.ill.puma.analysis.manager;

import eu.ill.puma.analysis.analyser.DocumentAnalyser;
import eu.ill.puma.analysis.factory.AnalyserFactory;
import eu.ill.puma.analysis.task.*;
import eu.ill.puma.analysis.utils.AnalysisUtils;
import eu.ill.puma.core.domain.analysis.AnalyserResponse;
import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.core.error.PumaError;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionAnalysisHistory;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionAnalysisState;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.service.analysis.DocumentVersionAnalysisHistoryService;
import eu.ill.puma.persistence.service.analysis.DocumentVersionAnalysisStateService;
import eu.ill.puma.persistence.service.converterV2.DocumentConverter;
import eu.ill.puma.persistence.service.document.DocumentVersionService;
import eu.ill.puma.taskmanager.Task;
import eu.ill.puma.taskmanager.TaskCompletionHandler;
import eu.ill.puma.taskmanager.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AnalyserManager implements TaskCompletionHandler {

	private static final Logger log = LoggerFactory.getLogger(AnalyserManager.class);

	@Autowired
	private AnalyserFactory analyserFactory;

	@Autowired
	private DocumentVersionAnalysisStateService analysisStateService;

	@Autowired
	private DocumentVersionService documentVersionService;

	@Autowired
	private DocumentVersionAnalysisHistoryService analysisHistoryService;

	@Autowired
	private DocumentConverter documentConverter;

	@Autowired
	private TaskManager taskManager;


	private AnalysisCalculator analysisCalculator;
	private String analyserSetup;
	private HashMap<Long, AnalysisTask> activeAnalysis = new LinkedHashMap<>();
	private List<AnalysisTask> cancelledTasks = new ArrayList<>();

	private Deque<Long> documentIdsToAnalyse = new ArrayDeque<>();
	private int maxNumberOfConcurrentTasks = 0;


	@PostConstruct
	public void init() {
		this.analyserSetup = this.analyserFactory.getAnalyserSetup();
		this.analysisCalculator = new AnalysisCalculator(this.analyserFactory);
		this.maxNumberOfConcurrentTasks = this.taskManager.getPoolSize();
	}

	public synchronized void performAnalysis(DocumentVersion documentVersion) {
		// Determine if analysis already in progress
		if (this.isAnalysingDocument(documentVersion)) {
			return;
		}

		this.analyse(documentVersion);
	}

	public void performAnalysisAsync(Long documentVersionId) {
		DocumentVersion documentVersion = this.documentVersionService.getByIdWithAllEntities(documentVersionId, true);

		PerformAnalysisTask performAnalysisTask = new PerformAnalysisTask(this, documentVersion);
		this.taskManager.executeTask(performAnalysisTask);
	}

	public void performAnalysisListAsync(List<Long> documentVersionIds) {
		PerformAnalysisListTask performAnalysisListTask = new PerformAnalysisListTask(this, documentVersionIds);
		this.taskManager.executeTask(performAnalysisListTask);
	}

	private synchronized void analyse(DocumentVersion documentVersion) {
		log.debug("analyse document " + documentVersion.getId());

		// Determine the analysis needed for the document
		DocumentAnalyser documentAnalyser = this.calculateAnalysisForDocument(documentVersion);

		// If analysis is necessary then execute the task
		if (documentAnalyser != null) {
			// Encapsulate the analyser in a task to be run async
			AnalysisTask analysisTask = new AnalysisTask(documentVersion, documentAnalyser);

			// Add to active tasks list
			this.activeAnalysis.put(documentVersion.getId(), analysisTask);

			// Execute task
			taskManager.executeTask(analysisTask, this);
		}
	}

	public synchronized void cancelAnalysis(DocumentVersion documentVersion) {
		if (this.isAnalysingDocument(documentVersion)) {
			// Get task and cancel it
			AnalysisTask analysisTask = this.activeAnalysis.get(documentVersion.getId());

			if (analysisTask != null) {
				log.info("Cancelling analysis of " + documentVersion.toString());
				this.cancelledTasks.add(analysisTask);

				// Remove from active analysis
				this.activeAnalysis.remove(documentVersion.getId());
			}

			if (this.documentIdsToAnalyse.contains(documentVersion.getId())) {
				this.documentIdsToAnalyse.remove(documentVersion.getId());
			}
		}

		// Start next analysis if we can
		this.initiateNextAnalysis();
	}

	public void cancelAnalysisAsync(DocumentVersion documentVersion) {
		CancelAnalysisTask cancelAnalysisTask = new CancelAnalysisTask(this, documentVersion);
		this.taskManager.executeTask(cancelAnalysisTask);
	}

	public synchronized void cancelAllAnalysis() {
		log.info("Cancelling analysis of all (" + this.activeAnalysis.size() + ") documents ");
		for (Long id : this.activeAnalysis.keySet()) {
			AnalysisTask analysisTask = this.activeAnalysis.get(id);

			this.cancelledTasks.add(analysisTask);
		}

		// Clear both active and pending analysis
		this.activeAnalysis.clear();
		this.documentIdsToAnalyse.clear();
	}

	public void cancelAllAnalysisAsync() {
		CancelAllAnalysisTask cancelAllAnalysisTask = new CancelAllAnalysisTask(this);
		this.taskManager.executeTask(cancelAllAnalysisTask);
	}

	private synchronized void removeActiveTask(AnalysisTask analysisTask) {
		DocumentVersion documentVersion = analysisTask.getDocumentVersion();

		AnalysisTask activeTask = this.activeAnalysis.get(documentVersion.getId());
		if (activeTask != null) {

			// Remove task array
			this.activeAnalysis.remove(documentVersion.getId());
		}
	}

	public void activatePendingAnalysis(Integer maxNumberToAnalyse) {
		// Get next group of docs to analyse, including the number currently active so that we are sure to obtain some new docs
		int numberOfActiveAnalysis = this.activeAnalysis.size();
		List<Long> documentIdsPendingAnalysis = this.analysisStateService.getNextDocumentVersionIdsRequiringAnalysis(this.analyserSetup, (maxNumberToAnalyse == null ? 0 : maxNumberToAnalyse) + numberOfActiveAnalysis);

		this.addDocumentVersionsToAnalyse(documentIdsPendingAnalysis, maxNumberToAnalyse);
	}

	public void addDocumentVersionsToAnalyse(List<Long> documentVersionIds, Integer maxNumberToAnalyse) {
		// Cleanup duplicates (currently active)
		List<Long> uniqueDocumentsPendingAnalysis = this.removeActiveAnalysis(documentVersionIds);

		int numberToAnalyse = uniqueDocumentsPendingAnalysis.size();
		if (maxNumberToAnalyse != null) {
			numberToAnalyse = Math.min(numberToAnalyse, maxNumberToAnalyse);
		}

		log.info("Adding " + numberToAnalyse + " documents to the analysis queue");

		this.documentIdsToAnalyse.addAll(uniqueDocumentsPendingAnalysis);

		log.info("Currently have " + this.documentIdsToAnalyse.size() + " pending and active documents for analysis");

		// Start next analysis if we can
		this.initiateNextAnalysis();
	}

	public void activatePendingAnalysisAsync(Integer maxNumberToAnalyse) {
		ActivatePendingAnalysisTask activatePendingAnalysisTask = new ActivatePendingAnalysisTask(this, maxNumberToAnalyse);
		this.taskManager.executeTask(activatePendingAnalysisTask);
	}

	private synchronized boolean isAnalysingDocumentWithId(Long id) {
		return this.activeAnalysis.containsKey(id) || this.documentIdsToAnalyse.contains(id);
	}

	private synchronized boolean isAnalysingDocument(DocumentVersion documentVersion) {
		return this.isAnalysingDocumentWithId(documentVersion.getId());
	}

	private void initiateNextAnalysis() {
		while (this.documentIdsToAnalyse.size() > 0 && this.activeAnalysis.size() < this.maxNumberOfConcurrentTasks) {
			// get next document Id to analyse
			Long documentVersionId = this.documentIdsToAnalyse.removeFirst();

			// Get document version with all entities
			DocumentVersion documentVersion = this.documentVersionService.getByIdWithAllEntities(documentVersionId, true);

			// Start the analysis process for the document version
			this.analyse(documentVersion);
		}
	}

	private List<Long> removeActiveAnalysis(List<Long> documentIdsPendingAnalysis) {
		List<Long> uniqueAnalysis = new ArrayList<>();
		for (Long documentVersionId : documentIdsPendingAnalysis) {
			if (!uniqueAnalysis.contains(documentVersionId) && !this.isAnalysingDocumentWithId(documentVersionId)) {
				uniqueAnalysis.add(documentVersionId);
			}
		}

		return uniqueAnalysis;
	}

	private DocumentAnalyser calculateAnalysisForDocument(DocumentVersion documentVersion) {
		DocumentVersionAnalysisState analysisState = documentVersion.getAnalysisState();

		// Get previous analyser names
		List<String> allAnalysers = this.analysisHistoryService.getAllSuccessfulAnalysersForDocumentVersion(documentVersion);

		// Calculate the next analysis to be performed on the document
		String documentAnalyserName = this.analysisCalculator.determineAnalysis(documentVersion, allAnalysers);

		DocumentAnalyser documentAnalyser = null;

		// If no analysers then we should flag the document version as having been fully analysed with the current setup
		if (documentAnalyserName == null) {
			// Mark document as having been fully analysed
			analysisState.setAnalysisSetup(this.analyserSetup);
			log.debug("Analysis terminated for document " + documentVersion.getId());

		} else {
			try {
				// Get the analysers from the factory
				documentAnalyser = this.analyserFactory.getAnalyserForName(documentAnalyserName);

			} catch (Exception e) {
				log.error("Could not get analyser \"" + documentAnalyserName + "\" from the factory : " + e.getMessage(), e);
			}
		}

		// Save analysis state as it can be updated by the calculator
		this.analysisStateService.save(analysisState);

		return documentAnalyser;
	}

	@Override
	public synchronized void onTaskCompleted(Task task) {
		AnalysisTask analysisTask = (AnalysisTask) task;
		DocumentVersion documentVersion = analysisTask.getDocumentVersion();
		DocumentAnalyser analyser = analysisTask.getAnalyser();

		// Remove task from active analysis
		this.removeActiveTask(analysisTask);

		if (this.cancelledTasks.contains(analysisTask)) {
			log.info("Ignoring result of cancelled analysis task : analyser = \"" + analyser.getName() + "\" document id = " + documentVersion.getId());
			return;
		}

		try {
			AnalyserResponse response = analysisTask.get();

			if (response.isSuccessful()) {
				// Clean up result : remove any unwanted entities
				DocumentVersionAnalysisState analysisState = documentVersion.getAnalysisState();
				BaseDocument documentFromAnalysis = response.getBaseDocument();
				AnalysisUtils.cleanDocumentFromAnalysis(documentFromAnalysis, analysisState);

				// Integrate response document into existing document version (new instance returned by the method)
				//documentVersion = this.documentConverterService.convertAndAppendAnalysedDocument(documentVersion, documentFromAnalysis, analyser.getName());
				DocumentVersion updatedDocumentVersion = this.documentConverter.convert(documentVersion, documentFromAnalysis, analyser.getName(), false, false);
				if (updatedDocumentVersion != null) {
					documentVersion = updatedDocumentVersion;
				}

				// Add analysis history
				DocumentVersionAnalysisHistory history = AnalysisUtils.createSuccessfulAnalysisHistory(documentVersion, analyser, response);
				this.analysisHistoryService.save(history);

				// Update analysis state
				analysisState = documentVersion.getAnalysisState();
				AnalysisUtils.updateAnalysisState(analysisState, analyser);
				analysisState.setAnalysisDate(analysisTask.getStartDate());
				this.analysisStateService.save(analysisState);

				// At end of an analysis task, get the next analysis to do
				this.analyse(documentVersion);

			} else {
				log.error("Error performing analysis task : analyser = \"" + analyser.getName() + "\" document id = " + documentVersion.getId() + " : " + response.getMessage());

				// Save a fail state for the analyser in the history
				DocumentVersionAnalysisHistory history = AnalysisUtils.createFailedAnalysisHistory(documentVersion, analyser, response.getMessage(), task.getDurationInMillis());
				this.analysisHistoryService.save(history);
			}
		} catch (Exception exception) {
			log.error("Error integrating response from analysis task : analyser = \"" + analyser.getName() + "\" document id = " + documentVersion.getId() + " : " + exception.getMessage(), exception);

			// Save a fail state for the analyser in the history
			DocumentVersionAnalysisHistory history = AnalysisUtils.createFailedAnalysisHistory(documentVersion, analyser, exception.getMessage(), task.getDurationInMillis());
			this.analysisHistoryService.save(history);
		}

		// Initiate any analysis if needed
		this.initiateNextAnalysis();
	}

	@Override
	public synchronized void onTaskError(Task task, PumaError error) {
		AnalysisTask analysisTask = (AnalysisTask) task;
		DocumentVersion documentVersion = analysisTask.getDocumentVersion();
		DocumentAnalyser analyser = analysisTask.getAnalyser();

		if (this.cancelledTasks.contains(analysisTask)) {
			log.info("Ignoring result of cancelled analysis task : analyser = \"" + analyser.getName() + "\" document id = " + documentVersion.getId());
			return;
		}

		log.error("Analysis task with \"" + analyser.getName() + "\" failed with error " + error.getMessage());

		// Save a fail state for the analyser in the history
		DocumentVersionAnalysisHistory history = AnalysisUtils.createFailedAnalysisHistory(documentVersion, analyser, error.getMessage(), task.getDurationInMillis());
		this.analysisHistoryService.save(history);

		// Remove task from active analysis
		this.removeActiveTask(analysisTask);

		// Initiate any analysis if needed
		this.initiateNextAnalysis();
	}

	public synchronized List<DocumentVersion> getActiveAnalysis() {
		return this.activeAnalysis.values().stream().map(task -> task.getDocumentVersion()).collect(Collectors.toList());
	}

	public String getAnalyserSetup() {
		return this.analyserSetup;
	}

	public long getNumberOfDocumentsInActiveAnalysis() {
		return this.activeAnalysis.size();
	}

	public long getNumberOfDocumentsActiveOrPendingAnalysis() {
		return this.activeAnalysis.size() + this.documentIdsToAnalyse.size();
	}

	protected void setAnalysisCalculator(AnalysisCalculator analysisCalculator) {
		this.analysisCalculator = analysisCalculator;
	}

	public long getNumberOfDocumentsPendingAnalysis() {
		return this.analysisStateService.getNumberOfDocumentsRequiringAnalysis(this.analyserSetup);
	}
}
