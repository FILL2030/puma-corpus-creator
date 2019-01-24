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
package eu.ill.puma.persistence.service.converterV2;

import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.persistence.domain.document.Document;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.DocumentVersionSource;
import eu.ill.puma.persistence.domain.document.enumeration.DocumentType;
import eu.ill.puma.persistence.service.analysis.DocumentVersionEntityOriginService;
import eu.ill.puma.persistence.service.converterV2.exception.PumaDocumentConversionException;
import eu.ill.puma.persistence.service.converterV2.integrater.*;
import eu.ill.puma.persistence.service.document.DocumentVersionService;
import eu.ill.puma.persistence.service.document.PumaFileService;
import eu.ill.puma.persistence.service.importer.ImporterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Created by letreguilly on 09/08/17.
 */
@Service
public class DocumentConverter {

	private static final Logger log = LoggerFactory.getLogger(DocumentConverter.class);

	/**
	 * persistence service
	 */
	@Autowired
	private DocumentVersionService documentVersionService;

	@Autowired
	private ImporterService importerService;

	@Autowired
	private DocumentVersionEntityOriginService entityOriginService;

	@Autowired
	private FileIntegrator fileConvertor;


	/**
	 * converter service
	 */

	@Autowired
	private EntitiesIntegrator entitiesConvertor;

	@Autowired
	private KeywordIntegrator keywordConvertor;

	@Autowired
	private ResearchDomainIntegrator researchDomainConvertor;

	@Autowired
	private ReferenceIntegrator referenceConvertor;

	@Autowired
	private FormulaIntegrator formulaConvertor;

	@Autowired
	private JournalPublisherIntegrator journalPublisherConvertor;

	@Autowired
	private FileToDownloadIntegrator fileToDownloadConvertor;

	@Autowired
	private AdditionalTextIntegrator additionalTextConvertor;

	@Autowired
	private LaboratoryPersonInstrumentIntegrator laboratoryIntegrator;

	@Autowired
	private CitationIntegrator citationIntegrator;


	/**
	 * @param baseDocument
	 * @param origin
	 * @return
	 * @throws PumaDocumentConversionException
	 */
	@Transactional
	public DocumentVersion convert(BaseDocument baseDocument, String origin) throws PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {
		DocumentVersion documentVersion = this.convert(baseDocument, origin, false, false);

		return documentVersion;
	}

	@Transactional
	public DocumentVersion convert(BaseDocument baseDocument, String origin, boolean forceDocumentUpdate, boolean updateCitations) throws PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {
		DocumentVersion documentVersion = this.convert(null, baseDocument, origin, forceDocumentUpdate, updateCitations);

		return documentVersion;
	}

	@Transactional
	public DocumentVersion convert(DocumentVersion documentVersionFromDb, BaseDocument baseDocument, String origin) throws PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {
		DocumentVersion documentVersion = this.convert(documentVersionFromDb, baseDocument, origin, false, false);

		return documentVersion;
	}

	@Transactional
	public DocumentVersion convert(DocumentVersion documentVersionFromDb, BaseDocument baseDocument, String origin, boolean forceDocumentUpdate, boolean updateCitations) throws PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {
		try {

			//get document already present in the db if exist
			if (documentVersionFromDb == null) {
				if (importerService.isImporterShortName(origin)) {
					documentVersionFromDb = documentVersionService.getDocumentVersionAlreadyPresent(baseDocument.getSourceId(), origin);
				} else {
					documentVersionFromDb = documentVersionService.getById(baseDocument.getPumaId());
				}
			}

			DocumentVersion documentVersion;

			//new document from importer
			if (documentVersionFromDb == null && importerService.isImporterShortName(origin) && !updateCitations) {
				//log
				log.debug("import new document from importer " + origin);

				//document from importer
				documentVersion = this.buildNewDocumentVersion(baseDocument, origin);

				//do conversion
				documentVersion = this.updateDocumentEntities(documentVersion, baseDocument, origin);

				return documentVersion;

				//update from analyser
			} else if (!importerService.isImporterShortName(origin)) {
				//log
				log.debug("receive update from analyser " + origin + " for documentVersion " + documentVersionFromDb.getId());

				//load entities
				documentVersionFromDb = documentVersionService.getByIdWithAllEntities(documentVersionFromDb.getId(), true);

				//do conversion
				documentVersion = this.updateDocumentEntities(documentVersionFromDb, baseDocument, origin);

				return documentVersion;

				//document already imported
			} else {

				// Check if only citations or update is forced or the original document has changed
				if (updateCitations) {
					//log
					log.info("Citation update for document version " + documentVersionFromDb.getId() + " : " + baseDocument.getSourceId());

					documentVersion = this.updateDocumentCitations(documentVersionFromDb, baseDocument, origin);

					return documentVersion;

				} else if (forceDocumentUpdate || baseDocument.isModifiedAtSource()) {
					//log
					log.info("Update forced for document version " + documentVersionFromDb.getId() + " : " + baseDocument.getSourceId());
					documentVersionService.eraseEntities(documentVersionFromDb);

					//load entities
					documentVersionFromDb = documentVersionService.getByIdWithAllEntities(documentVersionFromDb.getId(), true);

					//do conversion
					documentVersion = this.updateDocumentEntities(documentVersionFromDb, baseDocument, origin);
					return documentVersion;

				} else {
					//document already exist
					log.info("Document version already exists " + documentVersionFromDb.getId() + " : " + baseDocument.getSourceId() + " for importer " + origin);

					return null;
				}
			}
		} catch (DocumentVersionService.DocumentVersionPersistenceException e) {
			throw new PumaDocumentConversionException("failed to convert baseDocument " + baseDocument.getSourceId() + " from origin " + origin, e);
		}
	}


	private DocumentVersion updateDocumentEntities(DocumentVersion documentVersion, BaseDocument baseDocument, String origin) throws DocumentVersionService.DocumentVersionPersistenceException, PumaFileService.PumaFilePersistenceException, PumaDocumentConversionException {

		EntityOriginStore entityOriginStore = new EntityOriginStore(documentVersion, origin);

		int originalHashCode = documentVersion.hashCode();
		int originalNumberOfFiles = documentVersion.getFiles().size();

		//shortName
		entitiesConvertor.convertShortName(documentVersion, baseDocument);

		//subType
		entitiesConvertor.convertSubType(documentVersion, baseDocument);

		//files to download
		fileToDownloadConvertor.convert(documentVersion, baseDocument);

		//additionalText
		additionalTextConvertor.convert(documentVersion, baseDocument, entityOriginStore);

		//abstract
		entitiesConvertor.convertAbstract(documentVersion, baseDocument, entityOriginStore);

		//doi
		entitiesConvertor.convertDoi(documentVersion, baseDocument, entityOriginStore);

		//releaseDate
		entitiesConvertor.convertReleaseDate(documentVersion, baseDocument, entityOriginStore);

		//title
		entitiesConvertor.convertTitle(documentVersion, baseDocument, entityOriginStore);

		//keywords
		keywordConvertor.convert(documentVersion, baseDocument, entityOriginStore);

		//research domain
		researchDomainConvertor.convert(documentVersion, baseDocument, entityOriginStore);

		//reference
		referenceConvertor.convert(documentVersion, baseDocument, entityOriginStore);

		//files
		fileConvertor.convert(documentVersion, baseDocument, entityOriginStore);

		//formula
		formulaConvertor.convert(documentVersion, baseDocument, entityOriginStore);

		//journal publisher
		journalPublisherConvertor.convert(documentVersion, baseDocument, entityOriginStore);

		//laboratory person
		laboratoryIntegrator.convert(documentVersion, baseDocument, entityOriginStore);

		//citation
		citationIntegrator.convert(documentVersion, baseDocument, entityOriginStore);

		int finalHashCode = documentVersion.hashCode();
		int finalNumberOfFile = documentVersion.getFiles().size();

		boolean documentModified = entityOriginStore.hasModifications() || (finalHashCode != originalHashCode) || originalNumberOfFiles != finalNumberOfFile;

		if (documentModified) {
			// Update modification date
			documentVersion.setLastModificationDate(new Date());
		}

		//persist (in all cases just to be sure)
		documentVersion = documentVersionService.save(documentVersion);

		entityOriginStore.persistEntityOrigins(entityOriginService);

		if (!documentModified) {
			log.info("Document version unmodified from " + baseDocument.getSourceId() + " for importer " + origin);
			return null;
		}

		return documentVersion;
	}

	private DocumentVersion updateDocumentCitations(DocumentVersion documentVersion, BaseDocument baseDocument, String origin) throws DocumentVersionService.DocumentVersionPersistenceException, PumaFileService.PumaFilePersistenceException, PumaDocumentConversionException {

		EntityOriginStore entityOriginStore = new EntityOriginStore(documentVersion, origin);

		int originalNumberOfCitations = documentVersion.getDocument().getReferences().size();

		//citation
		citationIntegrator.convert(documentVersion, baseDocument, entityOriginStore);

		int finalNumberOfCitations = documentVersion.getDocument().getReferences().size();

		boolean documentModified = entityOriginStore.hasModifications() || (finalNumberOfCitations != originalNumberOfCitations);

		// Update modification date
		if (documentModified) {
			documentVersion.setLastModificationDate(new Date());
		}

		//persist (in all cases just to be sure)
		documentVersion = documentVersionService.save(documentVersion);

		entityOriginStore.persistEntityOrigins(entityOriginService);

		if (documentModified) {
			log.info("Document version " + documentVersion.getId() + " : "  + baseDocument.getSourceId() + " citations changed from " + originalNumberOfCitations + " to " + finalNumberOfCitations);

		} else {
			return null;
		}

		return documentVersion;
	}

	private DocumentVersion buildNewDocumentVersion(BaseDocument baseDocument, String origin) throws DocumentVersionService.DocumentVersionPersistenceException {
		//new document
		DocumentVersion documentVersion = new DocumentVersion();
		Document document = new Document();
		document.addDocumentVersion(documentVersion);
		documentVersion.setDocument(document);

		//set document Type
		if (baseDocument.getType() != null && documentVersion.getDocument().getDocumentType() == null) {
			switch (baseDocument.getType()) {
				case LETTER:
					document.setDocumentType(DocumentType.LETTER);
					break;
				case MEETING_ABSTRACT:
					document.setDocumentType(DocumentType.MEETING_ABSTRACT);
					break;
				case PATENT:
					document.setDocumentType(DocumentType.PATENT);
					break;
				case PROCEEDING:
					document.setDocumentType(DocumentType.PROCEEDING);
					break;
				case PROPOSAL:
					document.setDocumentType(DocumentType.PROPOSAL);
					break;
				case PUBLICATION:
					document.setDocumentType(DocumentType.PUBLICATION);
					break;
				case REVIEW:
					document.setDocumentType(DocumentType.REVIEW);
					break;
				case THESIS:
					document.setDocumentType(DocumentType.THESIS);
					break;
				case UNKNOWN:
					document.setDocumentType(DocumentType.UNKNOWN);
					break;
				default:
					document.setDocumentType(DocumentType.UNKNOWN);
					break;
			}
		}

		//add source
		DocumentVersionSource documentVersionSource = new DocumentVersionSource();
		documentVersionSource.setDocumentVersion(documentVersion);
		documentVersionSource.setImportDate(new Date());
		documentVersionSource.setImporterShortName(origin);
		documentVersionSource.setSourceId(baseDocument.getSourceId());
		documentVersion.addSource(documentVersionSource);

		//persist
		documentVersion = documentVersionService.save(documentVersion);

		return documentVersion;
	}
}