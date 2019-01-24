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
package eu.ill.puma.importermanager.resolver.task;

import eu.ill.puma.importermanager.ImporterManagerConfiguration;
import eu.ill.puma.importermanager.resolver.domain.ResolverResponse;
import eu.ill.puma.importermanager.resolver.domain.ResolverResponseCode;
import eu.ill.puma.persistence.domain.document.ResolverInfo;
import eu.ill.puma.taskmanager.Task;
import eu.ill.puma.taskmanager.TaskPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

public class ResolveFileUrlTask extends Task<ResolverResponse>  {

	private static final Logger log = LoggerFactory.getLogger(ResolveFileUrlTask.class);

	private ResolverInfo resolverInfo;
	private ImporterManagerConfiguration configuration;


	public ResolveFileUrlTask(ResolverInfo resolverInfo, ImporterManagerConfiguration configuration) {
		this.resolverInfo = resolverInfo;
		this.configuration = configuration;

		this.setPriority(TaskPriority.LOW);
	}

	@Override
	public ResolverResponse execute() throws Exception {

		// Build HTTP client request
		Client client = ClientBuilder.newClient();

//		String url = this.resolverInfo.getOriginUrl();
		String doi = this.resolverInfo.getDocumentVersion().getDoi();

		// Create target
		String resolverUrl = configuration.resolverUrl;

		WebTarget target = client.target(resolverUrl);
//		target = target.queryParam(configuration.resolverUrlParamName, url);
		target = target.queryParam(configuration.resolverDoiParamName, doi);

		log.info("Resolving download urls for resolve info (" + resolverInfo.getId() + ") : (" + doi + ") with resolve : " + target.getUri().toString());

		// Perform client HTTP request to the resolve to get the resolved URL
		try {
			ResolverResponse resolverResponse = target.request(MediaType.APPLICATION_JSON_TYPE).get(ResolverResponse.class);

			if (resolverResponse.getCode().equals(ResolverResponseCode.SUCCESS)) {
				log.info("Resolved download urls for resolve info (" + resolverInfo.getId() + ")");

			} else {
				log.warn("Resolver for resolve info (" + resolverInfo.getId() + ") returned code " + resolverResponse.getCode());
			}

			return resolverResponse;

		} catch (Exception e) {
			log.error("Resolver failed : " + target.getUri());

			throw e;
		}
	}

	public ResolverInfo getResolverInfo() {
		return resolverInfo;
	}
}
