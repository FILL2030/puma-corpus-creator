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
package eu.ill.puma.importermanager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImporterManagerConfiguration {

	@Value("${puma.importerManager.importer.apiBaseUrl}")
	public String importerApiBaseUrl;

	@Value("${puma.importerManager.importer.documentsUrl}")
	public String importerDocumentsUrl;

	@Value("${puma.importerManager.importer.citationsUrl}")
	public String importerCitationsUrl;

	@Value("${puma.importerManager.importer.infoUrl}")
	public String importerInfoUrl;

	@Value("${puma.importerManager.importer.cursorUrl}")
	public String importerCursorUrl;

	@Value("${puma.importerManager.importer.healthUrl}")
	public String importerHealthUrl;

	@Value("${puma.importerManager.resolver.url}")
	public String resolverUrl;

	@Value("${puma.importerManager.resolver.healthUrl}")
	public String resolverHealthUrl;

	@Value("${puma.importerManager.resolver.urlParamName}")
	public String resolverUrlParamName;

	@Value("${puma.importerManager.resolver.doiParamName}")
	public String resolverDoiParamName;

}
