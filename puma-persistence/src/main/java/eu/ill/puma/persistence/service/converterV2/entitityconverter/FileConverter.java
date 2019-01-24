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
package eu.ill.puma.persistence.service.converterV2.entitityconverter;

import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.core.domain.document.entities.BaseFile;
import eu.ill.puma.core.domain.document.enumeration.BaseFileType;
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileStatus;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileType;
import eu.ill.puma.persistence.service.converterV2.exception.PumaDocumentConversionException;
import eu.ill.puma.persistence.util.MD5Checksum;
import org.apache.commons.codec.binary.StringUtils;

import java.util.Base64;
import java.util.Date;

public class FileConverter {

	public static PumaFile convert(BaseFile importerFile, BaseDocument importerDocument) throws PumaDocumentConversionException {

		PumaFile pumaFile = new PumaFile();
		pumaFile.setName(importerFile.getName());
		pumaFile.setMimeType(importerFile.getMimeType());
//		if (importerFile.getPumaId() != null) {
//			pumaFile.setId(Math.abs(importerFile.getPumaId()));
//		}

		// Set origin URL to the file name if origin from importer is null
		if (importerFile.getOriginUrl() == null) {//null:proposal.pdf-pdfbox-fulltext.txt
			pumaFile.setOriginUrl(importerDocument.getSourceId() + ":" + importerFile.getName());
		} else {
			pumaFile.setOriginUrl(importerFile.getOriginUrl());
		}

		if (importerFile.getPageNumber() != null) {
			pumaFile.setPageNumber(importerFile.getPageNumber().shortValue());
		}

		pumaFile.setStatus(PumaFileStatus.DOWNLOAD_COMPLETED);
		pumaFile.setDocumentType(convertFileType(importerFile.getType()));

		byte[] decoded;
		if (importerFile.getBase64Encoded()) {
			// Decode base64 data to byte array
			decoded = Base64.getDecoder().decode(importerFile.getData());

		} else {
			// Convert to byte date
			decoded = StringUtils.getBytesUtf8(importerFile.getData());
		}
		pumaFile.setData(decoded);

		// Verify md5
		String md5 = MD5Checksum.getMD5Checksum(decoded).toLowerCase();
		if (!md5.equals(importerFile.getMd5().toLowerCase())) {
			throw new PumaDocumentConversionException("MD5 checksum does not match received data");
		}

		pumaFile.setDownloadDate(new Date());

		return pumaFile;
	}


	private static PumaFileType convertFileType(BaseFileType importerFileType) throws PumaDocumentConversionException {
		PumaFileType pumaFileType = PumaFileType.valueOf(importerFileType.toString());

		if (pumaFileType == null) {
			throw new PumaDocumentConversionException("Could not convert file type " + importerFileType);
		}

		return pumaFileType;
	}
}
