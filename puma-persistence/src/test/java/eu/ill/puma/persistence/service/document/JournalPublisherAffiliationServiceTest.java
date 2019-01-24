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
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class JournalPublisherAffiliationServiceTest extends PumaTest {

	@Autowired
	private JournalPublisherAffiliationService journalPublisherAffiliationService;

	@Autowired
	private DocumentVersionService documentVersionService;

	@Autowired
	private JournalService journalService;

	@Autowired
	private PublisherService publisherService;


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

	public Journal createAndSaveJournal(String name) {
		Journal journal = new Journal();
		journal.setName(name);

		journal = journalService.save(journal);

		return journal;
	}

	public Publisher createAndSavePublisher(String name) {
		Publisher publisher = new Publisher();
		publisher.setName(name);
		publisher.setAddress("abcdef");

		publisher = publisherService.save(publisher);

		return publisher;
	}


	public JournalPublisherAffiliation createDocumentWithAffiliation(String sourceId, String journalName, String laboName) throws Exception {
		DocumentVersion documentVersion = createAndSaveDocumentVersion(sourceId);
		Journal journal = createAndSaveJournal(journalName);
		Publisher publisher = createAndSavePublisher(laboName);

		// Create affiliation
		JournalPublisherAffiliation journalPublisherAffiliation = new JournalPublisherAffiliation();
		journalPublisherAffiliation.setJournal(journal);
		journalPublisherAffiliation.setPublisher(publisher);
		journalPublisherAffiliation.setDocumentVersion(documentVersion);

		journalPublisherAffiliation = journalPublisherAffiliationService.save(journalPublisherAffiliation);
		documentVersion.addJournalPublisherAffiliation(journalPublisherAffiliation);

		// Save document version again
		documentVersionService.save(documentVersion);

		return journalPublisherAffiliation;
	}

	@Test
	public void testCreationAndRetrieval() throws Exception {
		JournalPublisherAffiliation journalPublisherAffiliation = this.createDocumentWithAffiliation("source1", "journalName", "publisherName");

		DocumentVersion documentVersion = journalPublisherAffiliation.getDocumentVersion();
		Journal journal = journalPublisherAffiliation.getJournal();
		Publisher publisher = journalPublisherAffiliation.getPublisher();

		Assert.assertNotNull(documentVersion.getId());
		Assert.assertNotNull(journalPublisherAffiliation.getId());

		// Retrieve
		JournalPublisherAffiliation integratedJournalPublisherAffiliation1 = journalPublisherAffiliationService.getAllForDocumentVersion(documentVersion).get(0);
		Assert.assertEquals(journal.getId(), integratedJournalPublisherAffiliation1.getJournal().getId());
		Assert.assertEquals(journal, integratedJournalPublisherAffiliation1.getJournal());

		JournalPublisherAffiliation integratedJournalPublisherAffiliation2 = journalPublisherAffiliationService.getAllForJournal(journal).get(0);
		Assert.assertEquals(publisher.getId(), integratedJournalPublisherAffiliation2.getPublisher().getId());
		Assert.assertEquals(publisher, integratedJournalPublisherAffiliation2.getPublisher());

		JournalPublisherAffiliation integratedJournalPublisherAffiliation3 = journalPublisherAffiliationService.getAllForPublisher(publisher).get(0);
		Assert.assertEquals(journal.getId(), integratedJournalPublisherAffiliation3.getJournal().getId());
		Assert.assertEquals(journal, integratedJournalPublisherAffiliation3.getJournal());
	}

	@Test
	public void testMultipleDocumentVersions() throws Exception {
		JournalPublisherAffiliation journalPublisherAffiliation1 = this.createDocumentWithAffiliation("source1", "journalName", "laboName");
		JournalPublisherAffiliation journalPublisherAffiliation2 = this.createDocumentWithAffiliation("source2", "journalName", "laboName2");

		List<JournalPublisherAffiliation> affiliationsForDocumentVersion1 = this.journalPublisherAffiliationService.getAllForDocumentVersion(journalPublisherAffiliation1.getDocumentVersion());
		Assert.assertEquals(1, affiliationsForDocumentVersion1.size());
		Assert.assertEquals(journalPublisherAffiliation1.getJournal(), affiliationsForDocumentVersion1.get(0).getJournal());
		Assert.assertEquals(journalPublisherAffiliation2.getJournal(), affiliationsForDocumentVersion1.get(0).getJournal());
		Assert.assertEquals(journalPublisherAffiliation1.getPublisher(), affiliationsForDocumentVersion1.get(0).getPublisher());

		List<JournalPublisherAffiliation> affiliationsForDocumentVersion2 = this.journalPublisherAffiliationService.getAllForDocumentVersion(journalPublisherAffiliation2.getDocumentVersion());
		Assert.assertEquals(1, affiliationsForDocumentVersion2.size());
		Assert.assertEquals(journalPublisherAffiliation1.getJournal(), affiliationsForDocumentVersion2.get(0).getJournal());
		Assert.assertEquals(journalPublisherAffiliation2.getJournal(), affiliationsForDocumentVersion2.get(0).getJournal());
		Assert.assertEquals(journalPublisherAffiliation2.getPublisher(), affiliationsForDocumentVersion2.get(0).getPublisher());

		List<JournalPublisherAffiliation> affiliationsForJournal = this.journalPublisherAffiliationService.getAllForJournal(journalPublisherAffiliation1.getJournal());
		Assert.assertEquals(2, affiliationsForJournal.size());

		List<JournalPublisherAffiliation> affiliationsForPublisher = this.journalPublisherAffiliationService.getAllForPublisher(journalPublisherAffiliation1.getPublisher());
		Assert.assertEquals(1, affiliationsForPublisher.size());
	}


	@Test
	public void testObsolete() throws Exception {
		JournalPublisherAffiliation journalPublisherAffiliation = this.createDocumentWithAffiliation("dfsdsf", "fdsfs", "jdskfkls");

		Long documentVersionId = journalPublisherAffiliation.getDocumentVersion().getId();

		journalPublisherAffiliation.setObsolete(true);
		journalPublisherAffiliationService.save(journalPublisherAffiliation);

		DocumentVersion documentVersion = documentVersionService.getByIdWithAllEntities(documentVersionId, true);

		Assert.assertEquals(0, documentVersion.getJournalPublisherAffiliations().size());

	}
}
