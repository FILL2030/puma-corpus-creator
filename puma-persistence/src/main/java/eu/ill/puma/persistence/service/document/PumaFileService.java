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
package eu.ill.puma.persistence.service.document;

import eu.ill.puma.core.error.PumaException;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileStatus;
import eu.ill.puma.persistence.repository.document.PumaFileRepository;
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
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class PumaFileService {

	private static final Logger log = LoggerFactory.getLogger(PumaFileService.class);

	public static class PumaFilePersistenceException extends PumaException {
		public static PumaFilePersistenceException error(org.slf4j.Logger log, String message) {
			log.error(message);
			return new PumaFilePersistenceException(message);
		}

		public static PumaFilePersistenceException error(org.slf4j.Logger log, String message, Exception cause) {
			log.error(message, cause);
			return new PumaFilePersistenceException(message, cause);
		}

		public PumaFilePersistenceException(String message) {
			super(message);
		}

		public PumaFilePersistenceException(String message, Exception cause) {
			super(message, cause);
		}
	}

	@Value("${puma.persistence.files.root}")
	private String filesRoot;

	@Autowired
	private PumaFileRepository pumaFileRepository;

	@PostConstruct
	public void initialiseFilesRootPath() {
		log.info("Using files root : " + filesRoot);
	}

	public String getFilesRoot() {
		return filesRoot;
	}

	/**
	 * Returns all PumaFiles
	 * @return
	 */
	public List<PumaFile> getAll() {
		return this.pumaFileRepository.getAll();
	}

	/**
	 * Returns the PumaFile by its Id with the data as a byte array
	 * @param id The id of the PumaFile
	 * @return the PumaFile with the given Id
	 */
	public PumaFile getByIdWithData(Long id) throws IOException {
		//get puma file
		PumaFile pumaFile = this.pumaFileRepository.getById(id);

		if (pumaFile != null) {
			//get data
			this.readFileData(pumaFile);
		}

		return pumaFile;
	}

	public void readFileData(PumaFile pumaFile)  {
		File file = new File(filesRoot + File.separator + pumaFile.getFilePath());

		try {
			pumaFile.setData(FileUtils.readFileToByteArray(file));
		} catch (IOException e) {
			log.error("can not get data for pumaFile : " + pumaFile.getId() + " " + pumaFile.getFilePath());
		}
	}

	/**
	 * Returns the PumaFile by its Id
	 * @param id The id of the PumaFile
	 * @return the PumaFile with the given Id
	 */
	public PumaFile getById(Long id) {
		return this.pumaFileRepository.getById(id);
	}

	/**
	 * Returns all files for a document version
	 * @param documentVersion The document version containing files
	 * @return The files of the document version
	 */
	public List<PumaFile> getAllForDocumentVersion(DocumentVersion documentVersion) {
		return this.pumaFileRepository.getAllForDocumentVersion(documentVersion);
	}

	/**
	 * Persists the given PumaFile and ensures we do not create duplicates.
	 *  - if the id is set we assume we have an already persisted object and the object is updated
	 *  - If the PumaFile data match an existing PumaFile then we know it is already persisted
	 * @param pumaFile The PumaFile to be persisted
	 * @return The persisted PumaFile
	 */
	public synchronized PumaFile save(PumaFile pumaFile) throws PumaFilePersistenceException {
		PumaFile integratedPumaFile = null;

		// Check if it is a new object
		if (pumaFile.getId() == null) {
			// Determine if the object already exists
			integratedPumaFile = this.pumaFileRepository.getByPumaFileDetails(pumaFile.getName(), pumaFile.getOriginUrl());
			if (integratedPumaFile != null) {
				log.debug("pumaFile " + pumaFile.getName() + " already present in the db under the id " + integratedPumaFile.getId() + " : merging");

				// Merge new details into integrated one
				this.mergePumaFiles(integratedPumaFile, pumaFile);

				// merge
				integratedPumaFile = this.saveWithFile(integratedPumaFile);

			} else {
				// persist
				integratedPumaFile = this.saveWithFile(pumaFile);
			}

		} else {
			// merge
			integratedPumaFile = this.saveWithFile(pumaFile);
		}

		return integratedPumaFile;
	}

	/**
	 * Persists the given PumaFile and ensures we do not create duplicates.
	 *  - if the id is set we assume we have an already persisted object and the object is updated
	 *  - If the PumaFile data match an existing PumaFile then we know it is already persisted
	 * @param pumaFile The PumaFile to be persisted
	 * @return The persisted PumaFile
	 */
	public synchronized PumaFile saveDBOnly(PumaFile pumaFile) {
		PumaFile integratedPumaFile = null;

		// Check if it is a new object
		if (pumaFile.getId() == null) {
			// Determine if the object already exists
			integratedPumaFile = this.pumaFileRepository.getByPumaFileDetails(pumaFile.getName(), pumaFile.getOriginUrl());
			if (integratedPumaFile != null) {
				log.debug("pumaFile " + pumaFile.getName() + " already present in the db under the id " + integratedPumaFile.getId() + " : merging");

				// Merge new details into integrated one
				this.mergePumaFiles(integratedPumaFile, pumaFile);

				// merge
				integratedPumaFile = this.pumaFileRepository.merge(integratedPumaFile);

			} else {
				// persist
				integratedPumaFile = this.pumaFileRepository.persist(pumaFile);
			}

		} else {
			// merge
			integratedPumaFile = this.pumaFileRepository.merge(pumaFile);
		}

		return integratedPumaFile;
	}

	/**
	 * Copies all data from one puma file to another (persisted) one
	 * @param integratedPumaFile the persisted one
	 * @param fileToMerge the one to copy
	 */
	private void mergePumaFiles(PumaFile integratedPumaFile, PumaFile fileToMerge) {
		integratedPumaFile.setName(fileToMerge.getName());
		integratedPumaFile.setOriginUrl(fileToMerge.getOriginUrl());
		integratedPumaFile.setStatus(fileToMerge.getStatus());
		integratedPumaFile.setDownloadDate(fileToMerge.getDownloadDate());
		integratedPumaFile.setDocumentType(fileToMerge.getDocumentType());
		integratedPumaFile.setMimeType(fileToMerge.getMimeType());
		integratedPumaFile.setPageNumber(fileToMerge.getPageNumber());
		integratedPumaFile.setHash(fileToMerge.getHash());
		integratedPumaFile.setDocumentVersion(fileToMerge.getDocumentVersion());
		integratedPumaFile.setData(fileToMerge.getData());
	}

	/**
	 * Persists the PumaFile (both file and metadata)
	 * @param pumaFile The file to persist
	 * @return The persisted puma file
	 * @throws PumaFilePersistenceException
	 */
	private synchronized PumaFile saveWithFile(PumaFile pumaFile) throws PumaFilePersistenceException {

		// See if file data has changed or is new and save the file first
		if (pumaFile.getData() != null) {
			// Get MD5 checksum of file data
			String md5 = MD5Checksum.getMD5Checksum(pumaFile.getData());

			if (pumaFile.getFilePath() == null || pumaFile.getId() == null) {
				// New file
				this.saveDataFile(pumaFile, md5);

			} else {
				// Get last persisted pumaFile and see if checksums or filenames have changed
				PumaFile previousVersion = this.pumaFileRepository.getById(pumaFile.getId());
				if (!previousVersion.getMd5().equals(md5)) {
					// Save new file data
					this.saveDataFile(pumaFile, md5);

				} else {
					log.info("File already exists at " + pumaFile.getFilePath());
				}
			}

			// Ensure status is saved
			pumaFile.setStatus(PumaFileStatus.SAVED);

		} else {
			pumaFile.setFilePath(null);
			pumaFile.setMd5(null);
		}

		// Persist metadata
		PumaFile integratedPumaFile = null;
		if (pumaFile.getId() == null) {
			integratedPumaFile = this.pumaFileRepository.persist(pumaFile);

		} else {
			integratedPumaFile = this.pumaFileRepository.merge(pumaFile);
		}

		return integratedPumaFile;
	}

	/**
	 * delete the given file
	 * @param pumaFile the file to delete
	 * @return
	 */
	public boolean delete(PumaFile pumaFile){
		String path = filesRoot + File.separator + pumaFile.getFilePath();
		File file = new File(path);

		if(file.exists() && file.canWrite() && file.isFile()&& file.delete()){
			this.pumaFileRepository.delete(pumaFile);
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Creates a file containing the pumafile byte data
	 * @param pumaFile The puma file containing the byte data
	 * @throws PumaFilePersistenceException
	 */
	private void saveDataFile(PumaFile pumaFile, String md5) throws PumaFilePersistenceException {
		DocumentVersion documentVersion = pumaFile.getDocumentVersion();
		if (documentVersion == null || documentVersion.getId() == null) {
			throw PumaFilePersistenceException.error(log, "Cannot persist a file without a Document Version");
		}

		Long documentVersionId = documentVersion.getId();

		//destination folder setup
		String folderPath = (documentVersionId % 100) + File.separator + documentVersionId;
		File folder = new File(filesRoot + File.separator + folderPath);

		String filePath = folderPath + File.separator + pumaFile.getUniqueFileName();
		File file = new File(filesRoot + File.separator + filePath);

		// Create folder if it doesn't already exist
		if (!folder.exists()) {
			try {
				FileUtils.forceMkdir(folder);

			} catch (IOException e) {
				throw PumaFilePersistenceException.error(log, "Cannot create folder " + folderPath);
			}
		}

		try {
			// Write file
			FileUtils.writeByteArrayToFile(file, pumaFile.getData());

			// Store the file path
			pumaFile.setFilePath(filePath);

			// Store file name
			if (pumaFile.getName() == null) {
				pumaFile.setName(pumaFile.getUniqueFileName());
			}

			// Get MD5 checksum of file data
			pumaFile.setMd5(md5);

			//set file size
			pumaFile.setFileSize(pumaFile.getData().length + 0L);

			log.trace("File saved at " + file.getAbsolutePath());

		} catch (IOException e) {
			throw PumaFilePersistenceException.error(log, "Failed to write binary data to file " + file.getAbsolutePath());
		}
	}

	public List<PumaFile> getPendingFiles() {
		return this.pumaFileRepository.getAllForStatus(PumaFileStatus.PENDING);
	}

	public List<PumaFile> getAllRequiringDownload() {
		return this.pumaFileRepository.getAllForStatuses(Arrays.asList(
				PumaFileStatus.PENDING,
				PumaFileStatus.DOWNLOAD_COMPLETED));
	}

	public List<PumaFile> getAllForStatus(PumaFileStatus pumaFileStatus) {
		return this.pumaFileRepository.getAllForStatus(pumaFileStatus);
	}

}
