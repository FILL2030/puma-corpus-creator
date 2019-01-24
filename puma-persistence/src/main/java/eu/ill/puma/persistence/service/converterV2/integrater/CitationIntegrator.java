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
import eu.ill.puma.persistence.domain.document.Document;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.Reference;
import eu.ill.puma.persistence.service.converterV2.EntityOriginStore;
import eu.ill.puma.persistence.service.converterV2.entitityconverter.ReferenceConverter;
import eu.ill.puma.persistence.service.document.ReferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CitationIntegrator {

	@Autowired
	private ReferenceService referenceService;

	public void convert(DocumentVersion documentVersion, BaseDocument baseDocument, EntityOriginStore entityOriginStore) {

		if (baseDocument.getCitations() != null) {

			Document document = documentVersion.getDocument();

			for (BaseStringEntity baseCitation : baseDocument.getCitations()) {

				//delete reference (update & delete operation)
				if (baseCitation.getPumaId() != null) {
					Long referenceId = Math.abs(baseCitation.getPumaId());

					Optional<Reference> optionalCitationToDelete = document.getReferenceById(referenceId);

					if (optionalCitationToDelete.isPresent()) {
						documentVersion.getReferences().remove(optionalCitationToDelete.get());

						//create entity origin
						entityOriginStore.deleteEntity(optionalCitationToDelete.get().getId(), EntityType.CITATION, baseCitation.getConfidence());
					}

				}

				//create reference (add & update operation)
				if (baseCitation.getPumaId() == null || baseCitation.getPumaId() >= 0) {
					//new reference
					Reference citationToAdd = ReferenceConverter.convertCitation(baseCitation);
					citationToAdd.setCitedDocument(document);
					citationToAdd = referenceService.saveCitation(citationToAdd);

					if (!document.getReferences().contains(citationToAdd)) {
						document.addReference(citationToAdd);

						//create entity origin
						entityOriginStore.foundEntity(citationToAdd.getId(), EntityType.CITATION, baseCitation.getConfidence());
					}
				}
			}
		}
	}
}
