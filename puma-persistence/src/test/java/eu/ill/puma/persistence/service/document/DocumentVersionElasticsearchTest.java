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

import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.core.domain.importer.ImporterInfo;
import eu.ill.puma.core.utils.ResourceLoader;
import eu.ill.puma.persistence.PumaTest;
import eu.ill.puma.persistence.domain.importer.Importer;
import eu.ill.puma.persistence.domain.indexer.IndexedDocument;
import eu.ill.puma.persistence.esrepository.BulkIndexationResult;
import eu.ill.puma.persistence.esrepository.IndexationResult;
import eu.ill.puma.persistence.utils.ImporterCreator;
import org.elasticsearch.action.DocWriteResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class DocumentVersionElasticsearchTest extends PumaTest {

	private static final Logger log = LoggerFactory.getLogger(DocumentVersionElasticsearchTest.class);

	@Autowired
	private DocumentVersionService service;

	@Autowired
	private ImporterCreator importerCreator;

	@Before
	public void importData() throws Exception {
		Importer importer = this.importerCreator.createImporter("wos");

		BaseDocument[] importerDocuments = ResourceLoader.readType("ImporterDocuments.json", BaseDocument[].class);
		ImporterInfo importerInfo = ResourceLoader.readType("ImporterInfo.json", ImporterInfo.class);

		for (BaseDocument importerDocument : importerDocuments) {
			this.importerCreator.importDocument(importerDocument, importer, importerInfo);
		}

		// Reinitialise index
		this.service.removeIndex();
	}

	@Test
	public void testUpdateIndex() throws Exception {
		IndexationResult<IndexedDocument> result1 = this.service.indexDocumentVersionWithId(1l);
		IndexationResult<IndexedDocument> result2 = this.service.indexDocumentVersionWithId(1l);

		Assert.assertTrue(!result1.hasError());
		Assert.assertTrue(!result2.hasError());

		Assert.assertTrue(result1.getIndexResponse().getResult() == DocWriteResponse.Result.CREATED);
		Assert.assertTrue(result2.getIndexResponse().getResult() == DocWriteResponse.Result.UPDATED);
	}

	@Test
	public void testDifferentIndexes() throws Exception {
		IndexationResult<IndexedDocument> result1 = this.service.indexDocumentVersionWithId(1l);
		IndexationResult<IndexedDocument> result2 = this.service.indexDocumentVersionWithId(2l);

		Assert.assertTrue(!result1.hasError());
		Assert.assertTrue(!result2.hasError());

		Assert.assertTrue(result1.getIndexResponse().getResult() == DocWriteResponse.Result.CREATED);
		Assert.assertTrue(result2.getIndexResponse().getResult() == DocWriteResponse.Result.CREATED);
	}

	@Test
	public void testBulkIndex() throws Exception {
		List<Long> indexes = new ArrayList<>();
		indexes.add(1l);
		indexes.add(2l);

		BulkIndexationResult<IndexedDocument> result = this.service.indexDocumentVersionsWithIds(indexes);

		Assert.assertTrue(!result.hasError());
		Assert.assertEquals(indexes.size(), result.getEntities().size());
	}
}
