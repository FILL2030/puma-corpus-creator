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
package eu.ill.puma.persistence.service.converterV2.integrater;

import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.core.domain.document.entities.BaseStringEntity;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.ResearchDomain;
import eu.ill.puma.persistence.service.converterV2.EntityOriginStore;
import eu.ill.puma.persistence.service.converterV2.entitityconverter.ResearchDomainConverter;
import eu.ill.puma.persistence.service.document.ResearchDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class ResearchDomainIntegrator {
	@Autowired
	private ResearchDomainService researchDomainService;

	public void convert(DocumentVersion documentVersion, BaseDocument baseDocument, EntityOriginStore entityOriginStore) {

		if (baseDocument.getResearchDomains() != null) {

			for (BaseStringEntity baseResearchDomain : baseDocument.getResearchDomains()) {
				Date date = new Date();

				//delete research domain (update & delete operation)
				if (baseResearchDomain.getPumaId() != null) {
					Long researchDomainId = Math.abs(baseResearchDomain.getPumaId());

					Optional<ResearchDomain> researchDomainToDelete = documentVersion.getResearchDomainById(researchDomainId);

					if (researchDomainToDelete.isPresent()) {
						documentVersion.getResearchDomains().remove(researchDomainToDelete.get());

						//create entity origin
						entityOriginStore.deleteEntity(researchDomainToDelete.get().getId(), EntityType.RESEARCH_DOMAIN, baseResearchDomain.getConfidence());
					}
				}

				//create research domain (add & update operation)
				if (baseResearchDomain.getPumaId() == null || baseResearchDomain.getPumaId() >= 0) {
					//new research domain
					ResearchDomain researchDomainToAdd = ResearchDomainConverter.convert(baseResearchDomain);
					researchDomainToAdd = researchDomainService.save(researchDomainToAdd);

					if (!documentVersion.getResearchDomains().contains(researchDomainToAdd)) {
						documentVersion.addResearchDomain(researchDomainToAdd);

						//create entity origin
						entityOriginStore.foundEntity(researchDomainToAdd.getId(), EntityType.RESEARCH_DOMAIN, baseResearchDomain.getConfidence());
					}
				}
			}
		}
	}
}
