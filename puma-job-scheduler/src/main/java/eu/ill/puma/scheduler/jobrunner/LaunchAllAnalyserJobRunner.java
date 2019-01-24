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

import eu.ill.puma.analysis.manager.AnalyserManager;
import eu.ill.puma.persistence.domain.jobscheduler.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LaunchAllAnalyserJobRunner extends  JobRunner {

	private static final Logger log = LoggerFactory.getLogger(LaunchAllAnalyserJobRunner.class);

	@Autowired
	private AnalyserManager analyserManager;

	@Override
	public void run(Job job) {
		log.info("Executing analysis command for all documents");

		this.analyserManager.activatePendingAnalysisAsync(null);
	}

	@Override
	public String getName() {
		return "launch_all_analyser_job_runner";
	}
}
