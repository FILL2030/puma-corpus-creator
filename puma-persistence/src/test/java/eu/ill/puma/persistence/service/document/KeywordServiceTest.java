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
import eu.ill.puma.persistence.domain.document.Keyword;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class KeywordServiceTest extends PumaTest {

	private static final Logger log = LoggerFactory.getLogger(KeywordServiceTest.class);

	@Autowired
	private eu.ill.puma.persistence.service.document.KeywordService KeywordService;

	@Test
	public void contextLoads() throws Exception {
		Assert.assertNotNull(this.KeywordService);
	}

	@Test
	public void createAndRetrieve() {
		Keyword keyword = new Keyword();
		keyword.setWord("abcdef");
		KeywordService.save(keyword);

		Assert.assertNotNull(keyword.getId());

		Keyword result = KeywordService.getById(keyword.getId());

		Assert.assertEquals("abcdef", result.getWord());
	}

	@Test
	public void testSingleCreation() {
		String word = "This is a Keyword";

		Keyword keyword1 = new Keyword();
		keyword1.setWord(word);
		keyword1 = KeywordService.save(keyword1);

		Assert.assertNotNull(keyword1.getId());
		Long keyword1Id = keyword1.getId();

		Keyword keyword2 = new Keyword();
		keyword2.setWord(word);
		keyword2 = KeywordService.save(keyword2);

		Assert.assertNotNull(keyword2.getId());
		Long keyword2Id = keyword2.getId();

		Assert.assertEquals(keyword1Id, keyword2Id);
	}

}
