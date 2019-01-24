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

import eu.ill.puma.persistence.domain.document.Journal;
import eu.ill.puma.persistence.repository.document.JournalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class JournalService {

	private static final Logger log = LoggerFactory.getLogger(JournalService.class);

	@Autowired
	private JournalRepository journalRepository;

	@Autowired
	private JournalPublisherAffiliationService journalPublisherAffiliationService;

	/**
	 * Returns a Journal given its Id
	 * @param id The Id of the Journal to obtain
	 * @return The Journal with the given Id
	 */
	public Journal getById(Long id) {
		return this.journalRepository.getById(id);
	}

	/**
	 * Returns all journals
	 * @return A list of all Journals
	 */
	public List<Journal> getAll() {
		return this.journalRepository.getAll();
	}

	/**
	 * Save the given Journal, if the id is not set and a keyword with the same
	 * attributes is found, both are merged
	 *
	 * @return
	 */

	/**
	 * Persists the given Journal and ensures we do not create duplicates.
	 *  - if the id is set we assume we have an already persisted object and the object is updated
	 *  - If the Journal data match an existing Journal then we know it is already persisted
	 * @param journal The Journal to be persisted
	 * @return The persisted Journal
	 */
	public synchronized Journal save(Journal journal) {
		Journal integratedJournal = null;

		// Check if it's a new object
		if (journal.getId() == null) {
			// Check if it is already persisted
			integratedJournal = this.journalRepository.getByName(journal.getName());
			if (integratedJournal != null) {
				log.debug("journal " + journal.getName() + " already present in the db under the id " + integratedJournal.getId());

			} else {
				integratedJournal = this.journalRepository.persist(journal);
			}

		} else {
			// merge
			integratedJournal = this.journalRepository.merge(journal);
		}

		return integratedJournal;
	}
}
