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
package eu.ill.puma.persistence.repository;

import eu.ill.puma.persistence.PumaTest;
import eu.ill.puma.persistence.domain.document.Document;
import eu.ill.puma.persistence.domain.document.enumeration.DocumentType;
import eu.ill.puma.persistence.repository.document.DocumentRepository;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public class DocumentRepositoryTest extends PumaTest {

	private static final Logger log = LoggerFactory.getLogger(DocumentRepositoryTest.class);

	@Autowired
	private DocumentRepository repository;

	@Test
	public void contextLoads() throws Exception {
		Assert.assertNotNull(this.repository);
	}

	@Test
	public void sampleTestCase() {
		Document document = new Document();
		document.setDocumentType(DocumentType.PUBLICATION);
		document = repository.persist(document);

		Document result = repository.getById(document.getId());
		Assert.assertEquals(DocumentType.PUBLICATION, result.getDocumentType());
	}

	@Test
	public void findByTypeTest() {
		List<Document> docList = repository.getByType(DocumentType.PUBLICATION);

		log.info("size : " + docList.size());
		docList.forEach((currentDoc) -> {
			log.info("id : " + currentDoc.getId() + " type : " + currentDoc.getDocumentType());
			Assert.assertEquals(DocumentType.PUBLICATION, currentDoc.getDocumentType());
		});
	}
}
