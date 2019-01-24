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

import eu.ill.puma.persistence.domain.analysis.DocumentVersionAnalysisState;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileType;
import eu.ill.puma.persistence.repository.PumaRepository;
import org.hibernate.exception.GenericJDBCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class DocumentVersionAnalysisStateRepository extends PumaRepository<DocumentVersionAnalysisState> {

	private static final Logger log = LoggerFactory.getLogger(DocumentVersionAnalysisState.class);

	private static final List<PumaFileType> VALID_DOCUMENT_TYPES_FOR_ANALYSIS = Arrays.asList(
		PumaFileType.PROPOSAL,
		PumaFileType.PUBLICATION,
		PumaFileType.PROCEEDING,
		PumaFileType.MEETING_ABSTRACT,
		PumaFileType.REVIEW,
		PumaFileType.LETTER,
		PumaFileType.THESIS,
		PumaFileType.PATENT,
		PumaFileType.REPORT,
		PumaFileType.OTHER,
		PumaFileType.PUBLICATION_FIRST_PAGE
	);

	public DocumentVersionAnalysisState getByDocumentVersion(DocumentVersion documentVersion) {
		return this.getFirstEntity("documentVersion", documentVersion);
	}

	public List<DocumentVersionAnalysisState> getAllRequiringAnalysis(String analysisSetup) {
		TypedQuery<DocumentVersionAnalysisState> query = this.getRequiringAnalysisQuery(analysisSetup);
		return query.getResultList();
	}


	public List<DocumentVersionAnalysisState> getNextGroupRequiringAnalysis(String analysisSetup, int limit) {
		TypedQuery<DocumentVersionAnalysisState> query = this.getRequiringAnalysisQuery(analysisSetup);
		return query.setMaxResults(limit).getResultList();
	}

	public List<Long> getNextGroupIdsRequiringAnalysis(String analysisSetup, int limit) {
		// Build up query string from parameters
		String queryString = "select distinct d.documentVersion.id from DocumentVersionAnalysisState d, PumaFile f" +
			" where d.documentVersion = f.documentVersion" +
			" and (d.analysisSetup is null or d.analysisSetup not like :analysisSetup)" +
			" and f.documentType in :validDocumentTypes" +
			" and d.documentVersion not in (" +					// Remove documents that are still resolving URLS
			"   select r.documentVersion from ResolverInfo r" +
			"   where r.status = 'PENDING')" +
			" and d.documentVersion in (" +
			"   select f.documentVersion from PumaFile f" +		// Get only document that have saved files
			"   where f.status in ('SAVED'))" +
			" order by d.documentVersion.id";

		log.debug(queryString);

		// Generate the query
		TypedQuery<Long> query = this.entityManager.createQuery(queryString, Long.class);

		// Set the query parameters
		query.setParameter("analysisSetup", analysisSetup);
		query.setParameter("validDocumentTypes", VALID_DOCUMENT_TYPES_FOR_ANALYSIS);

		return query.setMaxResults(limit).getResultList();
	}

	public List<DocumentVersionAnalysisState> getAnalysisHistory(int limit) {
		// Build up query string from parameters
		String queryString = "select d from DocumentVersionAnalysisState d" +
			" where d.analysisSetup is not null" +
			" and d.analysisDate is not null" +
			" order by d.analysisDate desc";

		log.debug(queryString);

		// Generate the query
		TypedQuery<DocumentVersionAnalysisState> query = this.entityManager.createQuery(queryString, DocumentVersionAnalysisState.class);

		try {
			return query.setMaxResults(limit).getResultList();

		} catch (GenericJDBCException e) {
			// No results
			return new ArrayList<>();
		}
	}

	private TypedQuery<DocumentVersionAnalysisState> getRequiringAnalysisQuery(String analysisSetup) {

		// Build up query string from parameters
		String queryString = "select distinct d from DocumentVersionAnalysisState d, PumaFile f" +
			" where d.documentVersion = f.documentVersion" +
			" and (d.analysisSetup is null or d.analysisSetup not like :analysisSetup)" +
			" and f.documentType in :validDocumentTypes" +
			" and d.documentVersion not in (" +					// Remove documents that are still resolving URLS
			"   select r.documentVersion from ResolverInfo r" +
			"   where r.status = 'PENDING')" +
			" and d.documentVersion in (" +
			"   select f.documentVersion from PumaFile f" +		// Get only document that have saved files
			"   where f.status in ('SAVED'))" +
			" order by d.documentVersion.id";

		log.debug(queryString);

		// Generate the query
		TypedQuery<DocumentVersionAnalysisState> query = this.entityManager.createQuery(queryString, DocumentVersionAnalysisState.class);

		// Set the query parameters
		query.setParameter("analysisSetup", analysisSetup);
		query.setParameter("validDocumentTypes", VALID_DOCUMENT_TYPES_FOR_ANALYSIS);

		return query;
	}

	public long getNumberOfDocumentsRequiringAnalysis(String analysisSetup) {
		String queryString = "select count(distinct d) from DocumentVersionAnalysisState d, PumaFile f" +
			" where d.documentVersion = f.documentVersion" +
			" and (d.analysisSetup is null or d.analysisSetup not like :analysisSetup)" +
			" and f.documentType in :validDocumentTypes" +
			" and d.documentVersion not in (" +					// Remove documents that are still resolving URLS
			"   select r.documentVersion from ResolverInfo r" +
			"   where r.status = 'PENDING')" +
			" and d.documentVersion in (" +
			"   select f.documentVersion from PumaFile f" +		// Get only document that have saved files
			"   where f.status in ('SAVED'))";
		log.debug(queryString);

		// Generate the query
		TypedQuery<Long> query = this.entityManager.createQuery(queryString, Long.class);

		// Set the query parameters
		query.setParameter("analysisSetup", analysisSetup);
		query.setParameter("validDocumentTypes", VALID_DOCUMENT_TYPES_FOR_ANALYSIS);

		return query.getSingleResult();
	}
}
