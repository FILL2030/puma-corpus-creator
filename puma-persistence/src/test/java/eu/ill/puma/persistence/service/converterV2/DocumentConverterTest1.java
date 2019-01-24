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
package eu.ill.puma.persistence.service.converterV2;

import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionEntityOrigin;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import eu.ill.puma.persistence.domain.document.*;
import eu.ill.puma.persistence.domain.importer.Importer;
import eu.ill.puma.persistence.service.analysis.DocumentVersionEntityOriginService;
import eu.ill.puma.persistence.service.document.*;
import eu.ill.puma.persistence.service.importer.ImporterService;
import eu.ill.puma.persistence.utils.ResourceLoader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import java.util.List;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( locations={
		"classpath:/applicationContext-test.xml"
})
@TestExecutionListeners( {
		DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DocumentConverterTest1 {


	@Autowired
	protected ImporterService importerService;

	@Autowired
	protected JournalService journalService;

	@Autowired
	protected PublisherService publisherService;

	@Autowired
	protected PersonService personService;

	@Autowired
	protected LaboratoryService laboratoryService;

	@Autowired
	protected FormulaService formulaService;

	@Autowired
	protected InstrumentService instrumentService;

	@Autowired
	protected ResearchDomainService researchDomainService;

	@Autowired
	protected KeywordService keywordService;

	@Autowired
	protected DocumentService documentService;

	@Autowired
	protected DocumentVersionService documentVersionService;

	@Autowired
	protected DocumentConverter documentConverter;

	@Autowired
	protected DocumentVersionEntityOriginService entityOriginService;

	@Autowired
	protected PersonLaboratoryAffiliationService personLaboratoryAffiliationService;

	@Autowired
	protected JournalPublisherAffiliationService journalPublisherAffiliationService;

	public Importer createImporter() {
		// Create importer
		String importerName = "Test Importer";
		String shortName = "TEST";
		String importerUrl = "http://this.is.a.test/";

		Importer importer = new Importer();
		importer.setName(importerName);
		importer.setShortName(shortName);
		importer.setUrl(importerUrl);
		importerService.save(importer);

		return importerService.getById(importer.getId());
	}

	@Test
	public void verifyNoDuplicateImports() throws Exception {
		Importer importer = this.createImporter();

		BaseDocument importerDocument1 = ResourceLoader.readType("ImporterDocument1.json", BaseDocument.class);
		BaseDocument importerDocument2 = ResourceLoader.readType("ImporterDocument1.json", BaseDocument.class);

		// Persist and link entities
		DocumentVersion documentVersion1 = documentConverter.convert(importerDocument1, importer.getShortName());
		DocumentVersion documentVersion2 = documentConverter.convert(importerDocument2, importer.getShortName());

		Assert.assertEquals(1, journalService.getAll().size());
		Assert.assertEquals(1, publisherService.getAll().size());
		Assert.assertEquals(4, laboratoryService.getAll().size());
		Assert.assertEquals(3, personService.getAll().size());
		Assert.assertEquals(2, instrumentService.getAll().size());
		Assert.assertEquals(1, formulaService.getAll().size());
		Assert.assertEquals(3, keywordService.getAll().size());
		Assert.assertEquals(4, researchDomainService.getAll().size());
		Assert.assertEquals(1, documentVersionService.getAll().size());
		Assert.assertEquals(1, documentService.getAll().size());
		Assert.assertNull(documentVersion2);
	}

	@Test
	public void verifyEntitiesNotRemoved() throws Exception {
		Importer importer = this.createImporter();

		BaseDocument importerDocument1 = ResourceLoader.readType("ImporterDocument1.json", BaseDocument.class);
		BaseDocument importerDocument2 = ResourceLoader.readType("ImporterDocument2.json", BaseDocument.class);

		// Persist and link entities
		DocumentVersion documentVersion1 = documentConverter.convert(importerDocument1, importer.getShortName());
		DocumentVersion documentVersion2 = documentConverter.convert(importerDocument2, importer.getShortName());

		Assert.assertEquals(2, journalService.getAll().size());
		Assert.assertEquals(1, publisherService.getAll().size());
		Assert.assertEquals(4, laboratoryService.getAll().size());
		Assert.assertEquals(5, personService.getAll().size());
		Assert.assertEquals(3, instrumentService.getAll().size());
		Assert.assertEquals(2, formulaService.getAll().size());
		Assert.assertEquals(6, keywordService.getAll().size());
		Assert.assertEquals(7, researchDomainService.getAll().size());
		Assert.assertEquals(2, documentVersionService.getAll().size());
		Assert.assertEquals(2, documentService.getAll().size());
		//Assert.assertNotEquals(documentVersion1.getShortHash(), documentVersion2.getShortHash());

		int personCounter = 0;
		int instrumentCounter = 0;
		List<Laboratory> allLaboratories = laboratoryService.getAll();
		for (Laboratory laboratory : allLaboratories) {
			Laboratory completeLaboratory = laboratoryService.getByIdCompleted(laboratory.getId());
			instrumentCounter = instrumentCounter + completeLaboratory.getInstruments().size();
		}

		// Ensure people are not removed from laboratories
//		Assert.assertEquals(5, personCounter);
//
//		// Ensure instruments are not removed from laboratories
//		Assert.assertEquals(3, instrumentCounter);
	}

	@Test
	public void verifyEntityOrigins() throws Exception {
		Importer importer = this.createImporter();

		BaseDocument importerDocument1 = ResourceLoader.readType("ImporterDocument1.json", BaseDocument.class);
		BaseDocument importerDocument2 = ResourceLoader.readType("ImporterDocument2.json", BaseDocument.class);

		// Persist and link entities
		DocumentVersion documentVersion1 = documentConverter.convert(importerDocument1, importer.getShortName());
		DocumentVersion documentVersion2 = documentConverter.convert(importerDocument2, importer.getShortName());

		this.verifyEntityOriginsForDocument(documentVersion1, 4, 3, 2);
		this.verifyEntityOriginsForDocument(documentVersion2, 3, 3, 1);
	}

	private void verifyEntityOriginsForDocument(DocumentVersion documentVersion, int expectedLaboCount, int expectedPersonCount, int expectedInstCount) {

		List<PersonLaboratoryAffiliation> personLaboratoryAffiliations = this.personLaboratoryAffiliationService.getAllForDocumentVersion(documentVersion);
		List<Instrument> instruments = documentVersion.getInstruments();
		List<JournalPublisherAffiliation> journalPublisherAffiliations = this.journalPublisherAffiliationService.getAllForDocumentVersion(documentVersion);
		List<Keyword> keywords = documentVersion.getKeywords();
		List<Formula> formulas = documentVersion.getFormulas();
		List<ResearchDomain> researchDomains = documentVersion.getResearchDomains();

		int personCount = 0;
		int laboCount = 0;
		int instCount = 0;

		List<DocumentVersionEntityOrigin> entityOrigins = this.entityOriginService.getAllForDocumentVersion(documentVersion);
		for (DocumentVersionEntityOrigin entityOrigin : entityOrigins) {
			if (entityOrigin.getEntityType().equals(EntityType.LABORATORY)) {
				laboCount++;
				boolean found = false;
				for (PersonLaboratoryAffiliation personLaboratoryAffiliation : personLaboratoryAffiliations) {
					if (personLaboratoryAffiliation.getLaboratory().getId().equals(entityOrigin.getEntityId())) {
						found = true;
					}
				}

				for(Instrument instrument : instruments){
					if (instrument.getLaboratory().getId().equals(entityOrigin.getEntityId())) {
						found = true;
					}
				}

				Assert.assertTrue(found);
			}

			if (entityOrigin.getEntityType().equals(EntityType.PERSON)) {
				personCount++;
				boolean found = false;
				for (PersonLaboratoryAffiliation personLaboratoryAffiliation : personLaboratoryAffiliations) {
					if (personLaboratoryAffiliation.getPerson().getId().equals(entityOrigin.getEntityId())) {
						found = true;
					}
				}
				Assert.assertTrue(found);
			}

			if (entityOrigin.getEntityType().equals(EntityType.INSTRUMENT)) {
				instCount++;
				boolean found = false;
				for (Instrument instrument : instruments) {
					if (instrument.getId().equals(entityOrigin.getEntityId())) {
						found = true;
					}
				}
				Assert.assertTrue(found);
			}

			if (entityOrigin.getEntityType().equals(EntityType.JOURNAL)) {
				boolean found = false;
				for (JournalPublisherAffiliation journalPublisherAffiliation : journalPublisherAffiliations) {
					if (journalPublisherAffiliation.getJournal().getId().equals(entityOrigin.getEntityId())) {
						found = true;
					}
				}
				Assert.assertTrue(found);
			}

			if (entityOrigin.getEntityType().equals(EntityType.PUBLISHER)) {
				boolean found = false;
				for (JournalPublisherAffiliation journalPublisherAffiliation : journalPublisherAffiliations) {
					if (journalPublisherAffiliation.getPublisher().getId().equals(entityOrigin.getEntityId())) {
						found = true;
					}
				}
				Assert.assertTrue(found);
			}

			if (entityOrigin.getEntityType().equals(EntityType.KEYWORD)) {
				boolean found = false;
				for (Keyword keyword: keywords) {
					if (keyword.getId().equals(entityOrigin.getEntityId())) {
						found = true;
					}
				}
				Assert.assertTrue(found);
			}

			if (entityOrigin.getEntityType().equals(EntityType.RESEARCH_DOMAIN)) {
				boolean found = false;
				for (ResearchDomain researchDomain : researchDomains) {
					if (researchDomain.getId().equals(entityOrigin.getEntityId())) {
						found = true;
					}
				}
				Assert.assertTrue(found);
			}

			if (entityOrigin.getEntityType().equals(EntityType.FORMULA)) {
				boolean found = false;
				for (Formula formula : formulas) {
					if (formula.getId().equals(entityOrigin.getEntityId())) {
						found = true;
					}
				}
				Assert.assertTrue(found);
			}
		}

		Assert.assertEquals(expectedLaboCount, laboCount);
		Assert.assertEquals(expectedPersonCount, personCount);
		Assert.assertEquals(expectedInstCount, instCount);
	}

}
