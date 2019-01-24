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
import eu.ill.puma.core.domain.document.entities.BaseAdditionalText;
import eu.ill.puma.core.domain.document.enumeration.BaseAdditionalTextDataType;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import eu.ill.puma.persistence.domain.document.AdditionalText;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.enumeration.AdditionalTextDataType;
import eu.ill.puma.persistence.service.converterV2.EntityOriginStore;
import eu.ill.puma.persistence.service.converterV2.exception.PumaDocumentConversionException;
import eu.ill.puma.persistence.service.converterV2.entitityconverter.AdditionalTextConverter;
import eu.ill.puma.persistence.service.document.AdditionalTextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdditionalTextIntegrator {

	@Autowired
	private AdditionalTextService additionalTextService;

	public void convert(DocumentVersion documentVersion, BaseDocument baseDocument, EntityOriginStore  entityOriginStore) throws PumaDocumentConversionException {

		for (BaseAdditionalText baseAdditionalText : baseDocument.getAdditionalTexts()) {

			if (baseAdditionalText.getPumaId() != null) {
				Long additionalTextId = Math.abs(baseAdditionalText.getPumaId());

				Optional<AdditionalText> additionalTextToDelete = documentVersion.getAdditionalTextById(additionalTextId);

				if (additionalTextToDelete.isPresent()) {
					documentVersion.getAdditionalTexts().remove(additionalTextToDelete.get());

					//create entity origin
					entityOriginStore.deleteEntity(additionalTextToDelete.get().getId(), EntityType.ADDITIONAL_TEXT, baseAdditionalText.getConfidence());
				}
			}

			if (baseAdditionalText.getPumaId() == null || baseAdditionalText.getPumaId() >= 0) {
				AdditionalText additionalText = AdditionalTextConverter.convert(baseAdditionalText);

				for (BaseAdditionalTextDataType importerAdditionalTextDataType : baseAdditionalText.getSearchableDataTypes()) {
					additionalText.addSearchableDataType(convertAdditionalTextDataType(importerAdditionalTextDataType));
				}

				additionalText.setDocumentVersion(documentVersion);
				additionalText = additionalTextService.save(additionalText);

				if (!documentVersion.getAdditionalTexts().contains(additionalText)) {
					documentVersion.addAdditionalText(additionalText);

					entityOriginStore.foundEntity(additionalText.getId(), EntityType.ADDITIONAL_TEXT, baseAdditionalText.getConfidence());
				}
			}
		}

	}


	private static AdditionalTextDataType convertAdditionalTextDataType(BaseAdditionalTextDataType importerAdditionalTextDataType) throws PumaDocumentConversionException {
		AdditionalTextDataType additionalTextDataType = AdditionalTextDataType.valueOf(importerAdditionalTextDataType.toString());

		if (additionalTextDataType == null) {
			throw new PumaDocumentConversionException("Could not convert additional text data type " + importerAdditionalTextDataType);
		}

		return additionalTextDataType;
	}
}
