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

import eu.ill.puma.core.domain.document.entities.BaseStringEntity;
import eu.ill.puma.persistence.domain.document.ResearchDomain;

public class ResearchDomainConverter {

	public static ResearchDomain convert(BaseStringEntity importerResearchDomain) {

		ResearchDomain researchDomain = new ResearchDomain();

		researchDomain.setSubject(importerResearchDomain.getValue());

//		if (importerResearchDomain.getPumaId() != null) {
//			researchDomain.setId(Math.abs(importerResearchDomain.getPumaId()));
//		}

		return researchDomain;
	}

}
