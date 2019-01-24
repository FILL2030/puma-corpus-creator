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

import eu.ill.puma.persistence.domain.document.ResearchDomain;
import eu.ill.puma.persistence.repository.document.ResearchDomainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ResearchDomainService {

	private static final Logger log = LoggerFactory.getLogger(ResearchDomainService.class);

	@Autowired
	private ResearchDomainRepository researchDomainRepository;

	/**
	 * Returns ResearchDomain from the given Id
	 * @param id The Id of the ResearchDomain
	 * @return The ResearchDomain with the given ID
	 */
	public ResearchDomain getById(Long id) {
		return this.researchDomainRepository.getById(id);
	}

	/**
	 * Persists the given ResearchDomain and ensures we do not create duplicates.
	 *  - if the id is set we assume we have an already persisted object and the object is updated
	 *  - If the ResearchDomain data match an existing ResearchDomain then we know it is already persisted
	 * @param researchDomain The ResearchDomain to be persisted
	 * @return The persisted ResearchDomain
	 */
	public synchronized ResearchDomain save(ResearchDomain researchDomain) {
		ResearchDomain integratedResearchDomain = null;

		// Check if it is a new object
		if (researchDomain.getId() == null) {
			// Determine if the object already exists
			integratedResearchDomain = this.researchDomainRepository.getBySubject(researchDomain.getSubject());
			if (integratedResearchDomain != null) {
				log.debug("research-domain " + researchDomain.getSubject() + " already present in the db under the id " + integratedResearchDomain.getId());

			} else {
				integratedResearchDomain = this.researchDomainRepository.persist(researchDomain);
			}

		} else {
			// merge
			integratedResearchDomain = this.researchDomainRepository.merge(researchDomain);
		}

		return integratedResearchDomain;
	}

	/**
	 * Return all research domains
	 * @return A list of all research domains
	 */
	public List<ResearchDomain> getAll() {
		return this.researchDomainRepository.getAll();
	}
}
