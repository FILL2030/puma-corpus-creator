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

import eu.ill.puma.core.domain.document.MetadataConfidence;
import eu.ill.puma.persistence.domain.analysis.ConfidenceLevel;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionEntityOrigin;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.service.analysis.DocumentVersionEntityOriginService;

import java.util.LinkedHashSet;
import java.util.Set;

public class EntityOriginStore {

	private String origin;
	private DocumentVersion documentVersion;
	private Set<DocumentVersionEntityOrigin> entityOrigins = new LinkedHashSet<>();

	public EntityOriginStore(DocumentVersion documentVersion, String origin) {
		this.documentVersion = documentVersion;
		this.origin = origin;
	}

	public void foundEntity(Long entityId, EntityType entityType, MetadataConfidence metadataConfidence) {
		DocumentVersionEntityOrigin entityOrigin = DocumentVersionEntityOrigin.Found(this.documentVersion, entityId, entityType, this.origin, this.convertConfidenceLevel(metadataConfidence));

		// Add to set (duplicates ignored)
		this.entityOrigins.add(entityOrigin);
	}

	public void deleteEntity(Long entityId, EntityType entityType, MetadataConfidence metadataConfidence) {
		DocumentVersionEntityOrigin entityOrigin = DocumentVersionEntityOrigin.Deleted(this.documentVersion, entityId, entityType, this.origin, this.convertConfidenceLevel(metadataConfidence));

		// Add to set (duplicates ignored)
		this.entityOrigins.add(entityOrigin);
	}


	public void setDocumentVersion(DocumentVersion documentVersion) {
		this.documentVersion = documentVersion;

		for (DocumentVersionEntityOrigin entityOrigin : entityOrigins) {
			entityOrigin.setDocumentVersion(documentVersion);
		}
	}

	public boolean hasModifications() {
		return this.entityOrigins.size() > 0;
	}

	public void persistEntityOrigins(DocumentVersionEntityOriginService entityOriginService) {
		for (DocumentVersionEntityOrigin entityOrigin: this.entityOrigins) {
			entityOriginService.save(entityOrigin);
		}
	}

	public ConfidenceLevel convertConfidenceLevel(MetadataConfidence capability) {
		if (capability.equals(MetadataConfidence.TODO)) {
			// To do
			return ConfidenceLevel.TODO;

		} else if (capability.equals(MetadataConfidence.FOUND)) {
			// Found
			return ConfidenceLevel.FOUND;

		} else if (capability.equals(MetadataConfidence.CONFIDENT)) {
			// Confident
			return ConfidenceLevel.CONFIDENT;

		} else {
			// Sure
			return ConfidenceLevel.SURE;
		}
	}
}
