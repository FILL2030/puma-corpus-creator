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
package eu.ill.puma.importermanager;

import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.core.domain.importer.ImporterInfo;
import eu.ill.puma.core.domain.importer.MetaDataAnalysisState;
import eu.ill.puma.core.error.PumaError;
import eu.ill.puma.core.error.PumaException;
import eu.ill.puma.importermanager.downloader.PumaFileDownloader;
import eu.ill.puma.importermanager.importer.ImporterInfoHelper;
import eu.ill.puma.importermanager.importer.domain.ImporterResponse;
import eu.ill.puma.importermanager.importer.domain.ImporterStatusEnum;
import eu.ill.puma.importermanager.importer.domain.ResponseMetadata;
import eu.ill.puma.importermanager.importer.task.*;
import eu.ill.puma.importermanager.resolver.PumaFileUrlResolver;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.importer.Importer;
import eu.ill.puma.persistence.domain.importer.ImporterOperation;
import eu.ill.puma.persistence.domain.importer.ImporterOperationStatus;
import eu.ill.puma.persistence.service.converterV2.exception.PumaDocumentConversionException;
import eu.ill.puma.persistence.service.converterV2.DocumentConverter;
import eu.ill.puma.persistence.service.document.PumaFileService;
import eu.ill.puma.persistence.service.importer.CachedImporterFileService;
import eu.ill.puma.persistence.service.importer.ImporterService;
import eu.ill.puma.taskmanager.Task;
import eu.ill.puma.taskmanager.TaskCompletionHandler;
import eu.ill.puma.taskmanager.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.*;


/**
 * The DataSourceManager is the central point for performing data import from different importers.
 *
 * Different Tasks are created and executed by the TaskManager to perform data import which are expected to
 * return documents in blocks of data. Metadata included in the return message allows new Tasks to be generated
 * to obtain subsequent blocks.
 *
 * Documents obtained from the importers are converted into data corresponding to Puma format.
 *
 * The Puma-documents are persisted in the database.
 *
 */
@Component
public class ImporterManager implements TaskCompletionHandler {

	private static final Logger log = LoggerFactory.getLogger(ImporterManager.class);

	public static class PumaImporterOperationException extends PumaException {
		public PumaImporterOperationException(String message) {
			super(message);
		}
	}

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private PumaFileDownloader fileDownloader;

	@Autowired
	private PumaFileUrlResolver urlResolver;

	@Autowired
	private ImporterService importerService;

	@Autowired
	private CachedImporterFileService cachedImporterFileService;

	@Autowired
	private ImporterManagerConfiguration configuration;

	@Autowired
	private DocumentConverter documentConverter;

//	@Autowired
//	private DocumentConverterService documentConverterService;

	@Autowired
	ImporterInfoHelper importerInfoHelper;

	@Autowired
	ImporterAnalysisStateCreator analysisStateCreator;

	private Map<String, ImporterInfo> importerInfos = new HashMap<>();

	private Map<ImporterOperation, ImportTask> operationTasks = new HashMap<>();

	@Value("${puma.importerManager.importer.recoverOnRestart}")
	private Boolean recoverOnRestart;


	@PostConstruct
	public void init() {
		// Initialise importer infos
		this.importerInfos = this.importerInfoHelper.getAllImporterInfos();

		// Restart any operations that were running
		if (this.recoverOnRestart) {
			log.info("Recovering imports...");
			this.recoverRunningOperations();
		}
	}

	/**
	 * Determines if a particular importer is healthy (running correctly).
	 * @return True if the importer is healthy
	 */
	public boolean isHealthy(Importer importer) {

		// Build HTTP client request
		WebTarget target = ClientBuilder.newClient().target(importer.getUrl()).
				path(configuration.importerApiBaseUrl).
				path(configuration.importerHealthUrl);

		// Perform client HTTP request to the importer and convert to ImporterResponse
		try {
			log.info("Performing health test of importer " + importer.toString());
			Response response = target.request().get();
			return response.getStatusInfo().equals(Response.Status.OK);

		} catch (Exception e) {
			log.warn("Importer " + importer.toString() + " fails health check : " + e.getMessage(), e);
			return false;
		}
	}

	public Importer addImporter(Importer importer) {
		// Persist importer
		importer = this.importerService.addImporter(importer);

		ImporterInfo importerInfo = this.importerInfoHelper.getImporterInfo(importer);
		if (importerInfo != null) {
			importer.setShortName(importerInfo.getImporterShortName());

			// Add to importer infos
			this.importerInfos.put(importer.getShortName(), importerInfo);

			importerService.save(importer);
		}

		return importer;
	}

	/**
	 * Performs an import given the operation data.
	 * @param operation The import operation to perform
	 * @throws PumaImporterOperationException if the operation is already running
	 */
	public synchronized void performImport(ImporterOperation operation) throws PumaImporterOperationException {

		// Verify that operation isn't current under way
		if (this.operationTasks.containsKey(operation)) {
			throw new PumaImporterOperationException("The importer operation " + operation + " is already running");
		}

		// Create and execute task
		this.createAndExecuteImportTask(operation);
	}


	/**
	 * Creates and executes a Task on the TaskManager depending on the data of importer operation
	 * @param importerOperation The importer operation containing data to create the task
	 */
	private synchronized void createAndExecuteImportTask(ImporterOperation importerOperation) {
		ImporterOperationStatus oldStatus = importerOperation.getStatus();

		// Update operation status
		importerOperation.setStatus(ImporterOperationStatus.RUNNING);
		this.importerService.updateOperation(importerOperation);

		ImportTask importTask = null;
		if (importerOperation.getCursor() != null) {
			// Create a task to call importer API using a cursor
			importTask = new ImportWithCursorTask(importerOperation, this.configuration);

		} else if (importerOperation.getReimportAll()) {
			if (!importerOperation.getUpdateCitations()) {
				// Force update to true
				importerOperation.setUpdateExisting(true);
			}

			// Create a task to reimport all documents associated with an importer
			if (!oldStatus.equals(ImporterOperationStatus.FAILED)) {
				DocumentVersion documentVersion = this.getNextDocumentVersionForReimport(importerOperation.getImporter(), importerOperation.getLastReimportDocumentVersionId());
				if (documentVersion != null) {
					importerOperation.setLastReimportDocumentVersionId(documentVersion.getId());
					importTask = new ReimportAllTask(importerOperation, this.configuration, documentVersion);
				} else {
					this.terminateImport(importerOperation);
				}
			}

		} else {
			importTask = new ImportSearchTask(importerOperation, this.configuration);
		}

		if (importTask != null) {
			// Store task with operation
			this.operationTasks.put(importerOperation, importTask);

			// Send task to task manager with a callback
			this.taskManager.executeTask(importTask, this);
		}
	}

	/**
	 * Creates task to cancel an import operation. We create a task because we need to use synchronised methods - this
	 * has the disadvantage of blocking a client call.
	 * @param importerOperation The import operation to cancel
	 */
	public void performCancel(ImporterOperation importerOperation) {
		CancelImportTask cancelImportTask = new CancelImportTask(importerOperation);
		this.taskManager.executeTask(cancelImportTask, new TaskCompletionHandler() {
			@Override
			public synchronized void onTaskCompleted(Task task) {
				ImporterOperation importerOperationToCancel = ((CancelImportTask)task).getImporterOperation();
				cancelImport(importerOperationToCancel);
			}

			@Override
			public synchronized void onTaskError(Task task, PumaError error) {
				ImporterOperation importerOperationToCancel = ((CancelImportTask)task).getImporterOperation();
				cancelImport(importerOperationToCancel);
			}
		});
	}

	/**
	 * Cancels an import operation if it is currently running
	 * @param importerOperation The import operation to cancel
	 */
	private synchronized void cancelImport(ImporterOperation importerOperation) {
		// Test for running and pending states
		if (this.operationTasks.containsKey(importerOperation) ||
			importerOperation.getStatus().equals(ImporterOperationStatus.PENDING) ||
			importerOperation.getStatus().equals(ImporterOperationStatus.RUNNING)) {
			log.info("Cancelling operation " + importerOperation);

			// Set the status
			importerOperation.setStatus(ImporterOperationStatus.CANCELLED);

			// Update the importer operation
			this.importerService.updateOperation(importerOperation);

			// Attempt cancel of task (if running)
			ImportTask importTask = this.operationTasks.get(importerOperation);
			if (importTask != null) {
				this.taskManager.cancelTask(importTask);

				// Remove the task from the operations map
				this.operationTasks.remove(importerOperation);
			}
		}
	}

	/**
	 * Fails an import operation if it is currently running
	 * @param importerOperation The import operation to fail
	 */
	private synchronized void failImport(ImporterOperation importerOperation) {
		if (this.operationTasks.containsKey(importerOperation)) {
			// Set the status
			importerOperation.setStatus(ImporterOperationStatus.FAILED);

			// Update the importer operation
			this.importerService.updateOperation(importerOperation);

			// Remove the task from the operations map
			this.operationTasks.remove(importerOperation);
		}
	}

	/**
	 * Terminates an import operation if it is currently running
	 * @param importerOperation The import operation to terminate
	 */
	private synchronized void terminateImport(ImporterOperation importerOperation) {
		if (this.operationTasks.containsKey(importerOperation)) {
			// Set the status
			importerOperation.setStatus(ImporterOperationStatus.TERMINATED);

			// Update the importer operation
			this.importerService.updateOperation(importerOperation);

			// Remove the task from the operations map
			this.operationTasks.remove(importerOperation);
		}
	}

	/**
	 * Callback from the TaskManager when an import task has executed correctly. During the callback
	 * the importer data is converted into Puma documents which are integrated in to the DB.
	 * @param task the task that has completed
	 */
	@Override
	public synchronized void onTaskCompleted(Task task) {

		// Get importer operation
		ImportTask importTask = (ImportTask)task;
		ImporterOperation importerOperation = importTask.getImporterOperation();

		Importer importer = importerOperation.getImporter();

		// Verify task not cancelled
		if (!this.operationTasks.containsKey(importerOperation)) {
			log.info("Ignoring results from operation " + importerOperation);
			// Operation was cancelled
			return;
		}

		// Update operation run time
		importerOperation.increaseRunTime(task.getDurationInMillis());

		try {
			// Get task data
			ImporterResponse response = importTask.get();

			// Get the meta data
			ResponseMetadata metadata = response.getMetadata();

			// Determine if the response has returned an error
			ImporterStatusEnum importerStatus = metadata.getStatus();
			if (importerStatus.equals(ImporterStatusEnum.BUSY)) {
				// Retry/fail
				this.retryTask(importerOperation, new PumaError("BUSY response from Importer"));

			} else if (importerStatus.equals(ImporterStatusEnum.ERROR)) {
				// Retry/fail
				this.retryTask(importerOperation, new PumaError("ERROR response from Importer : " + metadata.getMessage()));

			} else if (importerStatus.equals(ImporterStatusEnum.NOT_SUPPORTED)) {
				// Fail the import
				this.failImport(importerOperation);

				log.error("NOT_SUPPORTED response from Importer " + importerOperation.getImporter().toString() + " with operation " + importerOperation.toString());
			} else if (importerStatus.equals(ImporterStatusEnum.NOT_FOUND)) {
				// Terminate import
				this.terminateImport(importerOperation);

				log.info("NOT_FOUND response from Importer " + importerOperation.getImporter().toString() + " with operation " + importerOperation.toString());

			} else {

				// Update operation
				importerOperation.setCursor(metadata.getNextCursor());
				importerOperation.setLastCursor(metadata.getPreviousCursor());
				if (importerOperation.getReimportAll()) {
					importerOperation.setDocumentsReceived(importerOperation.getDocumentsReceived() + response.getData().size());
					importerOperation.setTotalDocumentCount(-1l);
				} else {
					importerOperation.setDocumentsReceived(metadata.getCurrentCount());
					importerOperation.setTotalDocumentCount(metadata.getTotalCount());
				}
				importerOperation.setRetryCount(0);

				// Convert to PCC persisted objects
				List<BaseDocument> importedDocuments = response.getData();
				List<DocumentVersion> documentVersions = this.convertAndIntegrateDocuments(importedDocuments, importer, importerOperation.getUpdateExisting(), importerOperation.getUpdateCitations());
				importerOperation.setDocumentsIntegrated(importerOperation.getDocumentsIntegrated() + documentVersions.size());

				// Get highest documentVersionId
				OptionalLong maxDocumentVersionId = documentVersions.stream().map(documentVersion -> documentVersion.getId()).mapToLong(Long::longValue).max();
				if (maxDocumentVersionId.isPresent()) {
					importerOperation.setLastImportedDocumentVersionId(maxDocumentVersionId.getAsLong());
				}

				if (!importerOperation.getUpdateCitations()) {
					// Initialise analysis state
					this.initialiseAnalysisState(documentVersions, importer);

					// Obtain cached files initially
					this.obtainCachedFiles(documentVersions);

					if (importerOperation.getDownloadFiles()) {
						// Download files
						this.initiateFileDownloads(documentVersions);

						// Resolve URLs
						this.initiateResolvers(documentVersions);
					}
				}

				// Update the importer operation
				this.importerService.updateOperation(importerOperation);

				// Is response status working or finished
				if (metadata.getStatus().equals(ImporterStatusEnum.WORKING) || importerOperation.getReimportAll()) {
					this.createAndExecuteImportTask(importerOperation);

				} else {
					this.terminateImport(importerOperation);
				}
			}

		} catch (Exception e) {
			// Fail the import
			this.failImport(importerOperation);

			log.error("Failed to handle response from Importer " + importer + " with operation " + importerOperation.toString(), e);
		}
	}

	/**
	 * Callback from the TaskManager when an import task has failed. The relevant importer operation
	 * is retried to attempt to recover from the failure.
	 * @param task the task that has failed
	 */
	@Override
	public synchronized void onTaskError(Task task, PumaError error) {
		// Get importer operation
		ImportTask importTask = (ImportTask)task;
		ImporterOperation importerOperation = importTask.getImporterOperation();

		// Verify task not cancelled
		if (!this.operationTasks.containsKey(importerOperation)) {
			// Operation was cancelled
			return;
		}

		// Update operation run time
		importerOperation.increaseRunTime(task.getDurationInMillis());

		// Retry/fail
		this.retryTask(importerOperation, error);
	}

	private synchronized void retryTask(ImporterOperation importerOperation, PumaError pumaError) {

		// Decide what to do : retry or fail ?
		if (importerOperation.getRetryCount() < ImporterOperation.MAX_RETRIES) {
			importerOperation.setRetryCount(importerOperation.getRetryCount() + 1);
			log.error("Retry (" + importerOperation.getRetryCount() + ") to perform import from Importer " + importerOperation.getImporter().toString() + " with operation " + importerOperation.toString() + " after error " + pumaError.getMessage());

			// Try to do import again
			this.createAndExecuteImportTask(importerOperation);

		} else {
			// Fail the import
			this.failImport(importerOperation);

			log.error("Failed to perform import from Importer " + importerOperation.getImporter().toString() + " with operation " + importerOperation.toString() + ". Got error " + pumaError.getMessage());
		}
	}

	/**
	 * Converts data from an importer into puma entities and persists them in the Puma DB.
	 * @param importedDocuments The data returned from the importer
	 * @param importer The importer from which the data was obtained
	 * @return The integrated documents
	 */

	public List<DocumentVersion> convertAndIntegrateDocuments(List<BaseDocument> importedDocuments, Importer importer, boolean forceUpdate, boolean updateCitations) {

		List<DocumentVersion> documentVersions = new ArrayList<>();
		for (BaseDocument importerDocument : importedDocuments) {
			try {
				DocumentVersion documentVersion = documentConverter.convert(importerDocument, importer.getShortName(), forceUpdate, updateCitations);
				//DocumentVersion documentVersion = documentConverterService.convertAndIntegrateImportedDocument(importerDocument, importer);

				// Returns null if document is already integrated
				if (documentVersion != null) {
					documentVersions.add(documentVersion);
				}

			} catch (PumaDocumentConversionException | PumaFileService.PumaFilePersistenceException pdce) {
				log.error("Could not integrate document (" + importerDocument.toString() + ") : " + pdce.getMessage(), pdce);
			}
		}

		return documentVersions;
	}

	/**
	 * Iterate over documents and determine if files can be obtained from cache
	 * @param documentVersions The document versions containing PumaFiles and ResolverInfos
	 */
	private void obtainCachedFiles(List<DocumentVersion> documentVersions) {
		for (DocumentVersion documentVersion : documentVersions) {
			this.cachedImporterFileService.copyCachedFilesForDocumentVersion(documentVersion);
		}
	}

	/**
	 * Iterate over documents and request FileDownloader to download all files needed for the document.
	 * @param documentVersions The document versions containing PumaFiles
	 */
	private void initiateFileDownloads(List<DocumentVersion> documentVersions) {
		for (DocumentVersion documentVersion : documentVersions) {
			this.fileDownloader.downloadFilesForDocumentVersion(documentVersion);
		}
	}

	/**
	 * Iterate over documents and request URLResolver to resolve all those required for the document.
	 * @param documentVersions The document versions containing ResolverInfos
	 */
	private void initiateResolvers(List<DocumentVersion> documentVersions) {
		for (DocumentVersion documentVersion : documentVersions) {
			this.urlResolver.resolveUrlsForDocumentVersion(documentVersion);
		}
	}


	/**
	 * Restarts any import operations that are running. This is called when the system starts up to attempt
	 * to recover operations that were running when the system stopped.
	 */
	public void recoverRunningOperations() {

		// Get any operations that are running and continue imports
		Collection<ImporterOperation> runningOperations = this.importerService.getRunningOperations();

		// Iterate over all operations and launch new import tasks
		for (ImporterOperation importerOperation : runningOperations) {
			log.info("Recovering running operation " + importerOperation);
			try {
				this.performImport(importerOperation);

			} catch (PumaImporterOperationException e) {
				log.error("Failed to restart importer operation " + importerOperation + " : " + e.getMessage(), e);
			}
		}
	}

	/**
	 * Initialises the analysis state for each document, taking into account the confidence levels of the importer
	 */
	private void initialiseAnalysisState(List<DocumentVersion> documentVersions, Importer importer) {
		ImporterInfo importerInfo = this.importerInfos.get(importer.getShortName());

		// Create default confidence levels just in case we don't obtain one from an importer
		MetaDataAnalysisState metaDataAnalysisState = new MetaDataAnalysisState();
		if (importerInfo != null && importerInfo.getMetaDataAnalysisState() != null) {
			metaDataAnalysisState = importerInfo.getMetaDataAnalysisState();
		}

		for (DocumentVersion documentVersion : documentVersions) {
			this.analysisStateCreator.initialiseAnalysisState(documentVersion, metaDataAnalysisState);
		}
	}

	public long getNumberOfActiveOperations() {
		return this.operationTasks.size();
	}

	private DocumentVersion getNextDocumentVersionForReimport(Importer importer, Long lastReimportId) {
		return this.importerService.getNextDocumentVersionForReimport(importer, lastReimportId);
	}
}
