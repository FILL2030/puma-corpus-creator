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
import eu.ill.puma.persistence.domain.document.Formula;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class FormulaServiceTest extends PumaTest {

	private static final Logger log = LoggerFactory.getLogger(FormulaServiceTest.class);

	@Autowired
	private FormulaService formulaService;

	@Test
	public void contextLoads() throws Exception {
		Assert.assertNotNull(this.formulaService);
	}

	@Test
	public void createAndRetrieve() {
		Formula formula = new Formula();
		formula.setCode("abcdef");
		formulaService.save(formula);

		Assert.assertNotNull(formula.getId());

		Formula result = formulaService.getById(formula.getId());

		Assert.assertEquals("abcdef", result.getCode());

		List<Formula> formulas = formulaService.getAll();
		Assert.assertTrue(formulas.size() > 0);

	}

	@Test
	public void testSingleCreation() {
		String code = "abcdef";

		Formula formula1 = new Formula();
		formula1.setCode(code);
		formula1 = formulaService.save(formula1);

		Assert.assertNotNull(formula1.getId());
		Long formula1Id = formula1.getId();

		Formula formula2 = new Formula();
		formula2.setCode(code);
		formula2 = formulaService.save(formula2);

		Assert.assertNotNull(formula2.getId());
		Long formula2Id = formula2.getId();

		Assert.assertEquals(formula1Id, formula2Id);
	}

}
