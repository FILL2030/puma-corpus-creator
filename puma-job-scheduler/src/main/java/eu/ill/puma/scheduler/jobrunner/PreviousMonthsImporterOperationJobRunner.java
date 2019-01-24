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
import eu.ill.puma.scheduler.domain.PreviousMonthsImporterOperationJobData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Component
public class PreviousMonthsImporterOperationJobRunner extends JobRunner {

	private static final Logger log = LoggerFactory.getLogger(PreviousMonthsImporterOperationJobRunner.class);

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private ImporterService importerService;

	@Autowired
	private ImporterManager importerManager;

	@Override
	public void run(Job job) {
		log.info("Job runner : " + this.getName() + " has receive job : " + job.getName() + ", to run");
		try {
			PreviousMonthsImporterOperationJobData previousMonthsImporterOperationJobData = mapper.readValue(job.getJobData(), PreviousMonthsImporterOperationJobData.class);

			// Get current date and work out when to start importing
			int numberOfMonths = previousMonthsImporterOperationJobData.getNumberOfMonths();

			Date date = new Date();
			LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			int year  = localDate.getYear();
			int month = localDate.getMonthValue();

			// Go back in time by numberOfMonths
			while (numberOfMonths > 0) {
				month = month - 1;
				if (month == 0) {
					month = 12;
					year--;
				}
				numberOfMonths--;
			}

			//retrieve importer
			Long importerId = previousMonthsImporterOperationJobData.getImporterId();
			Importer importer = importerService.getById(importerId);

			//retrieve last imported id
			if (importer != null) {

				//prepare import
				ImporterOperation importerOperation = previousMonthsImporterOperationJobData.getImporterOperation();

				//perform import
				if (importerOperation != null) {
					importerOperation.getParams().put("startYear", new Integer(year).toString());
					importerOperation.getParams().put("startMonth", new Integer(month).toString());

					//prepare importer operation
					importerOperation = importerService.addImporterOperation(importer, importerOperation);

					String paramsString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(importerOperation.getParams());
					log.info("launch update on importer " + importerId + " with params " + paramsString);

					this.importerManager.performImport(importerOperation);

				} else {
					log.error("No importer operation provided for job " + job.getName());
				}

			} else {
				log.error("importer " + importerId + " for job " + job.getName() + " not found");
			}

		} catch (IOException | ImporterManager.PumaImporterOperationException e) {
			log.error("Job runner " + this.getName() + " has failed to run job : " + job.getName(), e);
		}
	}

	@Override
	public String getName() {
		return "previous_months_importer_operation_job_runner";
	}
}
