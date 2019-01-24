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
package eu.ill.puma.persistence.domain.importer;

import eu.ill.puma.persistence.util.JpaJsonConverter;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "importer_operation")
public class ImporterOperation implements Serializable {

	public static Integer MAX_RETRIES = 2;

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = IDENTITY)
	private Long id = null;

	@Column(name = "params", length = 10000)
	@Convert(converter = JpaJsonConverter.class)
	private Map<String, Object> params = new HashMap<>();

	@Column(name = "update_existing", columnDefinition = "boolean default false")
	private Boolean updateExisting = false;

	@Column(name = "download_files", columnDefinition = "boolean default false")
	private Boolean downloadFiles = false;

	@Column(name = "reimport_all", columnDefinition = "boolean default false")
	private Boolean reimportAll = false;

	@Column(name = "update_citations", columnDefinition = "boolean default false")
	private Boolean updateCitations = false;

	@Column(name = "last_imported_document_version_id")
	private Long lastImportedDocumentVersionId = 0l;

	@Column(name = "run_time")
	private Long runTime = null;

	@Column(name = "creation_date")
	private Date creationDate;

	@Column(name = "status", nullable = false, length = 1000)
	@Enumerated(EnumType.STRING)
	private ImporterOperationStatus status;

	@Column(name = "cursor", length = 1000)
	private String cursor;

	@Column(name = "last_cursor", length = 1000)
	private String lastCursor;

	@Column(name = "documents_received")
	private Long documentsReceived = 0L;

	@Column(name = "documents_integrated")
	private Long documentsIntegrated = 0L;

	@Column(name = "total_document_count")
	private Long totalDocumentCount;

	@Column(name = "retry_count")
	private Integer retryCount = 0;

	@ManyToOne
	@JoinColumn(name = "IMPORTER_ID", nullable = false, foreignKey = @ForeignKey(name = "fk_importer_operation_importer_id"))
	private Importer importer;

	private Long lastReimportDocumentVersionId = 0l;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getRunTime() {
		return runTime;
	}

	public void setRunTime(Long runTime) {
		this.runTime = runTime;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public Boolean getUpdateExisting() {
		return updateExisting;
	}

	public void setUpdateExisting(Boolean updateExisting) {
		this.updateExisting = updateExisting;
	}

	public Boolean getDownloadFiles() {
		return downloadFiles;
	}

	public void setDownloadFiles(Boolean downloadFiles) {
		this.downloadFiles = downloadFiles;
	}

	public Boolean getReimportAll() {
		return reimportAll;
	}

	public void setReimportAll(Boolean reimportAll) {
		this.reimportAll = reimportAll;
	}

	public Boolean getUpdateCitations() {
		return updateCitations;
	}

	public void setUpdateCitations(Boolean updateCitations) {
		this.updateCitations = updateCitations;
	}

	public Long getLastImportedDocumentVersionId() {
		return lastImportedDocumentVersionId;
	}

	public void setLastImportedDocumentVersionId(Long lastImportedDocumentVersionId) {
		this.lastImportedDocumentVersionId = lastImportedDocumentVersionId;
	}

	public ImporterOperationStatus getStatus() {
		return status;
	}

	public void setStatus(ImporterOperationStatus status) {
		this.status = status;
	}

	public String getCursor() {
		return cursor;
	}

	public void setCursor(String cursor) {
		this.cursor = cursor;
	}

	public String getLastCursor() {
		return lastCursor;
	}

	public void setLastCursor(String lastCursor) {
		this.lastCursor = lastCursor;
	}

	public Long getDocumentsReceived() {
		return documentsReceived;
	}

	public void setDocumentsReceived(Long documentsReceived) {
		this.documentsReceived = documentsReceived;
	}

	public Long getDocumentsIntegrated() {
		return documentsIntegrated;
	}

	public void setDocumentsIntegrated(Long documentsIntegrated) {
		this.documentsIntegrated = documentsIntegrated;
	}

	public Long getTotalDocumentCount() {
		return totalDocumentCount;
	}

	public void setTotalDocumentCount(Long totalDocumentCount) {
		this.totalDocumentCount = totalDocumentCount;
	}

	public Integer getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}

	public Long getLastReimportDocumentVersionId() {
		return lastReimportDocumentVersionId;
	}

	public void setLastReimportDocumentVersionId(Long lastReimportDocumentVersionId) {
		this.lastReimportDocumentVersionId = lastReimportDocumentVersionId;
	}

	public Importer getImporter() {
		return importer;
	}

	public void setImporter(Importer importer) {
		this.importer = importer;
	}

	public void increaseRunTime(Long increment) {
		if (this.runTime == null) {
			this.runTime = 0L;
		}
		runTime = runTime + increment;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ImporterOperation that = (ImporterOperation) o;

		if (params != null ? !params.equals(that.params) : that.params != null) return false;
		if ((creationDate != null && that.creationDate != null) ? creationDate.getTime() != that.creationDate.getTime() : (creationDate == null && that.creationDate == null) ? false : true) return false;
		return importer != null ? importer.equals(that.importer) : that.importer == null;
	}

	@Override
	public int hashCode() {
		int result = params != null ? params.hashCode() : 0;
		result = 31 * result + (creationDate != null ? new Long(creationDate.getTime()).hashCode() : 0);
		result = 31 * result + (importer != null ? importer.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("id", id)
			.append("params", params)
			.append("runTime", runTime)
			.append("creationDate", creationDate)
			.append("status", status)
			.append("cursor", cursor)
			.append("lastCursor", lastCursor)
			.append("documentsReceived", documentsReceived)
			.append("documentsIntegrated", documentsIntegrated)
			.append("totalDocumentCount", totalDocumentCount)
			.append("retryCount", retryCount)
			.toString();
	}
}
