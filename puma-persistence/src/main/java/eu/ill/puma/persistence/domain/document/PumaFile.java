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

import eu.ill.puma.persistence.domain.document.enumeration.PumaFileStatus;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileType;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "file", indexes = {
		@Index(name = "file_name_origin_url_index", columnList = "name,origin_url"),
	@Index(name = "file_document_version_id_index", columnList = "document_version_id")})
public class PumaFile {

	private static final Logger log = LoggerFactory.getLogger(PumaFile.class);

	public static final String PDF_MIME_TYPE = "application/pdf";
	public static final String XML_MIME_TYPE = "text/xml";
	public static final String HTML_MIME_TYPE = "text/html";

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Column(name = "name", length = 1000)
	private String name;

	@Column(name = "origin_url", length = 10000)
	private String originUrl;

	@Column(name = "status", length = 1000)
	@Enumerated(EnumType.STRING)
	private PumaFileStatus status = PumaFileStatus.PENDING;

	@Column(name = "download_date")
	private Date downloadDate;

	@Column(name = "file_path", length = 2000)
	private String filePath;

	@Column(name = "file_size")
	private Long fileSize;

	@Column(name = "md5", length = 128)
	private String md5;

	@Column(name = "document_type", length = 1000)
	@Enumerated(EnumType.STRING)
	private PumaFileType documentType;

	@Column(name = "mime_type", length = 1024)
	private String mimeType;

	@Column(name = "page_number")
	private Short pageNumber;

	@Column(name = "hash", length = 10000)
	private String hash;

	@ManyToOne
	@JoinColumn(name = "DOCUMENT_VERSION_ID", referencedColumnName = "ID", nullable = true, foreignKey = @ForeignKey(name = "fk_file_document_version_id"))
	private DocumentVersion documentVersion;

	@Column(name = "obsolete", columnDefinition = "boolean default false")
	private Boolean obsolete = false;

	@Transient
	private byte[] data;

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
		this.name = name;
	}

	public String getOriginUrl() {
		return originUrl;
	}

	public void setOriginUrl(String originUrl) {
		this.originUrl = originUrl;
	}

	public PumaFileStatus getStatus() {
		return status;
	}

	public void setStatus(PumaFileStatus status) {
		this.status = status;
	}

	public Date getDownloadDate() {
		return downloadDate;
	}

	public void setDownloadDate(Date downloadDate) {
		this.downloadDate = downloadDate;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
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

	public PumaFileType getDocumentType() {
		return documentType;
	}

	public void setDocumentType(PumaFileType documentType) {
		this.documentType = documentType;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public Short getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(Short pageNumber) {
		this.pageNumber = pageNumber;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
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

	public Boolean getObsolete() {
		return obsolete;
	}

	public void setObsolete(Boolean obsolete) {
		this.obsolete = obsolete;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getUniqueFileName() {
		// Convert hash to string
		return Integer.toHexString(this.hashCode()) + (this.name == null ? "" : "-" + this.name) + this.createFileExtension();
	}

	public String createFileExtension() {

		if (this.mimeType == null) {
			return "";
		}

		try {
			MimeType mimeType = MimeTypes.getDefaultMimeTypes().forName(this.mimeType);

			String extension = mimeType.getExtension();

			// Verify that the name doesn't already end with the same extension
			if (this.name != null && this.name.endsWith(extension)) {
				return "";
			}

			return extension;

		} catch (MimeTypeException e) {
			log.debug("could not get extension for mime type " + this.mimeType);
		}

		return "";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PumaFile pumaFile = (PumaFile) o;

		if (originUrl != null ? !originUrl.equals(pumaFile.originUrl) : pumaFile.originUrl != null) return false;
		if (documentType != pumaFile.documentType) return false;
		return mimeType != null ? mimeType.equals(pumaFile.mimeType) : pumaFile.mimeType == null;
	}

	@Override
	public int hashCode() {
		int result = originUrl != null ? originUrl.hashCode() : 0;
		result = 31 * result + (documentType != null ? documentType.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", id)
				.append("name", name)
				.append("originUrl", originUrl)
				.append("status", status)
				.append("downloadDate", downloadDate)
				.append("filePath", filePath)
				.append("fileSize", fileSize)
				.append("md5", md5)
				.append("documentType", documentType)
				.append("mimeType", mimeType)
				.append("pageNumber", pageNumber)
				.append("hash", hash)
				.toString();
	}
}
