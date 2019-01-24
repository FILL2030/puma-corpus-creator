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

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "laboratory", indexes = {
		@Index(name = "laboratory_name_short_name_address_city_country_index", columnList = "name,short_name,address,city,country")})
public class Laboratory {

	@JsonIgnore
	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Column(name = "name", length = 1000)
	private String name;

	@Column(name = "short_name", nullable = true)
	private String shortName;

	@Column(name = "address", nullable = false)
	private String address;

	@Column(name = "city", nullable = true)
	private String city;

	@Column(name = "country", nullable = true)
	private String country;

	@JsonIgnore
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name="laboratory_id")
	private Set<Instrument> instruments = new HashSet<>();

	@JsonIgnore
	@Column(name = "obsolete", columnDefinition = "boolean default false")
	private Boolean obsolete = false;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name == null ? null : name.toLowerCase();
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName == null ? null : shortName.toLowerCase();
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address == null ? null : address.toLowerCase();
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city == null ? null : city.toLowerCase();
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country == null ? null : country.toLowerCase();
	}

	public Set<Instrument> getInstruments() {
		return instruments;
	}

	public void addInstrument(Instrument instrument) {
		this.instruments.add(instrument);
	}

	public void removeAllInstruments() {
		this.instruments.clear();
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

		Laboratory that = (Laboratory) o;

		if (name != null ? !name.equals(that.name) : that.name != null) return false;
		if (shortName != null ? !shortName.equals(that.shortName) : that.shortName != null) return false;
		if (address != null ? !address.equals(that.address) : that.address != null) return false;
		if (city != null ? !city.equals(that.city) : that.city != null) return false;
		return country != null ? country.equals(that.country) : that.country == null;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (shortName != null ? shortName.hashCode() : 0);
		result = 31 * result + (address != null ? address.hashCode() : 0);
		result = 31 * result + (city != null ? city.hashCode() : 0);
		result = 31 * result + (country != null ? country.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("id", id)
			.append("name", name)
			.append("shortName", shortName)
			.append("address", address)
			.append("city", city)
			.append("country", country)
			.toString();
	}
}
