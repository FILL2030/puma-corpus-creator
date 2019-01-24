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
package eu.ill.puma.webapi.rest.importer;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.ill.puma.persistence.domain.importer.Importer;
import eu.ill.puma.persistence.domain.importer.ImporterOperation;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class RestImporter {

	@JsonIgnore
	private Importer importer;

	public RestImporter() {
		this.importer = new Importer();
	}

	public RestImporter(String name, String shortName, String url) {
		this.importer = new Importer();
		this.importer.setName(name);
		this.importer.setShortName(shortName);
		this.importer.setUrl(url);
	}

	public RestImporter(Importer importer) {
		this.importer = importer;

	}

	@JsonGetter
	@ApiModelProperty(example = "null", required = true, value = "")
	@NotNull
	public Long getId() {
		return this.importer.getId();
	}

	public void setId(Long id) {
		this.importer.setId(id);
	}


	@JsonGetter
	@ApiModelProperty(example = "null", required = true, value = "")
	@NotNull
	public String getName() {
		return this.importer.getName();
	}

	public void setName(String name) {
		this.importer.setName(name);
	}

	@JsonGetter
	@ApiModelProperty(example = "null", required = true, value = "")
	@NotNull
	public String getShortName() {
		return this.importer.getShortName();
	}

	public void setShortName(String name) {
		this.importer.setShortName(name);
	}

	@JsonGetter
	@ApiModelProperty(example = "null", required = true, value = "")
	@NotNull
	public String getUrl() {
		return this.importer.getUrl();
	}

	public void setUrl(String url) {
		this.importer.setUrl(url);
	}

	@JsonGetter
	public List<RestImporterOperation> getOperations() {
		List<RestImporterOperation> operations = new ArrayList<>();

		if (importer.getOperations() != null) {
			for (ImporterOperation importerOperation : importer.getOperations()) {
				operations.add(new RestImporterOperation(importerOperation));
			}
		}

		return operations;
	}

	public Importer getImporter() {
		return this.importer;
	}

}
