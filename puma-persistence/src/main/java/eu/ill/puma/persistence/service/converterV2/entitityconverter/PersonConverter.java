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
package eu.ill.puma.persistence.service.converterV2.entitityconverter;

import eu.ill.puma.core.domain.document.entities.BasePerson;
import eu.ill.puma.persistence.domain.document.Person;

public class PersonConverter {

	public static Person convert(BasePerson importerPerson) {
		Person person = new Person();

		if (importerPerson.getLastName() == null && importerPerson.getPublicationName() == null && importerPerson.getOrcidId() == null && importerPerson.getResearcherId() == null) {
			return null;
		}

		person.setFirstName(importerPerson.getFirstName());
		person.setLastName(importerPerson.getLastName());
		person.setPublicationName(importerPerson.getPublicationName());
		person.setOrcidId(importerPerson.getOrcidId());
		person.setResearcherId(importerPerson.getResearcherId());
		person.setEmail(importerPerson.getEmail());
		person.setOriginId(importerPerson.getOriginId());

//		if (importerPerson.getPumaId() != null) {
//			person.setId(Math.abs(importerPerson.getPumaId()));
//		}

		return person;
	}
}
