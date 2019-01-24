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
package eu.ill.puma.webapi.rest.downloader;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileStatus;

public class RestPumaFile {

	@JsonIgnore
	private PumaFile pumaFile;

	public RestPumaFile(PumaFile pumaFile) {
		this.pumaFile = pumaFile;
	}

	@JsonGetter
	public String getOriginUrl() {
		return this.pumaFile.getOriginUrl();
	}

	@JsonGetter
	public PumaFileStatus getStatus() {
		return this.pumaFile.getStatus();
	}
}
