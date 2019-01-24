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
package eu.ill.puma.persistence.service.importer;

import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.DocumentVersionSource;
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.domain.document.ResolverInfo;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileStatus;
import eu.ill.puma.persistence.domain.document.enumeration.ResolverInfoStatus;
import eu.ill.puma.persistence.domain.importer.CachedImporterFile;
import eu.ill.puma.persistence.repository.importer.CachedImporterFileRepository;
import eu.ill.puma.persistence.service.document.DocumentVersionService;
import eu.ill.puma.persistence.service.document.PumaFileService;
import eu.ill.puma.persistence.service.document.ResolverInfoService;
import eu.ill.puma.persistence.util.MD5Checksum;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class CachedImporterFileService {

	private static final Logger log = LoggerFactory.getLogger(CachedImporterFileService.class);

	@Autowired
	private CachedImporterFileRepository repository;

	@Autowired
	private DocumentVersionService documentVersionService;

	@Autowired
	private PumaFileService pumaFileService;

	@Autowired
	private ResolverInfoService resolverInfoService;

	@Value("${puma.persistence.files.cache.root}")
	private String cachedFilesRoot;

	@PostConstruct
	public void initialiseFilesRootPath() {
		log.info("Using cached files root : " + this.cachedFilesRoot);
	}

	public List<CachedImporterFile> getByImporterNameAndSourceId(String importerShortName, String sourceId) {
		return this.repository.getByImporterNameAndSourceId(importerShortName, sourceId);
	}

	public void copyCachedFilesForDocumentVersion(DocumentVersion documentVersion) {
		// Get all cached files for document
		List<CachedImporterFile> allCachedImporterFiles = new ArrayList<>();
		for (DocumentVersionSource source : documentVersion.getSources()) {
			List<CachedImporterFile> cachedImporterFiles = this.repository.getByImporterNameAndSourceId(source.getImporterShortName(), source.getSourceId());
			allCachedImporterFiles.addAll(cachedImporterFiles);
		}

		List<String> resolverUrls = new ArrayList<>();
		Date now = new Date();

		// Determine if we have cached file
		if (allCachedImporterFiles.size() > 0) {
			// Clear existing puma files and resolver infos and recreate them from cached version
			for (ResolverInfo resolverInfo: documentVersion.getResolverInfos()) {
				this.resolverInfoService.delete(resolverInfo);
			}
			documentVersion.removeAllResolverInfos();

			for (CachedImporterFile cachedImporterFile : allCachedImporterFiles) {
				String resolverUrl = cachedImporterFile.getResolverUrl();
				// Handle resolver infos
				if (resolverUrl != null && !resolverUrls.contains(resolverUrl)) {
					// Create new resolver info
					ResolverInfo resolverInfo = new ResolverInfo();
					resolverInfo.setOriginUrl(resolverUrl);
					resolverInfo.setStatus(ResolverInfoStatus.RESOLVE_COMPLETED);
					resolverInfo.setResolverHost(cachedImporterFile.getResolverHost());
					resolverInfo.setResolveCounter(1);
					resolverInfo.setLastResolveDate(now);
					resolverInfo.setDocumentVersion(documentVersion);

					this.resolverInfoService.save(resolverInfo);

					// Add resolver info to document version
					documentVersion.addResolverInfo(resolverInfo);
					resolverUrls.add(resolverUrl);
				}

				// Create puma file
				PumaFile pumaFile = new PumaFile();
				pumaFile.setDocumentVersion(documentVersion);
				pumaFile.setDocumentType(cachedImporterFile.getDocumentType());
				pumaFile.setMimeType(cachedImporterFile.getMimeType());
				pumaFile.setOriginUrl(cachedImporterFile.getDownloadUrl());
				pumaFile.setName(cachedImporterFile.getFileName());

				// Add it to document version
				documentVersion.addFile(pumaFile);

				if (cachedImporterFile.getFileStatus().equals(PumaFileStatus.SAVED)) {
					// Read old file date and save new file
					byte[] fileData = this.readCachedFile(cachedImporterFile);
					if (fileData != null) {
						log.info("Obtained file from cache (" + cachedImporterFile.getFilePath() + ") for document " + documentVersion.getId());
						pumaFile.setData(fileData);
						pumaFile.setDownloadDate(now);
						try {
							pumaFileService.save(pumaFile);

						} catch (PumaFileService.PumaFilePersistenceException e) {
							log.error("Unable to save file with cached file path " + cachedImporterFile.getFilePath());
							pumaFile.setStatus(PumaFileStatus.DOWNLOAD_FAILED);
							pumaFileService.saveDBOnly(pumaFile);
						}

					} else {
						pumaFile.setStatus(PumaFileStatus.DOWNLOAD_FAILED);
						pumaFileService.saveDBOnly(pumaFile);
					}

				} else {
					pumaFile.setStatus(cachedImporterFile.getFileStatus());
					pumaFileService.saveDBOnly(pumaFile);
				}
			}

//			// save modified document version
//			try {
//				this.documentVersionService.save(documentVersion);
//
//			} catch (Exception e) {
//				// Shouldn't be here...
//				log.error("Unable to save document version with id " + documentVersion.getId());
//			}
		}
	}

	private byte[] readCachedFile(CachedImporterFile cachedImporterFile) {
		String filePath = cachedImporterFile.getFilePath();
		File file = new File(cachedFilesRoot + File.separator + filePath);

		try {
			byte[] fileData = FileUtils.readFileToByteArray(file);

			// verify data
			String md5 = MD5Checksum.getMD5Checksum(fileData);
			long fileSize = (long)fileData.length;

			if (fileSize == cachedImporterFile.getFileSize() && md5.equals(cachedImporterFile.getMd5())) {
				return fileData;

			} else {
				log.error("Cached data and file do not match: " + filePath);
			}

		} catch (IOException e) {
			log.error("can not get cached data for file: " + filePath);
		}
		return null;
	}
}
