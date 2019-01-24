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
package eu.ill.puma.persistence.domain.analysis;

import eu.ill.puma.persistence.domain.document.DocumentVersion;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "document_version_analysis_history", indexes = {
	@Index(name = "analysis_history_document_version_id_index", columnList = "document_version_id")
})
public class DocumentVersionAnalysisHistory {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "DOCUMENT_VERSION_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "fk_analysis_history_document_version_id"))
	private DocumentVersion documentVersion;

	@Column(name = "analyser_name", nullable = false)
	private String analyserName;

	@Column(name = "run_date", nullable = false)
	private Date runDate;

	@Column(name = "duration", nullable = false)
	private Long duration;

	@Column(name = "successful", nullable = false)
	private boolean successful;

	@Column(name = "message", length = 1000)
	private String message;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DocumentVersion getDocumentVersion() {
		return documentVersion;
	}

	public void setDocumentVersion(DocumentVersion documentVersion) {
		this.documentVersion = documentVersion;
	}

	public String getAnalyserName() {
		return analyserName;
	}

	public void setAnalyserName(String analyserName) {
		this.analyserName = analyserName;
	}

	public Date getRunDate() {
		return runDate;
	}

	public void setRunDate(Date runDate) {
		this.runDate = runDate;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		DocumentVersionAnalysisHistory that = (DocumentVersionAnalysisHistory) o;

		return new EqualsBuilder()
				.append(id, that.id)
				.append(documentVersion, that.documentVersion)
				.append(analyserName, that.analyserName)
				.append(runDate, that.runDate)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(id)
				.append(documentVersion)
				.append(analyserName)
				.append(runDate)
				.toHashCode();
	}
}
