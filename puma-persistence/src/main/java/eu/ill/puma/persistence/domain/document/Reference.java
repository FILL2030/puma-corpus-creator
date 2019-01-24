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

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "reference", indexes = {
	@Index(name = "reference_document_version_id_index", columnList = "citing_document_version_id"),
	@Index(name = "reference_document_index", columnList = "cited_document_id"),
	@Index(name = "reference_citation_string_index", columnList = "citation_string"),
	@Index(name = "reference_document_version_source_id_index", columnList = "citing_document_version_source_id")
})
public class Reference {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Column(name = "citation_string", length = 20000)
	private String citationString; // used to obtain citedDocument

	@ManyToOne
	@JoinColumn(name = "CITED_DOCUMENT_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "fk_reference_cited_document_id"))
	private Document citedDocument;

	@Column(name = "citing_document_version_source_id", length = 20000)
	private String citingDocumentVersionSourceId; // used to obtain citingDocument

	@ManyToOne
	@JoinColumn(name = "CITING_DOCUMENT_VERSION_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "fk_reference_citing_document_version_id"))
	private DocumentVersion citingDocumentVersion;

	@Column(name = "obsolete", columnDefinition = "boolean default false")
	private Boolean obsolete = false;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCitationString() {
		return citationString;
	}

	public void setCitationString(String citationString) {
		this.citationString = citationString == null ? null : citationString.toLowerCase();
	}

	public Document getCitedDocument() {
		return citedDocument;
	}

	public void setCitedDocument(Document citedDocument) {
		this.citedDocument = citedDocument;
	}

	public void removeCitedDocument() {
		this.citedDocument = null;
	}

	public String getCitingDocumentVersionSourceId() {
		return citingDocumentVersionSourceId;
	}

	public void setCitingDocumentVersionSourceId(String citingDocumentVersionSourceId) {
		this.citingDocumentVersionSourceId = citingDocumentVersionSourceId;
	}

	public DocumentVersion getCitingDocumentVersion() {
		return citingDocumentVersion;
	}

	public void setCitingDocumentVersion(DocumentVersion citingDocumentVersion) {
		this.citingDocumentVersion = citingDocumentVersion;
	}

	public void removeCitingDocumentVersion() {
		this.citingDocumentVersion = null;
	}

	public Boolean getObsolete() {
		return obsolete;
	}

	public void setObsolete(Boolean obsolete) {
		this.obsolete = obsolete;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Reference reference = (Reference) o;

		if (citationString != null ? !citationString.equals(reference.citationString) : reference.citationString != null)
			return false;
		return citingDocumentVersionSourceId != null ? citingDocumentVersionSourceId.equals(reference.citingDocumentVersionSourceId) : reference.citingDocumentVersionSourceId == null;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + (citationString != null ? citationString.hashCode() : 0);
		result = 31 * result + (citingDocumentVersionSourceId != null ? citingDocumentVersionSourceId.hashCode() : 0);
		return result;
	}
}
