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
package eu.ill.puma.persistence.repository.analyser;

import eu.ill.puma.persistence.domain.analysis.DocumentVersionEntityOrigin;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.repository.PumaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class DocumentVersionEntityOriginRepository extends PumaRepository<DocumentVersionEntityOrigin> {


	public List<DocumentVersionEntityOrigin> getAllForDocumentVersion(DocumentVersion documentVersion) {
		return this.getEntities("documentVersion", documentVersion);
	}

	public List<DocumentVersionEntityOrigin> getAllForDocumentVersionAndEntityType(DocumentVersion documentVersion, EntityType entityType) {
		return this.getEntities(Arrays.asList("documentVersion", "entityType"), documentVersion, entityType);
	}

	public List<DocumentVersionEntityOrigin> getAllForDocumentVersionAndEntityOrigin(DocumentVersion documentVersion, String origin) {
		return this.getEntities(Arrays.asList("documentVersion", "entityOrigin"), documentVersion, origin);
	}

	public List<DocumentVersionEntityOrigin> getAllForEntityIdAndEntityType(Long entityId, EntityType entityType) {
		return this.getEntities(Arrays.asList("entityId", "entityType"), entityId, entityType);
	}

	public DocumentVersionEntityOrigin getByDocumentVersionEntityOriginDetails(DocumentVersionEntityOrigin origin) {
		return this.getFirstEntity(Arrays.asList("documentVersion", "entityId", "entityType", "entityOrigin", "action"), origin.getDocumentVersion(), origin.getEntityId(), origin.getEntityType(), origin.getEntityOrigin(), origin.getAction());
	}

	public List<DocumentVersionEntityOrigin> getAllForEntityOrigin(String origin) {
		return this.getEntities(Arrays.asList("entityOrigin"), origin);
	}
}
