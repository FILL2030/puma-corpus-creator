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
package eu.ill.puma.webapi.rest.monitoring;

import eu.ill.puma.analysis.factory.AnalyserFactory;
import eu.ill.puma.analysis.manager.AnalyserManager;
import eu.ill.puma.importermanager.ImporterManager;
import eu.ill.puma.importermanager.downloader.PumaFileDownloader;
import eu.ill.puma.importermanager.resolver.PumaFileUrlResolver;
import eu.ill.puma.indexer.manager.IndexerManager;
import eu.ill.puma.scheduler.Scheduler;
import eu.ill.puma.taskmanager.TaskManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Component
@Path("/monitoring")
public class MonitoringEndpoint {

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private PumaFileDownloader pumaFileDownloader;

	@Autowired
	private PumaFileUrlResolver pumaFileUrlResolver;

	@Autowired
	private ImporterManager importerManager;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private AnalyserFactory analyserFactory;

	@Autowired
	private AnalyserManager analyserManager;

	@Autowired
	private IndexerManager indexerManager;


	@GET
	@Path("/")
	@Produces({ "application/json" })
	public Response getOperations() {
		MonitoringData monitoringData = new MonitoringData();
		monitoringData.setNumberOfPendingTasks(this.taskManager.getNumberOfPendingTasks());
		monitoringData.setNumberOfActiveThreads(this.taskManager.getActiveNumberOfThreads());
		monitoringData.setNumberOfThreads(this.taskManager.getCurrentNumberOfThreads());

		monitoringData.setNumberOfImportTasks(this.importerManager.getNumberOfActiveOperations());
		monitoringData.setNumberOfDownloadTasks(this.pumaFileDownloader.getNumberOfActiveDownloads());
		monitoringData.setNumberOfResolverTasks(this.pumaFileUrlResolver.getNumberOfActiveResolvers());
		monitoringData.setNumberOfAnalysisTasks(this.analyserManager.getNumberOfDocumentsInActiveAnalysis());
		monitoringData.setNumberOfDocumentsPendingAnalysis(this.analyserManager.getNumberOfDocumentsPendingAnalysis());

		monitoringData.setNumberOfJobRunner(scheduler.getJobRunnerNumber());
		monitoringData.setNumberOfJobs(scheduler.getPlannedJobNumber());

		monitoringData.setNumberOfRegisteredAnalyser(analyserFactory.getNumberOfRegisteredAnalysers());
		monitoringData.setNumberOfInstantiatedAnalyser(analyserFactory.getNumberOfInstantiatedAnalyser());

		monitoringData.setIndexationState(indexerManager.getState());
		monitoringData.setNumberOfPendingIndexation(indexerManager.getNumberRemainingForIndexation());

		return Response.ok(monitoringData).build();
	}

}
