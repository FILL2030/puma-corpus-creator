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
import eu.ill.puma.persistence.domain.importer.Importer;
import eu.ill.puma.persistence.domain.importer.ImporterOperation;
import eu.ill.puma.persistence.domain.jobscheduler.Job;
import eu.ill.puma.persistence.service.importer.ImporterService;
import eu.ill.puma.scheduler.domain.ImporterOperationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by letreguilly on 13/06/17.
 */
@Component
public class GenericImporterOperationJobRunner extends JobRunner {

	private static final Logger log = LoggerFactory.getLogger(GenericImporterOperationJobRunner.class);

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private ImporterService importerService;

	@Autowired
	private ImporterManager importerManager;

	@Override
	public void run(Job job) {
		try {
			log.info("Job runner : " + this.getName() + " has receive job : " + job.getName() + ", to run");

			//retrieve object from data
			ImporterOperationData addImporterOperationData = mapper.readValue(job.getJobData(), ImporterOperationData.class);

			//get importer
			Importer importer = importerService.getById(addImporterOperationData.getImporterId());

			if (importer != null) {
				//create importer operation
				ImporterOperation importerOperation = importerService.addImporterOperation(importer, addImporterOperationData.getImporterOperation());

				//perform import
				if (importerOperation != null) {
					this.importerManager.performImport(importerOperation);
				}
			} else {
				log.error("importer " + addImporterOperationData.getImporterId() + " for job " + job.getName() + " not found");
			}
		} catch (Exception e) {
			log.error("Job runner " + this.getName() + " has failed to run job : " + job.getName(), e);
		}
	}

	@Override
	public String getName() {
		return "generic_importer_operation_job_runner";
	}

}
