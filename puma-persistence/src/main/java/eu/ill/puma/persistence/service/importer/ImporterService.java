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
import eu.ill.puma.persistence.domain.importer.Importer;
import eu.ill.puma.persistence.domain.importer.ImporterOperation;
import eu.ill.puma.persistence.domain.importer.ImporterOperationStatus;
import eu.ill.puma.persistence.repository.importer.ImporterOperationRepository;
import eu.ill.puma.persistence.repository.importer.ImporterRepository;
import eu.ill.puma.persistence.service.document.DocumentVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ImporterService {

	@Autowired
	private ImporterRepository importerRepository;

	@Autowired
	private ImporterOperationRepository importerOperationRepository;

	@Autowired
	private DocumentVersionService documentVersionService;

	/**
	 * returns all Importers
	 * @return list of all importers
	 */
	public List<Importer> getAll() {
		return this.importerRepository.getAll();
	}

	/**
	 * returns the required importer
	 * @param id The Id of the importer
	 * @return The required importer
	 */
	public Importer getById(Long id) {
		return this.importerRepository.getById(id);
	}


	/**
	 * Adds an importer, verifying that one doesn't already exist with the same URL
	 * @param importer The importer to add
	 * @return the new importer
	 */
	public Importer addImporter(Importer importer) {
		// Get with existing url
		Importer importerWithUrl = this.importerRepository.getByImporterDetails(importer.getUrl(), importer.getName());
		if (importerWithUrl == null) {
			return this.importerRepository.persist(importer);

		} else {
			return importerWithUrl;
		}
	}

	/**
	 * Save the given importer
	 * @param importer to persist
	 * @return The importer after persist
	 */
	public synchronized Importer save(Importer importer) {
		if (importer.getId() == null) {
			return this.importerRepository.persist(importer);

		} else {
			// Merge
			return this.importerRepository.merge(importer);
		}
	}


	/**
	 * deletes an Importer
	 */
	public synchronized void delete(Importer importer) {
		this.importerRepository.delete(importer);
	}

	/**
	 * updates an importer
	 */
	public synchronized Importer update(Importer importer) {
		Importer existingImporter = this.getById(importer.getId());
		if (existingImporter != null) {
			existingImporter.setName(importer.getName());
			existingImporter.setShortName(importer.getShortName());
			existingImporter.setUrl(importer.getUrl());
		}

		return this.save(existingImporter);
	}


	/**
	 * Adds a new operation to the importer
	 * @param importer The importer to which the operation is added
	 * @param importerOperation The operation to add to the importer
	 * @return The persisted operation
	 */
	public synchronized ImporterOperation addImporterOperation(Importer importer, ImporterOperation importerOperation) {
		// Set the default status
		importerOperation.setStatus(ImporterOperationStatus.PENDING);

		// Set the importer Id
		importerOperation.setImporter(importer);

		// Set the creation date
		importerOperation.setCreationDate(new Date());

		// Add the operation to the importer
		importer.addOperation(importerOperation);

		// Update the importer
		Importer integratedImporter = this.save(importer);

		// Get newest operation
		long biggestId = 0;
		ImporterOperation integratedOperationToReturn = null;
		for (ImporterOperation integratedOperation : integratedImporter.getOperations()) {
			if (integratedOperationToReturn == null || integratedOperation.getId().longValue() > integratedOperationToReturn.getId().longValue() ) {
				integratedOperationToReturn = integratedOperation;
			}
		}

		return integratedOperationToReturn;
	}

	/**
	 * Returns an importer operation by its Id
	 * @param id The operation Id
	 * @return The importer operation
	 */
	public ImporterOperation getOperationById(Long id) {
		return importerOperationRepository.getById(id);
	}

	/**
	 * Updates an importer operation
	 * @param importerOperation The operation to update
	 * @return The persisted operation
	 */
	public synchronized ImporterOperation updateOperation(ImporterOperation importerOperation) {
		if (importerOperation.getId() == null) {
			return importerOperationRepository.persist(importerOperation);

		} else {
			// merge
			return importerOperationRepository.merge(importerOperation);
		}
	}

	/**
	 * Deletes an importer operation
	 * @param importerOperation The operation to delete
	 */
	public synchronized void deleteOperation(ImporterOperation importerOperation) {
		this.importerOperationRepository.delete(importerOperation);
	}

	/**
	 * Returns all operations with status set as Running
	 * @return a list of all running operations
	 */
	public List<ImporterOperation> getRunningOperations() {
		return this.importerOperationRepository.getByStatus(ImporterOperationStatus.RUNNING);
	}

	/**
	 * Returns all previous operations
	 * @return a list of all running operations
	 */
	public List<ImporterOperation> getPreviousOperations() {
		return this.importerOperationRepository.getAll()
			.stream()
			.filter(operation -> !operation.getStatus().equals(ImporterOperationStatus.RUNNING))
			.collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * Returns all previous operations for given importer
	 * @return a list of all running operations
	 */
	public List<ImporterOperation> getPreviousOperationsForImporter(Importer importer) {
		return this.importerOperationRepository.getByImporter(importer)
			.stream()
			.filter(operation -> !operation.getStatus().equals(ImporterOperationStatus.RUNNING))
			.collect(Collectors.toCollection(ArrayList::new));
	}

	public boolean isImporterShortName(String candidate){
		return importerRepository.isImporterShortName(candidate);
	}

	public boolean isImporterName(String candidate){
		return importerRepository.isImporterName(candidate);
	}

	public DocumentVersion getNextDocumentVersionForReimport(Importer importer, Long lastReimportId) {
		return this.documentVersionService.getNextDocumentVersionForReimport(importer.getShortName(), lastReimportId);
	}
}
