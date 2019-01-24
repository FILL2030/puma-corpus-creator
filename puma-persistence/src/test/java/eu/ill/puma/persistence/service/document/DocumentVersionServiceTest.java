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
import eu.ill.puma.persistence.domain.document.Document;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.DocumentVersionSource;
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.domain.document.enumeration.DocumentType;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileType;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class DocumentVersionServiceTest extends PumaTest {

	@Autowired
	private DocumentVersionService documentVersionService;

	@Autowired
	private PumaFileService pumaFileService;

	@Autowired
	private JournalService journalService;

	@Test
	public void contextLoads() throws Exception {
		Assert.assertNotNull(this.documentVersionService);
	}

	private DocumentVersion createDocumentVersion(String sourceId) {
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

		return documentVersion;
	}

	@Test
	public void createAndRetrieveDocumentVersion() throws Exception {
		DocumentVersion documentVersion = this.createDocumentVersion("test1");

		documentVersionService.save(documentVersion);

		// Get documentVersion with the same Id
		DocumentVersion result = documentVersionService.getById(documentVersion.getId());
		Assert.assertEquals(DocumentType.PUBLICATION, result.getDocument().getDocumentType());
	}

	private void addFullTextFile(DocumentVersion documentVersion, String filePath) throws Exception {
		PumaFile pumaFile = new PumaFile();
		pumaFile.setDocumentVersion(documentVersion);
		pumaFile.setDocumentType(PumaFileType.EXTRACTED_FULL_TEXT);
		pumaFile.setOriginUrl(documentVersion.getSources().get(0).getSourceId() + ".txt");
		pumaFile.setFilePath(filePath);
		pumaFileService.saveDBOnly(pumaFile);
	}

	private void createDocumentsForIndexationTests() throws Exception {
		DocumentVersion d1 = this.createDocumentVersion("test1");
		Calendar c1 = new GregorianCalendar(2017, Calendar.OCTOBER, 8);
		d1.setIndexationDate(c1.getTime());
		documentVersionService.save(d1);
		this.addFullTextFile(d1, "/d1");

		DocumentVersion d1b = this.createDocumentVersion("test1b");
		Calendar c1b = new GregorianCalendar(2017, Calendar.OCTOBER, 8);
		d1b.setIndexationDate(c1b.getTime());
		documentVersionService.save(d1b);
		this.addFullTextFile(d1b, null);

		DocumentVersion d2 = this.createDocumentVersion("test2");
		Calendar c2 = new GregorianCalendar(2017, Calendar.OCTOBER, 9);
		d2.setIndexationDate(c2.getTime());
		documentVersionService.save(d2);
		this.addFullTextFile(d2, "/d2");

		DocumentVersion d2b = this.createDocumentVersion("test2b");
		Calendar c2b = new GregorianCalendar(2017, Calendar.OCTOBER, 9);
		d2b.setIndexationDate(c2b.getTime());
		documentVersionService.save(d2b);

		DocumentVersion d3 = this.createDocumentVersion("test3");
		Calendar c3 = new GregorianCalendar(2017, Calendar.OCTOBER, 11);
		d3.setIndexationDate(c3.getTime());
		documentVersionService.save(d3);
		this.addFullTextFile(d3, "/d3");
	}

	@Test
	public void testRetrievalByIndexationDate() throws Exception {
		this.createDocumentsForIndexationTests();

		Calendar calendar = new GregorianCalendar(2017, Calendar.OCTOBER, 10);
		Date date = calendar.getTime();

		List<Long> documentVersionIds = this.documentVersionService.getIdsForIndexationWithIndexationDateBefore(date);
		Assert.assertEquals(2, documentVersionIds.size());
	}

	@Test
	public void testRetrievalAllForIndexation() throws Exception {
		this.createDocumentsForIndexationTests();

		List<Long> documentVersionIds = this.documentVersionService.getAllIdsForIndexation();
		Assert.assertEquals(3, documentVersionIds.size());
	}

	@Test
	public void testRetrievalRemainingForIndexation() throws Exception {
		this.createDocumentsForIndexationTests();

		List<DocumentVersion> documentVersions = this.documentVersionService.getAll();

		for (DocumentVersion documentVersion : documentVersions) {
			documentVersion.setIndexationDate(new Date());
			this.documentVersionService.save(documentVersion);
		}

		List<Long> documentVersionIds = this.documentVersionService.getRemainingIdsForIndexation();
		Assert.assertEquals(0, documentVersionIds.size());
	}

}
