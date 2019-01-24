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

import org.elasticsearch.action.bulk.BulkResponse;

import java.util.Collection;

public class BulkIndexationResult<T> {

	private Collection<T> entities;
	private BulkResponse bulkResponse;
	boolean hasError = false;
	String error = "";

	public BulkIndexationResult(Collection<T> entities, BulkResponse bulkResponse) {
		this.entities = entities;
		this.bulkResponse = bulkResponse;
		if (bulkResponse.hasFailures()) {
			this.error = bulkResponse.buildFailureMessage();
			this.hasError = true;
		}
	}

	public Collection<T> getEntities() {
		return entities;
	}

	public BulkResponse getBulkResponse() {
		return bulkResponse;
	}

	public boolean hasError() {
		return this.hasError;
	}

	public String getError() {
		return this.error;
	}
}
