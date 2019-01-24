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
package eu.ill.puma.persistence.service.converterV2.integrater;

import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.persistence.domain.analysis.ConfidenceLevel;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.enumeration.DocumentVersionSubType;
import eu.ill.puma.persistence.service.analysis.DocumentVersionEntityOriginService;
import eu.ill.puma.persistence.service.converterV2.EntityOriginStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by letreguilly on 09/08/17.
 */
@Service
public class EntitiesIntegrator {

	@Autowired
	private DocumentVersionEntityOriginService entityOriginService;

	public void convertShortName(DocumentVersion documentVersion, BaseDocument baseDocument) {
		if (baseDocument.getShortName() != null && baseDocument.getShortName().getValue() != null) {
			documentVersion.setShortName(baseDocument.getShortName().getValue());
		}
	}

	public void convertSubType(DocumentVersion documentVersion, BaseDocument baseDocument) {
		if (baseDocument.getSubType() != null && documentVersion.getSubType() == null) {
			switch (baseDocument.getSubType()) {
				case FINAL:
					documentVersion.setSubType(DocumentVersionSubType.FINAL);
					break;
				case PREPRINT:
					documentVersion.setSubType(DocumentVersionSubType.PREPRINT);
					break;
				case UNKNOWN:
					documentVersion.setSubType(DocumentVersionSubType.UNKNOWN);
					break;
				default:
					documentVersion.setSubType(DocumentVersionSubType.UNKNOWN);
					break;
			}
		}
	}

	public void convertAbstract(DocumentVersion documentVersion, BaseDocument baseDocument, EntityOriginStore entityOriginStore) {
		if (baseDocument.getAbstract() != null && baseDocument.getAbstract().getValue() != null ) {
			if (abstractHasChanged(baseDocument, documentVersion)) {
				if (documentVersion.getAbstractText() != null) {
					ConfidenceLevel highestLevel = this.entityOriginService.getHighestConfidenceLevelForDocumentVersionAndEntityType(documentVersion, EntityType.ABSTRACT);
					if (highestLevel.getValue() <= entityOriginStore.convertConfidenceLevel(baseDocument.getAbstract().getConfidence()).getValue()) {
						// update abstract
						documentVersion.setAbstractText(baseDocument.getAbstract().getValue());
					}

				} else {
					//new abstract
					documentVersion.setAbstractText(baseDocument.getAbstract().getValue());
				}

				//create entity origin
				entityOriginStore.foundEntity(null, EntityType.ABSTRACT, baseDocument.getAbstract().getConfidence());
			}
		}
	}

	public void convertDoi(DocumentVersion documentVersion, BaseDocument baseDocument, EntityOriginStore entityOriginStore) {
		if (baseDocument.getDoi() != null && baseDocument.getDoi().getValue() != null ) {
			if (doiHasChanged(baseDocument, documentVersion)) {
				if (documentVersion.getDoi() != null) {
					ConfidenceLevel highestLevel = this.entityOriginService.getHighestConfidenceLevelForDocumentVersionAndEntityType(documentVersion, EntityType.DOI);
					if (highestLevel.getValue() <= entityOriginStore.convertConfidenceLevel(baseDocument.getDoi().getConfidence()).getValue()) {
						// update doi
						documentVersion.setDoi(baseDocument.getDoi().getValue());
					}

				} else {
					// new doi
					documentVersion.setDoi(baseDocument.getDoi().getValue());
				}

				//create entity origin
				entityOriginStore.foundEntity(null, EntityType.DOI, baseDocument.getDoi().getConfidence());
			}
		}
	}

	public void convertReleaseDate(DocumentVersion documentVersion, BaseDocument baseDocument, EntityOriginStore entityOriginStore) {
		if (baseDocument.getReleaseDate() != null && baseDocument.getReleaseDate().getValue() != null ) {
			if (releaseDateHasChanged(baseDocument, documentVersion)) {
				if (documentVersion.getReleaseDate() != null) {
					ConfidenceLevel highestLevel = this.entityOriginService.getHighestConfidenceLevelForDocumentVersionAndEntityType(documentVersion, EntityType.RELEASE_DATE);
					if (highestLevel.getValue() <= entityOriginStore.convertConfidenceLevel(baseDocument.getReleaseDate().getConfidence()).getValue()) {
						// update release date
						documentVersion.setReleaseDate(baseDocument.getReleaseDate().getValue());
					}

				} else {
					//new release date
					documentVersion.setReleaseDate(baseDocument.getReleaseDate().getValue());
				}

				//create entity origin
				entityOriginStore.foundEntity(null, EntityType.RELEASE_DATE, baseDocument.getReleaseDate().getConfidence());
			}
		}
	}

	public void convertTitle(DocumentVersion documentVersion, BaseDocument baseDocument, EntityOriginStore entityOriginStore) {
		if (baseDocument.getTitle() != null && baseDocument.getTitle().getValue() != null ) {
			if (titleHasChanged(baseDocument, documentVersion)) {
				if (documentVersion.getTitle() != null) {
					ConfidenceLevel highestLevel = this.entityOriginService.getHighestConfidenceLevelForDocumentVersionAndEntityType(documentVersion, EntityType.TITLE);
					if (highestLevel.getValue() <= entityOriginStore.convertConfidenceLevel(baseDocument.getTitle().getConfidence()).getValue()) {
						// update title
						documentVersion.setTitle(baseDocument.getTitle().getValue());
					}

				} else {
					//new title
					documentVersion.setTitle(baseDocument.getTitle().getValue());
				}

				//create entity origin
				entityOriginStore.foundEntity(null, EntityType.TITLE, baseDocument.getTitle().getConfidence());
			}
		}
	}

	private boolean abstractHasChanged(BaseDocument baseDocument, DocumentVersion documentVersion) {
		if (documentVersion.getAbstractText() == null) {
			return true;

		} else {
			return !(baseDocument.getAbstract().getValue().toLowerCase().equals(documentVersion.getAbstractText()));
		}
	}

	private boolean titleHasChanged(BaseDocument baseDocument, DocumentVersion documentVersion) {
		if (documentVersion.getTitle() == null) {
			return true;

		} else {
			return !(baseDocument.getTitle().getValue().toLowerCase().equals(documentVersion.getTitle()));
		}
	}

	private boolean doiHasChanged(BaseDocument baseDocument, DocumentVersion documentVersion) {
		if (documentVersion.getDoi() == null) {
			return true;

		} else {
			return !(baseDocument.getDoi().getValue().equals(documentVersion.getDoi()));
		}
	}

	private boolean releaseDateHasChanged(BaseDocument baseDocument, DocumentVersion documentVersion) {
		if (documentVersion.getReleaseDate() == null) {
			return true;

		} else {
			return !(baseDocument.getReleaseDate().getValue().equals(documentVersion.getReleaseDate()));
		}
	}

}
