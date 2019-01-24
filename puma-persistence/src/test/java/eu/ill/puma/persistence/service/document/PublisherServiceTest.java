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
import eu.ill.puma.persistence.domain.document.Publisher;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PublisherServiceTest extends PumaTest {

	@Autowired
	private eu.ill.puma.persistence.service.document.PublisherService PublisherService;

	@Test
	public void contextLoads() throws Exception {
		Assert.assertNotNull(this.PublisherService);
	}

	@Test
	public void createAndRetrieve() {
		Publisher publisher = new Publisher();
		publisher.setName("abcdef");
		PublisherService.save(publisher);

		Assert.assertNotNull(publisher.getId());

		Publisher result = PublisherService.getById(publisher.getId());

		Assert.assertEquals("abcdef", result.getName());
	}

	@Test
	public void testSingleCreation() {
		String text1 = "This is a Publisher";

		Publisher publisher1 = new Publisher();
		publisher1.setName(text1);
		publisher1 = PublisherService.save(publisher1);

		Assert.assertNotNull(publisher1.getId());
		Long publisher1Id = publisher1.getId();

		Publisher publisher2 = new Publisher();
		publisher2.setName(text1);
		publisher2 = PublisherService.save(publisher2);

		Assert.assertNotNull(publisher2.getId());
		Long publisher2Id = publisher2.getId();

		Assert.assertEquals(publisher1Id, publisher2Id);
	}

}
