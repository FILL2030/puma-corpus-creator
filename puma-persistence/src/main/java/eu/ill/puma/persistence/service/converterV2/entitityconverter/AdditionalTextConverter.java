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

import eu.ill.puma.core.domain.document.entities.BaseAdditionalText;
import eu.ill.puma.core.domain.document.enumeration.BaseAdditionalTextDataType;
import eu.ill.puma.persistence.domain.document.AdditionalText;
import eu.ill.puma.persistence.domain.document.enumeration.AdditionalTextDataType;
import eu.ill.puma.persistence.service.converterV2.exception.PumaDocumentConversionException;

public class AdditionalTextConverter {

	public static AdditionalText convert(BaseAdditionalText importerAdditionalText) throws PumaDocumentConversionException {
		AdditionalText additionalText = new AdditionalText();

		additionalText.setText(importerAdditionalText.getText());
//		if (importerAdditionalText.getPumaId() != null) {
//			additionalText.setId(Math.abs(importerAdditionalText.getPumaId()));
//		}

		for (BaseAdditionalTextDataType importerAdditionalTextDataType : importerAdditionalText.getSearchableDataTypes()) {
			additionalText.addSearchableDataType(convertAdditionalTextDataType(importerAdditionalTextDataType));
		}

		return additionalText;
	}


	private static AdditionalTextDataType convertAdditionalTextDataType(BaseAdditionalTextDataType importerAdditionalTextDataType) throws PumaDocumentConversionException {
		AdditionalTextDataType additionalTextDataType = AdditionalTextDataType.valueOf(importerAdditionalTextDataType.toString());

		if (additionalTextDataType == null) {
			throw new PumaDocumentConversionException("Could not convert additional text data type " + importerAdditionalTextDataType);
		}

		return additionalTextDataType;
	}
}
