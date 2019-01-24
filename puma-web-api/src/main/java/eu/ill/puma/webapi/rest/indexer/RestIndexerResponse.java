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
package eu.ill.puma.webapi.rest.indexer;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.ill.puma.indexer.manager.IndexationState;

public class RestIndexerResponse {

	@JsonIgnore
	private long totalNumberToIndex;

	@JsonIgnore
	private long totalNumberIndexed;

	@JsonIgnore
	private IndexationState state;


	public void setTotalNumberToIndex(long totalNumberToIndex) {
		this.totalNumberToIndex = totalNumberToIndex;
	}

	@JsonGetter
	public long getTotalNumberIndexed() {
		return totalNumberIndexed;
	}

	public void setTotalNumberIndexed(long totalNumberIndexed) {
		this.totalNumberIndexed = totalNumberIndexed;
	}

	@JsonGetter
	public long getTotalNumberToIndex() {
		return totalNumberToIndex;
	}

	public void setState(IndexationState state) {
		this.state = state;
	}

	@JsonGetter
	public IndexationState getState() {
		return state;
	}
}
