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
package eu.ill.puma.persistence.repository.document;

import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.repository.PumaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

@Repository
public class DocumentVersionRepository extends PumaRepository<DocumentVersion> {

	public List<Long> getIdsWithIndexationDateBefore(Date date) {
		// get doc ids with indexationDate < given date or null
		String queryString = "select distinct(d.id) from DocumentVersion d, PumaFile f" +
			" where f.documentVersion.id = d.id" +
			" and f.documentType = 'EXTRACTED_FULL_TEXT'" +
			" and f.filePath is not null" +
			" and (d.indexationDate < :date or d.indexationDate is null)" +
			" order by d.id";

		TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);

		query.setParameter("date", date);

		return query.getResultList();
	}

	public List<Long> getAllIdsForIndexation() {
		// get all doc ids that can be indexed
		String queryString = "select distinct(d.id) from DocumentVersion d, PumaFile f" +
			" where f.documentVersion.id = d.id" +
			" and f.documentType = 'EXTRACTED_FULL_TEXT'" +
			" and f.filePath is not null" +
			" order by d.id";

		TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);

		return query.getResultList();
	}

	public List<Long> getRemainingIdsForIndexation() {
		// get all doc ids that can be indexed
		String queryString = "select distinct(d.id) from DocumentVersion d, PumaFile f" +
			" where f.documentVersion.id = d.id" +
			" and f.documentType = 'EXTRACTED_FULL_TEXT'" +
			" and f.filePath is not null" +
			" and d.indexationDate is null" +
			" order by d.id";

		TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);

		return query.getResultList();
	}

	public Long getNumberRemainingForIndexation() {
		// get all doc ids that can be indexed
		String queryString = "select count(distinct d) from DocumentVersion d, PumaFile f" +
			" where f.documentVersion.id = d.id" +
			" and f.documentType = 'EXTRACTED_FULL_TEXT'" +
			" and f.filePath is not null" +
			" and d.indexationDate is null";

		TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);

		return query.getSingleResult();
	}


	public int resetAllIndexation() {
		// get all doc ids that can be indexed
		String queryString = "update DocumentVersion d set d.indexationDate = null";

		int updated = entityManager.createQuery(queryString).executeUpdate();

		return updated;
	}


	public boolean canDocumentWithIdBeIndexed(Long id) {
		// get all doc ids that can be indexed
		String queryString = "select distinct(d.id) from DocumentVersion d, PumaFile f" +
			" where f.documentVersion.id = d.id" +
			" and d.id = :id" +
			" and f.documentType = 'EXTRACTED_FULL_TEXT'" +
			" and f.filePath is not null" +
			" order by d.id";

		TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);

		query.setParameter("id", id);

		try {
			query.getSingleResult();

			return true;

		} catch (NoResultException e) {
			return false;
		}

	}

	public DocumentVersion getNextDocumentVersionForReimport(String importerShortName, Long offsetId) {
		// All documents with entities
//		String queryString = "select d from DocumentVersion d, DocumentVersionSource s, DocumentVersionEntityOrigin o" +
//			" where s.documentVersion.id = d.id" +
//			" and o.documentVersion.id = d.id" +
//			" and s.importerShortName = :importerShortName" +
//			" and d.id > :offsetId" +
//			" order by d.id";

		// All documents
		String queryString = "select d from DocumentVersion d, DocumentVersionSource s" +
			" where s.documentVersion.id = d.id" +
			" and s.importerShortName = :importerShortName" +
			" and d.id > :offsetId" +
			" order by d.id";

		TypedQuery<DocumentVersion> query = entityManager.createQuery(queryString, DocumentVersion.class);
		// Set the query parameters
		query.setParameter("importerShortName", importerShortName);
		query.setParameter("offsetId", offsetId);
		query.setMaxResults(1);

		try {
			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}
}
