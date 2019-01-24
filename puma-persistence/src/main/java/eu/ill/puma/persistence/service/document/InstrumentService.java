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

import eu.ill.puma.persistence.domain.document.Instrument;
import eu.ill.puma.persistence.domain.document.Laboratory;
import eu.ill.puma.persistence.repository.document.InstrumentRepository;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InstrumentService {

	private static final Logger log = LoggerFactory.getLogger(KeywordService.class);

	@Autowired
	private InstrumentRepository instrumentRepository;

	@Autowired
	private LaboratoryService laboratoryService;

	/**
	 * Returns an Instrument given the Id
	 *
	 * @param id The id of the instrument
	 * @return The Instrument with the given Id
	 */
	public Instrument getById(Long id) {
		return this.instrumentRepository.getById(id);
	}

	/**
	 * Returns the required Instrument with the laboratory instantiated
	 *
	 * @param id The id of the instrument
	 * @return The Instrument with the given Id
	 */
	public Instrument getByIdCompleted(Long id) {
		Instrument instrument = this.instrumentRepository.getById(id);
		Hibernate.initialize(instrument.getLaboratory());

		return instrument;
	}

	/**
	 * Persists the instrument
	 */
	public synchronized Instrument save(Instrument instrument) {
		Instrument integratedInstrument = null;

		//if it's a new object with laboratory
		if (instrument.getId() == null && instrument.getLaboratory() != null) {

			//save laboratory if required
			if (instrument.getLaboratory().getId() == null) {
				instrument.setLaboratory(laboratoryService.save(instrument.getLaboratory()));
			}

			// Check if it's a new object
			integratedInstrument = this.instrumentRepository.getByInstrumentDetails(instrument.getCode(), instrument.getLaboratory());
			if (integratedInstrument != null) {
				log.debug("keyword " + instrument.getName() + " already present in the db under the id " + integratedInstrument.getId());

			} else {
				integratedInstrument = this.instrumentRepository.persist(instrument);
			}
		} else if (instrument.getId() == null && instrument.getLaboratory() == null) {
			integratedInstrument = this.instrumentRepository.persist(instrument);
		} else {
			// merge
			integratedInstrument = this.instrumentRepository.merge(instrument);
		}

		return integratedInstrument;
	}


	/**
	 * Return all instruments
	 *
	 * @return A list of all instruments
	 */
	public List<Instrument> getAll() {
		return this.instrumentRepository.getAll();
	}

	/**
	 * Returns all instruments for a given laboratory
	 *
	 * @param laboratory the laboratory
	 * @return a set of instruments
	 */
	public List<Instrument> getAllForLaboratory(Laboratory laboratory) {
		return this.instrumentRepository.getAllForLaboratory(laboratory);
	}

	public Long getCountForLaboratory(Laboratory laboratory) {
		return this.instrumentRepository.getCountForLaboratory(laboratory);
	}
}
