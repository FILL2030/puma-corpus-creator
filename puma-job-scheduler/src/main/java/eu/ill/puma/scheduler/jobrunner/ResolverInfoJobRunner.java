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
import eu.ill.puma.importermanager.resolver.PumaFileUrlResolver;
import eu.ill.puma.persistence.domain.document.ResolverInfo;
import eu.ill.puma.persistence.domain.jobscheduler.Job;
import eu.ill.puma.persistence.service.document.ResolverInfoService;
import eu.ill.puma.scheduler.domain.ResolverInfoJobData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

@Component
public class ResolverInfoJobRunner extends JobRunner {

	private static final Logger log = LoggerFactory.getLogger(ResolverInfoJobRunner.class);
	private ObjectMapper mapper = new ObjectMapper();
	private static float DEFAULT_DELAY = 10;

	@Autowired
	private ResolverInfoService resolverInfoService;

	@Autowired
	private PumaFileUrlResolver urlResolver;

	private Deque<ResolverInfo> pendingResolvers = new ArrayDeque<>();

	@PostConstruct
	private void init() {
		// Test if resolver is configured
		if (this.urlResolver.isAvailable()) {
			// Get all resolvers that are pending, have transient resolve errors or have resolve not supported status
			List<ResolverInfo> pendingResolvers = this.resolverInfoService.getAllRequiringResolve();
			this.pendingResolvers.addAll(pendingResolvers);

			log.info("Added " + this.pendingResolvers.size() + " resolvers to scheduler");

		} else {
			log.info("URLResolver is disabled: not running any in the scheduler");
		}
	}

	@Override
	public void run(Job job) {
		log.debug("Job runner : " + this.getName() + " has received job: Number of pending resolvers = " + this.pendingResolvers.size());
		ResolverInfo resolverInfo = null;

		float delayMax = DEFAULT_DELAY;
		String host = null;

		// Get job data
		if (job.getJobData() != null && !job.getJobData().equals("")) {
			try {
				ResolverInfoJobData jobData = mapper.readValue(job.getJobData(), ResolverInfoJobData.class);
				if (jobData.getDelay() != null && jobData.getDelay() != 0.0) {
					delayMax = jobData.getDelay();
				}

				if (jobData.getHost() != null && !jobData.getHost().equals("")) {
					host = jobData.getHost();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		synchronized (this) {
			if (this.pendingResolvers.size() > 0) {
				final String hostToFind = host;
				if (hostToFind != null) {

					Optional<ResolverInfo> optionalResolverInfo = this.pendingResolvers.stream().filter(ri -> ri.getResolverHost() != null && ri.getResolverHost().matches(hostToFind)).findFirst();
					if (optionalResolverInfo.isPresent()) {
						resolverInfo =  optionalResolverInfo.get();

						this.pendingResolvers.remove(resolverInfo);

					} else {
						log.info("No resolvers exist for host " + hostToFind);
					}

				} else {
					resolverInfo = this.pendingResolvers.removeFirst();
				}
			}
		}

		if (resolverInfo != null) {
			// Random sleep
			long delay = (long)(Math.random() * delayMax);

			log.info("Sending resolver info to UrlResolver after " + delay + "s : " + resolverInfo);

			try {
				Thread.sleep(delay * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			this.urlResolver.resolve(resolverInfo);
		}
	}

	@Override
	public String getName() {
		return "resolver_info_job_runner";
	}
}
