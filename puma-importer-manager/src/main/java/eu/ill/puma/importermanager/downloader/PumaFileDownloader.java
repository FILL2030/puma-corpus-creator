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
package eu.ill.puma.importermanager.downloader;

import eu.ill.puma.core.error.PumaError;
import eu.ill.puma.core.utils.FileDownloader.FileDownloaderResponse;
import eu.ill.puma.core.utils.FileDownloader.FileDownloaderResponseStatus;
import eu.ill.puma.importermanager.downloader.task.ActivatePendingDownloadsTask;
import eu.ill.puma.importermanager.downloader.task.DownloadFileTask;
import eu.ill.puma.importermanager.resolver.PumaFileUrlResolver;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileStatus;
import eu.ill.puma.persistence.service.document.PumaFileService;
import eu.ill.puma.taskmanager.Task;
import eu.ill.puma.taskmanager.TaskCompletionHandler;
import eu.ill.puma.taskmanager.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Component
public class PumaFileDownloader implements TaskCompletionHandler {

	private static final Logger log = LoggerFactory.getLogger(PumaFileDownloader.class);

	@Autowired
	private PumaFileService pumaFileService;

	@Autowired
	private PumaFileUrlResolver urlResolver;

	@Autowired
	private TaskManager taskManager;

	private HashMap<PumaFile, DownloadFileTask> activeDownloads = new HashMap<>();

	@Value("${puma.importerManager.downloader.recoverOnRestart}")
	private Boolean recoverOnRestart;

	@PostConstruct
	public void init() {
		// Restart any downloads that are needed
		if (this.recoverOnRestart) {
			log.info("Recovering downloads...");

			this.recoverDownloads();
		}
	}

	public synchronized void performDownload(PumaFile pumaFile) {
		// Verify state of file
		if (pumaFile.getStatus().equals(PumaFileStatus.SAVED)) {
			// File is already saved
			return;

		} else if (this.isDownloadingFile(pumaFile)) {
			// File is currently being downloaded
			return;
		}

		// download for origin URL
		this.download(pumaFile);
	}


	/**
	 * Cancels download if it is currently running
	 * @param pumaFile The puma file download to cancel
	 */
	public synchronized void cancelDownload(PumaFile pumaFile) {
		if (this.activeDownloads.containsKey(pumaFile) || pumaFile.getStatus().equals(PumaFileStatus.PENDING)) {
			log.info("Cancelling download of " + pumaFile);

			// Set the status
			pumaFile.setStatus(PumaFileStatus.CANCELLED);

			// Update
			this.pumaFileService.saveDBOnly(pumaFile);

			DownloadFileTask activeDownloadTask = this.activeDownloads.get(pumaFile);
			if (activeDownloadTask != null) {
				this.taskManager.cancelTask(activeDownloadTask);

				// Remove from active downloads
				this.activeDownloads.remove(pumaFile);
			}
		}
	}

	public void downloadFilesForDocumentVersion(DocumentVersion documentVersion) {
		// Get the PumaFiles
		List<PumaFile> pumaFiles = documentVersion.getFiles();
		for (PumaFile pumaFile : pumaFiles) {
			this.performDownload(pumaFile);
		}
	}

	public synchronized boolean isDownloadingFile(PumaFile pumaFile) {
		return this.activeDownloads.keySet().contains(pumaFile);
	}

	public List<PumaFile> getActiveAndPendingDownloads() {
		// Get all files that are pending, have transient resolve errors, not saved or have resolve not supported status
		List<PumaFile> pendingFiles = this.pumaFileService.getAllRequiringDownload();

		return pendingFiles;
	}

	private void recoverDownloads() {
		this.activatePendingDownloads(null);
	}

	public void activatePendingDownloadsAsync(int maxNumberToDownload) {
		ActivatePendingDownloadsTask activatePendingDownloadsTask = new ActivatePendingDownloadsTask(maxNumberToDownload);
		this.taskManager.executeTask(activatePendingDownloadsTask, new TaskCompletionHandler() {
			@Override
			public synchronized void onTaskCompleted(Task task) {
				Integer maxNumberToDownload = ((ActivatePendingDownloadsTask)task).getMaxNumberToDownload();
				activatePendingDownloads(maxNumberToDownload);
			}

			@Override
			public synchronized void onTaskError(Task task, PumaError error) {
				Integer maxNumberToDownload = ((ActivatePendingDownloadsTask)task).getMaxNumberToDownload();
				activatePendingDownloads(maxNumberToDownload);
			}
		});
	}

	public void activatePendingDownloads(Integer maxNumberToDownload) {
		List<PumaFile> pendingFiles = this.getActiveAndPendingDownloads();

		// Cleanup duplicates (same origin url, doc version, doc type...)
		List<PumaFile> uniquePendingFiles = this.cancelDuplicatedPendingFiles(pendingFiles);

		int numberToDownload = uniquePendingFiles.size();
		if (maxNumberToDownload != null) {
			numberToDownload = Math.min(numberToDownload, maxNumberToDownload);
		}

		log.info("Restarting download of " + numberToDownload + " puma files");

		for (int i = 0; i < numberToDownload; i++) {
			PumaFile pumaFile = uniquePendingFiles.get(i);
			this.performDownload(pumaFile);
		}
	}

	private List<PumaFile> cancelDuplicatedPendingFiles(List<PumaFile> allPendingFiles) {
		List<PumaFile> uniqueFiles = new ArrayList<>();
		for (PumaFile pumaFile : allPendingFiles) {
			if (uniqueFiles.contains(pumaFile)) {
				pumaFile.setStatus(PumaFileStatus.CANCELLED);
				this.pumaFileService.saveDBOnly(pumaFile);
				log.info("Cancelling duplicated file" + pumaFile);

			} else {
				uniqueFiles.add(pumaFile);
			}
		}

		return uniqueFiles;
	}

	private void download(PumaFile pumaFile) {
		// Create new task
		DownloadFileTask downloadFileTask = new DownloadFileTask(pumaFile);

		// Add to active tasks
		this.activeDownloads.put(pumaFile, downloadFileTask);

		// Execute task
		taskManager.executeTask(downloadFileTask, this);
	}

	@Override
	public synchronized void onTaskCompleted(Task task) {
		DownloadFileTask downloadFileTask = (DownloadFileTask)task;
		PumaFile pumaFile = downloadFileTask.getPumaFile();

		// Verify that the task has not been cancelled
		if (!this.activeDownloads.containsKey(pumaFile)) {
			return;
		}

		try {
			FileDownloaderResponse response = downloadFileTask.get();

			// Check for busy response
			if (response.getStatus().equals(FileDownloaderResponseStatus.BUSY)) {
				// Sleep to avoid excessive CPU with only a couple of tasks in the pipeline
				Thread.sleep(1000);

				// Put the task back into the task queue
				taskManager.executeTask(downloadFileTask, this);

				return;

			} else if (response.getStatus().equals(FileDownloaderResponseStatus.ERROR)) {
				log.error("Failed to download the puma file " + pumaFile + " : " + response.getMessage());

				// Update status
				pumaFile.setStatus(PumaFileStatus.DOWNLOAD_FAILED);

			} else {
				// Save in DB and file system
				pumaFile.setDownloadDate(new Date());
				pumaFile = pumaFileService.save(pumaFile);

				// Update status
				pumaFile.setStatus(PumaFileStatus.SAVED);

			}

		} catch (PumaFileService.PumaFilePersistenceException e) {
			log.error("Failed to save the puma file " + pumaFile, e);

			// Update status
			pumaFile.setStatus(PumaFileStatus.SAVE_FAILED);

		} catch (Exception e) {
			log.error("Error while trying to get download response for the puma file " + pumaFile, e);

			// Update status
			pumaFile.setStatus(PumaFileStatus.SAVE_FAILED);
		}

		// Save in DB
		pumaFile = pumaFileService.saveDBOnly(pumaFile);

		// Remove from the hash map of current downloads
		this.activeDownloads.remove(pumaFile);
	}

	@Override
	public synchronized void onTaskError(Task task, PumaError error) {
		DownloadFileTask downloadFileTask = (DownloadFileTask)task;
		PumaFile pumaFile = downloadFileTask.getPumaFile();

		// Verify that the task has not been cancelled
		if (!this.activeDownloads.containsKey(pumaFile)) {
			return;
		}

		log.error("Failed to download the puma file " + pumaFile + " : " + error.getMessage());

		// Update status
		pumaFile.setStatus(PumaFileStatus.DOWNLOAD_FAILED);

		// Save in DB
		pumaFile = pumaFileService.saveDBOnly(pumaFile);

		// Remove from the hash map of current downloads
		this.activeDownloads.remove(pumaFile);
	}

	public long getNumberOfActiveDownloads() {
		return this.activeDownloads.size();
	}

}
