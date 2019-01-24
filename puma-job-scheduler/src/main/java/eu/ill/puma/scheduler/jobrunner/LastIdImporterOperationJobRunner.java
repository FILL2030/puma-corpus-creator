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
package eu.ill.puma.scheduler.jobrunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ill.puma.importermanager.ImporterManager;
import eu.ill.puma.persistence.domain.document.DocumentVersionSource;
import eu.ill.puma.persistence.domain.importer.Importer;
import eu.ill.puma.persistence.domain.importer.ImporterOperation;
import eu.ill.puma.persistence.domain.jobscheduler.Job;
import eu.ill.puma.persistence.service.document.DocumentVersionService;
import eu.ill.puma.persistence.service.importer.ImporterService;
import eu.ill.puma.scheduler.domain.ImporterOperationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by letreguilly on 21/06/17.
 */
@Component
public class LastIdImporterOperationJobRunner extends JobRunner {

	private static final Logger log = LoggerFactory.getLogger(LastIdImporterOperationJobRunner.class);

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private ImporterService importerService;

	@Autowired
	private ImporterManager importerManager;

	@Autowired
	private DocumentVersionService documentVersionService;

	@Override
	public void run(Job job) {
		log.info("Job runner : " + this.getName() + " has receive job : " + job.getName() + ", to run");

		try {
			//retrieve object from data
			ImporterOperationData importerOperationData = mapper.readValue(job.getJobData(), ImporterOperationData.class);

			//retrieve importer
			Long importerId = importerOperationData.getImporterId();
			Importer importer = importerService.getById(importerId);

			//retrieve last imported id
			if (importer != null) {
				DocumentVersionSource documentVersionSource = documentVersionService.getLastSourceByImporterShortName(importer.getShortName());

				//prepare import
				ImporterOperation importerOperation = importerOperationData.getImporterOperation();
				if (importerOperation == null) {
					importerOperation = new ImporterOperation();
				}
				importerOperation.getParams().put("startId", documentVersionSource.getSourceId());

				//perform import
				if (importerOperation != null) {
					//prepare importer operation
					importerOperation = importerService.addImporterOperation(importer, importerOperation);

					this.importerManager.performImport(importerOperation);
					log.info("launch update on importer " + importerId + " with startId " + documentVersionSource.getSourceId());
				}
			}else {
				log.error("importer " + importerId + " for job " + job.getName() + " not found");
			}
		} catch (IOException | ImporterManager.PumaImporterOperationException e) {
			log.error("Job runner " + this.getName() + " has failed to run job : " + job.getName(), e);
		}
	}


	@Override
	public String getName() {
		return "last_id_importer_operation_job_runner";
	}
}
