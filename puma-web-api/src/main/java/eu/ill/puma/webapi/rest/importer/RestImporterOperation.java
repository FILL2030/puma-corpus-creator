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
package eu.ill.puma.webapi.rest.importer;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.ill.puma.persistence.domain.importer.ImporterOperation;
import eu.ill.puma.persistence.domain.importer.ImporterOperationStatus;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Map;

public class RestImporterOperation {

	@JsonIgnore
	private ImporterOperation importerOperation;


	public RestImporterOperation() {
		this.importerOperation = new ImporterOperation();
	}

	public RestImporterOperation(Map<String, Object> params) {
		this.importerOperation = new ImporterOperation();
		this.importerOperation.setParams(params);
	}

	public RestImporterOperation(ImporterOperation importerOperation) {
		this.importerOperation = importerOperation;
	}

	@JsonGetter
	@ApiModelProperty(example = "null", value = "")
	@NotNull
	public Long getId() {
		return this.importerOperation.getId();
	}

	public void setId(Long id) {
		this.importerOperation.setId(id);
	}

	@JsonGetter
	@ApiModelProperty(example = "null", value = "")
	public Long getRunTime() {
		return this.importerOperation.getRunTime();
	}

	public void setRunTime(Long runTime) {
		this.importerOperation.setRunTime(runTime);
	}

	@JsonGetter
	@ApiModelProperty(example = "null", required = true, value = "")
	public Map<String, Object> getParams() {
		return importerOperation.getParams();
	}

	public void setParams(Map<String, Object> params) {
		this.importerOperation.setParams(params);
	}

	@JsonGetter
	public Boolean getUpdateExisting() {
		return this.importerOperation.getUpdateExisting();
	}

	public void setUpdateExisting(Boolean updateExisting) {
		this.importerOperation.setUpdateExisting(updateExisting);
	}

	@JsonGetter
	public Boolean getUpdateCitations() {
		return this.importerOperation.getUpdateCitations();
	}

	public void setUpdateCitations(Boolean updateCitations) {
		this.importerOperation.setUpdateCitations(updateCitations);
	}

	@JsonGetter
	public Boolean getDownloadFiles() {
		return this.importerOperation.getDownloadFiles();
	}

	public void setDownloadFiles(Boolean downloadFiles) {
		this.importerOperation.setDownloadFiles(downloadFiles);
	}

	@JsonGetter
	public Boolean getReimportAll() {
		return this.importerOperation.getReimportAll();
	}

	public void setReimportAll(Boolean reimportAll) {
		this.importerOperation.setReimportAll(reimportAll);
	}

	@JsonGetter
	public Long getLastImportedDocumentVersionId() {
		return this.importerOperation.getLastImportedDocumentVersionId();
	}

	public void setLastImportedDocumentVersionId(Long lastImportedDocumentVersionId) {
		this.importerOperation.setLastImportedDocumentVersionId(lastImportedDocumentVersionId);
		this.importerOperation.setLastReimportDocumentVersionId(lastImportedDocumentVersionId);
	}

	@JsonGetter
	@ApiModelProperty(example = "null", value = "")
	public ImporterOperationStatus getStatus() {
		return importerOperation.getStatus();
	}

	public void setStatus(ImporterOperationStatus status) {
		this.importerOperation.setStatus(status);
	}

	@JsonGetter
	@ApiModelProperty(example = "null", value = "")
	public Date getCreationDate() {
		return importerOperation.getCreationDate();
	}


	public ImporterOperation getImporterOperation() {
		return this.importerOperation;
	}

	@JsonGetter
	public Long getDocumentsReceived() {
		return importerOperation.getDocumentsReceived();
	}

	@JsonGetter
	public Long getDocumentsIntegrated() {
		return importerOperation.getDocumentsIntegrated();
	}

	@JsonGetter
	public Long getTotalDocumentCount() {
		return importerOperation.getTotalDocumentCount();
	}

}
