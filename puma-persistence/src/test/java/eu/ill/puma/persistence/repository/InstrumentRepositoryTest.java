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
import eu.ill.puma.persistence.domain.document.Laboratory;
import eu.ill.puma.persistence.repository.document.InstrumentRepository;
import eu.ill.puma.persistence.service.document.LaboratoryService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class InstrumentRepositoryTest extends PumaTest {

	@Autowired
	private InstrumentRepository instrumentRepository;

	@Autowired
	private LaboratoryService laboratoryService;

	@Test
	public void testCount() {
		Laboratory laboratory = new Laboratory();
		laboratory.setAddress("test");
		laboratory.setName("test");
		laboratory.setCity("test");
		laboratory.setCountry("test");
		laboratory.setShortName("ill");
		laboratory = this.laboratoryService.save(laboratory);

		Assert.assertEquals(0L, this.instrumentRepository.getCountForLaboratory(laboratory) + 0);
	}
}
