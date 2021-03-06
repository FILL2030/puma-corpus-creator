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

import java.util.ArrayList;
import java.util.List;

public class RestResolverInfoResponse {

	@JsonIgnore
	private int numberOfResolverInfos;

	private List<RestResolverInfo> resolverInfos = new ArrayList<>();

	public void addResolverInfo(RestResolverInfo resolverInfo) {
		this.resolverInfos.add(resolverInfo);
	}

	@JsonGetter
	public int getNumberOfResolverInfos() {
		return this.resolverInfos.size();
	}

	@JsonGetter
	public List<RestResolverInfo> getResolverInfos() {
		return this.resolverInfos;
	}

}
