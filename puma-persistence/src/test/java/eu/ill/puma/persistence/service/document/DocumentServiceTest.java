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
import eu.ill.puma.persistence.domain.document.enumeration.DocumentType;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class DocumentServiceTest extends PumaTest {

	private static final Logger log = LoggerFactory.getLogger(DocumentServiceTest.class);

	@Autowired
	private DocumentService documentService;

	@Test
	public void contextLoads() throws Exception {
		Assert.assertNotNull(this.documentService);
	}

	@Test
	public void createAndRetrieveDocument() {
		Document document = new Document();
		document.setDocumentType(DocumentType.PUBLICATION);
		documentService.save(document);

		// Get document with the same Id
		Document result = documentService.getById(document.getId());
		Assert.assertEquals(DocumentType.PUBLICATION, result.getDocumentType());
	}

	@Test
	public void createAndRetrieveAllDocuments() {
		Document document1 = new Document();
		document1.setDocumentType(DocumentType.PUBLICATION);
		documentService.save(document1);

		Document document2 = new Document();
		document2.setDocumentType(DocumentType.LETTER);
		documentService.save(document2);

		List<Document> documents = documentService.getAll();
		Assert.assertEquals(2, documents.size());
	}
}
