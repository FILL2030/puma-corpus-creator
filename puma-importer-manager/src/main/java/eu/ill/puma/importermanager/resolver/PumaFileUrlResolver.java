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
package eu.ill.puma.importermanager.resolver;

import eu.ill.puma.core.error.PumaError;
import eu.ill.puma.importermanager.ImporterManagerConfiguration;
import eu.ill.puma.importermanager.downloader.PumaFileDownloader;
import eu.ill.puma.importermanager.downloader.task.AsyncPumaFileDownloaderTask;
import eu.ill.puma.importermanager.resolver.domain.ResolverResponse;
import eu.ill.puma.importermanager.resolver.domain.ResolverResponseCode;
import eu.ill.puma.importermanager.resolver.domain.ResolverResponseDownloadData;
import eu.ill.puma.importermanager.resolver.domain.ResolverResponseUrl;
import eu.ill.puma.importermanager.resolver.task.ActivatePendingResolversTask;
import eu.ill.puma.importermanager.resolver.task.ResolveFileUrlTask;
import eu.ill.puma.importermanager.resolver.task.ResolveForHostTask;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.domain.document.ResolverInfo;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileStatus;
import eu.ill.puma.persistence.domain.document.enumeration.ResolverInfoStatus;
import eu.ill.puma.persistence.service.document.DocumentVersionService;
import eu.ill.puma.persistence.service.document.PumaFileService;
import eu.ill.puma.persistence.service.document.ResolverInfoService;
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

@Component
public class PumaFileUrlResolver implements TaskCompletionHandler {

	private static final Logger log = LoggerFactory.getLogger(PumaFileUrlResolver.class);

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private ResolverInfoService resolverInfoService;

	@Autowired
	private PumaFileService pumaFileService;

	@Autowired
	private PumaFileDownloader pumaFileDownloader;

	@Autowired
	private DocumentVersionService documentVersionService;

	@Autowired
	private ImporterManagerConfiguration configuration;

	private LinkedHashMap<ResolverInfo, ResolveFileUrlTask> activeResolvers = new LinkedHashMap<ResolverInfo, ResolveFileUrlTask>();

	private boolean available = true;

	@Value("${puma.importerManager.resolver.recoverOnRestart}")
	private Boolean recoverOnRestart;

	@PostConstruct
	public void checkAvailability() {
		// Verify if resolve environment variable has been set
		String resolverUrl = configuration.resolverUrl;

		if (resolverUrl == null || resolverUrl.equals("")) {
			log.warn("No resolve url has been specified");
			this.available = false;

		} else {
			log.info("Resolver url has been specified as " + resolverUrl);
		}

		if (this.isAvailable() && this.recoverOnRestart) {
			log.info("Recovering resolvers...");

			// Restart any downloads that are needed
			this.recoverResolvers();
		}
	}

	public boolean isHealthy() {
		if (this.available) {
			String resolverUrl = configuration.resolverUrl;

			// Build HTTP client request
			WebTarget target = ClientBuilder.newClient().target(resolverUrl).
					path(configuration.resolverHealthUrl);

			// Perform client HTTP request to the importer and convert to ImporterResponse
			try {
				log.info("Performing health test of url resolve (" + resolverUrl + ")");
				Response response = target.request().get();
				return response.getStatusInfo().equals(Response.Status.OK);

			} catch (Exception e) {
				log.warn("Url resolve (" + resolverUrl + ") fails health check : " + e.getMessage(), e);
				return false;
			}
		}

		return false;
	}

	public boolean isAvailable() {
		return available;
	}

	public synchronized boolean isResolving(ResolverInfo resolverInfo) {
		return this.activeResolvers.keySet().contains(resolverInfo);
	}

	public synchronized ResolverInfo resolve(ResolverInfo resolverInfo) {
		ResolverInfo returnValue = null;
		if (this.available && !this.isResolving(resolverInfo)) {
			// Get latest from DB
			resolverInfo = this.resolverInfoService.getById(resolverInfo.getId());

			if (resolverInfo.getStatus().equals(ResolverInfoStatus.RESOLVE_COMPLETED)) {
				return null;
			}

			// Create new task
			ResolveFileUrlTask resolveFileUrlTask = new ResolveFileUrlTask(resolverInfo, configuration);

			// Add to active tasks
			this.activeResolvers.put(resolverInfo, resolveFileUrlTask);

			// Execute task
			taskManager.executeTask(resolveFileUrlTask, this);

			returnValue = resolverInfo;
		}

		return returnValue;
	}

	public synchronized List<ResolverInfo> resolve(List<ResolverInfo> resolverInfos) {
		List<ResolverInfo> returnedResolverInfos = new ArrayList<>();
		for (ResolverInfo resolverInfo : resolverInfos) {
			ResolverInfo  returnValue = this.resolve(resolverInfo);
			if (returnValue != null) {
				returnedResolverInfos.add(returnValue);
			}
		}

		return returnedResolverInfos;
	}

	public void resolveForHost(String host) {
		if (this.available) {
			List<ResolverInfo> resolverInfos = this.resolverInfoService.getAllRequiringResolveForHost(host);

			for (ResolverInfo resolverInfo : resolverInfos) {
				this.resolve(resolverInfo);
			}
		}
	}

	public void resolveForHostAsync(String host) {
		ResolveForHostTask resolveForHostTask = new ResolveForHostTask(this, host);
		this.taskManager.executeTask(resolveForHostTask);
	}

	public void resolveUrlsForDocumentVersion(DocumentVersion documentVersion) {
		// Get the resolve infos
		List<ResolverInfo> resolverInfos = documentVersion.getResolverInfos();
		for (ResolverInfo resolverInfo : resolverInfos) {
			this.resolve(resolverInfo);
		}
	}

	public synchronized void cancelResolve(ResolverInfo resolverInfo) {
		if (this.activeResolvers.containsKey(resolverInfo) || resolverInfo.getStatus().equals(PumaFileStatus.PENDING)) {
			log.info("Cancelling url resolve for " + resolverInfo.getOriginUrl());

			// Set the status
			resolverInfo.setStatus(ResolverInfoStatus.CANCELLED);

			// Update
			this.resolverInfoService.save(resolverInfo);

			ResolveFileUrlTask resolveFileUrlTask = this.activeResolvers.get(resolverInfo);
			if (resolveFileUrlTask != null) {
				this.taskManager.cancelTask(resolveFileUrlTask);

				// Remove from active downloads
				this.activeResolvers.remove(resolverInfo);
			}
		}
	}

	public void activatePendingResolversAsync(int maxNumberToResolve) {
		ActivatePendingResolversTask activatePendingResolversTask = new ActivatePendingResolversTask(maxNumberToResolve);
		this.taskManager.executeTask(activatePendingResolversTask, new TaskCompletionHandler() {
			@Override
			public synchronized void onTaskCompleted(Task task) {
				Integer maxNumberToResolve = ((ActivatePendingResolversTask)task).getMaxNumberToResolve();
				activatePendingResolvers(maxNumberToResolve);
			}

			@Override
			public synchronized void onTaskError(Task task, PumaError error) {
				Integer maxNumberToResolve = ((ActivatePendingResolversTask)task).getMaxNumberToResolve();
				activatePendingResolvers(maxNumberToResolve);
			}
		});
	}

	public void activatePendingResolvers(int maxNumberToResolve) {
		if (this.isAvailable()) {
			this.recoverResolvers(maxNumberToResolve);
		}
	}

	@Override
	public synchronized void onTaskCompleted(Task task) {
		ResolveFileUrlTask resolveFileUrlTask = (ResolveFileUrlTask)task;
		ResolverInfo resolverInfo = resolveFileUrlTask.getResolverInfo();

		// Verify that the task has not been cancelled
		if (!this.activeResolvers.containsKey(resolverInfo)) {
			return;
		}

		resolverInfo.setResolveCounter(resolverInfo.getResolveCounter() + 1);
		resolverInfo.setLastResolveDate(new Date());

		try {
			ResolverResponse response = resolveFileUrlTask.get();

			// Check for busy response
			if (response.getCode().equals(ResolverResponseCode.BUSY)) {
				// Put the task back into the task queue
				taskManager.executeTask(resolveFileUrlTask, this);

				return;
			}

			// Store the domain so that we can do stats later
			resolverInfo.setResolverHost(response.getResolverHost());

			if (response.getCode().equals(ResolverResponseCode.SUCCESS)) {
				// Remove any previous errors
				resolverInfo.setResolverError(null);

				// Create PumaFiles that already contain data
				for (ResolverResponseDownloadData downloadData : response.getDownloads()) {
					PumaFile pumaFile = downloadData.convertToPumaFile();
					if (pumaFile != null) {
						log.info("Got a download from resolver having url " + pumaFile.getOriginUrl());

						// TODO set pumafile in document version
						pumaFile.setDocumentVersion(resolverInfo.getDocumentVersion());

						// Save the puma file
						this.pumaFileService.save(pumaFile);
					}
				}

				// Create PumaFiles to be downloaded
				for (ResolverResponseUrl url : response.getUrls()) {
					log.info("Got a resolved url from resolver : " + url.getUrl());

					// Create puma file and associate it with the document version
					PumaFile pumaFile = url.convertToPumaFile();

					// TODO set pumafile in document version
					pumaFile.setDocumentVersion(resolverInfo.getDocumentVersion());

					// Save the puma file
					this.pumaFileService.saveDBOnly(pumaFile);

					// Create task to do download
					this.taskManager.executeTask(new AsyncPumaFileDownloaderTask(pumaFile, pumaFileDownloader));
				}

				resolverInfo.setStatus(ResolverInfoStatus.RESOLVE_COMPLETED);

			} else if (response.getCode().equals(ResolverResponseCode.NOT_SUPPORTED)) {
				// not available... yet
				log.warn("No resolve available for URL \"" + response.getResolverHost() + "\" to download the file " + resolverInfo.getOriginUrl());
				resolverInfo.setStatus(ResolverInfoStatus.RESOLVE_NOT_SUPPORTED);

			} else if (response.getCode().equals(ResolverResponseCode.NON_RECOVERABLE_ERROR)) {
				// non-recoverable
				log.warn("Unable to resolve direct URLs to download the files from " + resolverInfo.getOriginUrl() +  " (" + response.getMessage() + ")");
				resolverInfo.setStatus(ResolverInfoStatus.RESOLVE_FAILED);
				resolverInfo.setResolverError(response.getMessage());

			} else {
				// re-tryable
				log.warn("Error while trying to resolve URLs to download the files from " + resolverInfo.getOriginUrl() +  " (" + response.getMessage() + ")");
				resolverInfo.setStatus(ResolverInfoStatus.RESOLVE_ERROR);
				resolverInfo.setResolverError(response.getMessage());
			}

		} catch (Exception e) {
			log.error("Error while trying to get resolve response for the files at " + resolverInfo.getOriginUrl() + " : " + e.getMessage());
			// re-tryable
			resolverInfo.setStatus(ResolverInfoStatus.RESOLVE_ERROR);
			resolverInfo.setResolverError(e.getMessage());
		}

		// Update status
		resolverInfo = resolverInfoService.save(resolverInfo);

		// remove from hash map of current resolvers
		this.activeResolvers.remove(resolverInfo);
	}

	@Override
	public synchronized void onTaskError(Task task, PumaError error) {
		ResolveFileUrlTask resolveFileUrlTask = (ResolveFileUrlTask)task;
		ResolverInfo resolverInfo = resolveFileUrlTask.getResolverInfo();

		// Verify that the task has not been cancelled
		if (!this.activeResolvers.containsKey(resolverInfo)) {
			return;
		}

		log.error("Failed to resolve the download urls for resolve at " + resolverInfo.getOriginUrl() + " : " + error.getMessage());

		// Update status - retryable
		resolverInfo.setStatus(ResolverInfoStatus.RESOLVE_ERROR);
		resolverInfo = resolverInfoService.save(resolverInfo);
		resolverInfo.setResolveCounter(resolverInfo.getResolveCounter() + 1);
		resolverInfo.setLastResolveDate(new Date());

		// remove from hash map of current resolvers
		this.activeResolvers.remove(resolverInfo);
	}


	public List<ResolverInfo> getRemainingResolvers() {
		// Get all resolvers that are pending, have transient resolve errors or have resolve not supported status
		List<ResolverInfo> pendingResolvers = this.resolverInfoService.getAllRequiringResolve();

		return pendingResolvers;
	}

	public List<ResolverInfo> getHistory(int number) {
		List<ResolverInfo> pendingResolvers = this.resolverInfoService.getLatestResolved(number);

		return pendingResolvers;
	}

	public List<ResolverInfo> getActiveResolvers() {
		// Get all resolvers that are pending, have transient resolve errors or have resolve not supported status
		List<ResolverInfo> pendingResolvers = new ArrayList<>(this.activeResolvers.keySet());

		return pendingResolvers;
	}

	public List<ResolverInfo> getPendingResolvers() {
		// Get all resolvers that are pending, have transient resolve errors or have resolve not supported status
		List<ResolverInfo> pendingResolvers = this.resolverInfoService.getAllPending();
		List<ResolverInfo> pendingButNotActiveResolvers = new ArrayList<ResolverInfo>();
		pendingResolvers.stream().forEach(resolverInfo -> {
			if (!this.activeResolvers.keySet().contains(resolverInfo)) {
				pendingButNotActiveResolvers.add(resolverInfo);
			}
		});

		return pendingButNotActiveResolvers;
	}

	public List<ResolverInfo> getFailedResolvers() {
		// Get all resolvers that are pending, have transient resolve errors or have resolve not supported status
		List<ResolverInfo> pendingResolvers = this.resolverInfoService.getAllFailed();

		return pendingResolvers;
	}

	private void recoverResolvers() {
		this.recoverResolvers(null);
	}

	private void recoverResolvers(Integer maxNumberToResolve) {
		List<ResolverInfo> pendingResolvers = this.getRemainingResolvers();

		// Cleanup duplicates (same origin url)
		List<ResolverInfo> uniquePendingResolvers = this.cancelDuplicatedPendingResolvers(pendingResolvers);

		int numberToResolve = uniquePendingResolvers.size();
		if (maxNumberToResolve != null) {
			numberToResolve = Math.min(numberToResolve, maxNumberToResolve);
		}

		log.info("Restarting resolve of " + numberToResolve + " puma files");

		for (int i = 0; i < numberToResolve; i++) {
			ResolverInfo resolverInfo = uniquePendingResolvers.get(i);
			this.resolve(resolverInfo);
		}
	}

	private List<ResolverInfo> cancelDuplicatedPendingResolvers(List<ResolverInfo> allPendingResolvers) {
		List<ResolverInfo> uniqueResolvers = new ArrayList<>();
		for (ResolverInfo resolverInfo : allPendingResolvers) {
			if (uniqueResolvers.contains(resolverInfo)) {
				resolverInfo.setStatus(ResolverInfoStatus.CANCELLED);
				this.resolverInfoService.save(resolverInfo);
				log.info("Cancelling duplicated resolver " + resolverInfo);

			} else {
				uniqueResolvers.add(resolverInfo);
			}
		}

		return uniqueResolvers;
	}

	public long getNumberOfActiveResolvers() {
		return this.activeResolvers.size();
	}

	public boolean setFileForResolverInfo(ResolverInfo resolverInfo, ResolverResponseDownloadData uploadFile) throws PumaFileService.PumaFilePersistenceException {

		PumaFile pumaFile = uploadFile.convertToPumaFile();
		if (pumaFile != null) {
			log.info("Got a download from resolver having url " + pumaFile.getOriginUrl());

			pumaFile.setDocumentVersion(resolverInfo.getDocumentVersion());

			// Save the puma file
			this.pumaFileService.save(pumaFile);

			// Update the resolver info
			resolverInfo.setStatus(ResolverInfoStatus.RESOLVE_COMPLETED);
			this.resolverInfoService.save(resolverInfo);

			return true;
		}

		return false;
	}

}
