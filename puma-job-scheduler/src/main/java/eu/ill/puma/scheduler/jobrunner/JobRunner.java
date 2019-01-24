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

import eu.ill.puma.persistence.domain.jobscheduler.Job;
import eu.ill.puma.scheduler.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by letreguilly on 13/06/17.
 */
@Component
public abstract class JobRunner {

	private static final Logger log = LoggerFactory.getLogger(JobRunner.class);

	@Autowired
	private Scheduler scheduler;

	@PostConstruct
	private void register() {
		scheduler.registerJobRunner(this);
	}

	public abstract void run(Job job);

	public abstract String getName();
}
