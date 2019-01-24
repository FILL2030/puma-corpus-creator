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
import eu.ill.puma.persistence.domain.document.Person;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PersonServiceTest extends PumaTest {

	@Autowired
	private eu.ill.puma.persistence.service.document.PersonService personService;

	@Test
	public void contextLoads() throws Exception {
		Assert.assertNotNull(this.personService);
	}

	@Test
	public void createAndRetrieve() {
		Person person = new Person();
		person.setFirstName("abcdef");
		person.setLastName("abcdef");
		personService.save(person);

		Assert.assertNotNull(person.getId());

		Person result = personService.getById(person.getId());

		Assert.assertEquals("abcdef", result.getFirstName());
	}

	@Test
	public void testSingleCreation() {
		String text1 = "This is a Person";
		String text2 = "Test";

		Person person1 = new Person();
		person1.setFirstName(text1);
		person1.setLastName(text2);
		person1 = personService.save(person1);

		Assert.assertNotNull(person1.getId());
		Long person1Id = person1.getId();

		Person person2 = new Person();
		person2.setFirstName(text1);
		person2.setLastName(text2);
		person2 = personService.save(person2);

		Assert.assertNotNull(person2.getId());
		Long person2Id = person2.getId();

		Assert.assertEquals(person1Id, person2Id);
	}

}
