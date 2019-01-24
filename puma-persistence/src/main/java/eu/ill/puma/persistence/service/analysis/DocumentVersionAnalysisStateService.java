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
package eu.ill.puma.persistence.service.analysis;

import eu.ill.puma.persistence.domain.analysis.DocumentVersionAnalysisState;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.repository.analyser.DocumentVersionAnalysisStateRepository;
import eu.ill.puma.persistence.service.document.DocumentVersionService;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DocumentVersionAnalysisStateService {

	private static final Logger log = LoggerFactory.getLogger(DocumentVersionAnalysisStateService.class);

	@Autowired
	private DocumentVersionAnalysisStateRepository repository;

	@Autowired
	private DocumentVersionService documentVersionService;

	/**
	 * Returns a DocumentVersionAnalysisState given its DocumentVersion
	 * @param documentVersion The DocumentVersion of the DocumentVersionAnalysisState to obtain
	 * @return The DocumentVersionAnalysisState for the given DocumentVersion
	 */
	public DocumentVersionAnalysisState getByDocumentVersion(DocumentVersion documentVersion) {
		return this.repository.getByDocumentVersion(documentVersion);
	}

	/**
	 * Persists the given DocumentVersionAnalysisState and ensures we do not create duplicates.
	 *  - if the id is set we assume we have an already persisted object and the object is updated
	 *  - If the DocumentVersionAnalysisState data match an existing DocumentVersionAnalysisState then we know it is already persisted
	 * @param documentVersionAnalysisState The DocumentVersionAnalysisState to be persisted
	 * @return The persisted DocumentVersionAnalysisState
	 */
	public synchronized DocumentVersionAnalysisState save(DocumentVersionAnalysisState documentVersionAnalysisState) {
		DocumentVersionAnalysisState integratedDocumentVersionAnalysisState = null;

		if(documentVersionAnalysisState.getDocumentVersion() == null) {
			log.error("Cannot save document versions analysis state without a document version");
			return documentVersionAnalysisState;
		}

		// Check if it's a new object
		if (documentVersionAnalysisState.getId() == null) {
			// Check if it is already persisted
			integratedDocumentVersionAnalysisState = this.repository.getByDocumentVersion(documentVersionAnalysisState.getDocumentVersion());
			if (integratedDocumentVersionAnalysisState != null) {
				log.debug("documentVersionAnalysisState already present in the db under the id " + integratedDocumentVersionAnalysisState.getId());

				integratedDocumentVersionAnalysisState.copyFrom(documentVersionAnalysisState);
				integratedDocumentVersionAnalysisState = this.repository.merge(integratedDocumentVersionAnalysisState);

			} else {
				integratedDocumentVersionAnalysisState = this.repository.persist(documentVersionAnalysisState);
			}

		} else {
			// merge
			integratedDocumentVersionAnalysisState = this.repository.merge(documentVersionAnalysisState);
		}

		return integratedDocumentVersionAnalysisState;
	}


	/**
	 * Gets all analysis states
	 * @return all analysis states
	 */
	public List<DocumentVersionAnalysisState> getAll() {
		return this.repository.getAll();
	}

	public List<DocumentVersion> getNextDocumentVersionsRequiringAnalysis(String analysisSetup, int limit, boolean withFiles) {
		return this.completeDocumentRetrievalOfDocumentVersions(this.repository.getNextGroupRequiringAnalysis(analysisSetup, limit), withFiles);
	}

	public List<DocumentVersion> getAnalysisHistory(int limit, boolean withFiles) {
		return this.completeDocumentRetrievalOfDocumentVersions(this.repository.getAnalysisHistory(limit), withFiles);
	}

	public List<Long> getNextDocumentVersionIdsRequiringAnalysis(String analysisSetup, int limit) {
		return this.repository.getNextGroupIdsRequiringAnalysis(analysisSetup, limit);
	}

	public long getNumberOfDocumentsRequiringAnalysis(String analysisSetup) {
		return this.repository.getNumberOfDocumentsRequiringAnalysis(analysisSetup);
	}

	private List<DocumentVersion> completeDocumentRetrievalOfDocumentVersions(List<DocumentVersionAnalysisState> analysisStates, boolean withFiles) {
		List<DocumentVersion> documentVersions = new ArrayList<>();

		for (DocumentVersionAnalysisState analysisState : analysisStates) {
			DocumentVersion documentVersion = analysisState.getDocumentVersion();

			Hibernate.initialize(documentVersion.getAnalysisState());

//			this.documentVersionService.getAllEntities(documentVersion, withFiles);

			documentVersions.add(documentVersion);
		}

		return documentVersions;
	}
}
