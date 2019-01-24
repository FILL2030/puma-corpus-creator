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

import eu.ill.puma.persistence.domain.analysis.ConfidenceLevel;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionEntityOrigin;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.repository.analyser.DocumentVersionEntityOriginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DocumentVersionEntityOriginService {

	@Autowired
	private DocumentVersionEntityOriginRepository repository;


	public List<DocumentVersionEntityOrigin> getAll() {
		return this.repository.getAll();
	}

	public List<DocumentVersionEntityOrigin> getAllForDocumentVersion(DocumentVersion documentVersion) {
		return this.repository.getAllForDocumentVersion(documentVersion);
	}

	public List<DocumentVersionEntityOrigin> getAllForDocumentVersionAndEntityOrigin(DocumentVersion documentVersion, String origin) {
		return this.repository.getAllForDocumentVersionAndEntityOrigin(documentVersion, origin);
	}

	public List<DocumentVersionEntityOrigin> getAllForEntityOrigin(String origin) {
		return this.repository.getAllForEntityOrigin(origin);
	}

	public List<DocumentVersionEntityOrigin> getAllForDocumentVersionAndEntityType(DocumentVersion documentVersion, EntityType entityType) {
		return this.repository.getAllForDocumentVersionAndEntityType(documentVersion, entityType);
	}

	public List<DocumentVersionEntityOrigin> getAllForEntityIdAndEntityType(Long entityId, EntityType entityType) {
		return this.repository.getAllForEntityIdAndEntityType(entityId, entityType);
	}

	public List<DocumentVersionEntityOrigin> saveAll(List<DocumentVersionEntityOrigin> entityOriginList){
		List<DocumentVersionEntityOrigin> savedEntityOrigins = new ArrayList();

		for(DocumentVersionEntityOrigin entityOrigin : entityOriginList){
			savedEntityOrigins.add(this.save(entityOrigin));
		}

		return savedEntityOrigins;
	}

	/**
	 * Persists the given DocumentVersionEntityOrigin and ensures we do not create duplicates.
	 *  - if the id is set we assume we have an already persisted object and the object is updated
	 *  - If the DocumentVersionEntityOrigin data match an existing DocumentVersionEntityOrigin then we know it is already persisted
	 * @param documentVersionEntityOrigin The DocumentVersionEntityOrigin to be persisted
	 * @return The persisted DocumentVersionEntityOrigin
	 */
	public synchronized DocumentVersionEntityOrigin save(DocumentVersionEntityOrigin documentVersionEntityOrigin) {
		DocumentVersionEntityOrigin integratedDocumentVersionEntityOrigin = null;

		// Check if it's a new object
		if (documentVersionEntityOrigin.getId() == null) {
			// Determine if already exists
			integratedDocumentVersionEntityOrigin = this.repository.getByDocumentVersionEntityOriginDetails(documentVersionEntityOrigin);

			if (integratedDocumentVersionEntityOrigin == null) {
				integratedDocumentVersionEntityOrigin = this.repository.persist(documentVersionEntityOrigin);
			}

		} else {
			// merge
			integratedDocumentVersionEntityOrigin = this.repository.merge(documentVersionEntityOrigin);
		}

		return integratedDocumentVersionEntityOrigin;
	}

	public boolean exists(DocumentVersionEntityOrigin entityOrigin) {
		DocumentVersionEntityOrigin existingEntityOrigin = this.repository.getByDocumentVersionEntityOriginDetails(entityOrigin);

		return  existingEntityOrigin != null;
	}

	public Map<EntityType, ArrayList<DocumentVersionEntityOrigin>> getAllInMapForDocumentVersion(DocumentVersion documentVersion) {
		List<DocumentVersionEntityOrigin> origins = this.getAllForDocumentVersion(documentVersion);

		return this.convertToMap(origins);
	}

	public Map<EntityType,ArrayList<DocumentVersionEntityOrigin>> getAllInMapForDocumentVersionAndEntityOrigin(DocumentVersion documentVersion, String origin) {
		List<DocumentVersionEntityOrigin> origins = this.getAllForDocumentVersionAndEntityOrigin(documentVersion, origin);

		return this.convertToMap(origins);
	}

	public synchronized void delete(DocumentVersionEntityOrigin entityOriginToDelete){
		this.repository.delete(entityOriginToDelete);
	}

	private Map<EntityType, ArrayList<DocumentVersionEntityOrigin>> convertToMap(List<DocumentVersionEntityOrigin> origins) {
		Map<EntityType, ArrayList<DocumentVersionEntityOrigin>> originsMap = new HashMap<>();
		for (DocumentVersionEntityOrigin origin : origins) {
			EntityType type = origin.getEntityType();
			if (!originsMap.containsKey(type)) {
				originsMap.put(type, new ArrayList<>());
			}
			originsMap.get(type).add(origin);
		}

		return originsMap;

	}


	public ConfidenceLevel getHighestConfidenceLevelForDocumentVersionAndEntityType(DocumentVersion documentVersion, EntityType entityType) {
		List<DocumentVersionEntityOrigin> origins = this.getAllForDocumentVersionAndEntityType(documentVersion, entityType);
		ConfidenceLevel highest = ConfidenceLevel.TODO;
		for (DocumentVersionEntityOrigin origin : origins) {
			if (origin.getConfidenceLevel().getValue() > highest.getValue()) {
				highest = origin.getConfidenceLevel();
			}
		}

		return highest;
	}
}
