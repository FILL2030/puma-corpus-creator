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
package eu.ill.puma.persistence.service.deduplication;

import eu.ill.puma.persistence.domain.analysis.ConfidenceLevel;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionEntityOrigin;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import eu.ill.puma.persistence.domain.document.Person;
import eu.ill.puma.persistence.domain.document.PersonLaboratoryAffiliation;
import eu.ill.puma.persistence.service.analysis.DocumentVersionEntityOriginService;
import eu.ill.puma.persistence.service.document.PersonLaboratoryAffiliationService;
import eu.ill.puma.persistence.service.document.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PersonDeduplicationService {

	private static final Logger log = LoggerFactory.getLogger(PersonDeduplicationService.class);

	@Autowired
	private PersonService personService;

	@Autowired
	private PersonLaboratoryAffiliationService personLaboratoryAffiliationService;

	@Autowired
	private DocumentVersionEntityOriginService entityOriginService;

	/**
	 * Updates a person
	 * @return
	 */
	public Person updatePerson(Person person, String origin, ConfidenceLevel confidenceLevel) {
		// Just save the person
		log.debug("Updating person " + person);

		// Get original person
		Person originalPerson = this.personService.getById(person.getId());

		// Mark person with Id as obsolete
		originalPerson.setObsolete(true);
		this.personService.save(originalPerson);

		// create a new person
		person.setId(null);
		Person newPerson = this.personService.save(person);

		// update all affiliations
		this.updateAffiliations(originalPerson, newPerson, origin, confidenceLevel);

		return newPerson;
	}

	/**
	 * Soft deletes all affiliations for the given person
	 * @param person
	 */
	@Transactional
	public void deletePerson(Person person, String origin, ConfidenceLevel confidenceLevel) {
		log.debug("Deleting person " + person);

		// Mark person with Id as obsolete
		person.setObsolete(true);
		this.personService.save(person);

		// update all affiliations
		this.updateAffiliations(person, null, origin, confidenceLevel);
	}

	/**
	 * Replaces a person and associated affiliations
	 * @param person
	 * @param personToKeep
	 */
	public void replace(Person person, Person personToKeep, String origin, ConfidenceLevel confidenceLevel) {
		// do nothing if they are the same persons
		if (person.getId().equals(personToKeep.getId())) {
			return;
		}

		log.debug("Replacing person " + person + " with person " + personToKeep);

		// Mark the person as obsolete
		person.setObsolete(true);
		this.personService.save(person);

		// Update all affiliations
		this.updateAffiliations(person, personToKeep, origin, confidenceLevel);
	}


	private void updateAffiliations(Person originalPerson, Person newPerson, String origin, ConfidenceLevel confidenceLevel) {
		List<PersonLaboratoryAffiliation> affiliations = this.personLaboratoryAffiliationService.getAllForPerson(originalPerson);
		for (PersonLaboratoryAffiliation oldAffiliation : affiliations) {

			// Set old affiliation as obsolete
			oldAffiliation.setObsolete(true);
			this.personLaboratoryAffiliationService.save(oldAffiliation);

			// Create entity origin for deleted entity
			DocumentVersionEntityOrigin entityOriginDelete = DocumentVersionEntityOrigin.Deleted(oldAffiliation.getDocumentVersion(), originalPerson.getId(), EntityType.PERSON, origin, confidenceLevel);
			entityOriginService.save(entityOriginDelete);

			// Create new affiliation (with or without new person)
			PersonLaboratoryAffiliation newAffiliation = new PersonLaboratoryAffiliation();
			newAffiliation.setDocumentVersion(oldAffiliation.getDocumentVersion());
			newAffiliation.setLaboratory(oldAffiliation.getLaboratory());
			newAffiliation.setPerson(newPerson);
			this.personLaboratoryAffiliationService.save(newAffiliation);

			if (newPerson != null) {
				// New 'found' entity origin
				DocumentVersionEntityOrigin entityOriginFound = DocumentVersionEntityOrigin.Found(newAffiliation.getDocumentVersion(), newPerson.getId(), EntityType.PERSON, origin, confidenceLevel);
				entityOriginService.save(entityOriginFound);
			}
		}
	}

	public PersonService getPersonService() {
		return personService;
	}
}
