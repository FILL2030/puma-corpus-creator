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

import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileStatus;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileType;
import eu.ill.puma.persistence.util.MD5Checksum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Date;

public class ResolverResponseDownloadData {

	private static final Logger log = LoggerFactory.getLogger(ResolverResponseDownloadData.class);

	private ResolverResponseUrl url;
	private String data;
	private String md5Checksum;
	private String mimeType;

	public ResolverResponseUrl getUrl() {
		return url;
	}

	public void setUrl(ResolverResponseUrl url) {
		this.url = url;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getMd5Checksum() {
		return md5Checksum;
	}

	public void setMd5Checksum(String md5Checksum) {
		this.md5Checksum = md5Checksum;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public PumaFile convertToPumaFile() {
		PumaFile pumaFile = new PumaFile();

		pumaFile.setOriginUrl(this.url.getUrl());
		pumaFile.setDocumentType(this.convertFileType(this.url.getFileType()));
		pumaFile.setMimeType(this.mimeType);
		pumaFile.setStatus(PumaFileStatus.DOWNLOAD_COMPLETED);

		// Decode base64 data to byte array
		byte[] decoded = Base64.getDecoder().decode(this.data);
		pumaFile.setData(decoded);

		// Verify md5
		String md5 = MD5Checksum.getMD5Checksum(decoded).toLowerCase();

		if (!md5.equals(this.md5Checksum.toLowerCase())) {
			log.error("MD5 checksum does not match received data from resolver");
			return null;
		}

		pumaFile.setMd5(this.md5Checksum);
		pumaFile.setData(decoded);
		pumaFile.setDownloadDate(new Date());

		return pumaFile;
	}

	public PumaFileType convertFileType(ResolverResponseUrlFileType resolverResponseUrlFileType) {
		PumaFileType pumaFileType = PumaFileType.valueOf(resolverResponseUrlFileType.toString());

		if (pumaFileType == null) {
			pumaFileType = PumaFileType.UNKNOWN;
			log.warn("Could not convert file type " + resolverResponseUrlFileType);
		}

		return pumaFileType;
	}
}
