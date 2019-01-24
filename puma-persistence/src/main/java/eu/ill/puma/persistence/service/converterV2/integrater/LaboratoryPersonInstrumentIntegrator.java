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
package eu.ill.puma.persistence.service.converterV2.integrater;

import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.core.domain.document.entities.BaseInstrument;
import eu.ill.puma.core.domain.document.entities.BaseLaboratory;
import eu.ill.puma.core.domain.document.entities.BasePerson;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import eu.ill.puma.persistence.domain.document.*;
import eu.ill.puma.persistence.domain.document.enumeration.PersonRole;
import eu.ill.puma.persistence.service.converterV2.EntityOriginStore;
import eu.ill.puma.persistence.service.converterV2.entitityconverter.InstrumentConverter;
import eu.ill.puma.persistence.service.converterV2.entitityconverter.LaboratoryConverter;
import eu.ill.puma.persistence.service.converterV2.entitityconverter.PersonConverter;
import eu.ill.puma.persistence.service.document.InstrumentService;
import eu.ill.puma.persistence.service.document.LaboratoryService;
import eu.ill.puma.persistence.service.document.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LaboratoryPersonInstrumentIntegrator {

	@Autowired
	private PersonService personService;

	@Autowired
	private LaboratoryService laboratoryService;

	@Autowired
	private InstrumentService instrumentService;

	public void convert(DocumentVersion documentVersion, BaseDocument baseDocument, EntityOriginStore entityOriginStore) {

		/**
		 * base object referenced by other object, indexed
		 */
		Map<Long, BaseLaboratory> baselaboratories = baseDocument.getLaboratories().stream()
				.collect(Collectors.toMap(BaseLaboratory::getId, baseLaboratory -> baseLaboratory));

		/**
		 * base object which reference other object
		 */
		Set<BasePerson> basePersons = new HashSet(baseDocument.getPersons());

		/**
		 * initiate laboratory cache
		 */
		Map<BaseLaboratory, Laboratory> laboratoryCacheMap = new HashMap();
		for (BaseLaboratory baseLaboratory : baseDocument.getLaboratories()) {
			laboratoryCacheMap.put(baseLaboratory, null);
		}

		/**
		 * person laboratory affiliation
		 */
		for (BasePerson basePerson : basePersons) {
			//delete basePerson (update & delete operation)
			if (basePerson.getPumaId() != null) {

				//get affiliation to delete
				List<PersonLaboratoryAffiliation> personLaboratoryAffiliations = new ArrayList();
				Long personToDeleteId = Math.abs(basePerson.getPumaId());

				if (basePerson.getLaboratoryId() == null) {
					personLaboratoryAffiliations = documentVersion.getPersonLaboratoryAffiliationsByPersonId(personToDeleteId);
				} else {
					BaseLaboratory baseLaboratoryToDelete = baselaboratories.get(basePerson.getLaboratoryId());

					//find the laboratory to delete in the db
					Long laboratoryToDeletePumaId = Math.abs(baseLaboratoryToDelete.getPumaId());

					//if laboratory exist, look for the affiliation to delete
					if (laboratoryToDeletePumaId != null) {
						Optional<PersonLaboratoryAffiliation> optionalLaboratory = documentVersion.getPersonLaboratoryAffiliationByPersonIdAndLaboratoryId(personToDeleteId, laboratoryToDeletePumaId);

						if (optionalLaboratory.isPresent()) {
							personLaboratoryAffiliations.add(optionalLaboratory.get());
						}
					}
				}

				//delete affiliation
				for (PersonLaboratoryAffiliation affiliation : personLaboratoryAffiliations) {
					affiliation.setObsolete(true);

					//delete laboratory
					if (affiliation.getLaboratory() != null && affiliation.getLaboratory().getId() != null) {
						BaseLaboratory baseLaboratory = baselaboratories.get(basePerson.getLaboratoryId());
						entityOriginStore.deleteEntity(affiliation.getLaboratory().getId(), EntityType.LABORATORY, baseLaboratory.getConfidence());
					}
				}

				//delete person
				entityOriginStore.deleteEntity(personToDeleteId, EntityType.PERSON, basePerson.getConfidence());

			}

			//create basePerson (add & update operation)
			if (basePerson.getPumaId() == null || basePerson.getPumaId() >= 0) {
				//person
				Person personToAdd = PersonConverter.convert(basePerson);
				personToAdd = personService.save(personToAdd);


				//affiliation
				PersonLaboratoryAffiliation affiliation = new PersonLaboratoryAffiliation();
				affiliation.setDocumentVersion(documentVersion);
				affiliation.setPerson(personToAdd);

				//laboratory
				BaseLaboratory baseLaboratory = baselaboratories.get(basePerson.getLaboratoryId());
				Laboratory laboratoryToAdd = null;
				if (baseLaboratory != null && (baseLaboratory.getPumaId() == null || baseLaboratory.getPumaId() >= 0)) {
					//getLab
					laboratoryToAdd = laboratoryCacheMap.get(baseLaboratory);

					if (laboratoryToAdd == null) {
						laboratoryToAdd = LaboratoryConverter.convert(baselaboratories.get(basePerson.getLaboratoryId()));
						laboratoryCacheMap.put(baseLaboratory, laboratoryToAdd);
					}

					laboratoryToAdd = this.laboratoryService.save(laboratoryToAdd);
					affiliation.setLaboratory(laboratoryToAdd);
				}

				//person roles
				basePerson.getRoles().forEach(role -> {
					PersonRole personRole = PersonRole.valueOf(role.toString());
					affiliation.addRole(personRole);
				});

				//set affiliation
				if (!documentVersion.getPersonLaboratoryAffiliations().contains(affiliation)) {
					documentVersion.addPersonLaboratoryAffiliation(affiliation);

					entityOriginStore.foundEntity(personToAdd.getId(), EntityType.PERSON, basePerson.getConfidence());
					if (laboratoryToAdd != null) {
						entityOriginStore.foundEntity(laboratoryToAdd.getId(), EntityType.LABORATORY, baseLaboratory.getConfidence());
					}
				}

			}
		}


		/**
		 * Instrument
		 */
		for (BaseInstrument baseInstrument : baseDocument.getInstruments()) {

			//delete instrument (update & delete operation)
			if (baseInstrument.getPumaId() != null) {
				Long instrumentId = Math.abs(baseInstrument.getPumaId());

				Optional<Instrument> optionalInstrumentToDelete = documentVersion.getInstrumentById(instrumentId);

				if (optionalInstrumentToDelete.isPresent()) {
					documentVersion.getInstruments().remove(optionalInstrumentToDelete.get());

					//create entity origin
					entityOriginStore.deleteEntity(optionalInstrumentToDelete.get().getId(), EntityType.INSTRUMENT, baseInstrument.getConfidence());
				}
			}

			//create instrument (add & update operation)
			if (baseInstrument.getPumaId() == null || baseInstrument.getPumaId() >= 0) {

				//laboratory
				BaseLaboratory baseLaboratory = baselaboratories.get(baseInstrument.getLaboratoryId());
				Laboratory laboratoryToAdd = null;
				if (baseLaboratory != null && (baseLaboratory.getPumaId() == null || baseLaboratory.getPumaId() >= 0)) {
					//getLab
					laboratoryToAdd = laboratoryCacheMap.get(baseLaboratory);

					if (laboratoryToAdd == null) {
						laboratoryToAdd = LaboratoryConverter.convert(baseLaboratory);
						laboratoryCacheMap.put(baseLaboratory, laboratoryToAdd);
					}

					laboratoryToAdd = this.laboratoryService.save(laboratoryToAdd);
				}

				//new instrument
				Instrument instrumentToAdd = InstrumentConverter.convert(baseInstrument);
				instrumentToAdd.setLaboratory(laboratoryToAdd);
				instrumentToAdd = instrumentService.save(instrumentToAdd);

				if (!documentVersion.getInstruments().contains(instrumentToAdd)) {
					documentVersion.addInstrument(instrumentToAdd);

					//create entity origin
					entityOriginStore.foundEntity(instrumentToAdd.getId(), EntityType.INSTRUMENT, baseInstrument.getConfidence());

					if (laboratoryToAdd != null) {
						entityOriginStore.foundEntity(laboratoryToAdd.getId(), EntityType.LABORATORY, baseLaboratory.getConfidence());
					}
				}
			}
		}

		/**
		 * orphan laboratory
		 */
		for (Map.Entry<BaseLaboratory, Laboratory> entry : laboratoryCacheMap.entrySet()) {
			BaseLaboratory baseLaboratory = entry.getKey();
			Laboratory laboratory = entry.getValue();


			//labo to delete alone (delete operation)
			if (laboratory == null && baseLaboratory.getPumaId() != null) {

				Long laboratoryToDeleteId = Math.abs(baseLaboratory.getPumaId());

				//delete person laboratory affiliation
				for (PersonLaboratoryAffiliation affiliation : documentVersion.getPersonLaboratoryAffiliationsByLaboratoryId(laboratoryToDeleteId)) {
					affiliation.setObsolete(true);

					//delete person
					if (affiliation.getPerson() != null && affiliation.getPerson().getId() != null) {
						entityOriginStore.deleteEntity(affiliation.getPerson().getId(), EntityType.PERSON, baseLaboratory.getConfidence());
					}
				}

				//delete laboratory
				entityOriginStore.deleteEntity(laboratoryToDeleteId, EntityType.LABORATORY, baseLaboratory.getConfidence());
			}

			//labo to add alone (add && update operation)
			if (laboratory == null && (baseLaboratory.getPumaId() == null || baseLaboratory.getPumaId() >= 0)) {

				Laboratory laboratoryToAdd = laboratoryCacheMap.get(baseLaboratory);

				if (laboratoryToAdd == null) {
					laboratoryToAdd = LaboratoryConverter.convert(baseLaboratory);
					laboratoryCacheMap.put(baseLaboratory, laboratoryToAdd);
				}

				laboratoryToAdd = this.laboratoryService.save(laboratoryToAdd);

				PersonLaboratoryAffiliation affiliation = new PersonLaboratoryAffiliation();
				affiliation.setLaboratory(laboratoryToAdd);
				affiliation.setDocumentVersion(documentVersion);

				if (!documentVersion.getPersonLaboratoryAffiliations().contains(affiliation)) {
					documentVersion.addPersonLaboratoryAffiliation(affiliation);

					entityOriginStore.foundEntity(laboratoryToAdd.getId(), EntityType.LABORATORY, baseLaboratory.getConfidence());
				}
			}

		}
	}


}
