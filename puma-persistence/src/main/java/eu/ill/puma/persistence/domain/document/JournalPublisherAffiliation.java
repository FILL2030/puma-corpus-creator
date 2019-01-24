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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "journal_publisher_affiliation"
		, uniqueConstraints = {@UniqueConstraint(columnNames = {"journal_id", "publisher_id", "document_version_id"}, name = "unique_journal_id_publisher_id_document_version_id")}
		, indexes = {@Index(name = "journal_publisher_affiliation_index", columnList = "obsolete,JOURNAL_ID,PUBLISHER_ID"),
	@Index(name = "journal_publisher_affiliation_document_version_id_index", columnList = "document_version_id")})
public class JournalPublisherAffiliation {

	@JsonIgnore
	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@JsonIgnore
	@Column(name = "obsolete", columnDefinition = "boolean default false")
	private Boolean obsolete = false;

	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name = "JOURNAL_ID", nullable = false, referencedColumnName = "ID", foreignKey = @ForeignKey(name = "fk_journal_publisher_journal_id"))
	private Journal journal;

	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name = "PUBLISHER_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "fk_journal_publisher_publisher_id"))
	private Publisher publisher;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "DOCUMENT_VERSION_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "fk_journal_publisher_document_version_id"))
	private DocumentVersion documentVersion;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getObsolete() {
		return obsolete;
	}

	public void setObsolete(Boolean obsolete) {
		this.obsolete = obsolete;
	}

	public Journal getJournal() {
		return journal;
	}

	public void setJournal(Journal journal) {
		this.journal = journal;
	}

	public Publisher getPublisher() {
		return publisher;
	}

	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}

	public DocumentVersion getDocumentVersion() {
		return documentVersion;
	}

	public void setDocumentVersion(DocumentVersion documentVersion) {
		this.documentVersion = documentVersion;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		JournalPublisherAffiliation that = (JournalPublisherAffiliation) o;

		return new EqualsBuilder()
			.append(journal, that.journal)
			.append(publisher, that.publisher)
			.append(documentVersion, that.documentVersion)
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(journal)
			.append(publisher)
			.append(documentVersion)
			.toHashCode();
	}
}
