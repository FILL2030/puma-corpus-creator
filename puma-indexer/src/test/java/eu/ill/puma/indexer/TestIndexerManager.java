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
package eu.ill.puma.indexer;


import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.core.utils.ResourceLoader;
import eu.ill.puma.indexer.manager.IndexationState;
import eu.ill.puma.indexer.manager.IndexerManager;
import eu.ill.puma.persistence.domain.importer.Importer;
import eu.ill.puma.persistence.service.converterV2.exception.PumaDocumentConversionException;
import eu.ill.puma.persistence.service.converterV2.DocumentConverter;
import eu.ill.puma.persistence.service.document.DocumentVersionService;
import eu.ill.puma.persistence.service.document.PumaFileService;
import eu.ill.puma.persistence.service.importer.ImporterService;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:/applicationContext-test.xml"
})
@TestExecutionListeners({
		DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TestIndexerManager {

	@Autowired
	private IndexerManager indexerManager;

	@Autowired
	private DocumentVersionService documentVersionService;

	@Autowired
	private DocumentConverter documentConverter;

	@Autowired
	private ImporterService importerService;

	@Before
	public void beforceTest() throws PumaFileService.PumaFilePersistenceException, PumaDocumentConversionException, IOException {
		Importer importer = new Importer();
		importer.setUrl("http://fjdsfjkhf");
		importer.setShortName("wos");
		importer.setName("wos importer");
		importerService.save(importer);

		BaseDocument baseDocument = ResourceLoader.readType("ImporterDocument1.json", BaseDocument.class);
		documentConverter.convert(baseDocument, "wos");

		baseDocument = ResourceLoader.readType("ImporterDocument2.json", BaseDocument.class);
		documentConverter.convert(baseDocument, "wos");

		baseDocument = ResourceLoader.readType("ImporterDocument3.json", BaseDocument.class);
		documentConverter.convert(baseDocument, "wos");

		baseDocument = ResourceLoader.readType("ImporterDocument4.json", BaseDocument.class);
		documentConverter.convert(baseDocument, "wos");
	}

	@Test
	public void testTest() {
		Assert.assertTrue(true);
		Assert.assertNotNull(this.indexerManager);
		Assert.assertEquals(4, this.documentVersionService.getAll().size());
	}

	@Test
	public void testSingleDocument() throws InterruptedException {
		Assert.assertEquals(IndexationState.Pending, indexerManager.getState());

		this.indexerManager.indexAsync(1L);
		Assert.assertEquals(IndexationState.Running, indexerManager.getState());
		Thread.sleep(3000);

		Assert.assertEquals(IndexationState.Pending, indexerManager.getState());
	}

	@Test
	public void testPause() throws InterruptedException {
		Assert.assertEquals(IndexationState.Pending, indexerManager.getState());

		//pause
		this.indexerManager.pause();
		Assert.assertEquals(IndexationState.Paused, indexerManager.getState());

		//check indexing is stopped
		this.indexerManager.indexAsync(1L);
		Thread.sleep(1000);

		Assert.assertEquals(IndexationState.Paused, indexerManager.getState());

		//resume
		this.indexerManager.resume();
		this.indexerManager.indexAsync(1L);
	}

	@Test
	public void testBulk() throws InterruptedException {
		Assert.assertEquals(IndexationState.Pending, indexerManager.getState());

		List<Long> ids = Arrays.asList(1L,2L, 3L);

		this.indexerManager.indexAsync(ids);
		Assert.assertEquals(IndexationState.Running, indexerManager.getState());
		Thread.sleep(5000);

		Assert.assertEquals(IndexationState.Pending, indexerManager.getState());

	}

//	@Test
//	public void testReIndex() throws InterruptedException {
//		Assert.assertEquals(IndexationState.Pending, indexerManager.getState());
//
//		this.indexerManager.indexAllAsync();
//		Assert.assertEquals(IndexationState.Pending, indexerManager.getState());
//
//		this.indexerManager.indexAsync(1L);
//		this.indexerManager.indexAsync(2L);
//		this.indexerManager.indexAsync(3L);
//		this.indexerManager.indexAsync(4L);
//
//		Thread.sleep(3000);
//		Assert.assertEquals(IndexationState.Pending, indexerManager.getState());
//		Assert.assertEquals(4L, this.indexerManager.getCompletedTask().longValue());
//
//
//		this.indexerManager.indexAllAsync();
//		Thread.sleep(3000);
//
//		Assert.assertEquals(4L, this.indexerManager.getIndexedDocument().longValue());
//		Assert.assertEquals(5L, this.indexerManager.getCompletedTask().longValue());
//
//	}
}
