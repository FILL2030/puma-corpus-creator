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

import eu.ill.puma.persistence.domain.document.Person;
import eu.ill.puma.persistence.repository.document.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PersonService {

	private static final Logger log = LoggerFactory.getLogger(PersonService.class);

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private PersonLaboratoryAffiliationService personLaboratoryAffiliationService;

	/**
	 * Returns a Person based on their Id
	 *
	 * @param id The Id of the Person
	 * @return The Person corresponding to the Id
	 */
	public Person getById(Long id) {
		return this.personRepository.getById(id);
	}

	/**
	 * Persists the given Person and ensures we do not create duplicates.
	 * - if the id is set we assume we have an already persisted object and the object is updated
	 * - If the Person data match an existing Person then we know it is already persisted
	 *
	 * @param person The Person to be persisted
	 * @return The persisted Person
	 */
	public synchronized Person save(Person person) {
		Person integratedPerson = null;

		// Check if it is a new object
		if (person.getId() == null) {
			// Determine if the object already exists
			integratedPerson = this.personRepository.getByPersonDetails(person.getFirstName(), person.getLastName(), person.getPublicationName(), person.getOrcidId(), person.getResearcherId(), person.getOriginId(), person.getEmail());
			if (integratedPerson != null) {
				log.debug("person " + person.getLastName() + " already present in the db under the id " + integratedPerson.getId());

			} else {
				integratedPerson = this.personRepository.persist(person);
			}

		} else {
			// merge
			integratedPerson = this.personRepository.merge(person);
		}

		return integratedPerson;
	}

	/**
	 * Soft deletes a person
	 *
	 * @param person The person to delete
	 */
	public void delete(Person person) {
		person.setObsolete(true);
		this.save(person);
	}

	/**
	 * Return all persons
	 *
	 * @return A list of all persons
	 */
	public List<Person> getAll() {
		return this.personRepository.getAll();
	}

	/**
	 * Returns the count of all persons
	 */
	public long count() {
		return this.personRepository.count();
	}
}
