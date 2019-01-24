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
import eu.ill.puma.core.domain.document.MetadataConfidence;
import eu.ill.puma.core.domain.document.entities.*;
import eu.ill.puma.persistence.domain.document.*;
import eu.ill.puma.persistence.domain.document.enumeration.DocumentType;
import eu.ill.puma.persistence.domain.importer.Importer;
import eu.ill.puma.persistence.service.document.DocumentVersionService;
import eu.ill.puma.persistence.service.document.InstrumentService;
import eu.ill.puma.persistence.service.document.PersonLaboratoryAffiliationService;
import eu.ill.puma.persistence.service.document.PumaFileService;
import eu.ill.puma.persistence.service.importer.ImporterService;
import eu.ill.puma.persistence.utils.ResourceLoader;
import org.junit.Assert;
import org.junit.Before;
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
import eu.ill.puma.persistence.service.converterV2.exception.PumaDocumentConversionException;

import java.io.IOException;

/**
 * Created by letreguilly on 09/08/17.
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:/applicationContext-test.xml"
})
@TestExecutionListeners({
		DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DocumentConverterTest2 {

	@Autowired
	protected DocumentConverter documentConverter;

	@Autowired
	private DocumentVersionService documentVersionService;

	@Autowired
	private ImporterService importerService;

	@Autowired
	private PersonLaboratoryAffiliationService personLaboratoryAffiliationService;

	@Autowired
	private InstrumentService instrumentService;

	@Before
	public void before() {
		Importer wosImporter = new Importer();
		wosImporter.setName("wos importer");
		wosImporter.setShortName("wos");
		wosImporter.setUrl("http://127.0.0.1");
		importerService.save(wosImporter);

		Importer floraImporter = new Importer();
		floraImporter.setName("flora importer");
		floraImporter.setShortName("flora");
		floraImporter.setUrl("http://127.0.0.2");
		importerService.save(floraImporter);
	}

	@Test
	public void testImport() throws DocumentVersionService.DocumentVersionPersistenceException, PumaDocumentConversionException, IOException, PumaFileService.PumaFilePersistenceException {
		BaseDocument importerDocument = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);

		DocumentVersion importedDocument = documentConverter.convert(importerDocument, "wos");

		Assert.assertNotNull(importedDocument);
		Assert.assertNotNull(importedDocument.getDocument());
		Assert.assertEquals(DocumentType.REVIEW, importedDocument.getDocument().getDocumentType());

		Assert.assertNotNull(importedDocument.getSources());
		Assert.assertEquals(1, importedDocument.getSources().size());

		Assert.assertNotNull(importedDocument.getReleaseDate());
		Assert.assertEquals("Estdeseruntexercitationestfugiatet", importedDocument.getDoi());

		Assert.assertEquals("duis qui duis exercitation non", importedDocument.getTitle());
		Assert.assertNotNull(importedDocument.getAbstractText());
		Assert.assertTrue(importedDocument.getAbstractText().length() > 50);

		Assert.assertNotNull(importedDocument.getSubType());
		Assert.assertNotNull(importedDocument.getDoi());
		Assert.assertNotNull(importedDocument.getTitle());
		Assert.assertTrue(importedDocument.getKeywords().size() > 0);
		Assert.assertTrue(importedDocument.getResearchDomains().size() > 0);
		Assert.assertTrue(importedDocument.getReferences().size() > 0);
		Assert.assertTrue(importedDocument.getFormulas().size() > 0);

		Assert.assertEquals(3, importedDocument.getKeywords().size());
	}

	@Test
	public void testImport2() throws DocumentVersionService.DocumentVersionPersistenceException, PumaDocumentConversionException, IOException, PumaFileService.PumaFilePersistenceException {
		BaseDocument importerDocument = ResourceLoader.readType("ImporterDocument5.json", BaseDocument.class);

		DocumentVersion importedDocument = documentConverter.convert(importerDocument, "wos");

		Assert.assertNotNull(importedDocument);
		Assert.assertNotNull(importedDocument.getDocument());
		Assert.assertEquals(DocumentType.PROPOSAL, importedDocument.getDocument().getDocumentType());

		Assert.assertNotNull(importedDocument.getSources());
		Assert.assertEquals(1, importedDocument.getSources().size());

		Assert.assertNotNull(importedDocument.getReleaseDate());

		Assert.assertNotNull(importedDocument.getAbstractText());
		Assert.assertTrue(importedDocument.getAbstractText().length() > 50);

		Assert.assertNotNull(importedDocument.getSubType());
		Assert.assertNull(importedDocument.getDoi());
		Assert.assertNotNull(importedDocument.getTitle());
		Assert.assertTrue(importedDocument.getFormulas().size() > 0);

		importedDocument.getPersonLaboratoryAffiliations().forEach(affiliation -> {
			Assert.assertTrue(affiliation.getRoles().size() > 0);
		});

	}

	@Test
	public void testDuplicateDocument() throws DocumentVersionService.DocumentVersionPersistenceException, PumaDocumentConversionException, IOException, PumaFileService.PumaFilePersistenceException {
		BaseDocument importerDocument = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);

		DocumentVersion importedDocument1 = documentConverter.convert(importerDocument, "wos");

		DocumentVersion importedDocument2 = documentConverter.convert(importerDocument, "wos");

		Assert.assertNotNull(importedDocument1);

		Assert.assertNull(importedDocument2);
	}

	@Test
	public void testAddKeyword() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {

		BaseDocument importerDocument = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
		DocumentVersion importedDocument = documentConverter.convert(importerDocument, "wos");

		BaseStringEntity keyword = new BaseStringEntity();
		keyword.setValue("gfidsghd");
		keyword.setConfidence(MetadataConfidence.CONFIDENT);

		BaseDocument fromAnalyser = new BaseDocument();
		fromAnalyser.setPumaId(importedDocument.getId());
		fromAnalyser.setSourceId(importedDocument.getSources().get(0).getSourceId());
		fromAnalyser.addKeyword(keyword);

		DocumentVersion analysed = documentConverter.convert(fromAnalyser, "grobid");

		Assert.assertEquals(3, importedDocument.getKeywords().size());
		Assert.assertEquals(4, analysed.getKeywords().size());

	}

	@Test
	public void testUpdateKeyword() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {

		BaseDocument importerDocument = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
		DocumentVersion importedDocument = documentConverter.convert(importerDocument, "wos");

		BaseStringEntity keyword = new BaseStringEntity();
		keyword.setValue("gfidsghd");
		keyword.setConfidence(MetadataConfidence.CONFIDENT);
		keyword.setPumaId(2L);

		BaseDocument fromAnalyser = new BaseDocument();
		fromAnalyser.setPumaId(importedDocument.getId());
		fromAnalyser.setSourceId(importedDocument.getSources().get(0).getSourceId());
		fromAnalyser.addKeyword(keyword);

		DocumentVersion analysed = documentConverter.convert(fromAnalyser, "grobid");

		Assert.assertEquals(3, importedDocument.getKeywords().size());
		Assert.assertEquals(3, analysed.getKeywords().size());

		Assert.assertEquals("gfidsghd", analysed.getKeywords().get(2).getWord());
	}

	@Test
	public void testDeleteKeyword() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {

		//doc1
		BaseDocument importerDocument1 = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
		DocumentVersion importedDocument1 = documentConverter.convert(importerDocument1, "wos");

		//doc2
		BaseDocument importerDocument2 = ResourceLoader.readType("ImporterDocument4.json", BaseDocument.class);
		DocumentVersion importedDocument2 = documentConverter.convert(importerDocument2, "flora");

		Assert.assertNotEquals(importerDocument1.getId(), importerDocument2.getId());

		//find keyword to delete in doc 1
		BaseStringEntity baseKeyword = new BaseStringEntity();
		for (Keyword keyword : importedDocument1.getKeywords()) {
			if (keyword.getWord().equals("sunt")) {
				baseKeyword.setPumaId(-keyword.getId());
			}
		}
		Assert.assertNotNull(baseKeyword.getPumaId());

		//delete message
		BaseDocument fromAnalyser = new BaseDocument();
		fromAnalyser.setPumaId(importedDocument1.getId());
		fromAnalyser.setSourceId(importedDocument1.getSources().get(0).getSourceId());
		fromAnalyser.addKeyword(baseKeyword);

		DocumentVersion analysed = documentConverter.convert(fromAnalyser, "grobid");

		//check that the keyword has been deleted
		DocumentVersion finalDoc1 = documentVersionService.getByIdWithAllEntities(analysed.getId(), true);
		Assert.assertEquals(2, finalDoc1.getKeywords().size());

		//check that the keyword stiff exist in the other document
		DocumentVersion finalDoc2 = documentVersionService.getByIdWithAllEntities(importedDocument2.getId(), true);
		Assert.assertEquals(4, finalDoc2.getKeywords().size());

	}

	@Test
	public void testAddResearchDomain() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {

		BaseDocument importerDocument = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
		DocumentVersion importedDocument = documentConverter.convert(importerDocument, "wos");

		BaseStringEntity researchDomain = new BaseStringEntity();
		researchDomain.setValue("gfidsghd");
		researchDomain.setConfidence(MetadataConfidence.CONFIDENT);

		BaseDocument fromAnalyser = new BaseDocument();
		fromAnalyser.setPumaId(importedDocument.getId());
		fromAnalyser.setSourceId(importedDocument.getSources().get(0).getSourceId());
		fromAnalyser.addResearchDomain(researchDomain);

		DocumentVersion analysed = documentConverter.convert(fromAnalyser, "grobid");

		Assert.assertEquals(4, importedDocument.getResearchDomains().size());
		Assert.assertEquals(5, analysed.getResearchDomains().size());

	}

	@Test
	public void testUpdateResearchDomain() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {

		BaseDocument importerDocument = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
		DocumentVersion importedDocument = documentConverter.convert(importerDocument, "wos");

		BaseStringEntity researchDomain = new BaseStringEntity();
		researchDomain.setValue("gfidsghd");
		researchDomain.setConfidence(MetadataConfidence.CONFIDENT);
		researchDomain.setPumaId(2L);

		BaseDocument fromAnalyser = new BaseDocument();
		fromAnalyser.setPumaId(importedDocument.getId());
		fromAnalyser.setSourceId(importedDocument.getSources().get(0).getSourceId());
		fromAnalyser.addResearchDomain(researchDomain);

		DocumentVersion analysed = documentConverter.convert(fromAnalyser, "grobid");

		Assert.assertEquals(4, importedDocument.getResearchDomains().size());
		Assert.assertEquals(4, analysed.getResearchDomains().size());

		Assert.assertEquals("gfidsghd", analysed.getResearchDomains().get(3).getSubject());
	}

	@Test
	public void testDeleteResearchDomain() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {

		//doc1
		BaseDocument importerDocument1 = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
		DocumentVersion importedDocument1 = documentConverter.convert(importerDocument1, "wos");

		//doc2
		BaseDocument importerDocument2 = ResourceLoader.readType("ImporterDocument4.json", BaseDocument.class);
		DocumentVersion importedDocument2 = documentConverter.convert(importerDocument2, "flora");

		Assert.assertNotEquals(importerDocument1.getId(), importerDocument2.getId());

		//find researchDomain to delete in doc 1
		BaseStringEntity baseResearchDomain = new BaseStringEntity();
		for (ResearchDomain researchDomain : importedDocument1.getResearchDomains()) {
			if (researchDomain.getSubject().equals("sunt")) {
				baseResearchDomain.setPumaId(-researchDomain.getId());
			}
		}
		Assert.assertNotNull(baseResearchDomain.getPumaId());

		//delete message
		BaseDocument fromAnalyser = new BaseDocument();
		fromAnalyser.setPumaId(importedDocument1.getId());
		fromAnalyser.setSourceId(importedDocument1.getSources().get(0).getSourceId());
		fromAnalyser.addResearchDomain(baseResearchDomain);

		DocumentVersion analysed = documentConverter.convert(fromAnalyser, "grobid");

		//check that the keyword has been deleted
		DocumentVersion finalDoc1 = documentVersionService.getByIdWithAllEntities(analysed.getId(), true);
		Assert.assertEquals(3, finalDoc1.getResearchDomains().size());

		//check that the keyword stiff exist in the other document
		DocumentVersion finalDoc2 = documentVersionService.getByIdWithAllEntities(importedDocument2.getId(), true);
		Assert.assertEquals(5, finalDoc2.getResearchDomains().size());

	}


	@Test
	public void testAddReference() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {

		BaseDocument importerDocument = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
		DocumentVersion importedDocument = documentConverter.convert(importerDocument, "wos");

		BaseStringEntity reference = new BaseStringEntity();
		reference.setValue("gfidsghd");
		reference.setConfidence(MetadataConfidence.CONFIDENT);

		BaseDocument fromAnalyser = new BaseDocument();
		fromAnalyser.setPumaId(importedDocument.getId());
		fromAnalyser.setSourceId(importedDocument.getSources().get(0).getSourceId());
		fromAnalyser.addReference(reference);

		DocumentVersion analysed = documentConverter.convert(fromAnalyser, "grobid");
		analysed = documentVersionService.getByIdWithAllEntities(analysed.getId(), true);


		Assert.assertEquals(2, importedDocument.getReferences().size());
		Assert.assertEquals(3, analysed.getReferences().size());

	}

	@Test
	public void testUpdateReference() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {

		BaseDocument importerDocument = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
		DocumentVersion importedDocument = documentConverter.convert(importerDocument, "wos");

		BaseStringEntity reference = new BaseStringEntity();
		reference.setValue("gfidsghd");
		reference.setConfidence(MetadataConfidence.CONFIDENT);
		reference.setPumaId(1L);

		BaseDocument fromAnalyser = new BaseDocument();
		fromAnalyser.setPumaId(importedDocument.getId());
		fromAnalyser.setSourceId(importedDocument.getSources().get(0).getSourceId());
		fromAnalyser.addReference(reference);

		DocumentVersion analysed = documentConverter.convert(fromAnalyser, "grobid");

		Assert.assertEquals(2, importedDocument.getReferences().size());
		Assert.assertEquals(2, analysed.getReferences().size());

		Assert.assertEquals("gfidsghd", analysed.getReferences().get(1).getCitationString());
	}

//	@Test
//	public void testDeleteReference() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {
//
//		//doc1
//		BaseDocument importerDocument1 = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
//		DocumentVersion importedDocument1 = documentConverter.convert(importerDocument1, "wos");
//
//		//doc2
//		BaseDocument importerDocument2 = ResourceLoader.readType("ImporterDocument4.json", BaseDocument.class);
//		DocumentVersion importedDocument2 = documentConverter.convert(importerDocument2, "flora");
//
//		Assert.assertNotEquals(importerDocument1.getId(), importerDocument2.getId());
//
//		//find reference to delete in doc 1
//		BaseStringEntity baseReference = new BaseStringEntity();
//		for (Reference reference : importedDocument1.getReferences()) {
//			if (reference.getCitationString().equals("id fugiat aliqua irure ex laborum duis")) {
//				baseReference.setPumaId(-reference.getId());
//			}
//		}
//		Assert.assertNotNull(baseReference.getPumaId());
//
//		//delete message
//		BaseDocument fromAnalyser = new BaseDocument();
//		fromAnalyser.setPumaId(importedDocument1.getId());
//		fromAnalyser.setSourceId(importedDocument1.getSources().get(0).getSourceId());
//		fromAnalyser.addReference(baseReference);
//
//		DocumentVersion analysed = documentConverter.convert(fromAnalyser, "grobid");
//
//		//check that the keyword has been deleted
//		DocumentVersion finalDoc1 = documentVersionService.getByIdWithAllEntities(analysed.getId(), true);
//		Assert.assertEquals(1, finalDoc1.getReferences().size());
//
//		//check that the keyword stiff exist in the other document
//		DocumentVersion finalDoc2 = documentVersionService.getByIdWithAllEntities(importedDocument2.getId(), true);
//		Assert.assertEquals(2, finalDoc2.getReferences().size());
//
//	}


	@Test
	public void testAddFormula() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {

		BaseDocument importerDocument = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
		DocumentVersion importedDocument = documentConverter.convert(importerDocument, "wos");

		BaseFormula formula = new BaseFormula();
		formula.setConfidence(MetadataConfidence.CONFIDENT);
		formula.setCode("007");
		formula.setConsistence("vide");
		formula.setPressure("super fort");
		formula.setTemperature("pas tres chaud");
		formula.setMagneticField("null");

		BaseDocument fromAnalyser = new BaseDocument();
		fromAnalyser.setPumaId(importedDocument.getId());
		fromAnalyser.setSourceId(importedDocument.getSources().get(0).getSourceId());
		fromAnalyser.addFormula(formula);

		DocumentVersion analysed = documentConverter.convert(fromAnalyser, "grobid");
		analysed = documentVersionService.getByIdWithAllEntities(analysed.getId(), true);


		Assert.assertEquals(1, importedDocument.getFormulas().size());
		Assert.assertEquals(2, analysed.getFormulas().size());

	}

	@Test
	public void testUpdateFormula() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {

		BaseDocument importerDocument = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
		DocumentVersion importedDocument = documentConverter.convert(importerDocument, "wos");

		BaseFormula baseFormula = new BaseFormula();
		baseFormula.setConfidence(MetadataConfidence.CONFIDENT);
		baseFormula.setCode("007");
		baseFormula.setConsistence("vide");
		baseFormula.setPressure("super pas fort");
		baseFormula.setTemperature("un peu plus chaud");
		baseFormula.setMagneticField("null");
		baseFormula.setConfidence(MetadataConfidence.CONFIDENT);
		baseFormula.setPumaId(1L);

		BaseDocument fromAnalyser = new BaseDocument();
		fromAnalyser.setPumaId(importedDocument.getId());
		fromAnalyser.setSourceId(importedDocument.getSources().get(0).getSourceId());
		fromAnalyser.addFormula(baseFormula);

		DocumentVersion analysed = documentConverter.convert(fromAnalyser, "grobid");

		Assert.assertEquals(1, importedDocument.getFormulas().size());
		Assert.assertEquals(1, analysed.getFormulas().size());

		Assert.assertEquals("un peu plus chaud", analysed.getFormulas().get(0).getTemperature());
	}

	@Test
	public void testDeleteFormula() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {

		//doc1
		BaseDocument importerDocument1 = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
		DocumentVersion importedDocument1 = documentConverter.convert(importerDocument1, "wos");

		//doc2
		BaseDocument importerDocument2 = ResourceLoader.readType("ImporterDocument4.json", BaseDocument.class);
		DocumentVersion importedDocument2 = documentConverter.convert(importerDocument2, "flora");

		Assert.assertNotEquals(importerDocument1.getId(), importerDocument2.getId());

		//find reference to delete in doc 1
		BaseFormula baseFormula = new BaseFormula();
		for (Formula formula : importedDocument1.getFormulas()) {
			if (formula.getCode().equals("007")) {
				baseFormula.setPumaId(-formula.getId());
			}
		}
		Assert.assertNotNull(baseFormula.getPumaId());

		//delete message
		BaseDocument fromAnalyser = new BaseDocument();
		fromAnalyser.setPumaId(importedDocument1.getId());
		fromAnalyser.setSourceId(importedDocument1.getSources().get(0).getSourceId());
		fromAnalyser.addFormula(baseFormula);

		DocumentVersion analysed = documentConverter.convert(fromAnalyser, "grobid");

		//check that the keyword has been deleted
		DocumentVersion finalDoc1 = documentVersionService.getByIdWithAllEntities(analysed.getId(), true);
		Assert.assertEquals(0, finalDoc1.getFormulas().size());

		//check that the keyword stiff exist in the other document
		DocumentVersion finalDoc2 = documentVersionService.getByIdWithAllEntities(importedDocument2.getId(), true);
		Assert.assertEquals(2, finalDoc2.getFormulas().size());

	}

	@Test
	public void testAddJournalAffiliation() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {

		BaseDocument importerDocument = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
		DocumentVersion importedDocument = documentConverter.convert(importerDocument, "wos");

		BaseJournal baseJournal = new BaseJournal();
		baseJournal.setName("gjkdgsj");
		baseJournal.setConfidence(MetadataConfidence.CONFIDENT);

		BasePublisher basePublisher = new BasePublisher();
		basePublisher.setAddress("gfdgsdf");
		basePublisher.setCity("gdsd");
		basePublisher.setName("fdsfn,bvn");
		basePublisher.setConfidence(MetadataConfidence.CONFIDENT);

		BaseDocument fromAnalyser = new BaseDocument();
		fromAnalyser.setPumaId(importedDocument.getId());
		fromAnalyser.setSourceId(importedDocument.getSources().get(0).getSourceId());
		fromAnalyser.setJournal(baseJournal);
		fromAnalyser.getPublishers().add(basePublisher);

		DocumentVersion analysed = documentConverter.convert(fromAnalyser, "grobid");
		analysed = documentVersionService.getByIdWithAllEntities(analysed.getId(), true);


		Assert.assertEquals(2, analysed.getJournalPublisherAffiliations().size());

	}

	@Test
	public void testUpdateJournalAffiliation() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {

		BaseDocument importerDocument = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
		DocumentVersion importedDocument = documentConverter.convert(importerDocument, "wos");


		BaseJournal baseJournal = new BaseJournal();
		baseJournal.setName("gjkdgsj");
		baseJournal.setConfidence(MetadataConfidence.CONFIDENT);
		baseJournal.setPumaId(1L);

		BasePublisher basePublisher = new BasePublisher();
		basePublisher.setAddress("gfdgsdf");
		basePublisher.setCity("gdsd");
		basePublisher.setName("fdsfn,bvn");
		basePublisher.setConfidence(MetadataConfidence.CONFIDENT);

		BaseDocument fromAnalyser = new BaseDocument();
		fromAnalyser.setPumaId(importedDocument.getId());
		fromAnalyser.setSourceId(importedDocument.getSources().get(0).getSourceId());
		fromAnalyser.setJournal(baseJournal);
		fromAnalyser.getPublishers().add(basePublisher);

		DocumentVersion analysed = documentConverter.convert(fromAnalyser, "grobid");

		analysed = this.documentVersionService.getByIdWithAllEntities(analysed.getId(), true);

		Assert.assertEquals(1, analysed.getJournalPublisherAffiliations().size());
		Assert.assertEquals("gjkdgsj", analysed.getJournalPublisherAffiliations().get(0).getJournal().getName());

	}

	@Test
	public void testDeleteJournalAffiliation() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {

		//doc1
		BaseDocument importerDocument1 = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
		DocumentVersion importedDocument1 = documentConverter.convert(importerDocument1, "wos");

		//doc2
		BaseDocument importerDocument2 = ResourceLoader.readType("ImporterDocument4.json", BaseDocument.class);
		DocumentVersion importedDocument2 = documentConverter.convert(importerDocument2, "flora");

		Assert.assertNotEquals(importerDocument1.getId(), importerDocument2.getId());

		//find reference to delete in doc 1
		BaseJournal baseJournal = new BaseJournal();
		baseJournal.setPumaId(-1L);

		//delete message
		BaseDocument fromAnalyser = new BaseDocument();
		fromAnalyser.setPumaId(importedDocument1.getId());
		fromAnalyser.setSourceId(importedDocument1.getSources().get(0).getSourceId());
		fromAnalyser.setJournal(baseJournal);

		DocumentVersion analysed = documentConverter.convert(fromAnalyser, "grobid");

		//check that the keyword has been deleted
		DocumentVersion finalDoc1 = documentVersionService.getByIdWithAllEntities(analysed.getId(), true);
		Assert.assertEquals(0, finalDoc1.getJournalPublisherAffiliations().size());

		//check that the keyword stiff exist in the other document
		DocumentVersion finalDoc2 = documentVersionService.getByIdWithAllEntities(importedDocument2.getId(), true);
		Assert.assertEquals(1, finalDoc2.getJournalPublisherAffiliations().size());

	}

//	@Test
//	public void testUpdateFile() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {
//
//		BaseDocument importerDocument = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
//		DocumentVersion importedDocument = documentConverter.convert(importerDocument, "wos");
//
//		Assert.assertEquals(3, importedDocument.getFiles().size());
//		Assert.assertEquals(1, importedDocument.getResolverInfos().size());
//
//		BaseFile file = new BaseFile();
//		file.setHash("ghuyhghjgvbugvbuhbhuhjk,nknn ");
//		file.setPageNumber(15L);
//		file.setPumaId(3L);
//
//		BaseDocument fromAnalyser = new BaseDocument();
//		fromAnalyser.setPumaId(importedDocument.getId());
//		fromAnalyser.setSourceId(importedDocument.getSources().get(0).getSourceId());
//		fromAnalyser.addFile(file);
//
//		DocumentVersion analysed = documentConverter.convert(fromAnalyser, "grobid");
//		analysed = documentVersionService.getByIdWithAllEntities(analysed.getId(), true);
//
//		Assert.assertEquals(3, analysed.getFiles().size());
//		Assert.assertEquals("ghuyhghjgvbugvbuhbhuhjk,nknn ", analysed.getFiles().get(2).getHash());
//		Assert.assertEquals(15, analysed.getFiles().get(2).getPageNumber()+ 0);
//	}

	@Test
	public void testAdditionalText() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {

		BaseDocument importerDocument = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
		DocumentVersion importedDocument = documentConverter.convert(importerDocument, "wos");

		Assert.assertEquals(3, importedDocument.getAdditionalTexts().size());
	}

	@Test
	public void testAddInstrument() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {

		BaseDocument importerDocument = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
		DocumentVersion importedDocument = documentConverter.convert(importerDocument, "wos");

		BaseInstrument baseInstrument = new BaseInstrument();
		baseInstrument.setName("instrument");
		baseInstrument.setShortName("instr");
		baseInstrument.setConfidence(MetadataConfidence.CONFIDENT);
		baseInstrument.setLaboratoryId(11L);
		baseInstrument.setExperimentalTechniqueId(12L);

		BaseLaboratory baseLaboratory = new BaseLaboratory();
		baseLaboratory.setId(11L);
		baseLaboratory.setAddress("somewhere");
		baseLaboratory.setCity("in");
		baseLaboratory.setCountry("france");
		baseLaboratory.setName("name");
		baseLaboratory.setConfidence(MetadataConfidence.FOUND);


		BaseDocument fromAnalyser = new BaseDocument();
		fromAnalyser.setPumaId(importedDocument.getId());
		fromAnalyser.setSourceId(importedDocument.getSources().get(0).getSourceId());
		fromAnalyser.addInstrument(baseInstrument);
		fromAnalyser.addLaboratory(baseLaboratory);

		DocumentVersion analysed = documentConverter.convert(fromAnalyser, "grobid");
		analysed = documentVersionService.getByIdWithAllEntities(analysed.getId(), true);

//		Assert.assertEquals(2, importedDocument.getInstrumentScientificTechniqueAffiliations().size());


//		Assert.assertEquals(3, analysed.getInstrumentScientificTechniqueAffiliations().size());

	}

	@Test
	public void testAddInstrumentOnly() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {

		BaseDocument importerDocument = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
		DocumentVersion importedDocument = documentConverter.convert(importerDocument, "wos");

		BaseInstrument baseInstrument = new BaseInstrument();
		baseInstrument.setName("instrument");
		baseInstrument.setShortName("instr");
		baseInstrument.setConfidence(MetadataConfidence.CONFIDENT);
		baseInstrument.setLaboratoryId(11L);
		baseInstrument.setExperimentalTechniqueId(12L);

		BaseDocument fromAnalyser = new BaseDocument();
		fromAnalyser.setPumaId(importedDocument.getId());
		fromAnalyser.setSourceId(importedDocument.getSources().get(0).getSourceId());
		fromAnalyser.addInstrument(baseInstrument);

		DocumentVersion analysed = documentConverter.convert(fromAnalyser, "grobid");
		analysed = documentVersionService.getByIdWithAllEntities(analysed.getId(), true);

//		Assert.assertEquals(importedDocument.getInstrumentScientificTechniqueAffiliations().size() + 1, analysed.getInstrumentScientificTechniqueAffiliations().size());
	}

	@Test
	public void testDeleteInstrumentOnly() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {

		BaseDocument importerDocument = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
		DocumentVersion importedDocument = documentConverter.convert(importerDocument, "wos");

		BaseInstrument baseInstrument = new BaseInstrument();
		baseInstrument.setPumaId(-1L);
		baseInstrument.setConfidence(MetadataConfidence.CONFIDENT);

		BaseDocument fromAnalyser = new BaseDocument();
		fromAnalyser.setPumaId(importedDocument.getId());
		fromAnalyser.setSourceId(importedDocument.getSources().get(0).getSourceId());
		fromAnalyser.addInstrument(baseInstrument);

		DocumentVersion analysed = documentConverter.convert(fromAnalyser, "grobid");
		analysed = documentVersionService.getByIdWithAllEntities(analysed.getId(), true);

		int count = 0;
//		for (InstrumentScientificTechniqueAffiliation affiliation : importedDocument.getInstrumentScientificTechniqueAffiliations()) {
//			if (affiliation.getInstrument() != null && affiliation.getInstrument().getId() == 1L) {
//				count++;
//			}
//		}
//
//		Assert.assertEquals(importedDocument.getInstrumentScientificTechniqueAffiliations().size() - count, analysed.getInstrumentScientificTechniqueAffiliations().size());

	}

	@Test
	public void testUpdateInstrumentOnly() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {

		BaseDocument importerDocument = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
		DocumentVersion importedDocument = documentConverter.convert(importerDocument, "wos");

		BaseInstrument baseInstrument = new BaseInstrument();
		baseInstrument.setPumaId(1L);
		baseInstrument.setName("instrument");
		baseInstrument.setShortName("instr");
		baseInstrument.setConfidence(MetadataConfidence.CONFIDENT);


		BaseDocument fromAnalyser = new BaseDocument();
		fromAnalyser.setPumaId(importedDocument.getId());
		fromAnalyser.setSourceId(importedDocument.getSources().get(0).getSourceId());
		fromAnalyser.addInstrument(baseInstrument);

		DocumentVersion analysed = documentConverter.convert(fromAnalyser, "grobid");
		analysed = documentVersionService.getByIdWithAllEntities(analysed.getId(), true);
//
//		for (InstrumentScientificTechniqueAffiliation affiliation : this.instrumentScientificTechniqueAffiliationService.getAll()) {
//			if (affiliation.getInstrument() != null && affiliation.getInstrument().getId() == 1L) {
//				Assert.assertEquals("instrument", baseInstrument.getName());
//			}
//		}
//
//		Assert.assertEquals(importedDocument.getInstrumentScientificTechniqueAffiliations().size(), analysed.getInstrumentScientificTechniqueAffiliations().size());
	}



	@Test
	public void testUpdatePersonAffiliation() throws IOException, PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {
		BaseDocument importerDocument = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
		DocumentVersion importedDocument = documentConverter.convert(importerDocument, "wos");

		PersonLaboratoryAffiliation affiliationToUpdate = importedDocument.getPersonLaboratoryAffiliations().get(0);

		BaseLaboratory baseLaboratory = new BaseLaboratory();
		baseLaboratory.setId(1L);
		baseLaboratory.setPumaId(affiliationToUpdate.getLaboratory().getId());
		baseLaboratory.setAddress("somewhere");
		baseLaboratory.setCity("in");
		baseLaboratory.setCountry("france");
		baseLaboratory.setName("name");
		baseLaboratory.setConfidence(MetadataConfidence.FOUND);

		BasePerson basePerson = new BasePerson();
		basePerson.setLaboratoryId(1L);
		basePerson.setPumaId(affiliationToUpdate.getPerson().getId());
		basePerson.setPublicationName("pubName");
		basePerson.setFirstName("firstName");
		basePerson.setLastName("lastName");
		basePerson.setEmail("email@email.com");
		basePerson.setOrcidId("orcirdId");
		basePerson.setResearcherId("researcherId");

		BaseDocument fromAnalyser = new BaseDocument();
		fromAnalyser.setPumaId(importedDocument.getId());
		fromAnalyser.setSourceId(importedDocument.getSources().get(0).getSourceId());
		fromAnalyser.addLaboratory(baseLaboratory);
		fromAnalyser.addPerson(basePerson);

		DocumentVersion analysed = documentConverter.convert(fromAnalyser, "grobid");
		analysed = documentVersionService.getByIdWithAllEntities(analysed.getId(), true);

		Assert.assertEquals(importedDocument.getPersonLaboratoryAffiliations().size(), analysed.getPersonLaboratoryAffiliations().size());
	}
}
