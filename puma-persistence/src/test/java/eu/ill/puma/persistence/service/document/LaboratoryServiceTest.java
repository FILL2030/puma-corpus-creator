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
import eu.ill.puma.persistence.domain.document.Laboratory;
import eu.ill.puma.persistence.service.document.LaboratoryService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class LaboratoryServiceTest extends PumaTest {

	@Autowired
	private LaboratoryService laboratoryService;

	@Test
	public void contextLoads() throws Exception {
		Assert.assertNotNull(this.laboratoryService);
	}

	@Test
	public void createAndRetrieve() {
		Laboratory laboratory = new Laboratory();
		laboratory.setName("abcdef");
		laboratory.setAddress("abcdef");
		laboratoryService.save(laboratory);

		Assert.assertNotNull(laboratory.getId());

		Laboratory result = laboratoryService.getById(laboratory.getId());

		Assert.assertEquals("abcdef", result.getName());
	}

	@Test
	public void testSingleCreation() {
		String text = "This is a Laboratory";
		String addr = "Number 1 street";

		Laboratory laboratory1 = new Laboratory();
		laboratory1.setName(text);
		laboratory1.setAddress(addr);
		laboratory1 = laboratoryService.save(laboratory1);

		Assert.assertNotNull(laboratory1.getId());
		Long laboratory1Id = laboratory1.getId();

		Laboratory laboratory2 = new Laboratory();
		laboratory2.setName(text);
		laboratory2.setAddress(addr);
		laboratory2 = laboratoryService.save(laboratory2);

		Assert.assertNotNull(laboratory2.getId());
		Long laboratory2Id = laboratory2.getId();

		Assert.assertEquals(laboratory1Id, laboratory2Id);
	}

}
