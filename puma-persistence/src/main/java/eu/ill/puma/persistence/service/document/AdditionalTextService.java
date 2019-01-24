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

import eu.ill.puma.persistence.domain.document.AdditionalText;
import eu.ill.puma.persistence.repository.document.AdditionalTextRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdditionalTextService {

	private static final Logger log = LoggerFactory.getLogger(AdditionalTextService.class);

	@Autowired
	private AdditionalTextRepository additionalTextRepository;

	/**
	 * returns the AdditionalText specified by the Id
	 * @param id the AdditionalText Id
	 * @return The desired AdditionalText
	 */
	public AdditionalText getById(Long id) {
		return this.additionalTextRepository.getById(id);
	}

	/**
	 * returns all additionalTexts
	 * @return List of all additionalTexts
	 */
	public List<AdditionalText> getAll() {
		return this.additionalTextRepository.getAll();
	}

	/**
	 * Persists the given AdditionalText and ensures we do not create duplicates.
	 *  - if the id is set we assume we have an already persisted object and the object is updated
	 *  - If the AdditionalText data are identical then we assume it is already persisted
	 * @param additionalText The additionalText to be persisted
	 * @return The persisted AdditionalText
	 */
	public synchronized AdditionalText save(AdditionalText additionalText) {
		AdditionalText integratedAdditionalText = null;

		// Check if it is a new object
		if (additionalText.getId() == null) {
			integratedAdditionalText = this.additionalTextRepository.persist(additionalText);

		} else {
			// merge
			integratedAdditionalText = this.additionalTextRepository.merge(additionalText);
		}

		return integratedAdditionalText;
	}

}
