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
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "formula", indexes = {
	@Index(name = "formula_code_consistence_temp_pressure_mag_field_index", columnList = "code,consistence,temperature,pressure,magnetic_field")})
public class Formula {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Column(name = "code", nullable = false, length = 4000)
	private String code;

	@Column(name = "consistence", length = 1000)
	private String consistence;

	@Column(name = "temperature", length = 1000)
	private String temperature;

	@Column(name = "pressure", length = 1000)
	private String pressure;

	@Column(name = "magnetic_field", length = 1000)
	private String magneticField;

	@ManyToMany(mappedBy = "formulas")
	private List<DocumentVersion> documentVersions = new ArrayList<>();

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

	public String getConsistence() {
		return consistence;
	}

	public void setConsistence(String consistence) {
		this.consistence = consistence == null ? null : consistence.toLowerCase();
	}

	public String getTemperature() {
		return temperature;
	}

	public void setTemperature(String temperature) {
		this.temperature = temperature == null ? null : temperature.toLowerCase();
	}

	public String getPressure() {
		return pressure;
	}

	public void setPressure(String pressure) {
		this.pressure = pressure == null ? null : pressure.toLowerCase();
	}

	public String getMagneticField() {
		return magneticField;
	}

	public void setMagneticField(String magneticField) {
		this.magneticField = magneticField == null ? null : magneticField.toLowerCase();
	}

	public List<DocumentVersion> getDocumentVersions() {
		return documentVersions;
	}

	public void addDocumentVersion(DocumentVersion documentVersion) {
		this.documentVersions.add(documentVersion);
	}

	public void removeAllDocumentVersions() {
		this.documentVersions.clear();
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

		Formula formula = (Formula) o;

		if (code != null ? !code.equals(formula.code) : formula.code != null) return false;
		if (consistence != null ? !consistence.equals(formula.consistence) : formula.consistence != null) return false;
		if (temperature != null ? !temperature.equals(formula.temperature) : formula.temperature != null) return false;
		if (pressure != null ? !pressure.equals(formula.pressure) : formula.pressure != null) return false;
		return magneticField != null ? magneticField.equals(formula.magneticField) : formula.magneticField == null;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + (code != null ? code.hashCode() : 0);
		result = 31 * result + (consistence != null ? consistence.hashCode() : 0);
		result = 31 * result + (temperature != null ? temperature.hashCode() : 0);
		result = 31 * result + (pressure != null ? pressure.hashCode() : 0);
		result = 31 * result + (magneticField != null ? magneticField.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("id", id)
			.append("code", code)
			.append("consistence", consistence)
			.append("temperature", temperature)
			.append("pressure", pressure)
			.append("magneticField", magneticField)
			.toString();
	}
}
