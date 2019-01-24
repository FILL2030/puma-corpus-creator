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

import eu.ill.puma.persistence.domain.document.Reference;
import eu.ill.puma.persistence.repository.document.ReferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReferenceService {

	private static final Logger log = LoggerFactory.getLogger(ReferenceService.class);

	@Autowired
	private ReferenceRepository referenceRepository;

	/**
	 * returns the Reference specified by the Id
	 *
	 * @param id the Reference Id
	 * @return The desired Reference
	 */
	public Reference getById(Long id) {
		return this.referenceRepository.getById(id);
	}

	/**
	 * returns all references
	 *
	 * @return List of all references
	 */
	public List<Reference> getAll() {
		return this.referenceRepository.getAll();
	}

	/**
	 * Persists the given Reference and ensures we do not create duplicates.
	 * - if the id is set we assume we have an already persisted object and the object is updated
	 * - If the Reference data are identical then we assume it is already persisted
	 *
	 * @param reference The reference to be persisted
	 * @return The persisted Reference
	 */
	public synchronized Reference saveReference(Reference reference) {
		Reference integratedReference = null;

		// Check if it is a new object
		if (reference.getId() == null) {
			// Check if it is already persisted
			integratedReference = this.referenceRepository.getByCitationStringAndCitingDocumentVersion(reference.getCitationString(), reference.getCitingDocumentVersion());

			if (integratedReference != null) {
				log.debug("reference " + reference.getCitationString() + " already present in the db under the id " + integratedReference.getId());

			} else {
				integratedReference = this.referenceRepository.persist(reference);
			}

		} else {
			// merge
			integratedReference = this.referenceRepository.merge(reference);
		}

		return integratedReference;
	}

	/**
	 * Persists the given Reference and ensures we do not create duplicates.
	 * - if the id is set we assume we have an already persisted object and the object is updated
	 * - If the Reference data are identical then we assume it is already persisted
	 *
	 * @param citation The citation to be persisted
	 * @return The persisted Reference
	 */
	public synchronized Reference saveCitation(Reference citation) {
		Reference integratedCitation = null;

		// Check if it is a new object
		if (citation.getId() == null) {
			// Check if it is already persisted
			integratedCitation = this.referenceRepository.getByCitingDocumentVersionSourceIdAndCitedDocument(citation.getCitingDocumentVersionSourceId(), citation.getCitedDocument());

			if (integratedCitation != null) {
				log.debug("citation " + citation.getCitationString() + " already present in the db under the id " + integratedCitation.getId());

			} else {
				integratedCitation = this.referenceRepository.persist(citation);
			}

		} else {
			// merge
			integratedCitation = this.referenceRepository.merge(citation);
		}

		return integratedCitation;
	}

	public synchronized void delete(Reference referenceToDelete) {
		this.referenceRepository.delete(referenceToDelete);
	}

}
