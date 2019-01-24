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
package eu.ill.puma.analysis.manager;

import eu.ill.puma.analysis.factory.AnalyserFactory;
import eu.ill.puma.core.domain.analysis.AnalysisState;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionAnalysisState;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.domain.document.enumeration.DocumentType;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileType;
import eu.ill.puma.persistence.util.PumaFileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class AnalysisCalculator {

	public static final String ELSEVIER_ANALYSER = "elsevierxml";
	private static final String ABBY_BATCH_ANALYSER = "abby";


	private static final Logger log = LoggerFactory.getLogger(AnalysisCalculator.class);

	private Map<String, List<EntityType>> analyserProduction;
	private static final String[] FULL_TEXT_ANALYSER_TOPOLOGY = {/*ABBY_BATCH_ANALYSER*/};

	public AnalysisCalculator(AnalyserFactory factory) {
		this.analyserProduction = factory.getAnalyserEntityProduction();
	}

	/**
	 * Determines a list of analysers required to the current state of a document
	 *
	 * @param documentVersion The document to be analysed
	 * @return A list of analyser names
	 */
	public String determineAnalysis(DocumentVersion documentVersion, List<String> analysisHistory) {
		DocumentVersionAnalysisState analysisState = documentVersion.getAnalysisState();

		// Stage 1a : get full text if it doesn't already exist
		if (analysisState.getFullText().equals(AnalysisState.TO_ANALYSE)) {
			// Return analyser to get the full text
			String fullTextCreationAnalyser = this.getAnalyserToObtainFullText(documentVersion, analysisHistory);
			return fullTextCreationAnalyser;
		}

		// Get full text data
		boolean hasFullTextFile = PumaFileUtil.hasFilesOfType(documentVersion, PumaFileType.EXTRACTED_FULL_TEXT);

		if (hasFullTextFile) {

			// Stage 2 : analyse full text
			for (String analyserName : FULL_TEXT_ANALYSER_TOPOLOGY) {
				if (this.requiresAnalyser(analyserName, analysisState, analysisHistory)) {
					return analyserName;
				}
			}

		} else {
			// If we can't find a fulltext file then document analysis ends
			log.info("No full text file exists for document " + documentVersion.getId() + " - terminating analysis");
		}

		// Stage 3: get images if we haven't already tried
//		if (analysisState.getExtractedImage().equals(AnalysisState.TO_ANALYSE)) {
//			// Return analyser to extract images
//			String analyser = this.getAnalyserToObtainExtractedImages(documentVersion, analysisHistory);
//			if (analyser != null) {
//				return analyser;
//			}
//		}

		return null;
	}

	/**
	 * Determine if an analyser should run by determining if has already run or if it produces an entity of interest
	 */
	private boolean requiresAnalyser(String analyserName, DocumentVersionAnalysisState analysisState, List<String> analysisHistory) {

		// verify analyser hasn't already run
		if (analysisHistory.contains(analyserName)) {
			return false;
		}

		if (!this.analyserProduction.containsKey(analyserName)) {
			return false;
		}

		// verify each entity type: check not closed
		List<EntityType> produces = this.analyserProduction.get(analyserName);
		for (EntityType entityType : produces) {
			if (!analysisState.getEntityState(entityType).equals(AnalysisState.CLOSED)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Determines the analyser necessary to obtain the full text for a document, depending on the type of
	 * document and the types of files it has available
	 *
	 * @param documentVersion The document to be analysed
	 * @return A specific analyser name
	 */
	private String getAnalyserToObtainFullText(DocumentVersion documentVersion, List<String> analysisHistory) {

		// ILL PDFs
		if (documentVersion.getDocument().getDocumentType().equals(DocumentType.PROPOSAL) && !analysisHistory.contains(ABBY_BATCH_ANALYSER)) {
			return ABBY_BATCH_ANALYSER;
		}

		// Elsevier XML publication files
		if (PumaFileUtil.hasFilesOfType(documentVersion, PumaFileType.PUBLICATION, PumaFile.XML_MIME_TYPE) && !analysisHistory.contains(ELSEVIER_ANALYSER)) {
			return ELSEVIER_ANALYSER;
		}

		// Publication PDF files
		if (PumaFileUtil.hasFilesOfMimeType(documentVersion, PumaFile.PDF_MIME_TYPE) && !analysisHistory.contains(ABBY_BATCH_ANALYSER)) {
			return ABBY_BATCH_ANALYSER;
		}

		return null;
	}


	/**
	 * Returns an analyser that is capable of extracting images
	 *
	 * @param documentVersion The document to be analysed
	 * @return An analyser name
	 */
	private String getAnalyserToObtainExtractedImages(DocumentVersion documentVersion, List<String> analysisHistory) {
		// Verify that extracted images don't already exist
		if (PumaFileUtil.hasFilesOfType(documentVersion, PumaFileType.EXTRACTED_IMAGE)) {
			documentVersion.getAnalysisState().setExtractedImage(AnalysisState.CLOSED);
			return null;
		}

//		if(PumaFileUtil.hasFilesOfMimeType(documentVersion, PumaFile.PDF_MIME_TYPE) && analysisHistory.contains(ABBY_BATCH_ANALYSER) == false){
//			return ABBY_BATCH_ANALYSER;
//		}

		return null;
	}
}
