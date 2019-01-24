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
package eu.ill.puma.persistence.service.converterV2;

import eu.ill.puma.core.domain.document.entities.BaseJournal;
import eu.ill.puma.persistence.domain.document.Journal;
import eu.ill.puma.persistence.service.converterV2.entitityconverter.JournalConverter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
public class JournalNameCleanerTest {

	@Test
	public void verifyNameCleaned() throws Exception {
		List<String> testValues = Arrays.asList(", Vol. 3, page 2", ", Vol 3, page 2", ", Vol. 3", ", Vol 3");
		String startText = "This is a test";

		testValues.stream().forEach(testValue -> {
			String journalName = startText + testValue;

			BaseJournal baseJournal = new BaseJournal();
			baseJournal.setName(journalName);

			Journal journal = JournalConverter.convert(baseJournal);
			Assert.assertEquals(startText.toLowerCase(), journal.getName());
		});


		BaseJournal baseJournal = new BaseJournal();
		baseJournal.setName("This is a normal journal");

		Journal journal = JournalConverter.convert(baseJournal);
		Assert.assertEquals(baseJournal.getName().toLowerCase(), journal.getName());
	}

}
