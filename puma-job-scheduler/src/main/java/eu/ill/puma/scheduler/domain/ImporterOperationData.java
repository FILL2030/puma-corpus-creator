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
package eu.ill.puma.scheduler.domain;

import eu.ill.puma.persistence.domain.importer.ImporterOperation;

/**
 * Created by letreguilly on 14/06/17.
 */
public class ImporterOperationData {
	private ImporterOperation importerOperation;

	private Long importerId;

	public ImporterOperation getImporterOperation() {
		return importerOperation;
	}

	public void setImporterOperation(ImporterOperation importerOperation) {
		this.importerOperation = importerOperation;
	}

	public Long getImporterId() {
		return importerId;
	}

	public void setImporterId(Long importerId) {
		this.importerId = importerId;
	}
}
