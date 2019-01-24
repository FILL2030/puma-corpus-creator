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
import eu.ill.puma.core.domain.analysis.AnalysisState;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionAnalysisState;

import java.util.Date;

public class RestAnalysisState {

	@JsonIgnore
	private DocumentVersionAnalysisState analysisState;

	public RestAnalysisState(DocumentVersionAnalysisState analysisState) {
		this.analysisState = analysisState;
	}

	@JsonGetter
	public Long getId() {
		return this.analysisState.getId();
	}

	@JsonGetter
	public AnalysisState getDoi() {
		return this.analysisState.getDoi();
	}

	@JsonGetter
	public AnalysisState getTitle() {
		return this.analysisState.getTitle();
	}

	@JsonGetter
	public AnalysisState getAbstractText() {
		return this.analysisState.getAbstractText();
	}

	@JsonGetter
	public AnalysisState getFullText() {
		return this.analysisState.getFullText();
	}

	@JsonGetter
	public AnalysisState getReleaseDate() {
		return this.analysisState.getReleaseDate();
	}

	@JsonGetter
	public AnalysisState getPerson() {
		return this.analysisState.getPerson();
	}

	@JsonGetter
	public AnalysisState getInstrument() {
		return this.analysisState.getInstrument();
	}

	@JsonGetter
	public AnalysisState getLaboratory() {
		return this.analysisState.getLaboratory();
	}

	@JsonGetter
	public AnalysisState getKeyword() {
		return this.analysisState.getKeyword();
	}

	@JsonGetter
	public AnalysisState getFormula() {
		return this.analysisState.getFormula();
	}

	@JsonGetter
	public AnalysisState getReference() {
		return this.analysisState.getReference();
	}

	@JsonGetter
	public AnalysisState getCitation() {
		return this.analysisState.getCitation();
	}

	@JsonGetter
	public AnalysisState getResearchDomain() {
		return this.analysisState.getResearchDomain();
	}

	@JsonGetter
	public AnalysisState getJournal() {
		return this.analysisState.getJournal();
	}

	@JsonGetter
	public AnalysisState getPublisher() {
		return this.analysisState.getPublisher();
	}

	@JsonGetter
	public String getAnalysisSetup() {
		return this.analysisState.getAnalysisSetup();
	}

	@JsonGetter
	public Date getAnalysisDate() {
		return this.analysisState.getAnalysisDate();
	}

}
