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
package eu.ill.puma.webapi.rest;

import eu.ill.puma.persistence.domain.jobscheduler.Job;
import eu.ill.puma.persistence.repository.PumaRepository;
import eu.ill.puma.scheduler.jobrunner.JobRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by letreguilly on 23/06/17.
 */
@Component
public class FakeJobRunner extends JobRunner {

	private static final Logger log = LoggerFactory.getLogger(PumaRepository.class);

	private boolean hasRun = false;

	@Override
	public void run(Job job) {
		log.info("fakeJobRunner has run");
		hasRun=true;
	}

	@Override
	public String getName() {
		return "fake_job_runner";
	}

	public boolean isHasRun() {
		return hasRun;
	}
}
