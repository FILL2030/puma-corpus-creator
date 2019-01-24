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
import eu.ill.puma.persistence.domain.document.Laboratory;
import eu.ill.puma.persistence.domain.document.Person;
import eu.ill.puma.persistence.domain.document.PersonLaboratoryAffiliation;
import eu.ill.puma.persistence.repository.document.PersonLaboratoryAffiliationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PersonLaboratoryAffiliationService {

	private static final Logger log = LoggerFactory.getLogger(PersonLaboratoryAffiliationService.class);

	@Autowired
	private PersonLaboratoryAffiliationRepository personLaboratoryAffiliationRepository;

	/**
	 * Returns a Affiliation based on their Id
	 * @param id The Id of the Affiliation
	 * @return The Affiliation corresponding to the Id
	 */
	public PersonLaboratoryAffiliation getById(Long id) {
		return this.personLaboratoryAffiliationRepository.getById(id);
	}

	/**
	 * Persists the given Affiliation.
	 * @param personLaboratoryAffiliation The Affiliation to be persisted
	 * @return The persisted Affiliation
	 */
	public synchronized PersonLaboratoryAffiliation save(PersonLaboratoryAffiliation personLaboratoryAffiliation) {
		PersonLaboratoryAffiliation integratedPersonLaboratoryAffiliation;
		// Check if it is a new object
		if (personLaboratoryAffiliation.getId() == null) {
			integratedPersonLaboratoryAffiliation = this.personLaboratoryAffiliationRepository.persist(personLaboratoryAffiliation);

		} else {
			// merge
			integratedPersonLaboratoryAffiliation = this.personLaboratoryAffiliationRepository.merge(personLaboratoryAffiliation);
		}

		return integratedPersonLaboratoryAffiliation;
	}

	/**
	 * Return all affiliations
	 * @return A list of all affiliations
	 */
	public List<PersonLaboratoryAffiliation> getAll() {
		return this.personLaboratoryAffiliationRepository.getAll();
	}

	/**
	 * Return all affiliations for a given person
	 * @return A list of all affiliations for the person
	 */
	public List<PersonLaboratoryAffiliation> getAllForPerson(Person person) {
		return this.personLaboratoryAffiliationRepository.getAllForPerson(person);
	}

	/**
	 * Return all affiliations for a given laboratory
	 * @return A list of all affiliations for the laboratory
	 */
	public List<PersonLaboratoryAffiliation> getAllForLaboratory(Laboratory laboratory) {
		return this.personLaboratoryAffiliationRepository.getAllForLaboratory(laboratory);
	}

	public Long getCountForLaboratory(Laboratory laboratory) {
		return this.personLaboratoryAffiliationRepository.getCountForLaboratory(laboratory);
	}

	/**
	 * Return all affiliations for a given documentVersion
	 * @return A list of all affiliations for the documentVersion
	 */
	public List<PersonLaboratoryAffiliation> getAllForDocumentVersion(DocumentVersion documentVersion) {
		return this.personLaboratoryAffiliationRepository.getAllForDocumentVersion(documentVersion);
	}

	/**
	 * return all afflition for a document version and person
	 * @param documentVersion
	 * @param person
	 * @return
	 */
	public List<PersonLaboratoryAffiliation> getAllForDocumentVersionAndPerson(DocumentVersion documentVersion, Person person) {
		return this.personLaboratoryAffiliationRepository.getAllForDocumentVersionAndPerson(documentVersion, person);
	}

	/**
	 * return all afflition for a document version and laboratory
	 * @param documentVersion
	 * @param laboratory
	 * @return
	 */
	public List<PersonLaboratoryAffiliation> getAllForDocumentVersionAndLaboratory(DocumentVersion documentVersion, Laboratory laboratory) {
		return this.personLaboratoryAffiliationRepository.getAllForDocumentVersionAndLaboratory(documentVersion, laboratory);
	}

	public List<PersonLaboratoryAffiliation> getAllForDocumentVersionAndPersonAndLaboratory(DocumentVersion documentVersion, Person person, Laboratory laboratory) {
		return this.personLaboratoryAffiliationRepository.getAllForDocumentVersionAndPersonAndLaboratory(documentVersion, person, laboratory);
	}

	/**
	 * delete the given affiliation
	 * @param affiliationToDelete the affiliation to delete
	 */
	public synchronized void delete(PersonLaboratoryAffiliation affiliationToDelete){
		this.personLaboratoryAffiliationRepository.delete(affiliationToDelete);
	}

}


