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
package eu.ill.puma.analysis.utils;

import eu.ill.puma.core.domain.analysis.AnalyserResponse;
import eu.ill.puma.analysis.analyser.DocumentAnalyser;
import eu.ill.puma.core.domain.analysis.AnalysisState;
import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.core.domain.document.entities.BaseFile;
import eu.ill.puma.core.domain.document.entities.BaseInstrument;
import eu.ill.puma.core.domain.document.entities.BaseLaboratory;
import eu.ill.puma.core.domain.document.entities.BasePerson;
import eu.ill.puma.core.domain.document.enumeration.BaseFileType;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionAnalysisHistory;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionAnalysisState;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import eu.ill.puma.persistence.domain.document.DocumentVersion;

import java.util.*;

public class AnalysisUtils {

	public static DocumentVersionAnalysisHistory createSuccessfulAnalysisHistory(DocumentVersion documentVersion, DocumentAnalyser analyser, AnalyserResponse response) {

		DocumentVersionAnalysisHistory history = new DocumentVersionAnalysisHistory();
		history.setDocumentVersion(documentVersion);
		history.setAnalyserName(analyser.getName());
		history.setDuration(response.getDuration());
		history.setRunDate(new Date());
		history.setMessage(response.getMessage());
		history.setSuccessful(response.isSuccessful());

		return history;
	}

	public static DocumentVersionAnalysisHistory createFailedAnalysisHistory(DocumentVersion documentVersion, DocumentAnalyser analyser, String errorMessage, long duration) {

		DocumentVersionAnalysisHistory history = new DocumentVersionAnalysisHistory();
		history.setDocumentVersion(documentVersion);
		history.setAnalyserName(analyser.getName());
		history.setDuration(duration);
		history.setRunDate(new Date());
		history.setMessage(errorMessage);
		history.setSuccessful(false);

		return history;
	}


	public static void cleanDocumentFromAnalysis(BaseDocument documentFromAnalysis, DocumentVersionAnalysisState analysisState) {
		if (analysisState.getDoi().equals(AnalysisState.CLOSED)) {
			documentFromAnalysis.setDoi(null);
		}
		if (analysisState.getTitle().equals(AnalysisState.CLOSED)) {
			documentFromAnalysis.setTitle(null);
		}
		if (analysisState.getAbstractText().equals(AnalysisState.CLOSED)) {
			documentFromAnalysis.setAbstract(null);
		}
		if (analysisState.getReleaseDate().equals(AnalysisState.CLOSED)) {
			documentFromAnalysis.setReleaseDate(null);
		}
		if (analysisState.getFullText().equals(AnalysisState.CLOSED)) {
			List<BaseFile> files = documentFromAnalysis.getFiles();
			for (BaseFile file : files) {
				if (file.getType().equals(BaseFileType.EXTRACTED_FULL_TEXT)) {
					documentFromAnalysis.removeFile(file);
				}
			}
		}
		if (analysisState.getPerson().equals(AnalysisState.CLOSED)) {
			documentFromAnalysis.removeAllPersons();
		}
		if (analysisState.getInstrument().equals(AnalysisState.CLOSED)) {
			documentFromAnalysis.removeAllInstruments();
		}
		if (analysisState.getLaboratory().equals(AnalysisState.CLOSED) && analysisState.getPerson().equals(AnalysisState.CLOSED) && analysisState.getInstrument().equals(AnalysisState.CLOSED)) {
			// Remove all laboratories only if all persons and instruments are closed too
			documentFromAnalysis.removeAllLaboratories();

		} else if (analysisState.getLaboratory().equals(AnalysisState.CLOSED) && analysisState.getPerson().equals(AnalysisState.CLOSED)) {
			// keep laboratories associated only with instruments
			Set<BaseLaboratory> laboratories = new HashSet<>();
			for (BaseInstrument instrument : documentFromAnalysis.getInstruments()) {
				Long laboratoryId = instrument.getLaboratoryId();
				for (BaseLaboratory laboratory : documentFromAnalysis.getLaboratories()) {
					if (laboratory.getId().equals(laboratoryId)) {
						laboratories.add(laboratory);
					}
				}
			}
			documentFromAnalysis.removeAllLaboratories();
			for (BaseLaboratory laboratory : laboratories) {
				documentFromAnalysis.addLaboratory(laboratory);
			}

		} else if (analysisState.getLaboratory().equals(AnalysisState.CLOSED) && analysisState.getInstrument().equals(AnalysisState.CLOSED)) {
			// keep laboratories associated only with persons
			Set<BaseLaboratory> laboratories = new HashSet<>();
			for (BasePerson person: documentFromAnalysis.getPersons()) {
				Long laboratoryId = person.getLaboratoryId();
				for (BaseLaboratory laboratory : documentFromAnalysis.getLaboratories()) {
					if (laboratory.getId().equals(laboratoryId)) {
						laboratories.add(laboratory);
					}
				}
			}
			documentFromAnalysis.removeAllLaboratories();
			for (BaseLaboratory laboratory : laboratories) {
				documentFromAnalysis.addLaboratory(laboratory);
			}
		}

		if (analysisState.getKeyword().equals(AnalysisState.CLOSED)) {
			documentFromAnalysis.removeAllKeywords();
		}
		if (analysisState.getFormula().equals(AnalysisState.CLOSED)) {
			documentFromAnalysis.removeAllFormulas();
		}
		if (analysisState.getReference().equals(AnalysisState.CLOSED)) {
			documentFromAnalysis.removeAllReferences();
		}
		if (analysisState.getCitation().equals(AnalysisState.CLOSED)) {
			documentFromAnalysis.removeAllCitations();
		}
		if (analysisState.getResearchDomain().equals(AnalysisState.CLOSED)) {
			documentFromAnalysis.removeAllResearchDomains();
		}
		if (analysisState.getJournal().equals(AnalysisState.CLOSED)) {
			documentFromAnalysis.setJournal(null);
		}
		if (analysisState.getPublisher().equals(AnalysisState.CLOSED) && analysisState.getJournal().equals(AnalysisState.CLOSED)) {
			// Remove publishers only if journal has been removed
			documentFromAnalysis.removeAllPublishers();
		}
		if (analysisState.getExtractedImage().equals(AnalysisState.CLOSED)) {
			List<BaseFile> files = documentFromAnalysis.getFiles();
			for (BaseFile file : files) {
				if (file.getType().equals(BaseFileType.EXTRACTED_IMAGE)) {
					documentFromAnalysis.removeFile(file);
				}
			}
		}
	}

	public static void updateAnalysisState(DocumentVersionAnalysisState analysisState, DocumentAnalyser analyser) {
		List<EntityType> producedEntities = analyser.getProducedEntities();

		// Any entity that was to analyse should now be analysed if it has been produced
		for (EntityType entityType : producedEntities) {
			if (analysisState.getEntityState(entityType) == AnalysisState.TO_ANALYSE) {
				analysisState.setEntityState(entityType, AnalysisState.ANALYSED);
			}
		}
	}
}
