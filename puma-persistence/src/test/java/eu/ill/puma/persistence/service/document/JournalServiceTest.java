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
import eu.ill.puma.persistence.domain.document.Journal;
import eu.ill.puma.persistence.service.document.JournalService;
import eu.ill.puma.persistence.service.document.PublisherService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class JournalServiceTest extends PumaTest {

	@Autowired
	private JournalService journalService;

	@Autowired
	private PublisherService publisherService;

	@Test
	public void contextLoads() throws Exception {
		Assert.assertNotNull(this.journalService);
	}

	@Test
	public void createAndRetrieve() {
		Journal journal = new Journal();
		journal.setName("abcdef");
		journalService.save(journal);

		Assert.assertNotNull(journal.getId());

		Journal result = journalService.getById(journal.getId());

		Assert.assertEquals("abcdef", result.getName());
	}

	@Test
	public void testSingleCreation() {
		String name = "This is a journal";

		Journal journal1 = new Journal();
		journal1.setName(name);
		journal1 = journalService.save(journal1);

		Assert.assertNotNull(journal1.getId());
		Long journal1Id = journal1.getId();

		Journal journal2 = new Journal();
		journal2.setName(name);
		journal2 = journalService.save(journal2);

		Assert.assertNotNull(journal2.getId());
		Long journal2Id = journal2.getId();

		Assert.assertEquals(journal1Id, journal2Id);
	}

}