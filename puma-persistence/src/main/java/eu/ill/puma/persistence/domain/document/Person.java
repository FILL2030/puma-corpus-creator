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

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

import static eu.ill.puma.core.utils.StringComparer.nullOrEmpty;
import static eu.ill.puma.core.utils.StringComparer.nullOrShorterThan;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "person", indexes = {
	@Index(name = "person_details_index", columnList = "firstname,lastname,orcid_id,publication_name,researcher_id,origin_id,email")})
public class Person {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Column(name = "firstname", length = 1000)
	private String firstName;

	@Column(name = "lastname", length = 1000)
	private String lastName;

	@Column(name = "orcid_id", length = 1000)
	private String orcidId;

	@Column(name = "publication_name", length = 1000)
	private String publicationName;

	@Column(name = "researcher_id", length = 1000)
	private String researcherId;

	@Column(name = "origin_id", length = 1000)
	private String originId;

	@Column(name = "email", length = 1000)
	private String email;

	@Column(name = "obsolete", columnDefinition = "boolean default false")
	private Boolean obsolete = false;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName == null ? null : firstName.toLowerCase();
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName == null ? null : lastName.toLowerCase();
	}

	public String getOrcidId() {
		return orcidId;
	}

	public void setOrcidId(String orcidId) {
		this.orcidId = orcidId;
	}

	public String getPublicationName() {
		return publicationName;
	}

	public void setPublicationName(String publicationName) {
		this.publicationName = publicationName == null ? null : publicationName.toLowerCase();
	}

	public String getResearcherId() {
		return researcherId;
	}

	public void setResearcherId(String researcherId) {
		this.researcherId = researcherId;
	}

	public String getOriginId() {
		return originId;
	}

	public void setOriginId(String originId) {
		this.originId = originId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

		Person person = (Person) o;

		if (firstName != null ? !firstName.equals(person.firstName) : person.firstName != null) return false;
		if (lastName != null ? !lastName.equals(person.lastName) : person.lastName != null) return false;
		if (orcidId != null ? !orcidId.equals(person.orcidId) : person.orcidId != null) return false;
		if (publicationName != null ? !publicationName.equals(person.publicationName) : person.publicationName != null)
			return false;
		if (researcherId != null ? !researcherId.equals(person.researcherId) : person.researcherId != null)
			return false;
		return email != null ? email.equals(person.email) : person.email == null;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
		result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
		result = 31 * result + (orcidId != null ? orcidId.hashCode() : 0);
		result = 31 * result + (publicationName != null ? publicationName.hashCode() : 0);
		result = 31 * result + (researcherId != null ? researcherId.hashCode() : 0);
		result = 31 * result + (originId != null ? originId.hashCode() : 0);
		result = 31 * result + (email != null ? email.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("id", id)
			.append("firstName", firstName)
			.append("lastName", lastName)
			.append("orcidId", orcidId)
			.append("publicationName", publicationName)
			.append("researcherId", researcherId)
			.append("originId", originId)
			.append("email", email)
			.toString();
	}

	public void augmentFrom(Person person) {

		// Keep the best first name
		if (!nullOrEmpty(person.getFirstName()) && nullOrShorterThan(firstName, person.getFirstName().length())) {
			this.setFirstName(person.getFirstName());
		}

		// Keep the best last name
		if (!nullOrEmpty(person.getLastName()) && nullOrShorterThan(lastName, person.getLastName().length())) {
			this.setLastName(person.getLastName());
		}

		// Keep the best publisher name
		if (!nullOrEmpty(person.getPublicationName()) && nullOrShorterThan(publicationName, person.getPublicationName().length())) {
			this.setPublicationName(person.getPublicationName());
		}

		// Keep the orcid Id if currently null
		if (!nullOrEmpty(person.getOrcidId()) && orcidId == null) {
			this.setOrcidId(person.getOrcidId());
		}

		// Keep the researcher Id if currently null
		if (!nullOrEmpty(person.getResearcherId()) && researcherId == null) {
			this.setResearcherId(person.getResearcherId());
		}

		// Keep the origin Id if currently null
		if (!nullOrEmpty(person.getOriginId()) && originId == null) {
			this.setOriginId(person.getOriginId());
		}

		// Keep the email if currently null
		if (!nullOrEmpty(person.getEmail()) && email == null) {
			this.setEmail(person.getEmail());
		}
	}
}
