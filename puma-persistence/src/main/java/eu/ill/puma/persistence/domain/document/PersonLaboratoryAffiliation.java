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
import eu.ill.puma.persistence.domain.document.enumeration.PersonRole;
import org.apache.commons.lang3.builder.EqualsBuilder;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "person_laboratory_affiliation"
		, uniqueConstraints = {@UniqueConstraint(columnNames = {"person_id", "laboratory_id", "document_version_id"}, name = "unique_person_id_laboratory_id_document_version_id")}
		, indexes = {@Index(name = "person_laboratory_affiliation_person_id_index", columnList = "PERSON_ID"),
	@Index(name = "person_laboratory_affiliation_laboratory_id_index", columnList = "LABORATORY_ID"),
	@Index(name = "person_laboratory_affiliation_document_version_id_index", columnList = "document_version_id")})
public class
PersonLaboratoryAffiliation {

	@JsonIgnore
	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@JsonIgnore
	@Column(name = "obsolete", columnDefinition = "boolean default false")
	private Boolean obsolete = false;

	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name = "PERSON_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "fk_person_laboratory_affiliation_person_id"))
	private Person person;

	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name = "LABORATORY_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "fk_person_laboratory_affiliation_laboratory_id"))
	private Laboratory laboratory;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "DOCUMENT_VERSION_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "fk_person_laboratory_affiliation_document_version_id"))
	private DocumentVersion documentVersion;

	@ElementCollection
	@Enumerated(EnumType.STRING)
	@JoinTable(name = "person_laboratory_affiliation_role", indexes = {@Index(name = "person_laboratory_affiliation_id_index", columnList = "person_laboratory_affiliation_id")}, joinColumns = {
		@JoinColumn(name = "person_laboratory_affiliation_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_person_laboratory_affiliation_id"))})
	@Column(name="role", length = 100)
	private Set<PersonRole> roles = new HashSet<>();

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

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public Laboratory getLaboratory() {
		return laboratory;
	}

	public void setLaboratory(Laboratory laboratory) {
		this.laboratory = laboratory;
	}

	public DocumentVersion getDocumentVersion() {
		return documentVersion;
	}

	public void setDocumentVersion(DocumentVersion documentVersion) {
		this.documentVersion = documentVersion;
	}


	public Set<PersonRole> getRoles() {
		return roles;
	}

	public void addRole(PersonRole personRole) {
		this.roles.add(personRole);
	}


//	@Override
//	public boolean equals(Object o) {
//		if (this == o) return true;
//		if (o == null || getClass() != o.getClass()) return false;
//
//		PersonLaboratoryAffiliation that = (PersonLaboratoryAffiliation) o;
//
//		if ((person != null && person.getId() != null)
//				? !person.getId().
//				equals(that.person.getId()) :
//				(that.person != null && that.person.getId()
//				!= null)) return false;
//		if ((laboratory != null && laboratory.getId() != null) ? !laboratory.getId().equals(that.laboratory.getId()) : (that.laboratory != null && that.laboratory.getId() != null)) return false;
//		return (documentVersion != null && documentVersion.getId() != null) ? documentVersion.getId().equals(that.documentVersion.getId()) : (that.documentVersion == null && that.documentVersion.getId() != null);
//	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		PersonLaboratoryAffiliation that = (PersonLaboratoryAffiliation) o;

		return new EqualsBuilder()
				.append(obsolete, that.obsolete)
				.append(person, that.person)
				.append(laboratory, that.laboratory)
				.append(documentVersion, that.documentVersion)
				.isEquals();
	}

	@Override
	public int hashCode() {
		int result = (person != null && person.getId() != null) ? person.getId().hashCode() : 0;
		result = 31 * result + ((laboratory != null && laboratory.getId() != null) ? laboratory.getId().hashCode() : 0);
		result = 31 * result + ((documentVersion != null && documentVersion.getId() != null) ? documentVersion.getId().hashCode() : 0);
		return result;
	}

}
