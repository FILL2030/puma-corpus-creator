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
import eu.ill.puma.core.utils.StringUtils;
import eu.ill.puma.persistence.domain.document.DocumentVersion;

public class RestAnalysisDocument {

	private DocumentVersion documentVersion;
	private static int TEXT_LIMIT = 100;

	public RestAnalysisDocument(DocumentVersion documentVersion) {
		this.documentVersion = documentVersion;
	}

	@JsonGetter
	public Long getId() {
		return this.documentVersion.getId();
	}

	@JsonGetter
	public String getTitle() {
		return StringUtils.limitText(this.documentVersion.getTitle(), TEXT_LIMIT);
	}

	@JsonGetter
	public String getAbstract() {
		return StringUtils.limitText(this.documentVersion.getAbstractText(), TEXT_LIMIT);
	}

	@JsonGetter
	public String getDoi() {
		return  this.documentVersion.getDoi();
	}

//	@JsonGetter
//	public int getNumberOfPersonLaboratoryAffiliations() {
//		return  this.documentVersion.getPersonLaboratoryAffiliations().size();
//	}

//	@JsonGetter
//	public int getNumberOfInstruments() {
//		return  this.documentVersion.getInstrumentScientificTechniqueAffiliations().size();
//	}

	public RestAnalysisState getAnalysisState() {
		return new RestAnalysisState(this.documentVersion.getAnalysisState());
	}

}
