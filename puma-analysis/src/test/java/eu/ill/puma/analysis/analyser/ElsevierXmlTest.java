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
package eu.ill.puma.analysis.analyser;

import eu.ill.puma.analysis.analyser.elsevieranalyser.ElsevierXMLAnalyser;
import eu.ill.puma.core.domain.analysis.AnalyserResponse;
import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.core.utils.ResourceLoader;
import eu.ill.puma.persistence.domain.document.Document;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.DocumentVersionSource;
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.domain.document.enumeration.DocumentType;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileType;
import eu.ill.puma.persistence.service.converterV2.DocumentConverter;
import eu.ill.puma.persistence.service.document.DocumentVersionService;
import org.junit.Assert;
import org.junit.Ignore;
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
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.util.Date;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
	"classpath:/applicationContext-test.xml"
})
@TestExecutionListeners({
	DependencyInjectionTestExecutionListener.class,
	TransactionalTestExecutionListener.class,
	DirtiesContextTestExecutionListener.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Ignore
public class ElsevierXmlTest {

	@Autowired
	private DocumentConverter converter;

	@Autowired
	private DocumentVersionService documentVersionService;

	public DocumentVersion createDocumentVersion(String filename) throws Exception {
		Document document = new Document();
		document.setDocumentType(DocumentType.PUBLICATION);

		DocumentVersion documentVersion = new DocumentVersion();
		documentVersion.setDocument(document);

		DocumentVersionSource documentVersionSource = new DocumentVersionSource();
		documentVersionSource.setImporterShortName("TEST");
		documentVersionSource.setSourceId("testId");
		documentVersionSource.setImportDate(new Date());
		documentVersionSource.setDocumentVersion(documentVersion);

		documentVersion.addSource(documentVersionSource);

		PumaFile pumaFile = new PumaFile();
		pumaFile.setMimeType(PumaFile.XML_MIME_TYPE);
		pumaFile.setDocumentType(PumaFileType.PUBLICATION);

		byte[] fileBytes = ResourceLoader.readByteArray(filename);
		pumaFile.setData(fileBytes);

		documentVersion.addFile(pumaFile);

		documentVersionService.save(documentVersion);

		return documentVersion;
	}

	@Test
	public void testAnalyser() throws Exception {
		DocumentVersion documentVersion = this.createDocumentVersion("private/publication-elsevier.xml");

		ElsevierXMLAnalyser elsevierXMLAnalyser = new ElsevierXMLAnalyser(0, null);

		AnalyserResponse response = elsevierXMLAnalyser.analyse(documentVersion);
		Assert.assertNotNull(response);

		BaseDocument baseDocument = response.getBaseDocument();
		Assert.assertNotNull(baseDocument);

		Assert.assertEquals(1, baseDocument.getFiles().size());
	}


	@Test
	public void testEncoding() throws Exception {
		DocumentVersion documentVersion = this.createDocumentVersion("private/publication-elsevier-encoding.xml");

		ElsevierXMLAnalyser elsevierXMLAnalyser = new ElsevierXMLAnalyser(0, null);

		AnalyserResponse response = elsevierXMLAnalyser.analyse(documentVersion);
		Assert.assertNotNull(response);

		BaseDocument baseDocument = response.getBaseDocument();
		Assert.assertNotNull(baseDocument);
		Assert.assertEquals(1, baseDocument.getFiles().size());
	}

	@Test
	public void testScanned() throws Exception {
		DocumentVersion documentVersion = this.createDocumentVersion("private/publication-elsevier-scanned.xml");

		ElsevierXMLAnalyser elsevierXMLAnalyser = new ElsevierXMLAnalyser(0, null);

		AnalyserResponse response = elsevierXMLAnalyser.analyse(documentVersion);
		Assert.assertNotNull(response);

		BaseDocument baseDocument = response.getBaseDocument();
		Assert.assertNotNull(baseDocument);

		DocumentVersion integratedDocumentVersion = converter.convert(documentVersion, baseDocument, "test");
		Assert.assertTrue(integratedDocumentVersion.getFiles().size() > 0);
	}

	@Test
	public void testReferenceCrash() throws Exception {
		DocumentVersion documentVersion = this.createDocumentVersion("private/publication-elsevier-referencecrash.xml");

		ElsevierXMLAnalyser elsevierXMLAnalyser = new ElsevierXMLAnalyser(0, null);

		AnalyserResponse response = elsevierXMLAnalyser.analyse(documentVersion);
		Assert.assertNotNull(response);

		BaseDocument baseDocument = response.getBaseDocument();
		Assert.assertNotNull(baseDocument);

	}
}
