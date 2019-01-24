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
package eu.ill.puma.persistence.service.converterV2.entitityconverter;

import eu.ill.puma.core.domain.document.entities.BaseFormula;
import eu.ill.puma.persistence.domain.document.Formula;

public class FormulaConverter {

	public static Formula convert(BaseFormula importerFormula) {
		Formula formula = new Formula();

		formula.setCode(importerFormula.getCode());
		formula.setConsistence(importerFormula.getConsistence());
		formula.setTemperature(importerFormula.getTemperature());
		formula.setPressure(importerFormula.getPressure());
		formula.setMagneticField(importerFormula.getMagneticField());
//		if (importerFormula.getPumaId() != null) {
//			formula.setId(Math.abs(importerFormula.getPumaId()));
//		}

		return formula;
	}

}
