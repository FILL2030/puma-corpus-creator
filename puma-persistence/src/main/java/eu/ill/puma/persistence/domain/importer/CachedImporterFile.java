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
package eu.ill.puma.persistence.domain.importer;

import eu.ill.puma.persistence.domain.document.enumeration.PumaFileStatus;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

// How to create data from previous import:
//
//insert into cached_importer_file (
//	importer_short_name,
//	source_id,
//	resolver_url,
//	resolver_host,
//	document_type,
//	file_path,
//	file_name,
//	download_url,
//	file_status,
//	file_size,
//	md5,
//	mime_type
//	)
//	select s.importer_short_name, s.source_id, r.origin_url resolver_url, r.unsupported_resolver resolver_host, f.document_type, f.file_path, f.name file_name, f.origin_url download_url, f.status file_status, f.file_size, f.md5, f.mime_type from file f, document_version_source s, resolver_info r
//	where r.document_version_id = s.document_version_id
//	and f.document_version_id = s.document_version_id
//	order by importer_short_name, source_id;
//
// export this table from the previous DB into the new DB and restart the import

@Entity
@Table(name = "cached_importer_file", indexes = {
		@Index(name = "cached_importer_file_name_source_id_url_index", columnList = "file_name,resolver_url,source_id,download_url")})
public class CachedImporterFile {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Column(name = "importer_short_name", length = 1000, nullable = false)
	String importerShortName;

	@Column(name = "source_id", length = 1000, nullable = false)
	String sourceId;

	@Column(name = "resolver_url", length = 10000)
	private String resolverUrl;

	@Column(name = "resolver_host", length = 10000)
	private String resolverHost;

	@Column(name = "document_type", length = 1000)
	@Enumerated(EnumType.STRING)
	private PumaFileType documentType;

	@Column(name = "file_path", length = 2000)
	private String filePath;

	@Column(name = "file_name", length = 1000)
	private String fileName;

	@Column(name = "download_url", length = 10000)
	private String downloadUrl;

	@Column(name = "file_status", length = 1000)
	@Enumerated(EnumType.STRING)
	private PumaFileStatus fileStatus = PumaFileStatus.PENDING;

	@Column(name = "file_size")
	private Long fileSize;

	@Column(name = "md5", length = 128)
	private String md5;

	@Column(name = "mime_type", length = 1024)
	private String mimeType;

	@Column(name = "cache_tag")
	private String cacheTag;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getImporterShortName() {
		return importerShortName;
	}

	public void setImporterShortName(String importerShortName) {
		this.importerShortName = importerShortName;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getResolverUrl() {
		return resolverUrl;
	}

	public void setResolverUrl(String resolverUrl) {
		this.resolverUrl = resolverUrl;
	}

	public String getResolverHost() {
		return resolverHost;
	}

	public void setResolverHost(String resolverHost) {
		this.resolverHost = resolverHost;
	}

	public PumaFileType getDocumentType() {
		return documentType;
	}

	public void setDocumentType(PumaFileType documentType) {
		this.documentType = documentType;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public PumaFileStatus getFileStatus() {
		return fileStatus;
	}

	public void setFileStatus(PumaFileStatus fileStatus) {
		this.fileStatus = fileStatus;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getCacheTag() {
		return cacheTag;
	}

	public void setCacheTag(String cacheTag) {
		this.cacheTag = cacheTag;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		CachedImporterFile that = (CachedImporterFile) o;

		return new EqualsBuilder()
			.append(importerShortName, that.importerShortName)
			.append(sourceId, that.sourceId)
			.append(resolverUrl, that.resolverUrl)
			.append(resolverHost, that.resolverHost)
			.append(filePath, that.filePath)
			.append(downloadUrl, that.downloadUrl)
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(importerShortName)
			.append(sourceId)
			.append(resolverUrl)
			.append(resolverHost)
			.append(filePath)
			.append(downloadUrl)
			.toHashCode();
	}
}
