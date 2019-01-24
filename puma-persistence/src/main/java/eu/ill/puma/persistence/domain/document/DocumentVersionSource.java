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
package eu.ill.puma.persistence.domain.document;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "document_version_source", indexes = {
		@Index(name = "document_version_source_source_id_index", columnList = "source_id"),
		@Index(name = "document_version_source_document_version_id_index", columnList = "document_version_id")})
public class DocumentVersionSource {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = IDENTITY)
	Long id;

	@Column(name = "importer_short_name", length = 1000, nullable = false)
	String importerShortName;

	@Column(name = "source_id", length = 1000, nullable = false)
	String sourceId;

	@Column(name = "import_date", nullable = false)
	Date importDate;

	@ManyToOne
	@JoinColumn(name = "DOCUMENT_VERSION_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "fk_document_version_source_document_version_id"))
	DocumentVersion documentVersion;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getImporterShortName() {
		return importerShortName;
	}

	public void setImporterShortName(String importerShortName) {
		this.importerShortName = importerShortName;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public Date getImportDate() {
		return importDate;
	}

	public void setImportDate(Date importDate) {
		this.importDate = importDate;
	}

	public DocumentVersion getDocumentVersion() {
		return documentVersion;
	}

	public void setDocumentVersion(DocumentVersion documentVersion) {
		this.documentVersion = documentVersion;
	}
}
