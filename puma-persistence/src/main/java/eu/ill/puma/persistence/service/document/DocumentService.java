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

import eu.ill.puma.persistence.domain.document.Document;
import eu.ill.puma.persistence.repository.document.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DocumentService {

	@Autowired
	private DocumentRepository documentRepository;

	/**
	 * returns all documents
	 * @return A list containing all documents
	 */
	public List<Document> getAll() {
		return this.documentRepository.getAll();
	}

	/**
	 * Returns a specific document
	 * @param id The id of the document
	 * @return The corresponding document
	 */
	public Document getById(Long id) {
		return this.documentRepository.getById(id);
	}

	/**
	 * Persists the document
	 * @param document The document to be persisted
	 */
	public synchronized Document save(Document document) {
		if (document.getId() == null) {
			return this.documentRepository.persist(document);

		} else {
			// Merge
			return this.documentRepository.merge(document);
		}
	}

}
