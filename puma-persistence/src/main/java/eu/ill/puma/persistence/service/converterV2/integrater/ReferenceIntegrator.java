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
import eu.ill.puma.persistence.domain.document.Reference;
import eu.ill.puma.persistence.service.converterV2.EntityOriginStore;
import eu.ill.puma.persistence.service.converterV2.entitityconverter.ReferenceConverter;
import eu.ill.puma.persistence.service.document.ReferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class ReferenceIntegrator {

	@Autowired
	private ReferenceService referenceService;

	public void convert(DocumentVersion documentVersion, BaseDocument baseDocument, EntityOriginStore entityOriginStore) {

		if (baseDocument.getReferences() != null) {

			for (BaseStringEntity baseReferences : baseDocument.getReferences()) {
				Date date = new Date();

				//delete reference (update & delete operation)
				if (baseReferences.getPumaId() != null) {
					Long referenceId = Math.abs(baseReferences.getPumaId());

					Optional<Reference> optionalReferenceToDelete = documentVersion.getReferenceById(referenceId);

					if (optionalReferenceToDelete.isPresent()) {
						documentVersion.getReferences().remove(optionalReferenceToDelete.get());

						//create entity origin
						entityOriginStore.deleteEntity(optionalReferenceToDelete.get().getId(), EntityType.REFERENCE, baseReferences.getConfidence());
					}

				}

				//create reference (add & update operation)
				if (baseReferences.getPumaId() == null || baseReferences.getPumaId() >= 0) {
					//new reference
					Reference referenceToAdd = ReferenceConverter.convertReference(baseReferences);
					referenceToAdd.setCitingDocumentVersion(documentVersion);
					referenceToAdd = referenceService.saveReference(referenceToAdd);

					if (!documentVersion.getReferences().contains(referenceToAdd)) {
						documentVersion.addReference(referenceToAdd);

						//create entity origin
						entityOriginStore.foundEntity(referenceToAdd.getId(), EntityType.REFERENCE, baseReferences.getConfidence());
					}
				}
			}
		}
	}
}
