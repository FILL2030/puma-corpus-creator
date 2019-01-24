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

import eu.ill.puma.core.domain.document.entities.BaseFileToDownload;
import eu.ill.puma.core.domain.document.enumeration.BaseFileType;
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileType;
import eu.ill.puma.persistence.service.converterV2.exception.PumaDocumentConversionException;

public class FileToDownloadConverter {

	public static PumaFile convert(BaseFileToDownload importerFileToDownload) throws PumaDocumentConversionException {

		PumaFile pumaFile = new PumaFile();
		pumaFile.setOriginUrl(importerFileToDownload.getUrl());
		pumaFile.setDocumentType(convertFileType(importerFileToDownload.getType()));
//		if (importerFileToDownload.getPumaId() != null) {
//			pumaFile.setId(Math.abs(importerFileToDownload.getPumaId()));
//		}
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
