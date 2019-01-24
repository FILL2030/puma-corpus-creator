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
package eu.ill.puma.importermanager.resolver.domain;

import java.util.ArrayList;
import java.util.List;

public class ResolverResponse {

	private String originUrl;
	private String doi;
	private String resolverHost;
	private List<ResolverResponseUrl> urls = new ArrayList<ResolverResponseUrl>();
	private List<ResolverResponseDownloadData> downloads = new ArrayList<ResolverResponseDownloadData>();
	private String message;
	private ResolverResponseCode code;

	public String getOriginUrl() {
		return originUrl;
	}

	public void setOriginUrl(String originUrl) {
		this.originUrl = originUrl;
	}

	public String getDoi() {
		return doi;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public String getResolverHost() {
		return resolverHost;
	}

	public void setResolverHost(String resolverHost) {
		this.resolverHost = resolverHost;
	}

	public List<ResolverResponseUrl> getUrls() {
		return urls;
	}

	public void setUrls(List<ResolverResponseUrl> urls) {
		this.urls = urls;
	}

	public List<ResolverResponseDownloadData> getDownloads() {
		return downloads;
	}

	public void setDownloads(List<ResolverResponseDownloadData> downloads) {
		this.downloads = downloads;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ResolverResponseCode getCode() {
		return code;
	}

	public void setCode(ResolverResponseCode code) {
		this.code = code;
	}
}
