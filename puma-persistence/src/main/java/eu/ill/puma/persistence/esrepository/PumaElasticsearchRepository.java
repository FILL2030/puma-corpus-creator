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

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.ill.puma.persistence.domain.indexer.IndexedDocumentJsonMapper;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Collection;


public abstract class PumaElasticsearchRepository<T> {

	private static final Logger log = LoggerFactory.getLogger(PumaElasticsearchRepository.class);

	public static class NonIndexableEntityException extends RuntimeException {
		public NonIndexableEntityException(String message) {
			super(message);
		}
	}

	private IndexedDocumentJsonMapper jsonMapper = new IndexedDocumentJsonMapper();

	@Autowired
	private ElasticsearchClientProvider provider;

	@Value("${puma.persistence.elasticsearch.index}")
	private String index;


	@PostConstruct
	private void init() {
		log.info("Using elasticsearch index : " + this.index);
	}

	public long getIndexSize(String type) {
		try {
			SearchResponse response = this.provider.getClient().prepareSearch(this.index)
				.setTypes(type)
				.setSize(0) // Don't return any documents, we don't need them.
				.get();

			SearchHits hits = response.getHits();
			long hitsCount = hits.getTotalHits();

			return hitsCount;

		} catch (Exception e) {
			return 0;
		}

	}

	public IndexationResult<T> save(T entity, String type) {

		// Prepare request
		IndexRequestBuilder indexRequestBuilder = this.prepareIndex(entity, type);

		// Perform indexation
		IndexResponse indexResponse = indexRequestBuilder.get();

		ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
		if (shardInfo.getFailed() > 0) {
			for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
				String reason = failure.reason();
				log.warn("Shard failed to index entity : " + reason);
			}
		}

		return new IndexationResult<T>(entity, indexResponse);
	}


	public BulkIndexationResult<T> saveAll(Collection<T> entities, String type) {

		// Prepare request
		BulkRequestBuilder bulkRequestBuilder = provider.getClient().prepareBulk();
		entities.forEach(entity -> bulkRequestBuilder.add(this.prepareIndex(entity, type)));

		// Perform indexation
		BulkResponse bulkResponse = bulkRequestBuilder.get();

		if (bulkResponse.hasFailures()) {
			log.error("Failed to save bulk entities : " + bulkResponse.buildFailureMessage());
		}

		BulkIndexationResult<T> bulkIndexationResult = new BulkIndexationResult<T>(entities, bulkResponse);

		return bulkIndexationResult;
	}

	public void removeIndex() {
		IndicesAdminClient adminClient = this.provider.getClient().admin().indices();
		if (adminClient.prepareExists(this.index).execute().actionGet().isExists()) {
			DeleteIndexResponse delete = adminClient.delete(new DeleteIndexRequest(this.index)).actionGet();
			if (!delete.isAcknowledged()) {
				log.error("Index " + this.index + " wasn't deleted");
			}
		}

		if (adminClient.prepareExists(this.index).execute().actionGet().isExists()) {
			adminClient.flush(new FlushRequest(this.index)).actionGet();
		}
	}

	private IndexRequestBuilder prepareIndex(T entity, String type) {
		try {
			// Convert to json
			byte[] jsonValue = this.jsonMapper.writeValueAsBytes(entity);

			Long id = null;
			try {
				Method idGetter = entity.getClass().getMethod("getId");
				id = (Long)idGetter.invoke(entity);

			} catch (Exception e) {
				log.warn("Failed to get Id for indexation from object of type " + entity.getClass().toString());
			}

			String idString = id == null ? null : id.toString();
			IndexRequestBuilder request = provider.getClient().prepareIndex(this.index, type, idString).setSource(jsonValue, XContentType.JSON);

			return request;

		} catch (JsonProcessingException e) {
			log.error("Failed to convert entity of type " + entity.getClass().toString() + " to json");
		}

		return null;
	}
}
