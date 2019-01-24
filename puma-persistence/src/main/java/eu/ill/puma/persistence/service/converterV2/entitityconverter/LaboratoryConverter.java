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

import eu.ill.puma.core.domain.document.entities.BaseLaboratory;
import eu.ill.puma.persistence.domain.document.Laboratory;

public class LaboratoryConverter {

	public static Laboratory convert(BaseLaboratory importerLaboratory) {
		Laboratory laboratory = new Laboratory();

		laboratory.setName(importerLaboratory.getName());
		laboratory.setShortName(importerLaboratory.getShortName());
		laboratory.setAddress(importerLaboratory.getAddress());
		laboratory.setCity(importerLaboratory.getCity());
		laboratory.setCountry(importerLaboratory.getCountry());

//		if (importerLaboratory.getPumaId() != null) {
//			laboratory.setId(Math.abs(importerLaboratory.getPumaId()));
//		}

		return laboratory;
	}
}
