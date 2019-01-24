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
package eu.ill.puma.importermanager;

import eu.ill.puma.core.domain.importer.MetaDataAnalysisState;
import eu.ill.puma.core.domain.analysis.AnalysisState;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionAnalysisState;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.service.analysis.DocumentVersionAnalysisStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImporterAnalysisStateCreator {

	@Autowired
	private DocumentVersionAnalysisStateService analysisStateService;

	public void initialiseAnalysisState(DocumentVersion documentVersion, MetaDataAnalysisState metaDataAnalysisState) {
		DocumentVersionAnalysisState analysisState = new DocumentVersionAnalysisState();

		analysisState.setDocumentVersion(documentVersion);
		analysisState.setDoi(this.convertConfidenceLevel(metaDataAnalysisState.getDoi()));
		analysisState.setTitle(this.convertConfidenceLevel(metaDataAnalysisState.getTitle()));
		analysisState.setAbstractText(this.convertConfidenceLevel(metaDataAnalysisState.getAbstract()));
		analysisState.setReleaseDate(this.convertConfidenceLevel(metaDataAnalysisState.getDate()));
		analysisState.setPerson(this.convertConfidenceLevel(metaDataAnalysisState.getPerson()));
		analysisState.setInstrument(this.convertConfidenceLevel(metaDataAnalysisState.getInstrument()));
		analysisState.setLaboratory(this.convertConfidenceLevel(metaDataAnalysisState.getLaboratory()));
		analysisState.setKeyword(this.convertConfidenceLevel(metaDataAnalysisState.getKeyword()));
		analysisState.setFormula(this.convertConfidenceLevel(metaDataAnalysisState.getFormula()));
		analysisState.setReference(this.convertConfidenceLevel(metaDataAnalysisState.getReference()));
		analysisState.setCitation(this.convertConfidenceLevel(metaDataAnalysisState.getCitation()));
		analysisState.setResearchDomain(this.convertConfidenceLevel(metaDataAnalysisState.getResearchDomain()));
		analysisState.setJournal(this.convertConfidenceLevel(metaDataAnalysisState.getJournal()));
		analysisState.setPublisher(this.convertConfidenceLevel(metaDataAnalysisState.getPublisher()));
		analysisState.setExtractedImage(this.convertConfidenceLevel(metaDataAnalysisState.getExtractedImage()));
		analysisState.setAdditionalText(this.convertConfidenceLevel(metaDataAnalysisState.getAdditionalText()));

		this.analysisStateService.save(analysisState);
	}

	private AnalysisState convertConfidenceLevel(AnalysisState analysisState) {
		return analysisState;
	}
}
