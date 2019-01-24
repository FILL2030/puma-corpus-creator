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

import eu.ill.puma.importermanager.downloader.PumaFileDownloader;
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.taskmanager.Task;
import eu.ill.puma.taskmanager.TaskPriority;

public class AsyncPumaFileDownloaderTask extends Task<Void>  {

	private PumaFile pumaFile;
	private PumaFileDownloader pumaFileDownloader;

	public AsyncPumaFileDownloaderTask(PumaFile pumaFile, PumaFileDownloader pumaFileDownloader) {
		this.pumaFile = pumaFile;
		this.pumaFileDownloader = pumaFileDownloader;

		this.setPriority(TaskPriority.ASAP);
	}

	@Override
	public Void execute() throws Exception {
		this.pumaFileDownloader.performDownload(pumaFile);

		return null;
	}
}
