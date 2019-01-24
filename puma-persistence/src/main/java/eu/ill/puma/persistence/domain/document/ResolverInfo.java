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

import eu.ill.puma.persistence.domain.document.enumeration.ResolverInfoStatus;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "resolver_info", indexes = {
	@Index(name = "resolver_info_origin_url_index", columnList = "origin_url"),
	@Index(name = "resolver_info_document_version_id_index", columnList = "document_version_id")})
public class ResolverInfo {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Column(name = "origin_url", nullable = false, length = 10000)
	private String originUrl;

	@Column(name = "unsupported_resolver", length = 10000)
	private String resolverHost;

	@Column(name = "resolver_error", length = 10000)
	private String resolverError;

	@Column(name = "last_resolve_date")
	private Date lastResolveDate;

	@Column(name = "resolve_counter")
	private Integer resolveCounter = 0;

	@Column(name = "status", length = 1000)
	@Enumerated(EnumType.STRING)
	private ResolverInfoStatus status = ResolverInfoStatus.PENDING;

	@ManyToOne
	@JoinColumn(name = "DOCUMENT_VERSION_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "fk_resolver_info_document_version_id"))
	private DocumentVersion documentVersion;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOriginUrl() {
		return originUrl;
	}

	public void setOriginUrl(String originUrl) {
		this.originUrl = originUrl;
	}

	public String getResolverHost() {
		return resolverHost;
	}

	public void setResolverHost(String resolverHost) {
		this.resolverHost = resolverHost;
	}

	public String getResolverError() {
		return resolverError;
	}

	public void setResolverError(String resolverError) {
		this.resolverError = resolverError;
	}

	public ResolverInfoStatus getStatus() {
		return status;
	}

	public void setStatus(ResolverInfoStatus status) {
		this.status = status;
	}

	public Date getLastResolveDate() {
		return lastResolveDate;
	}

	public void setLastResolveDate(Date lastResolveDate) {
		this.lastResolveDate = lastResolveDate;
	}

	public Integer getResolveCounter() {
		return resolveCounter == null ? 0 : resolveCounter;
	}

	public void setResolveCounter(Integer resolveCounter) {
		this.resolveCounter = resolveCounter;
	}

	public DocumentVersion getDocumentVersion() {
		return documentVersion;
	}

	public void setDocumentVersion(DocumentVersion documentVersion) {
		this.documentVersion = documentVersion;
	}

	public void removeDocumentVersion() {
		this.documentVersion = null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ResolverInfo pumaFile = (ResolverInfo) o;

		return originUrl != null ? originUrl.equals(pumaFile.originUrl) : pumaFile.originUrl == null;
	}

	@Override
	public int hashCode() {
		int result = originUrl != null ? originUrl.hashCode() : 0;
		return result;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("id", id)
			.append("originUrl", originUrl)
			.append("resolverHost", resolverHost)
			.append("resolverError", resolverError)
			.append("lastResolveDate", lastResolveDate)
			.append("resolveCounter", resolveCounter)
			.append("status", status)
			.toString();
	}
}
