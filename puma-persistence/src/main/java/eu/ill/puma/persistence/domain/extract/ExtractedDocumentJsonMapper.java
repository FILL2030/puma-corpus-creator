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
package eu.ill.puma.persistence.domain.extract;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import eu.ill.puma.persistence.domain.document.*;
import eu.ill.puma.persistence.domain.extract.mixin.*;

public class ExtractedDocumentJsonMapper extends ObjectMapper{

	public ExtractedDocumentJsonMapper() {
		this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		this.setDateFormat(new ISO8601DateFormat());
		this.setSerializationInclusion(JsonInclude.Include.NON_NULL);

		// Set mixins
		this.addMixIn(ExtractedDocument.class, ExtractedDocumentMixin.class);
		this.addMixIn(DocumentVersionSource.class, ExtractedDocumentVersionSourceMixin.class);
		this.addMixIn(Reference.class, ExtractedReferenceMixin.class);
		this.addMixIn(Keyword.class, ExtractedKeywordMixin.class);
		this.addMixIn(Formula.class, ExtractedFormulaMixin.class);
		this.addMixIn(AdditionalText.class, ExtractedAdditionalTextMixin.class);
		this.addMixIn(ResearchDomain.class, ExtractedResearchDomainMixin.class);
		this.addMixIn(Person.class, ExtractedPersonMixin.class);
		this.addMixIn(Laboratory.class, ExtractedLaboratoryMixin.class);
		this.addMixIn(Instrument.class, ExtractedInstrumentMixin.class);
		this.addMixIn(Journal.class, ExtractedJournalMixin.class);
		this.addMixIn(Publisher.class, ExtractedPublisherMixin.class);
	}
}
