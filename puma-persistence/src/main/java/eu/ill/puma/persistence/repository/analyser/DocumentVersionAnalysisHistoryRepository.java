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
package eu.ill.puma.persistence.repository.analyser;

import eu.ill.puma.persistence.domain.analysis.DocumentVersionAnalysisHistory;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.repository.PumaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;

@Repository
public class DocumentVersionAnalysisHistoryRepository extends PumaRepository<DocumentVersionAnalysisHistory> {

	public DocumentVersionAnalysisHistory getForDocumentVersionAndSuccessfulAnalyser(DocumentVersion documentVersion, String analyserName) {
		return this.getFirstEntity(Arrays.asList("documentVersion", "analyserName", "successful"), documentVersion, analyserName, true);
	}

	public DocumentVersionAnalysisHistory getForDocumentVersion(DocumentVersion documentVersion, String analyserName) {
		return this.getFirstEntity(Arrays.asList("documentVersion", "analyserName"), documentVersion, analyserName);
	}

	public List<DocumentVersionAnalysisHistory> getAllForDocumentVersion(DocumentVersion documentVersion) {
		return this.getEntities("documentVersion", documentVersion);
	}

	public List<DocumentVersionAnalysisHistory> getAllSuccessfulForDocumentVersion(DocumentVersion documentVersion) {
		return this.getEntities(Arrays.asList("documentVersion", "successful"), documentVersion, true);
	}

	public List<DocumentVersionAnalysisHistory> getAllForAnalyser(String analyserName) {
		return this.getEntities("analyserName", analyserName);
	}

	public List<Long> getAllDocumentVersionIdForAnalyserNameContains(String analyserName){
		String queryString = "select dvah.documentVersion.id from  DocumentVersionAnalysisHistory dvah where dvah.analyserName LIKE :analyserName";

		TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);

		query.setParameter("analyserName", "%" + analyserName + "%");

		return query.getResultList();
	}

}
