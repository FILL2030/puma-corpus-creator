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
package eu.ill.puma.persistence.domain.extract.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import eu.ill.puma.persistence.domain.document.*;
import eu.ill.puma.persistence.domain.document.enumeration.DocumentType;
import eu.ill.puma.persistence.domain.document.enumeration.DocumentVersionSubType;

import java.util.*;

public interface ExtractedDocumentMixin {

	@JsonProperty
	Long getId();

	@JsonProperty
	DocumentType getDocumentType();

	@JsonProperty
	Date getReleaseDate();

	@JsonProperty
	String getDoi();

	@JsonProperty
	DocumentVersionSubType getSubType();

	@JsonProperty
	String getShortName();

	@JsonProperty
	String getTitle();

	@JsonProperty
	String getAbstractText();

	@JsonProperty
	List<Reference> getReferences();

	@JsonProperty
	List<DocumentVersionSource> getSources();

	@JsonProperty
	Set<Person> getPersons();

	@JsonProperty
	Set<Laboratory> getLaboratories();

	@JsonProperty
	List<Instrument> getInstruments();

	@JsonProperty
	Set<Publisher> getPublishers();

	@JsonProperty
	Journal getJournal();

	@JsonProperty
	List<Keyword> getKeywords();

	@JsonProperty
	List<Formula> getFormulas();

	@JsonProperty
	List<ResearchDomain> getResearchDomains();

	@JsonProperty
	List<AdditionalText> getAdditionalTexts();

	@JsonProperty
	String getFullText();

	@JsonRawValue
	String getFormattedFullText();
}
