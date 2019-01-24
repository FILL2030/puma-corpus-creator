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
import eu.ill.puma.core.domain.document.entities.BaseFile;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.service.converterV2.EntityOriginStore;
import eu.ill.puma.persistence.service.converterV2.exception.PumaDocumentConversionException;
import eu.ill.puma.persistence.service.converterV2.entitityconverter.FileConverter;
import eu.ill.puma.persistence.service.document.PumaFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class FileIntegrator {

	private static final Logger log = LoggerFactory.getLogger(FileIntegrator.class);

	@Autowired
	private PumaFileService fileService;

	public void convert(DocumentVersion documentVersion, BaseDocument baseDocument, EntityOriginStore entityOriginStore) throws PumaDocumentConversionException, PumaFileService.PumaFilePersistenceException {

		for (BaseFile baseFile : baseDocument.getFiles()) {

			Date date = new Date();

			//create file
			if (baseFile.getPumaId() == null) {
				//new file
				PumaFile fileToAdd = FileConverter.convert(baseFile, baseDocument);
				fileToAdd.setDocumentVersion(documentVersion);

				//save file
				try {
					fileToAdd = fileService.save(fileToAdd);
				} catch (PumaFileService.PumaFilePersistenceException e) {
					log.error("Failed to save puma file", e);
					throw new PumaDocumentConversionException("Failed to save file " + fileToAdd);
				}
				documentVersion.addFile(fileToAdd);

				//create entity origin
				//entityOriginStore.foundEntity(fileToAdd.getId(), EntityType.FILE, baseFile.getConfidence());

				//update File
			} else if (baseFile.getPumaId() != null && baseFile.getPumaId() > 0) {
				Optional<PumaFile> fileToUpdate = documentVersion.getFileById(baseFile.getPumaId());

				if (fileToUpdate.isPresent()) {
					if (baseFile.getHash() != null) {
						fileToUpdate.get().setHash(baseFile.getHash());
					}

					if (baseFile.getPageNumber() != null) {
						fileToUpdate.get().setPageNumber(baseFile.getPageNumber().shortValue());
					}

					fileService.saveDBOnly(fileToUpdate.get());
				}
			}
		}
	}

}
