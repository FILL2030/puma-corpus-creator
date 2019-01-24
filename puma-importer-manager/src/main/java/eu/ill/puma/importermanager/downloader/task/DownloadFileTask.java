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
package eu.ill.puma.importermanager.downloader.task;

import eu.ill.puma.core.utils.FileDownloader.FileDownloader;
import eu.ill.puma.core.utils.FileDownloader.FileDownloaderResponse;
import eu.ill.puma.core.utils.FileDownloader.FileDownloaderResponseStatus;
import eu.ill.puma.core.utils.throttle.Throttle;
import eu.ill.puma.core.utils.throttle.ThrottleHosts;
import eu.ill.puma.core.utils.throttle.ThrottleStore;
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.taskmanager.Task;
import eu.ill.puma.taskmanager.TaskPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class DownloadFileTask extends Task<FileDownloaderResponse>  {

	private static final Logger log = LoggerFactory.getLogger(DownloadFileTask.class);

	protected PumaFile pumaFile;

	private FileDownloader fileDownloader;

	public DownloadFileTask(PumaFile pumaFile) {
		this.pumaFile = pumaFile;
		this.fileDownloader = new FileDownloader(true);
		this.setPriority(TaskPriority.LOW);
	}

	@Override
	public FileDownloaderResponse execute() throws Exception {
		// get url
		String urlString = pumaFile.getOriginUrl();

		// Convert to URL
		URL url = new URL(urlString);

		// Get host from the URL and match to known resolvers
		String host = url.getHost();

		FileDownloaderResponse fileDownloaderResponse = null;

		// Throttle calls by URL
		Throttle throttle = ThrottleStore.getInstance().get(host);
		if (throttle.throttleOrBusy(ThrottleHosts.getInstance().getThrottleTime(host))) {
			// perform download
			log.info("Downloading puma file (" + pumaFile.getId() + ") from : " + urlString);
			fileDownloaderResponse = fileDownloader.downloadFileFrom(urlString);
			log.info("Downloaded  puma file (" + pumaFile.getId() + ") from : " + urlString);

			if (fileDownloaderResponse.getFileData().length == 0) {
				fileDownloaderResponse.setStatus(FileDownloaderResponseStatus.ERROR);
				fileDownloaderResponse.setMessage("Downloaded file contains 0 bytes");

			} else {
				// return response
				pumaFile.setMimeType(fileDownloaderResponse.getMimeType());
				pumaFile.setData(fileDownloaderResponse.getFileData());
			}

		} else {
			log.info("Downloader BUSY for puma file (" + pumaFile.getId() + ") from : " + urlString);
			fileDownloaderResponse = new FileDownloaderResponse();
			fileDownloaderResponse.setStatus(FileDownloaderResponseStatus.BUSY);
		}

		return fileDownloaderResponse;
	}

	public PumaFile getPumaFile() {
		return pumaFile;
	}
}
