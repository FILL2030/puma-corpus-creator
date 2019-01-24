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
package eu.ill.puma.webapi.rest.analyser;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class RestAnalyserResponse {

	@JsonIgnore
	private long totalNumberToAnalyse;

	@JsonIgnore
	private List<RestAnalysisDocument> documents = new ArrayList<>();

	public RestAnalyserResponse(long totalNumberToAnalyse) {
		this.totalNumberToAnalyse = totalNumberToAnalyse;
	}

	public void addDocument(RestAnalysisDocument document) {
		this.documents.add(document);
	}

	@JsonGetter
	public long getTotalNumberToAnalyse() {
		return totalNumberToAnalyse;
	}

	public void setTotalNumberToAnalyse(long totalNumberToAnalyse) {
		this.totalNumberToAnalyse = totalNumberToAnalyse;
	}

	@JsonGetter
	public List<RestAnalysisDocument> getDocuments() {
		return documents;
	}
}
