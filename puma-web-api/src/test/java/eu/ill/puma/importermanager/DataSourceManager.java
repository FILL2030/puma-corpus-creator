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

import eu.ill.puma.persistence.domain.importer.ImporterOperation;
import eu.ill.puma.persistence.domain.importer.ImporterOperationStatus;
import eu.ill.puma.persistence.service.importer.ImporterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataSourceManager {

	@Autowired
	ImporterService importerService;

	public void performImport(ImporterOperation importerOperation) {
		importerOperation.setStatus(ImporterOperationStatus.RUNNING);
		this.importerService.updateOperation(importerOperation);

	}

	public void performCancel(ImporterOperation importerOperation) {
		importerOperation.setStatus(ImporterOperationStatus.CANCELLED);
		this.importerService.updateOperation(importerOperation);
	}

}