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

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Profile("!test")
@Component
public class ProductionElasticsearchClientProvider implements ElasticsearchClientProvider {

	private static final Logger log = LoggerFactory.getLogger(TestElasticsearchClientProvider.class);

	@Value("${puma.persistence.elasticsearch.hosts}")
	private String hostsString;

	@Value("${puma.persistence.elasticsearch.port}")
	private Integer port;

	@Value("${puma.persistence.elasticsearch.clusterName}")
	private String clusterName;


	private List<String> hosts = new ArrayList<>();

	private Client client;

	@PostConstruct
	private void init() {
		// Verify if hosts environment variable has been set
		if (this.hostsString != null && !this.hostsString.equals("")) {
			this.hosts = Arrays.asList(hostsString.split(","));
			log.info("Using elasticsearch hosts : " + this.hosts.stream().map(Object::toString).collect(Collectors.joining(", ")));

		} else {
			log.warn("No elasticsearch hosts set");
		}

		log.info("Using elasticsearch port : " + this.port);
		log.info("Using elasticsearch cluster name : " + this.clusterName);

		if (this.hosts.size() > 0) {
			Settings settings = Settings.builder().put("cluster.name", this.clusterName).build();
			TransportClient transportClient= new PreBuiltTransportClient(settings);

			for (String host : this.hosts) {
				try {

					transportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), this.port));

					this.client = transportClient;

				} catch (UnknownHostException e) {
					log.error("Failed to connect to elasticsearch host " + host + " : " + e.getMessage());
				}
			}
		}
	}

	@PreDestroy
	private void shutdown() {
		if (this.client != null) {
			this.client.close();
			this.client = null;
		}
	}

	public Client getClient() {
		return client;
	}
}
