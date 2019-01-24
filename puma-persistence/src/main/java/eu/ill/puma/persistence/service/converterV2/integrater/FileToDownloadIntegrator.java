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
package eu.ill.puma.persistence.service.converterV2.integrater;

import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.core.domain.document.entities.BaseFileToDownload;
import eu.ill.puma.core.domain.document.enumeration.BaseUrlType;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.domain.document.ResolverInfo;
import eu.ill.puma.persistence.service.converterV2.exception.PumaDocumentConversionException;
import eu.ill.puma.persistence.service.converterV2.entitityconverter.FileToDownloadConverter;
import eu.ill.puma.persistence.service.converterV2.entitityconverter.ResolverInfoConverter;
import eu.ill.puma.persistence.service.document.PumaFileService;
import eu.ill.puma.persistence.service.document.ResolverInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileToDownloadIntegrator {

	@Autowired
	private PumaFileService pumaFileService;

	@Autowired
	private ResolverInfoService resolverInfoService;

	public void convert(DocumentVersion documentVersion, BaseDocument baseDocument) throws PumaDocumentConversionException {

		for (BaseFileToDownload baseFileToDownload : baseDocument.getFilesToDownload()) {

			if (baseFileToDownload.getUrlType().equals(BaseUrlType.ARTICLE_PAGE)) {
				ResolverInfo resolverInfo = ResolverInfoConverter.convert(baseFileToDownload);

				if (documentVersion.getResolverInfos().size() == 0) {
					// Only add a resolver info if haven't already got one
					resolverInfo.setDocumentVersion(documentVersion);
					resolverInfo = resolverInfoService.save(resolverInfo);
					documentVersion.addResolverInfo(resolverInfo);
				}
			} else if(baseFileToDownload.getUrlType().equals(BaseUrlType.DIRECT)) {
				PumaFile pumaFile = FileToDownloadConverter.convert(baseFileToDownload);

				if (!documentVersion.getFiles().contains(pumaFile)) {
					pumaFile.setDocumentVersion(documentVersion);
					pumaFile = pumaFileService.saveDBOnly(pumaFile);
					documentVersion.addFile(pumaFile);

				}
			}

		}
	}
}
