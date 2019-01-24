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
package eu.ill.puma.core.domain.importer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ImporterInfo {

	private String version;
	private String importerName;
	private String importerShortName;
	private MetaDataAnalysisState metaDataAnalysisState;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getImporterName() {
		return importerName;
	}

	public void setImporterName(String importerName) {
		this.importerName = importerName;
	}

	public String getImporterShortName() {
		return importerShortName;
	}

	public void setImporterShortName(String importerShortName) {
		this.importerShortName = importerShortName;
	}

	public MetaDataAnalysisState getMetaDataAnalysisState() {
		return metaDataAnalysisState;
	}

	public void setMetaDataAnalysisState(MetaDataAnalysisState providedMetaData) {
		this.metaDataAnalysisState = providedMetaData;
	}
}
