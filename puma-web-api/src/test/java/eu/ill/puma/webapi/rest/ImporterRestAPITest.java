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
package eu.ill.puma.webapi.rest;

import eu.ill.puma.persistence.domain.importer.ImporterOperationStatus;
import eu.ill.puma.persistence.service.importer.ImporterService;
import eu.ill.puma.webapi.rest.importer.RestImporter;
import eu.ill.puma.webapi.rest.importer.RestImporterOperation;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ImporterRestAPITest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ImporterService importerService;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public RestImporter[] getImporters() {
		ResponseEntity<RestImporter[]> response = restTemplate.getForEntity("/api/v1/importers", RestImporter[].class);

		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());

		return response.getBody();
	}

	public RestImporter addImporter(String importerName, String shortName, String importerUrl) {
		RestImporter newImporter = new RestImporter(importerName, shortName, importerUrl);

		// Create entity to post
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<RestImporter> entity = new HttpEntity<RestImporter>(newImporter, headers);

		ResponseEntity<RestImporter> response = restTemplate.postForEntity("/api/v1/importers", entity, RestImporter.class);

		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());
		Assert.assertEquals(importerName, response.getBody().getName());
		Assert.assertEquals(importerUrl, response.getBody().getUrl());
		Assert.assertNotNull(response.getBody().getId());

		return response.getBody();
	}

	public void updateImporter(RestImporter importer) {
		// Create entity to put
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<RestImporter> entity = new HttpEntity<RestImporter>(importer, headers);

		restTemplate.put("/api/v1/importers/" + importer.getId(), importer, RestImporter.class);
	}

	public void deleteImporter(RestImporter importer) {
		restTemplate.delete("/api/v1/importers/" + importer.getId());
	}

	public RestImporterOperation[] getOperations(RestImporter importer) {
		ResponseEntity<RestImporterOperation[]> response = restTemplate.getForEntity("/api/v1/importers/" + importer.getId() + "/operations", RestImporterOperation[].class);

		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());

		return response.getBody();
	}

	public RestImporterOperation addOperation(RestImporter importer, Map<String, Object> params) {
		RestImporterOperation newImporterOperation = new RestImporterOperation(params);

		// Create entity to post
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<RestImporterOperation> entity = new HttpEntity<RestImporterOperation>(newImporterOperation, headers);

		ResponseEntity<RestImporterOperation> response = restTemplate.postForEntity("/api/v1/importers/" + importer.getId() + "/operations", entity, RestImporterOperation.class);

		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());
		RestImporterOperation integratedOperation = response.getBody();

		Assert.assertEquals(params, integratedOperation.getParams());
		Assert.assertNotNull(integratedOperation.getId());

		return integratedOperation;
	}

	public RestImporterOperation addOperation(RestImporter importer, List<String> documentIds) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ids", documentIds);

		RestImporterOperation newImporterOperation = new RestImporterOperation(params);

		// Create entity to post
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<RestImporterOperation> entity = new HttpEntity<RestImporterOperation>(newImporterOperation, headers);

		ResponseEntity<RestImporterOperation> response = restTemplate.postForEntity("/api/v1/importers/" + importer.getId() + "/operations", entity, RestImporterOperation.class);

		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());
		RestImporterOperation integratedOperation = response.getBody();

		Assert.assertEquals(params, integratedOperation.getParams());
		Assert.assertNotNull(integratedOperation.getId());

		return integratedOperation;
	}

	public void deleteOperation(RestImporterOperation operation) {
		restTemplate.delete("/api/v1/operations/" + operation.getId());
	}

	public RestImporterOperation getOperation(Long operationId) {
		ResponseEntity<RestImporterOperation> response =  restTemplate.getForEntity("/api/v1/operations/" + operationId, RestImporterOperation.class);
		return response.getBody();
	}


	@Test
	public void testAddImporter() {
		String importerName = "Test Importer";
		String shortName = "TEST";
		String importerUrl = "https://a.b.c";

		this.addImporter(importerName, shortName, importerUrl);
		RestImporter[] importers = this.getImporters();

		// Verify length of importers
		Assert.assertEquals(1, importers.length);

		// Verify importer content
		RestImporter importer = importers[0];

		// Verify content
		Assert.assertEquals(importerName, importer.getName());
		Assert.assertEquals(shortName, importer.getShortName());
		Assert.assertEquals(importerUrl, importer.getUrl());
	}

	@Test
	public void testUpdateImporter() {
		String importerName = "Test Importer";
		String shortName = "TEST";
		String importerUrl = "https://a.b.c";

		// Create importer
		RestImporter importer = this.addImporter(importerName, shortName, importerUrl);

		String anotherName = "Another name";
		String anotherShortName = "Another short name";
		String anotherUrl = "http://e.f.g";
		importer.setName(anotherName);
		importer.setShortName(anotherShortName);
		importer.setUrl(anotherUrl);

		// Update importer
		this.updateImporter(importer);

		// Get importers
		RestImporter[] importers = this.getImporters();

		// Verify length of importers
		Assert.assertEquals(1, importers.length);

		RestImporter updatedImporter = importers[0];

		// Verify update
		Assert.assertEquals(anotherName, updatedImporter.getName());
		Assert.assertEquals(anotherShortName, updatedImporter.getShortName());
		Assert.assertEquals(anotherUrl, updatedImporter.getUrl());
	}

	@Test
	public void testDeleteImporter() {
		String importerName = "Test Importer";
		String shortName = "TEST";
		String importerUrl = "https://a.b.c";

		// Create importer
		RestImporter importer = this.addImporter(importerName, shortName, importerUrl);

		// Delete the importer
		this.deleteImporter(importer);

		// Get importers
		RestImporter[] importers = this.getImporters();

		// Verify length of importers
		Assert.assertEquals(0, importers.length);
	}

	@Test
	public void testCreateParamsOperation() {
		String importerName = "Test Importer";
		String shortName = "TEST";
		String importerUrl = "https://a.b.c";

		// Create importer
		RestImporter importer = this.addImporter(importerName, shortName, importerUrl);

		Map<String, Object> params = new HashMap<>();
		params.put("query", "abcdef");
		params.put("filter", "12345");

		// Add operation to importer
		this.addOperation(importer, params);

		// Get all operations
		RestImporterOperation[] operations = this.getOperations(importer);

		// Verify length of importers
		Assert.assertEquals(1, operations.length);

		// Verify importer content
		RestImporterOperation operation = operations[0];

		// Verify content
		Assert.assertEquals(params, operation.getParams());
	}

	@Test
	public void testCreateListIdsOperation() {
		String importerName = "Test Importer";
		String shortName = "TEST";
		String importerUrl = "https://a.b.c";

		// Create importer
		RestImporter importer = this.addImporter(importerName, shortName, importerUrl);

		List<String> documentIds = new ArrayList<>(Arrays.asList("1", "2", "3", "4", "5", "6"));
		Map<String, Object> params = new HashMap<>();
		params.put("ids", documentIds);

		// Add operation to importer
		this.addOperation(importer, documentIds);

		// Get all operations
		RestImporterOperation[] operations = this.getOperations(importer);

		// Verify length of importers
		Assert.assertEquals(1, operations.length);

		// Verify importer content
		RestImporterOperation operation = operations[0];

		// Verify content
		Assert.assertEquals(params, operation.getParams());
	}

	@Test
	public void testCancelOperation() throws Exception {
		String importerName = "Test Importer";
		String shortName = "TEST";
		String importerUrl = "https://a.b.c";

		// Create importer
		RestImporter importer = this.addImporter(importerName, shortName, importerUrl);

		Map<String, Object> params = new HashMap<>();
		params.put("query", "abcdef");
		params.put("filter", "12345");

		// Add operation to importer
		RestImporterOperation integratedOperation = this.addOperation(importer, params);

		// Delete the importer operation
		this.deleteOperation(integratedOperation);

		ImporterOperationStatus status;
		do {
			status = this.importerService.getOperationById(integratedOperation.getId()).getStatus();
			Thread.sleep(100);
		} while (status.equals(ImporterOperationStatus.RUNNING) || status.equals(ImporterOperationStatus.PENDING));

		// Get all operations
		RestImporterOperation[] operations = this.getOperations(importer);

		// Verify length of importers
		Assert.assertEquals(0, operations.length);
	}

	@Test
	public void testGetOperation() throws Exception {
		String importerName = "Test Importer";
		String shortName = "TEST";
		String importerUrl = "https://a.b.c";

		// Create importer
		RestImporter importer = this.addImporter(importerName, shortName, importerUrl);

		Map<String, Object> params = new HashMap<>();
		params.put("query", "abcdef");
		params.put("filter", "12345");

		// Add operation to importer
		RestImporterOperation integratedOperation = this.addOperation(importer, params);

		// Delete the importer operation
		this.deleteOperation(integratedOperation);

		ImporterOperationStatus status;
		do {
			status = this.importerService.getOperationById(integratedOperation.getId()).getStatus();
			Thread.sleep(100);
		} while (status.equals(ImporterOperationStatus.RUNNING) || status.equals(ImporterOperationStatus.PENDING));

		// Get all operations
		RestImporterOperation[] operations = this.getOperations(importer);

		// Verify length of importers
		Assert.assertEquals(0, operations.length);

		// Get the importer operation
		RestImporterOperation deletedOperation = this.getOperation(integratedOperation.getId());
		Assert.assertEquals(ImporterOperationStatus.CANCELLED, deletedOperation.getStatus());
	}

}
