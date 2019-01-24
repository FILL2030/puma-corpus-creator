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

import eu.ill.puma.persistence.domain.document.enumeration.AdditionalTextDataType;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "additional_text", indexes = {
	@Index(name = "formula_document_version_id_index", columnList = "document_version_id")
})
public class AdditionalText {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Column(name = "text", length = 100000, nullable = false)
	private String text;

	@ElementCollection
	@Enumerated(EnumType.STRING)
	@JoinTable(name = "additional_text_searchable_data_types", indexes = {@Index(name = "additional_text_id_index", columnList = "additional_text_id")}, joinColumns = {
			@JoinColumn(name = "additional_text_id", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "fk_additional_text_id"))})
	@Column(name="DATA_TYPE", length = 1000)
	private Set<AdditionalTextDataType> searchableDataTypes = new HashSet<>();

	@ManyToOne
	@JoinColumn(name = "DOCUMENT_VERSION_ID", referencedColumnName = "ID", nullable = true, foreignKey = @ForeignKey(name = "fk_additional_text_document_version_id"))
	private DocumentVersion documentVersion;

	@Column(name = "obsolete", columnDefinition = "boolean default false")
	private Boolean obsolete = false;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Set<AdditionalTextDataType> getSearchableDataTypes() {
		return searchableDataTypes;
	}

	public void addSearchableDataType(AdditionalTextDataType searchableDataType) {
		this.searchableDataTypes.add(searchableDataType);
	}

	public DocumentVersion getDocumentVersion() {
		return documentVersion;
	}

	public void setDocumentVersion(DocumentVersion documentVersion) {
		this.documentVersion = documentVersion;
	}
	public void removeDocumentVersion() {
		this.documentVersion = null;
	}

	public Boolean getObsolete() {
		return obsolete;
	}

	public void setObsolete(Boolean obsolete) {
		this.obsolete = obsolete;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("id", id)
			.append("text", text)
			.append("searchableDataTypes", searchableDataTypes)
			.toString();
	}
}
