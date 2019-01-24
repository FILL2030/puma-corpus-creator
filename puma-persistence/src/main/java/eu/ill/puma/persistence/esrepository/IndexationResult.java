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

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;

public class IndexationResult<T> {

	private T entity;
	private IndexResponse indexResponse;
	boolean hasError = false;
	private String error = "";

	public IndexationResult(T entity, IndexResponse indexResponse) {
		this.entity = entity;
		this.indexResponse = indexResponse;
		ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
		if (shardInfo.getFailed() > 0) {
			for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
				hasError = true;
				String reason = failure.reason();
				error += reason + " : ";
			}
		}
	}

	public T getEntity() {
		return entity;
	}

	public void setEntity(T entity) {
		this.entity = entity;
	}

	public IndexResponse getIndexResponse() {
		return indexResponse;
	}

	public ReplicationResponse.ShardInfo getShardInfo() {
		return this.indexResponse.getShardInfo();
	}

	public boolean hasError() {
		return this.hasError;
	}

	public String getError() {
		return this.error;
	}
}
