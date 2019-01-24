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
package eu.ill.puma.persistence.esrepository;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

@Profile("test")
@Component
public class TestElasticsearchClientProvider implements ElasticsearchClientProvider {

	private static final Logger log = LoggerFactory.getLogger(ProductionElasticsearchClientProvider.class);

	private static Client client;

	@PostConstruct
	private void init() {

		if (client == null) {
			try {
				FileUtils.deleteDirectory(new File("target/elasticsearch"));

			} catch (IOException e) {
				e.printStackTrace();
			}

			// Create client pointing to local directory
			Settings settings = Settings.builder()
				.put("path.home", "target/elasticsearch")
				.put("transport.type", "local")
				.put("http.enabled", false)
				.build();

			try {
				client = new Node(settings).start().client();

				client.admin().cluster().prepareHealth()
					.setWaitForGreenStatus()
					.get();

			} catch (NodeValidationException e) {
				e.printStackTrace();
			}
		}
	}

	public Client getClient() {
		return client;
	}
}
