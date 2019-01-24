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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "instrument")
public class Instrument {

	@JsonIgnore
	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Column(name = "code", length = 1000)
	private String code;

	@Column(name = "name", length = 1000)
	private String name;

	@ElementCollection
	@CollectionTable(name = "instrument_alias", joinColumns = @JoinColumn(name = "instrument_id"))
	@Column(name = "alias")
	private List<String> aliases;

	@ManyToMany(mappedBy = "instruments")
	private List<DocumentVersion> documentVersions = new ArrayList<>();

	@JsonIgnore
	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name = "LABORATORY_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "fk_instrument_laboratory_id"))
	private Laboratory laboratory;

	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinTable(name = "instrument_scientific_technique", joinColumns = {
			@JoinColumn(name = "INSTRUMENT_ID", foreignKey = @ForeignKey(name = "fk_instrument_scientific_technique_instrument_id"))}, inverseJoinColumns = {
			@JoinColumn(name = "SCIENTIFIC_TECHNIQUE_ID", foreignKey = @ForeignKey(name = "fk_instrument_scientific_technique_scientific_technique_id"))},
			indexes = {
					@Index(name = "instrument_scientific_technique_instrument_id", columnList = "instrument_id"),
					@Index(name = "instrument_scientific_technique_scientific_technique_id", columnList = "scientific_technique_id")},
			uniqueConstraints = {@UniqueConstraint(columnNames = {"scientific_technique_id", "instrument_id"}, name = "unique_scientific_technique_id_instrument_id")})
	@Where(clause = "obsolete = false")
	private List<ScientificTechnique> scientificTechniques = new ArrayList();

	@JsonIgnore
	@Column(name = "fixed", columnDefinition = "boolean default false")
	private Boolean fixed = false;

	@JsonIgnore
	@Column(name = "analyser_confidence", columnDefinition = "integer default 50", nullable = false)
	private Integer analyserConfidence = 50;

	@JsonIgnore
	@Column(name = "obsolete", columnDefinition = "boolean default false")
	private Boolean obsolete = false;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code == null ? null : code.toLowerCase();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name == null ? null : name.toLowerCase();
	}

	public List<String> getAliases() {
		return aliases;
	}

	public void setAliases(List<String> aliases) {
		this.aliases = aliases;
	}

	public List<DocumentVersion> getDocumentVersions() {
		return documentVersions;
	}

	public void setDocumentVersions(List<DocumentVersion> documentVersions) {
		this.documentVersions = documentVersions;
	}

	public List<ScientificTechnique> getScientificTechniques() {
		return scientificTechniques;
	}

	public void setScientificTechniques(List<ScientificTechnique> scientificTechniques) {
		this.scientificTechniques = scientificTechniques;
	}

	public Laboratory getLaboratory() {
		return laboratory;
	}

	public void setLaboratory(Laboratory laboratory) {
		this.laboratory = laboratory;
	}

	public Boolean getObsolete() {
		return obsolete;
	}

	public void setObsolete(Boolean obsolete) {
		this.obsolete = obsolete;
	}

	public Boolean getFixed() {
		return fixed;
	}

	public void setFixed(Boolean fixed) {
		this.fixed = fixed;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Instrument that = (Instrument) o;

		if (code != null ? !code.equals(that.code) : that.code != null) return false;
		return name != null ? name.equals(that.name) : that.name == null;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + (code != null ? code.hashCode() : 0);
		result = 31 * result + (name != null ? name.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", id)
				.append("code", code)
				.append("name", name)
				.toString();
	}

	@Entity
	@Table(name = "scientific_technique", indexes = {
			@Index(name = "scientific_technique_name_index", columnList = "name")})
	private class ScientificTechnique {

		@Id
		@Column(name = "id", nullable = false)
		@GeneratedValue(strategy = IDENTITY)
		private Long id;

		@Column(name = "name", nullable = false, length = 1000)
		private String name;

		@Column(name = "obsolete", columnDefinition = "boolean default false")
		private Boolean obsolete = false;

		@ManyToMany(mappedBy = "scientificTechniques")
		private List<Instrument> Instruments = new ArrayList<>();

		@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
		@JoinColumn(name = "parent_id", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "fk_scientific_technique_parent_id"))
		private ScientificTechnique parent;
	}

	@Entity
	@Table(name = "scientific_technique_alias", indexes = {
			@Index(name = "scientific_technique_alias_name_index", columnList = "name")})
	private class ScientificTechniqueAlias {

		@Id
		@Column(name = "id", nullable = false)
		@GeneratedValue(strategy = IDENTITY)
		private Long id;

		@Column(name = "name", nullable = false, length = 1000)
		private String name;

		@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
		@JoinColumn(name = "scientific_technique_id", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "fk_scientific_technique_alias_scientific_technique_id"))
		private ScientificTechnique scientificTechnique;
	}

}

