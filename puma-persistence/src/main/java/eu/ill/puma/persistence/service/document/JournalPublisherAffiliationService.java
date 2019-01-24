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

import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.Publisher;
import eu.ill.puma.persistence.domain.document.Journal;
import eu.ill.puma.persistence.domain.document.JournalPublisherAffiliation;
import eu.ill.puma.persistence.repository.document.JournalPublisherAffiliationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class JournalPublisherAffiliationService {

	private static final Logger log = LoggerFactory.getLogger(JournalPublisherAffiliationService.class);

	@Autowired
	private JournalPublisherAffiliationRepository journalPublisherAffiliationRepository;

	/**
	 * Returns a Affiliation based on their Id
	 * @param id The Id of the Affiliation
	 * @return The Affiliation corresponding to the Id
	 */
	public JournalPublisherAffiliation getById(Long id) {
		return this.journalPublisherAffiliationRepository.getById(id);
	}

	/**
	 * Persists the given Affiliation.
	 * @param journalPublisherAffiliation The Affiliation to be persisted
	 * @return The persisted Affiliation
	 */
	public synchronized JournalPublisherAffiliation save(JournalPublisherAffiliation journalPublisherAffiliation) {
		JournalPublisherAffiliation integratedJournalPublisherAffiliation;
		// Check if it is a new object
		if (journalPublisherAffiliation.getId() == null) {
			integratedJournalPublisherAffiliation = this.journalPublisherAffiliationRepository.persist(journalPublisherAffiliation);

		} else {
			// merge
			integratedJournalPublisherAffiliation = this.journalPublisherAffiliationRepository.merge(journalPublisherAffiliation);
		}

		return integratedJournalPublisherAffiliation;
	}

	/**
	 * Return all affiliations
	 * @return A list of all affiliations
	 */
	public List<JournalPublisherAffiliation> getAll() {
		return this.journalPublisherAffiliationRepository.getAll();
	}

	/**
	 * Return all affiliations for a given journal
	 * @return A list of all affiliations for the journal
	 */
	public List<JournalPublisherAffiliation> getAllForJournal(Journal journal) {
		return this.journalPublisherAffiliationRepository.getAllForJournal(journal);
	}

	/**
	 * Return all affiliations for a given publisher
	 * @return A list of all affiliations for the publisher
	 */
	public List<JournalPublisherAffiliation> getAllForPublisher(Publisher publisher) {
		return this.journalPublisherAffiliationRepository.getAllForPublisher(publisher);
	}

	/**
	 * Return all affiliations for a given documentVersion
	 * @return A list of all affiliations for the documentVersion
	 */
	public List<JournalPublisherAffiliation> getAllForDocumentVersion(DocumentVersion documentVersion) {
		return this.journalPublisherAffiliationRepository.getAllForDocumentVersion(documentVersion);
	}

	public List<JournalPublisherAffiliation> getFirstForJournalAndDocumentVersion(Journal journal, DocumentVersion documentVersion){
		return this.journalPublisherAffiliationRepository.getForJournalAndDocumentVersion(journal, documentVersion);
	}

	public List<JournalPublisherAffiliation> getFirstForJournalAndDocumentVersion(Publisher publisher, DocumentVersion documentVersion){
		return this.journalPublisherAffiliationRepository.getForPublisherAndDocumentVersion(publisher, documentVersion);
	}

	/**
	 * delete the given affiliation
	 * @param affiliationToDelete the affiliation to delete
	 */
	public synchronized void delete(JournalPublisherAffiliation affiliationToDelete){
		this.journalPublisherAffiliationRepository.delete(affiliationToDelete);
	}
}


