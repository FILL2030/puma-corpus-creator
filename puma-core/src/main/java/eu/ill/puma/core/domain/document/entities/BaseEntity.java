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
package eu.ill.puma.core.domain.document.entities;

import eu.ill.puma.core.domain.document.MetadataConfidence;

/**
 * Created by letreguilly on 26/07/17.
 */
public class BaseEntity {
	protected MetadataConfidence confidence = MetadataConfidence.TODO;

	protected Long pumaId;

	public MetadataConfidence getConfidence() {
		return confidence;
	}

	public void setConfidence(MetadataConfidence confidence) {
		this.confidence = confidence;
	}

	public Long getPumaId() {
		return pumaId;
	}

	public void setPumaId(Long pumaId) {
		this.pumaId = pumaId;
	}
}
