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

import eu.ill.puma.persistence.PumaTest;
import eu.ill.puma.persistence.domain.document.*;
import eu.ill.puma.persistence.domain.document.enumeration.DocumentType;
import eu.ill.puma.persistence.service.document.PersonLaboratoryAffiliationService;
import eu.ill.puma.persistence.service.document.DocumentVersionService;
import eu.ill.puma.persistence.service.document.LaboratoryService;
import eu.ill.puma.persistence.service.document.PersonService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class PersonLaboratoryAffiliationServiceTest extends PumaTest {

	@Autowired
	private PersonLaboratoryAffiliationService personLaboratoryAffiliationService;

	@Autowired
	private DocumentVersionService documentVersionService;

	@Autowired
	private PersonService personService;

	@Autowired
	private LaboratoryService laboratoryService;


	public DocumentVersion createAndSaveDocumentVersion(String sourceId) throws DocumentVersionService.DocumentVersionPersistenceException {
		Document document = new Document();
		document.setDocumentType(DocumentType.PUBLICATION);

		DocumentVersion documentVersion = new DocumentVersion();
		documentVersion.setDocument(document);

		DocumentVersionSource documentVersionSource = new DocumentVersionSource();
		documentVersionSource.setImporterShortName("TEST");
		documentVersionSource.setSourceId(sourceId);
		documentVersionSource.setImportDate(new Date());
		documentVersionSource.setDocumentVersion(documentVersion);

		documentVersion.addSource(documentVersionSource);

		documentVersionService.save(documentVersion);

		return documentVersion;
	}

	public Person createAndSavePerson(String firstName, String lastName) {
		Person person = new Person();
		person.setFirstName(firstName);
		person.setLastName(lastName);

		person = personService.save(person);

		return person;
	}

	public Laboratory createAndSaveLaboratory(String name) {
		Laboratory laboratory = new Laboratory();
		laboratory.setName(name);
		laboratory.setAddress("abcdef");

		laboratory = laboratoryService.save(laboratory);

		return laboratory;
	}


	public PersonLaboratoryAffiliation createDocumentWithAffiliation(String sourceId, String personFirstName, String personLastName, String laboName) throws Exception {
		DocumentVersion documentVersion = createAndSaveDocumentVersion(sourceId);
		Person person = createAndSavePerson(personFirstName, personLastName);
		Laboratory laboratory = createAndSaveLaboratory(laboName);

		// Create affiliation
		PersonLaboratoryAffiliation personLaboratoryAffiliation = new PersonLaboratoryAffiliation();
		personLaboratoryAffiliation.setPerson(person);
		personLaboratoryAffiliation.setLaboratory(laboratory);
		personLaboratoryAffiliation.setDocumentVersion(documentVersion);

		personLaboratoryAffiliation = personLaboratoryAffiliationService.save(personLaboratoryAffiliation);
		documentVersion.addPersonLaboratoryAffiliation(personLaboratoryAffiliation);

		// Save document version again
		documentVersionService.save(documentVersion);

		return personLaboratoryAffiliation;
	}

	@Test
	public void testCreationAndRetrieval() throws Exception {
		PersonLaboratoryAffiliation personLaboratoryAffiliation = this.createDocumentWithAffiliation("source1", "first", "last", "laboName");

		DocumentVersion documentVersion = personLaboratoryAffiliation.getDocumentVersion();
		Person person = personLaboratoryAffiliation.getPerson();
		Laboratory laboratory = personLaboratoryAffiliation.getLaboratory();

		Assert.assertNotNull(documentVersion.getId());
		Assert.assertNotNull(personLaboratoryAffiliation.getId());

		// Retrieve
		PersonLaboratoryAffiliation integratedPersonLaboratoryAffiliation1 = personLaboratoryAffiliationService.getAllForDocumentVersion(documentVersion).get(0);
		Assert.assertEquals(person.getId(), integratedPersonLaboratoryAffiliation1.getPerson().getId());
		Assert.assertEquals(person, integratedPersonLaboratoryAffiliation1.getPerson());

		PersonLaboratoryAffiliation integratedPersonLaboratoryAffiliation2 = personLaboratoryAffiliationService.getAllForPerson(person).get(0);
		Assert.assertEquals(laboratory.getId(), integratedPersonLaboratoryAffiliation2.getLaboratory().getId());
		Assert.assertEquals(laboratory, integratedPersonLaboratoryAffiliation2.getLaboratory());

		PersonLaboratoryAffiliation integratedPersonLaboratoryAffiliation3 = personLaboratoryAffiliationService.getAllForLaboratory(laboratory).get(0);
		Assert.assertEquals(person.getId(), integratedPersonLaboratoryAffiliation3.getPerson().getId());
		Assert.assertEquals(person, integratedPersonLaboratoryAffiliation3.getPerson());
	}

	@Test
	public void testMultipleDocumentVersions() throws Exception {
		PersonLaboratoryAffiliation personLaboratoryAffiliation1 = this.createDocumentWithAffiliation("source1", "first", "last", "laboName");
		PersonLaboratoryAffiliation personLaboratoryAffiliation2 = this.createDocumentWithAffiliation("source2", "first", "last", "laboName2");

		List<PersonLaboratoryAffiliation> affiliationsForDocumentVersion1 = this.personLaboratoryAffiliationService.getAllForDocumentVersion(personLaboratoryAffiliation1.getDocumentVersion());
		Assert.assertEquals(1, affiliationsForDocumentVersion1.size());
		Assert.assertEquals(personLaboratoryAffiliation1.getPerson(), affiliationsForDocumentVersion1.get(0).getPerson());
		Assert.assertEquals(personLaboratoryAffiliation2.getPerson(), affiliationsForDocumentVersion1.get(0).getPerson());
		Assert.assertEquals(personLaboratoryAffiliation1.getLaboratory(), affiliationsForDocumentVersion1.get(0).getLaboratory());

		List<PersonLaboratoryAffiliation> affiliationsForDocumentVersion2 = this.personLaboratoryAffiliationService.getAllForDocumentVersion(personLaboratoryAffiliation2.getDocumentVersion());
		Assert.assertEquals(1, affiliationsForDocumentVersion2.size());
		Assert.assertEquals(personLaboratoryAffiliation1.getPerson(), affiliationsForDocumentVersion2.get(0).getPerson());
		Assert.assertEquals(personLaboratoryAffiliation2.getPerson(), affiliationsForDocumentVersion2.get(0).getPerson());
		Assert.assertEquals(personLaboratoryAffiliation2.getLaboratory(), affiliationsForDocumentVersion2.get(0).getLaboratory());

		List<PersonLaboratoryAffiliation> affiliationsForPerson = this.personLaboratoryAffiliationService.getAllForPerson(personLaboratoryAffiliation1.getPerson());
		Assert.assertEquals(2, affiliationsForPerson.size());

		List<PersonLaboratoryAffiliation> affiliationsForLaboratory = this.personLaboratoryAffiliationService.getAllForLaboratory(personLaboratoryAffiliation1.getLaboratory());
		Assert.assertEquals(1, affiliationsForLaboratory.size());
	}

}
