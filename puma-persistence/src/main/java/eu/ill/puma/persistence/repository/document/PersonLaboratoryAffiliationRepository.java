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
package eu.ill.puma.persistence.repository.document;

import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.Laboratory;
import eu.ill.puma.persistence.domain.document.Person;
import eu.ill.puma.persistence.domain.document.PersonLaboratoryAffiliation;
import eu.ill.puma.persistence.repository.PumaDocumentEntityRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PersonLaboratoryAffiliationRepository extends PumaDocumentEntityRepository<PersonLaboratoryAffiliation> {

	public List<PersonLaboratoryAffiliation> getAllForPerson(Person person) {
		return this.getEntities("person", person);
	}

	public List<PersonLaboratoryAffiliation> getAllForLaboratory(Laboratory laboratory) {
		return this.getEntities("laboratory", laboratory);
	}

	public Long getCountForLaboratory(Laboratory laboratory){
		String queryString = "select count(pla) from  PersonLaboratoryAffiliation pla where pla.laboratory = :laboratory";

		TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);

		query.setParameter("laboratory", laboratory);
		query.setMaxResults(1);

		return query.getSingleResult();
	}


	public List<PersonLaboratoryAffiliation> getAllForDocumentVersion(DocumentVersion documentVersion) {
		return this.getEntities("documentVersion", documentVersion);
	}

	public List<PersonLaboratoryAffiliation> getAllForDocumentVersionAndPerson(DocumentVersion documentVersion, Person person) {
		List<String> parameters = new ArrayList();
		parameters.add("documentVersion");
		parameters.add("person");
		return this.getEntities(parameters, documentVersion, person);
	}

	public List<PersonLaboratoryAffiliation> getAllForDocumentVersionAndLaboratory(DocumentVersion documentVersion, Laboratory laboratory) {
		List<String> parameters = new ArrayList();
		parameters.add("documentVersion");
		parameters.add("laboratory");
		return this.getEntities(parameters, documentVersion, laboratory);
	}

	public List<PersonLaboratoryAffiliation> getAllForDocumentVersionAndPersonAndLaboratory(DocumentVersion documentVersion, Person person, Laboratory laboratory) {
		List<String> parameters = new ArrayList();
		parameters.add("documentVersion");
		parameters.add("person");
		parameters.add("laboratory");
		return this.getEntities(parameters, documentVersion, person, laboratory);
	}
}
