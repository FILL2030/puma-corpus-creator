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
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileStatus;
import eu.ill.puma.persistence.repository.PumaDocumentEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;

@Repository
public class PumaFileRepository extends PumaDocumentEntityRepository<PumaFile> {

	private static final Logger log = LoggerFactory.getLogger(PumaFileRepository.class);

	public PumaFile getByPumaFileDetails(String name, String originUrl) {
		return this.getFirstEntity(Arrays.asList("name", "originUrl"), name, originUrl);
	}

	public List<PumaFile> getAllForDocumentVersion(DocumentVersion documentVersion) {
		return this.getEntities("documentVersion", documentVersion);
	}

	public List<PumaFile> getAllForStatus(PumaFileStatus pumaFileStatus) {
		return this.getEntities("status", pumaFileStatus);
	}

	public List<PumaFile> getAllForStatuses(List<PumaFileStatus> pumaFileStatuses) {

		// Build up query string from parameters
		String queryString = "select f from PumaFile f where obsolete = false and status in :statuses order by f.id asc";
		log.debug(queryString);

		// Generate the query
		TypedQuery<PumaFile> query = this.entityManager.createQuery(queryString, PumaFile.class);

		// Set the query parameters
		query.setParameter("statuses", pumaFileStatuses);

		return query.getResultList();
	}
}
