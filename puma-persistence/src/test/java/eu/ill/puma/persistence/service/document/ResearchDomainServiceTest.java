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
import eu.ill.puma.persistence.domain.document.ResearchDomain;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ResearchDomainServiceTest extends PumaTest {

	@Autowired
	private eu.ill.puma.persistence.service.document.ResearchDomainService ResearchDomainService;

	@Test
	public void contextLoads() throws Exception {
		Assert.assertNotNull(this.ResearchDomainService);
	}

	@Test
	public void createAndRetrieve() {
		ResearchDomain researchDomain = new ResearchDomain();
		researchDomain.setSubject("abcdef");
		ResearchDomainService.save(researchDomain);

		Assert.assertNotNull(researchDomain.getId());

		ResearchDomain result = ResearchDomainService.getById(researchDomain.getId());

		Assert.assertEquals("abcdef", result.getSubject());
	}

	@Test
	public void testSingleCreation() {
		String word = "This is a ResearchDomain";

		ResearchDomain researchDomain1 = new ResearchDomain();
		researchDomain1.setSubject(word);
		researchDomain1 = ResearchDomainService.save(researchDomain1);

		Assert.assertNotNull(researchDomain1.getId());
		Long researchDomain1Id = researchDomain1.getId();

		ResearchDomain researchDomain2 = new ResearchDomain();
		researchDomain2.setSubject(word);
		researchDomain2 = ResearchDomainService.save(researchDomain2);

		Assert.assertNotNull(researchDomain2.getId());
		Long researchDomain2Id = researchDomain2.getId();

		Assert.assertEquals(researchDomain1Id, researchDomain2Id);
	}

}
