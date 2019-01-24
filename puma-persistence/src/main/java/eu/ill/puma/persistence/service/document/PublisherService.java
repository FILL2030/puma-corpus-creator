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

import eu.ill.puma.persistence.domain.document.Publisher;
import eu.ill.puma.persistence.repository.document.PublisherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PublisherService {

	private static final Logger log = LoggerFactory.getLogger(PublisherService.class);

	@Autowired
	private PublisherRepository publisherRepository;

	@Autowired
	private JournalPublisherAffiliationService journalPublisherAffiliationService;

	/**
	 * Return a Publisher given the Id
	 * @param id The Id of the Publisher
	 * @return The publisher with the given Id
	 */
	public Publisher getById(Long id) {
		return this.publisherRepository.getById(id);
	}

	/**
	 * Return all publishers
	 * @return a List of all Publishers
	 */
	public List<Publisher> getAll() {
		return this.publisherRepository.getAll();
	}

	/**
	 * Persists the given Publisher and ensures we do not create duplicates.
	 *  - if the id is set we assume we have an already persisted object and the object is updated
	 *  - If the Publisher data match an existing Publisher then we know it is already persisted
	 * @param publisher The Publisher to be persisted
	 * @return The persisted Publisher
	 */
	public synchronized Publisher save(Publisher publisher) {

		// Check if it is a new object
		Publisher integratedPublisher = null;
		if (publisher.getId() == null) {
			// Determine if the object already exists
			integratedPublisher = this.publisherRepository.getByPublisherDetails(publisher.getName(), publisher.getCity(), publisher.getAddress());
			if (integratedPublisher != null) {
				log.debug("publisher " + publisher.getName() + " already present in the db under the id " + integratedPublisher.getId());

			} else {
				integratedPublisher = this.publisherRepository.persist(publisher);
			}

		} else {
			// merge
			integratedPublisher = this.publisherRepository.merge(publisher);
		}

		return integratedPublisher;
	}
}
