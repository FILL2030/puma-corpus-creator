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
package eu.ill.puma.importermanager.importer.task;

import eu.ill.puma.importermanager.ImporterManagerConfiguration;
import eu.ill.puma.importermanager.importer.domain.ImporterResponse;
import eu.ill.puma.persistence.domain.importer.Importer;
import eu.ill.puma.persistence.domain.importer.ImporterOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

public class ImportWithCursorTask extends ImportTask {

	private static final Logger log = LoggerFactory.getLogger(ImportWithCursorTask.class);

	public ImportWithCursorTask(ImporterOperation importerOperation, ImporterManagerConfiguration configuration) {
		super(importerOperation, configuration);
	}

	@Override
	public ImporterResponse execute() throws Exception {
		String cursor = this.importerOperation.getCursor();

		// Get the base importer URL
		Importer importer = this.importerOperation.getImporter();

		// Build HTTP client request
		Client client = ClientBuilder.newClient();

		// Create target
		String endpoint = this.importerOperation.getUpdateCitations() ? configuration.importerCitationsUrl : configuration.importerDocumentsUrl;
		WebTarget target = client.target(importer.getUrl()).path(configuration.importerApiBaseUrl).path(endpoint).path(configuration.importerCursorUrl).path(cursor);

		log.info("Performing import (" + importerOperation.getId() + ") with cursor task to endpoint : " + target.getUri().toString());

		// Perform client HTTP request to the importer and convert to ImporterResponse
		ImporterResponse importerResponse = target.request(MediaType.APPLICATION_JSON_TYPE).get(ImporterResponse.class);

		log.info("Got import (" + importerOperation.getId() + ") with cursor task result for endpoint : " + target.getUri().toString() + ", obtained " + importerResponse.getMetadata().getCurrentCount() + " documents out of " + importerResponse.getMetadata().getTotalCount());

		return importerResponse;
	}

}
