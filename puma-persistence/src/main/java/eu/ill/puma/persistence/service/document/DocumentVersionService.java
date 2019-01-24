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
package eu.ill.puma.persistence.service.document;

import eu.ill.puma.core.error.PumaException;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.DocumentVersionSource;
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileStatus;
import eu.ill.puma.persistence.domain.indexer.IndexedDocument;
import eu.ill.puma.persistence.esrepository.BulkIndexationResult;
import eu.ill.puma.persistence.esrepository.IndexationResult;
import eu.ill.puma.persistence.esrepository.IndexedDocumentElasticsearchRepository;
import eu.ill.puma.persistence.repository.document.DocumentVersionRepository;
import eu.ill.puma.persistence.repository.document.DocumentVersionSourceRepository;
import eu.ill.puma.persistence.repository.document.PumaJDBCDocumentEraser;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class DocumentVersionService {

	private static final Logger log = LoggerFactory.getLogger(DocumentVersionService.class);

	@Autowired
	private PumaFileService pumaFileService;

	public static class DocumentVersionPersistenceException extends PumaException {
		public DocumentVersionPersistenceException(String message) {
			super(message);
		}
	}

	@Autowired
	private DocumentVersionRepository documentVersionRepository;

	@Autowired
	private IndexedDocumentElasticsearchRepository elasticsearchRepository;

	@Autowired
	private DocumentVersionSourceRepository documentVersionSourceRepository;

	@Autowired
	private PumaJDBCDocumentEraser documentEraser;

	/**
	 * returns all document versions
	 *
	 * @return A list containing all document versions
	 */
	public List<DocumentVersion> getAll() {
		return this.documentVersionRepository.getAll();
	}

	public List<Long> getAllIds() {
		return this.documentVersionRepository.getAllIds();
	}

	/**
	 * return a specific document version
	 *
	 * @param id The id of the document version
	 * @return The corresponding document version
	 */
	public DocumentVersion getById(Long id) {
		return this.documentVersionRepository.getById(id);
	}

	public DocumentVersion getByIdWithAllEntities(Long id, boolean withFiles) {
		DocumentVersion documentVersion = this.getById(id);

		this.getAllEntities(documentVersion, withFiles);

		return documentVersion;
	}

	/**
	 * Persists the given document version
	 *
	 * @param documentVersion The document version to persist
	 */
	public synchronized DocumentVersion save(DocumentVersion documentVersion) throws DocumentVersionPersistenceException {
		DocumentVersion integratedDocumentVersion = null;

		// Check if it's a new object
		if (documentVersion.getId() == null) {
			// New imported document : check it doesn't already exist for the importer - verify we only have one source
			if (documentVersion.getSources().size() > 1) {
				throw new DocumentVersionPersistenceException("Imported document cannot have more than one source");

			} else if (documentVersion.getSources().size() == 0) {
				throw new DocumentVersionPersistenceException("Imported document must have a source");
			}

			// Get first document version source
			DocumentVersionSource documentVersionSource = documentVersion.getSources().iterator().next();

			// Determine if already imported
			DocumentVersionSource integratedDocumentVersionSource = this.documentVersionSourceRepository.getFirstBySourceIdAndImporterShortName(documentVersionSource.getSourceId(), documentVersionSource.getImporterShortName());

			if (integratedDocumentVersionSource != null) {
				// Get the associated document version
				integratedDocumentVersion = integratedDocumentVersionSource.getDocumentVersion();
				log.debug("DocumentVersion with source Id (" + documentVersionSource.getSourceId() + ") already present in the db under the id " + integratedDocumentVersion.getId());

			} else {
				// Persist the document version
				if (documentVersion.getOriginalDocument() == null) {
					// Set the original document
					documentVersion.setOriginalDocument(documentVersion.getDocument());
				}
				integratedDocumentVersion = this.documentVersionRepository.persist(documentVersion);
			}

		} else {
			// Merge
			integratedDocumentVersion = this.documentVersionRepository.merge(documentVersion);
		}

		return integratedDocumentVersion;
	}

	/**
	 * Returns Document Version corresponding to the data presented by
	 *
	 * @param documentVersionSource The document version source to test
	 * @return The persisted document version corresponding to the one passed if it exists
	 */
	public DocumentVersion getDocumentVersionAlreadyPresent(DocumentVersionSource documentVersionSource) {
		DocumentVersionSource integratedDocumentVersionSource = this.documentVersionSourceRepository.getFirstBySourceId(documentVersionSource.getSourceId());

		if (integratedDocumentVersionSource != null) {
			return integratedDocumentVersionSource.getDocumentVersion();
		}

		return null;
	}

	public DocumentVersion getDocumentVersionAlreadyPresent(String sourceId, String importerShortName) {
		DocumentVersionSource integratedDocumentVersionSource = this.documentVersionSourceRepository.getFirstBySourceIdAndImporterShortName(sourceId, importerShortName);

		if (integratedDocumentVersionSource != null) {
			return integratedDocumentVersionSource.getDocumentVersion();
		}

		return null;
	}

	/**
	 * Return the last DocumentVersionSource importer by the specified importer
	 *
	 * @param importerShortName the shortname of the importer
	 * @return the last DocumentVersionSource importer by the specified importer
	 */
	public DocumentVersionSource getLastSourceByImporterShortName(String importerShortName) {
		return this.documentVersionSourceRepository.getLastSourceByImporterShortName(importerShortName);
	}

	public void getAllEntities(DocumentVersion documentVersion, boolean withFiles) {
		Hibernate.initialize(documentVersion.getPersonLaboratoryAffiliations());
		Hibernate.initialize(documentVersion.getInstruments());
		Hibernate.initialize(documentVersion.getJournalPublisherAffiliations());
		Hibernate.initialize(documentVersion.getFiles());
		Hibernate.initialize(documentVersion.getFormulas());
		Hibernate.initialize(documentVersion.getKeywords());
		Hibernate.initialize(documentVersion.getResearchDomains());
		Hibernate.initialize(documentVersion.getDocument());
		Hibernate.initialize(documentVersion.getReferences());
		Hibernate.initialize(documentVersion.getAdditionalTexts());
		Hibernate.initialize(documentVersion.getAnalysisState());
		Hibernate.initialize(documentVersion.getSources());
		Hibernate.initialize(documentVersion.getResolverInfos());

		if (withFiles) {
			for (PumaFile pumaFile : documentVersion.getFiles()) {
				if (pumaFile.getStatus().equals(PumaFileStatus.SAVED)) {
					pumaFileService.readFileData(pumaFile);
				}
			}
		}
	}

	public DocumentVersion getBySourceId(String sourceId, String importerShortName) {
		DocumentVersionSource source = this.documentVersionSourceRepository.getFirstBySourceIdAndImporterShortName(sourceId, importerShortName);

		return source.getDocumentVersion();
	}

	public DocumentVersion getBySourceIdWithEntities(String sourceId, String importerShortName, boolean withFile) {
		DocumentVersionSource source = this.documentVersionSourceRepository.getFirstBySourceIdAndImporterShortName(sourceId, importerShortName);

		if (source != null && source.getDocumentVersion() != null) {
			this.getAllEntities(source.getDocumentVersion(), withFile);
		}

		return source.getDocumentVersion();
	}

	public Long getNumberIndexed() {
		return this.elasticsearchRepository.getIndexSize(IndexedDocument.INDEXER_TYPE);
	}

	public IndexationResult<IndexedDocument> indexDocumentVersionWithId(Long id) throws DocumentVersionPersistenceException {
		// Get document version using current session
		DocumentVersion documentVersion = this.getById(id);

		// Create document to be indexed
		IndexedDocument indexedDocument = new IndexedDocument(documentVersion, this.pumaFileService);

		// Save index
		IndexationResult<IndexedDocument> result = elasticsearchRepository.save(indexedDocument, IndexedDocument.INDEXER_TYPE);

		if (result.hasError()) {
			log.error("Indexation failed for document " + documentVersion.getId() + " : " + result.getError());

		} else {
			// Save in DB with indexation date
			documentVersion.setIndexationDate(new Date());
			this.save(documentVersion);
		}

		return result;
	}

	public BulkIndexationResult<IndexedDocument> indexDocumentVersionsWithIds(List<Long> documentVersionIds) throws DocumentVersionPersistenceException {
		// Create documents to be indexed
		List<DocumentVersion> documentVersions = new ArrayList<>();
		List<IndexedDocument> indexedDocuments = new ArrayList<>();
		documentVersionIds.forEach(id -> {
			DocumentVersion documentVersion = this.getById(id);
			if (documentVersion != null) {
				documentVersions.add(documentVersion);
				IndexedDocument indexedDocument = new IndexedDocument(documentVersion, this.pumaFileService);
				indexedDocuments.add(indexedDocument);
			}
		});

		// Save index
		BulkIndexationResult<IndexedDocument> result = elasticsearchRepository.saveAll(indexedDocuments, IndexedDocument.INDEXER_TYPE);

		if (result.hasError()) {
			log.error("Bulk indexation failed : " + result.getError());

		} else {
			Date now = new Date();

			// Save in DB with indexation date
			for (DocumentVersion documentVersion : documentVersions) {
				documentVersion.setIndexationDate(now);
				this.save(documentVersion);
			}
		}

		return result;
	}

	public void removeIndex() {
		this.documentVersionRepository.resetAllIndexation();
		this.elasticsearchRepository.removeIndex();
	}

	public List<Long> getAllIdsForIndexation() {
		return this.documentVersionRepository.getAllIdsForIndexation();
	}

	public List<Long> getRemainingIdsForIndexation() {
		return this.documentVersionRepository.getRemainingIdsForIndexation();
	}

	public Long getNumberRemainingForIndexation() {
		return this.documentVersionRepository.getNumberRemainingForIndexation();
	}

	public List<Long> getIdsForIndexationWithIndexationDateBefore(Date date) {
		return this.documentVersionRepository.getIdsWithIndexationDateBefore(date);
	}

	public boolean canDocumentWithIdBeIndexed(Long id) {
		return this.documentVersionRepository.canDocumentWithIdBeIndexed(id);
	}

	public void eraseEntities(DocumentVersion documentVersion) {
		this.documentEraser.eraseDocumentEntities(documentVersion);
	}

	public DocumentVersion getNextDocumentVersionForReimport(String shortName, Long offsetId) {
		DocumentVersion documentVersion = this.documentVersionRepository.getNextDocumentVersionForReimport(shortName, offsetId);
		if (documentVersion != null) {
			Hibernate.initialize(documentVersion.getSources());
		}

		return documentVersion;
	}

}
