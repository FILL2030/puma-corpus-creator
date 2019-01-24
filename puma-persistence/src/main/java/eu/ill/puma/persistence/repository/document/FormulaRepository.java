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
package eu.ill.puma.persistence.repository.document;

import eu.ill.puma.persistence.domain.document.Formula;
import eu.ill.puma.persistence.repository.PumaDocumentEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;

@Repository
public class FormulaRepository extends PumaDocumentEntityRepository<Formula> {

	public Formula getByMatchingValues(String code, String consistence, String temperature, String pressure, String magneticField) {
		return this.getFirstEntity(Arrays.asList("code", "consistence", "temperature", "pressure", "magneticField"), code, consistence, temperature, pressure, magneticField);
	}

}
