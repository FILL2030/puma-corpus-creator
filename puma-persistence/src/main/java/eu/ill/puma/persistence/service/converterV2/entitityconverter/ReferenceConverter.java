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
import eu.ill.puma.persistence.domain.document.Reference;

public class ReferenceConverter {

	public static Reference convertReference(BaseStringEntity importerReference) {

		Reference reference = new Reference();

		reference.setCitationString(importerReference.getValue());

//		if (importerReference.getPumaId() != null) {
//			reference.setId(Math.abs(importerReference.getPumaId()));
//		}

		return reference;
	}

	public static Reference convertCitation(BaseStringEntity importerCitingDocumentId) {

		Reference reference = new Reference();

		reference.setCitingDocumentVersionSourceId(importerCitingDocumentId.getValue());

//		if (importerCitingDocumentId.getPumaId() != null) {
//			reference.setId(Math.abs(importerCitingDocumentId.getPumaId()));
//		}

		return reference;
	}

}
