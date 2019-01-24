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
package eu.ill.puma.webapi.rest.resolver;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.ill.puma.persistence.domain.document.ResolverInfo;
import eu.ill.puma.persistence.domain.document.enumeration.ResolverInfoStatus;

import java.util.Date;

public class RestResolverInfo {

	@JsonIgnore
	private ResolverInfo resolverInfo;

	public RestResolverInfo(ResolverInfo resolverInfo) {
		this.resolverInfo = resolverInfo;
	}

	@JsonGetter
	public Long getId() {
		return this.resolverInfo.getId();
	}

	@JsonGetter
	public String getOriginUrl() {
		return this.resolverInfo.getOriginUrl();
	}

	@JsonGetter
	public String getResolverHost() {
		return this.resolverInfo.getResolverHost();
	}

	@JsonGetter
	public Long getDocumentVersionId() {
		if (this.resolverInfo.getDocumentVersion() != null) {
			return this.resolverInfo.getDocumentVersion().getId();
		}
		return null;
	}

	@JsonGetter
	public Date getLastResolveDate() {
		return this.resolverInfo.getLastResolveDate();
	}

	@JsonGetter
	public String getResolverError() {
		return this.resolverInfo.getResolverError();
	}

	@JsonGetter
	public ResolverInfoStatus getStatus() {
		return this.resolverInfo.getStatus();
	}
}
