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
package eu.ill.puma.persistence.domain.indexer.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ill.puma.persistence.domain.document.Instrument;

import java.util.Set;

public interface IndexedLaboratoryMixin {

	@JsonIgnore
	Long getId();

	@JsonProperty
	String getName();

	@JsonProperty
	String getShortName();

	@JsonProperty
	String getAddress();

	@JsonProperty
	String getCity();

	@JsonProperty
	String getCountry();

	@JsonIgnore
	Set<Instrument> getInstruments();

	@JsonIgnore
	Boolean getObsolete();
}
