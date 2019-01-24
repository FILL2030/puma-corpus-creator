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
package eu.ill.puma.importermanager.importer;

import eu.ill.puma.importermanager.ImporterManagerConfiguration;
import eu.ill.puma.core.domain.importer.ImporterInfo;
import eu.ill.puma.persistence.domain.importer.Importer;
import eu.ill.puma.persistence.service.importer.ImporterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ImporterInfoHelper {

	@Autowired
	private ImporterManagerConfiguration configuration;

	private static final Logger log = LoggerFactory.getLogger(ImporterInfoHelper.class);

	@Autowired
	private ImporterService importerService;


	public Map<String, ImporterInfo> getAllImporterInfos() {
		Map<String, ImporterInfo> importerInfos = new HashMap<>();

		List<Importer> importers = this.importerService.getAll();

		for (Importer importer : importers) {
			ImporterInfo importerInfo = this.makeImporterInfoRequest(importer);
			if (importerInfo != null) {
				importerInfos.put(importer.getShortName(), importerInfo);
			}
		}

		return importerInfos;
	}

	public ImporterInfo getImporterInfo(Importer importer) {
		return this.makeImporterInfoRequest(importer);
	}

	private ImporterInfo makeImporterInfoRequest(Importer importer) {
		try {
			// Build HTTP client request to get the short name from the importer
			Client client = ClientBuilder.newClient();

			// Create target
			WebTarget target = client.target(importer.getUrl()).path(configuration.importerApiBaseUrl).path(configuration.importerInfoUrl);

			log.info("Getting importer (" + importer.getName() + ") info at " + target.getUri().toString());

			// Perform client HTTP request to the importer and convert to ImporterResponse
			ImporterInfo importerInfo = target.request(MediaType.APPLICATION_JSON_TYPE).get(ImporterInfo.class);

			return importerInfo;

		} catch (Exception e) {
			log.error("Failed to get importer info for " + importer + " : " + e.getMessage(), e);
			return null;
		}
	}

}
