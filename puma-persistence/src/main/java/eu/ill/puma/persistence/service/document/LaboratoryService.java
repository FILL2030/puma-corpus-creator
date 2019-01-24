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

import eu.ill.puma.persistence.domain.document.Laboratory;
import eu.ill.puma.persistence.repository.document.LaboratoryRepository;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LaboratoryService {

	private static final Logger log = LoggerFactory.getLogger(LaboratoryService.class);

	@Autowired
	private LaboratoryRepository laboratoryRepository;

	@Autowired
	private PersonLaboratoryAffiliationService personLaboratoryAffiliationService;

	@Autowired
	private InstrumentService instrumentService;

	/**
	 * Returns a Laboratory based on the Id
	 */
	public Laboratory getById(Long id) {
		return this.laboratoryRepository.getById(id);
	}

	/**
	 * Return a Laboratory based on the Id with instruments and persons instantiated
	 */
	public Laboratory getByIdCompleted(Long id) {
		Laboratory laboratory = this.laboratoryRepository.getById(id);

		// Initialize collections/Entities
		Hibernate.initialize(laboratory.getInstruments());

		return laboratory;
	}

	/**
	 * Return all laboratories
	 * @return A list of all laboratories
	 */
	public List<Laboratory> getAll() {
		return this.laboratoryRepository.getAll();
	}

	/**
	 * Persists the given Laboratory and ensures we do not create duplicates.
	 *  - if the id is set we assume we have an already persisted object and the object is updated
	 *  - If the Laboratory data match an existing Laboratory then we know it is already persisted
	 * @param laboratory The Laboratory to be persisted
	 * @return The persisted Laboratory
	 */
	public synchronized Laboratory save(Laboratory laboratory) {
		Laboratory integratedLaboratory = null;

		// Check if it's a new object
		if (laboratory.getId() == null) {
			// Determine if already exists
			integratedLaboratory = this.laboratoryRepository.getByLaboratoryDetails(laboratory.getName(), laboratory.getShortName(), laboratory.getAddress(), laboratory.getCity(), laboratory.getCountry());

			if (integratedLaboratory != null) {
				log.debug("laboratory " + laboratory.getName() + " already present in the db under the id " + integratedLaboratory.getId());

			} else {
				integratedLaboratory = this.laboratoryRepository.persist(laboratory);
			}

		} else {
			// merge
			integratedLaboratory = this.laboratoryRepository.merge(laboratory);
		}

		return integratedLaboratory;
	}

	/**
	 * Returns the count of all laboratories
	 */
	public long count() {
		return this.laboratoryRepository.count();
	}
}
