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

import eu.ill.puma.core.utils.throttle.Throttle;
import eu.ill.puma.importermanager.ImporterManager;
import eu.ill.puma.persistence.domain.importer.Importer;
import eu.ill.puma.persistence.domain.importer.ImporterOperation;
import eu.ill.puma.persistence.domain.importer.ImporterOperationStatus;
import eu.ill.puma.persistence.service.importer.ImporterService;
import eu.ill.puma.taskmanager.TaskManager;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( locations={
		"classpath:/applicationContext-test.xml"
})
@TestExecutionListeners( {
		DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ImporterOperationTest {

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private ImporterService importerService;

	@Autowired
	private ImporterManager importerManager;

	@Before
	public void beforeMethod() {
		org.junit.Assume.assumeTrue(System.getenv("PUMA_PCC_TEST_IMPORTER_URL") != null);

		Importer importer = this.createImporter();
		org.junit.Assume.assumeTrue(this.importerManager.isHealthy(importer));

		Throttle.setDefaultDelay(0);
	}

	@Test
	public void contextLoads() throws Exception {
		Assert.assertNotNull(this.taskManager);
		Assert.assertTrue(this.taskManager.isTest);
		Assert.assertNotNull(this.importerManager);
	}

	public Importer createImporter() {
		// Create importer
		String importerName = "Test Importer";
		String shortName = "TEST";
		String importerUrl = System.getenv("PUMA_PCC_TEST_IMPORTER_URL");

		Importer importer = new Importer();
		importer.setName(importerName);
		importer.setShortName(shortName);
		importer.setUrl(importerUrl);
		importerService.save(importer);

		return importerService.getById(importer.getId());
	}

	public ImporterOperation createImportAllOperation(Importer importer) {
		ImporterOperation importerOperation = new ImporterOperation();

		return importerService.addImporterOperation(importer, importerOperation);
	}

	public ImporterOperation createImportListOperation(Importer importer) {
		ImporterOperation importerOperation = new ImporterOperation();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ids", Arrays.asList("10", "20", "30", "40", "50", "60", "70", "80", "90"));

		importerOperation.setParams(params);

		return importerService.addImporterOperation(importer, importerOperation);
	}

	public ImporterOperation createSearchOperation(Importer importer) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("modulo", "3");

		ImporterOperation importerOperation = new ImporterOperation();
		importerOperation.setParams(params);

		return importerService.addImporterOperation(importer, importerOperation);
	}

	public void performAndCancelImport(ImporterOperation importerOperation) throws Exception {
		// Ensure asynchronous running
		this.taskManager.setAsync(true);

		// Start the import
		this.importerManager.performImport(importerOperation);

		// Wait for confirmation of running
		while ((!importerOperation.getStatus().equals(ImporterOperationStatus.RUNNING) || importerOperation.getCursor() == null) && !importerOperation.getStatus().equals(ImporterOperationStatus.FAILED)) {
			Thread.sleep(50);
		}


		Assert.assertNotEquals(ImporterOperationStatus.FAILED, importerOperation.getStatus());

		// Send cancel to data source manager
		this.importerManager.performCancel(importerOperation);

		Thread.sleep(1000);
	}

	@Test
	public void testSearchImport() {
		// Ensure synchronous running
		this.taskManager.setAsync(false);

		// Create importer
		Importer importer = this.createImporter();

		// Create search operation
		ImporterOperation importerOperation = this.createSearchOperation(importer);

		// Send operation to data source manager
		try {
			this.importerManager.performImport(importerOperation);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Verify import is terminated
		Assert.assertEquals(ImporterOperationStatus.TERMINATED, importerOperation.getStatus());
		Assert.assertEquals(importerOperation.getTotalDocumentCount(), importerOperation.getDocumentsReceived());
		Assert.assertEquals(importerOperation.getTotalDocumentCount(), importerOperation.getDocumentsIntegrated());
	}

	@Test
	public void testAllImport() {
		// Ensure synchronous running
		this.taskManager.setAsync(false);

		// Create importer
		Importer importer = this.createImporter();

		// Create search operation
		ImporterOperation importerOperation = this.createImportAllOperation(importer);

		// Send operation to data source manager
		try {
			this.importerManager.performImport(importerOperation);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Verify import is terminated
		Assert.assertEquals(ImporterOperationStatus.TERMINATED, importerOperation.getStatus());
		Assert.assertEquals(importerOperation.getTotalDocumentCount(), importerOperation.getDocumentsReceived());
		Assert.assertEquals(importerOperation.getTotalDocumentCount(), importerOperation.getDocumentsIntegrated());
	}

	@Test
	public void testIdsImport() {
		// Ensure synchronous running
		this.taskManager.setAsync(false);

		// Create importer
		Importer importer = this.createImporter();

		// Create search operation
		ImporterOperation importerOperation = this.createImportListOperation(importer);

		// Send operation to data source manager
		try {
			this.importerManager.performImport(importerOperation);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Verify import is terminated
		Assert.assertEquals(ImporterOperationStatus.TERMINATED, importerOperation.getStatus());
		Assert.assertEquals(importerOperation.getTotalDocumentCount(), importerOperation.getDocumentsReceived());
		Assert.assertEquals(importerOperation.getTotalDocumentCount(), importerOperation.getDocumentsIntegrated());
	}

	@Test
	public void testCancelImport() throws Exception {
		// Create importer
		Importer importer = this.createImporter();

		// Create search operation
		ImporterOperation importerOperation = this.createImportAllOperation(importer);

		// Start then cancel import
		this.performAndCancelImport(importerOperation);

		// Verify cancelled
		Assert.assertEquals(ImporterOperationStatus.CANCELLED, importerOperation.getStatus());
	}

	@Test
	public void testRecoverImport() throws Exception {
		// Create importer
		Importer importer = this.createImporter();

		// Create search operation
		ImporterOperation importerOperation = this.createImportAllOperation(importer);

		// Start then cancel import
		this.performAndCancelImport(importerOperation);

		Thread.sleep(1000);

		Assert.assertEquals(ImporterOperationStatus.CANCELLED, importerOperation.getStatus());

		// Force status back to running
		importerOperation.setStatus(ImporterOperationStatus.RUNNING);
		this.importerService.updateOperation(importerOperation);

		// Perform recovery operation
		this.taskManager.setAsync(false);
		this.importerManager.recoverRunningOperations();

		// Get operation from DB
		ImporterOperation recoveredOperation = this.importerService.getOperationById(importerOperation.getId());

		// Verify terminated
		Assert.assertEquals(ImporterOperationStatus.TERMINATED, recoveredOperation.getStatus());
		Assert.assertEquals(recoveredOperation.getTotalDocumentCount(), recoveredOperation.getDocumentsReceived());
		Assert.assertEquals(recoveredOperation.getTotalDocumentCount(), recoveredOperation.getDocumentsIntegrated());
	}

}
