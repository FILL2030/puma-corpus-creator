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
import eu.ill.puma.persistence.domain.document.Instrument;
import eu.ill.puma.persistence.domain.document.Laboratory;
import eu.ill.puma.persistence.domain.document.PersonLaboratoryAffiliation;
import eu.ill.puma.persistence.service.analysis.DocumentVersionEntityOriginService;
import eu.ill.puma.persistence.service.document.InstrumentService;
import eu.ill.puma.persistence.service.document.LaboratoryService;
import eu.ill.puma.persistence.service.document.PersonLaboratoryAffiliationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LaboratoryDeduplicationService {

	private static final Logger log = LoggerFactory.getLogger(LaboratoryDeduplicationService.class);

	@Autowired
	private LaboratoryService laboratoryService;

	@Autowired
	private InstrumentService instrumentService;

	@Autowired
	private PersonLaboratoryAffiliationService personLaboratoryAffiliationService;

	@Autowired
	private DocumentVersionEntityOriginService entityOriginService;

	public Laboratory updateLaboratory(Laboratory laboratory, String origin, ConfidenceLevel confidenceLevel) {
		log.debug("Updating laboratory " + laboratory);

		// Get original laboratory
		Laboratory originalLaboratory = this.laboratoryService.getById(laboratory.getId());

		// Mark laboratory with Id as obsolete
		originalLaboratory.setObsolete(true);
		this.laboratoryService.save(originalLaboratory);

		// create a new laboratory
		laboratory.setId(null);
		Laboratory newLaboratory = this.laboratoryService.save(laboratory);

		// update all affiliations
		this.updateAffiliations(originalLaboratory, newLaboratory, origin, confidenceLevel);

		return newLaboratory;
	}

	@Transactional
	public void deleteLaboratory(Laboratory laboratory, String origin, ConfidenceLevel confidenceLevel) {
		log.debug("Deleting laboratory " + laboratory);

		// Mark laboratory with Id as obsolete
		laboratory.setObsolete(true);
		this.laboratoryService.save(laboratory);

		// update all affiliations
		this.updateAffiliations(laboratory, null, origin, confidenceLevel);
	}

	public void replace(Laboratory laboratory, Laboratory laboratoryToKeep, String origin, ConfidenceLevel confidenceLevel) {
		// do nothing if they are the same laboratories
		if (laboratory.getId().equals(laboratoryToKeep.getId())) {
			return;
		}

		log.debug("Replacing laboratory " + laboratory + " with laboratory " + laboratoryToKeep);

		// Mark the laboratory as obsolete
		laboratory.setObsolete(true);
		this.laboratoryService.save(laboratory);

		// Update all affiliations
		this.updateAffiliations(laboratory, laboratoryToKeep, origin, confidenceLevel);
	}


	private void updateAffiliations(Laboratory originalLaboratory, Laboratory newLaboratory, String origin, ConfidenceLevel confidenceLevel) {
		// Person laboratory affiliations
		List<PersonLaboratoryAffiliation> affiliations = this.personLaboratoryAffiliationService.getAllForLaboratory(originalLaboratory);
		for (PersonLaboratoryAffiliation oldAffiliation : affiliations) {

			// Set old affiliation as obsolete
			oldAffiliation.setObsolete(true);
			this.personLaboratoryAffiliationService.save(oldAffiliation);

			// Create entity origin for deleted entity
			DocumentVersionEntityOrigin entityOriginDelete = DocumentVersionEntityOrigin.Deleted(oldAffiliation.getDocumentVersion(), originalLaboratory.getId(), EntityType.LABORATORY, origin, confidenceLevel);
			entityOriginService.save(entityOriginDelete);

			// Create new affiliation (with or without new laboratory)
			PersonLaboratoryAffiliation newAffiliation = new PersonLaboratoryAffiliation();
			newAffiliation.setDocumentVersion(oldAffiliation.getDocumentVersion());
			newAffiliation.setPerson(oldAffiliation.getPerson());
			newAffiliation.setLaboratory(newLaboratory);
			this.personLaboratoryAffiliationService.save(newAffiliation);

			if (newLaboratory != null) {
				// New 'found' entity origin
				DocumentVersionEntityOrigin entityOriginFound = DocumentVersionEntityOrigin.Found(newAffiliation.getDocumentVersion(), newLaboratory.getId(), EntityType.LABORATORY, origin, confidenceLevel);
				entityOriginService.save(entityOriginFound);
			}
		}

		// Update instruments
		List<Instrument> instruments = this.instrumentService.getAllForLaboratory(originalLaboratory);
		for (Instrument instrument : instruments) {
			instrument.setLaboratory(newLaboratory);
			this.instrumentService.save(instrument);
		}
	}

	public LaboratoryService getLaboratoryService() {
		return laboratoryService;
	}
}
