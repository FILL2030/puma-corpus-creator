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
package eu.ill.puma.persistence.service.importer;

import eu.ill.puma.persistence.PumaTest;
import eu.ill.puma.persistence.domain.importer.Importer;
import eu.ill.puma.persistence.service.importer.ImporterService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ImporterServiceTest extends PumaTest {

	@Autowired
	private ImporterService importerService;

	@Test
	public void contextLoads() throws Exception {
		Assert.assertNotNull(this.importerService);
	}

	@Test
	public void createAndRetrieveImporter() {
		String importerName = "Test Importer";
		String shortName = "TEST";
		String importerUrl = "https://a.b.c";

		Importer importer = new Importer();
		importer.setName(importerName);
		importer.setShortName(shortName);
		importer.setUrl(importerUrl);
		importerService.save(importer);

		// Get importer with the same Id
		Importer result = importerService.getById(importer.getId());
		Assert.assertEquals(importerName, result.getName());
		Assert.assertEquals(importerUrl, result.getUrl());
	}

	@Test
	public void createAndRetrieveAllImporters() {
		String importerName1 = "Test Importer1";
		String shortName1 = "TEST1";
		String importerName2 = "Test Importer2";
		String shortName2 = "TEST2";
		String importerUrl1 = "https://a.b.c";
		String importerUrl2 = "https://d.e.f";

		Importer importer1 = new Importer();
		importer1.setName(importerName1);
		importer1.setShortName(shortName1);
		importer1.setUrl(importerUrl1);
		importerService.addImporter(importer1);

		Importer importer2 = new Importer();
		importer2.setName(importerName2);
		importer2.setShortName(shortName2);
		importer2.setUrl(importerUrl2);
		importerService.addImporter(importer2);

		List<Importer> importers = importerService.getAll();
		Assert.assertEquals(2, importers.size());
	}

	@Test
	public void cannotCreateIdenticalUrlAndName() {
		String importerName1 = "Test Importer1";
		String shortName1 = "TEST1";
		String importerName2 = "Test Importer1";
		String shortName2 = "TEST2";
		String importerUrl = "https://a.b.c";

		List<Importer> importers = importerService.getAll();

		Importer importer1 = new Importer();
		importer1.setName(importerName1);
		importer1.setShortName(shortName1);
		importer1.setUrl(importerUrl);
		Importer savedImporter1 = importerService.addImporter(importer1);

		Importer importer2 = new Importer();
		importer2.setName(importerName2);
		importer2.setShortName(shortName2);
		importer2.setUrl(importerUrl);
		Importer savedImporter2 = importerService.addImporter(importer2);

		Assert.assertNotNull(savedImporter1);
		Assert.assertEquals(savedImporter2.getId(), savedImporter1.getId());
		Assert.assertEquals(savedImporter2.getShortName(), savedImporter1.getShortName());
	}
}
