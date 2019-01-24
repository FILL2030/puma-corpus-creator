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
package eu.ill.puma.importermanager.importer;

import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.core.utils.ResourceLoader;
import eu.ill.puma.importermanager.ImporterManager;
import eu.ill.puma.importermanager.utils.ImporterCreator;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionEntityOrigin;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.importer.Importer;
import eu.ill.puma.persistence.service.analysis.DocumentVersionEntityOriginService;
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

import java.io.IOException;
import java.util.Arrays;
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
@Ignore
public class ImporterUpdateOperationTest {

	@Autowired
	private DocumentVersionService documentVersionService;

	@Autowired
	private DocumentVersionEntityOriginService entityOriginService;

	@Autowired
	private ImporterManager importerManager;

	@Autowired
	private ImporterCreator importerCreator;

	@Test
	public void testNormalImport() {
		try {
			Importer importer = this.importerCreator.createImporter("wos");

			BaseDocument[] importerDocuments = ResourceLoader.readType("private/ImporterDocuments1.json", BaseDocument[].class);

			List<BaseDocument> documents = Arrays.asList(importerDocuments);

			List<DocumentVersion> integrated = this.importerManager.convertAndIntegrateDocuments(documents, importer, false, false);

			List<DocumentVersion> documentVersions = this.documentVersionService.getAll();

			Assert.assertEquals(documents.size(), integrated.size());
			Assert.assertEquals(documents.size(), documentVersions.size());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testIdenticalImport() {
		try {
			Importer importer = this.importerCreator.createImporter("wos");

			BaseDocument[] importerDocuments = ResourceLoader.readType("private/ImporterDocuments1.json", BaseDocument[].class);

			List<BaseDocument> documents = Arrays.asList(importerDocuments);

			// First integration
			List<DocumentVersion> integrated = this.importerManager.convertAndIntegrateDocuments(documents, importer, false, false);
			List<DocumentVersion> integrated2 = this.importerManager.convertAndIntegrateDocuments(documents, importer, false, false);

			Assert.assertEquals(0, integrated2.size());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testIdenticalImportWithForce() {
		try {
			Importer importer = this.importerCreator.createImporter("wos");

			BaseDocument[] importerDocuments = ResourceLoader.readType("private/ImporterDocuments1.json", BaseDocument[].class);

			List<BaseDocument> documents = Arrays.asList(importerDocuments);

			// First integration
			List<DocumentVersion> integrated = this.importerManager.convertAndIntegrateDocuments(documents, importer, true, false);
			List<DocumentVersionEntityOrigin> entityOrigins = this.entityOriginService.getAll();

			List<DocumentVersion> integrated2 = this.importerManager.convertAndIntegrateDocuments(documents, importer, true, false);
			List<DocumentVersionEntityOrigin> entityOrigins2 = this.entityOriginService.getAll();

			Assert.assertEquals(entityOrigins.size(), entityOrigins2.size());
			Assert.assertEquals(integrated.size(), integrated2.size());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDifferentImportWithForce() {
		try {
			Importer importer = this.importerCreator.createImporter("wos");

			BaseDocument[] importerDocuments1 = ResourceLoader.readType("private/ImporterDocuments1.json", BaseDocument[].class);
			BaseDocument[] importerDocuments2 = ResourceLoader.readType("private/ImporterDocuments2.json", BaseDocument[].class);

			List<BaseDocument> documents1 = Arrays.asList(importerDocuments1);
			List<BaseDocument> documents2 = Arrays.asList(importerDocuments2);

			// First integration
			List<DocumentVersion> integrated1 = this.importerManager.convertAndIntegrateDocuments(documents1, importer, true, false);
			List<DocumentVersionEntityOrigin> entityOrigins1 = this.entityOriginService.getAll();

			List<DocumentVersion> integrated2 = this.importerManager.convertAndIntegrateDocuments(documents2, importer, true, false);
			List<DocumentVersionEntityOrigin> entityOrigins2 = this.entityOriginService.getAll();

			Assert.assertEquals(entityOrigins1.size() + 1, entityOrigins2.size());
			Assert.assertEquals(integrated1.size(), integrated2.size());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
