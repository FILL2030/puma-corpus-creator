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

import eu.ill.puma.persistence.domain.document.DocumentVersionSource;
import eu.ill.puma.persistence.repository.PumaRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DocumentVersionSourceRepository extends PumaRepository<DocumentVersionSource> {

	public DocumentVersionSource getFirstBySourceId(String sourceId) {
		DocumentVersionSource documentVersionSource = this.getFirstEntity("sourceId", sourceId);

		// If we have a document version source then initialise the document version
		if (documentVersionSource != null) {
			Hibernate.initialize(documentVersionSource.getDocumentVersion());

			return documentVersionSource;
		}

		return null;
	}

	public DocumentVersionSource getFirstBySourceIdAndImporterShortName(String sourceId, String importerShortName) {

		List<String> parametersList = new ArrayList();
		parametersList.add("sourceId");
		parametersList.add("importerShortName");

		DocumentVersionSource documentVersionSource = this.getFirstEntity(parametersList, sourceId, importerShortName);

		// If we have a document version source then initialise the document version
		if (documentVersionSource != null) {
			Hibernate.initialize(documentVersionSource.getDocumentVersion());

			return documentVersionSource;
		}

		return null;
	}


	public DocumentVersionSource getLastSourceByImporterShortName(String importerShortName) {
		String queryString = "select dvs from  DocumentVersionSource dvs where dvs.importerShortName = :importerShortName " +
				"order by length(dvs.sourceId) desc, dvs.sourceId desc";

		TypedQuery<DocumentVersionSource> query = entityManager.createQuery(queryString, DocumentVersionSource.class);

		query.setParameter("importerShortName", importerShortName);
		query.setMaxResults(1);

		return query.getSingleResult();
	}


}
