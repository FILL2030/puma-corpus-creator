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
package eu.ill.puma.core.domain.document.entities;

public class BaseFormula  extends BaseEntity {

	private String code = null;
	private String consistence = null;
	private String temperature = null;
	private String pressure = null;
	private String magneticField = null;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getConsistence() {
		return consistence;
	}

	public void setConsistence(String consistence) {
		this.consistence = consistence;
	}

	public String getTemperature() {
		return temperature;
	}

	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}

	public String getPressure() {
		return pressure;
	}

	public void setPressure(String pressure) {
		this.pressure = pressure;
	}

	public String getMagneticField() {
		return magneticField;
	}

	public void setMagneticField(String magneticField) {
		this.magneticField = magneticField;
	}

//	public List<Long> getInstrumentIds() {
//		return instrumentIds;
//	}
//
//	public void setInstrumentIds(List<Long> instrumentIds) {
//		this.instrumentIds = instrumentIds;
//	}
}

