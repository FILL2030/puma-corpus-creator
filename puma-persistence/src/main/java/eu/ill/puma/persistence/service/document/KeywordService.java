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

import eu.ill.puma.persistence.domain.document.Keyword;
import eu.ill.puma.persistence.repository.document.KeywordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class KeywordService {

	private static final Logger log = LoggerFactory.getLogger(KeywordService.class);

	@Autowired
	private KeywordRepository keywordRepository;

	/**
	 * Return a Keyword given its Id
	 * @param id The Id of the keyword
	 * @return The Keyword associated with the Id
	 */
	public Keyword getById(Long id) {
		return this.keywordRepository.getById(id);
	}

	/**
	 * Persists the given Keyword and ensures we do not create duplicates.
	 *  - if the id is set we assume we have an already persisted object and the object is updated
	 *  - If the Keyword data match an existing Keyword then we know it is already persisted
	 * @param keyword The Keyword to be persisted
	 * @return The persisted Keyword
	 */
	public synchronized Keyword save(Keyword keyword) {
		Keyword integratedKeyword = null;

		//if it's a new object
		if (keyword.getId() == null) {
			// Check if it's a new object
			integratedKeyword = this.keywordRepository.getByWord(keyword.getWord());
			if (integratedKeyword != null) {
				log.debug("keyword " + keyword.getWord() + " already present in the db under the id " + integratedKeyword.getId());

			} else {
				integratedKeyword = this.keywordRepository.persist(keyword);
			}
		} else {
			// merge
			integratedKeyword = this.keywordRepository.merge(keyword);
		}

		return integratedKeyword;
	}

	/**
	 * Return all keywords
	 * @return A list of all keywords
	 */
	public List<Keyword> getAll() {
		return this.keywordRepository.getAll();
	}
}
