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
import eu.ill.puma.persistence.domain.document.ResolverInfo;
import eu.ill.puma.persistence.domain.document.enumeration.ResolverInfoStatus;
import eu.ill.puma.persistence.repository.PumaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;

@Repository
public class ResolverInfoRepository extends PumaRepository<ResolverInfo> {

	private static final Logger log = LoggerFactory.getLogger(ResolverInfoRepository.class);

	public ResolverInfo getByOriginUrlAndDocumentVersion(String originUrl, DocumentVersion documentVersion) {
		return this.getFirstEntity(Arrays.asList("originUrl", "documentVersion"), originUrl, documentVersion);
	}

	public List<ResolverInfo> getAllForDocumentVersion(DocumentVersion documentVersion) {
		return this.getEntities("documentVersion", documentVersion);
	}

	public List<ResolverInfo> getLatestResolved(int number) {
		// Build up query string from parameters
		String queryString = "select r from ResolverInfo r where lastResolveDate is not null order by r.lastResolveDate desc";
		log.debug(queryString);

		// Generate the query
		TypedQuery<ResolverInfo> query = this.entityManager.createQuery(queryString, ResolverInfo.class);

		return query.setMaxResults(number).getResultList();
	}

	public List<ResolverInfo> getAllForStatus(ResolverInfoStatus resolverInfoStatus) {
		return this.getEntities("status", resolverInfoStatus);
	}

	public List<ResolverInfo> getAllForStatusAndHost(ResolverInfoStatus resolverInfoStatus, String host) {
		return this.getEntities(Arrays.asList("status", "resolverHost"), resolverInfoStatus, host);
	}

	public ResolverInfo getNextForStatus(ResolverInfoStatus resolverInfoStatus) {
		return this.getFirstEntity("status", resolverInfoStatus);
	}

	public ResolverInfo getNextForStatusAfter(ResolverInfoStatus resolverInfoStatus, Long resolverId) {
		// Build up query string from parameters
		String queryString = "select r from ResolverInfo r where status = :status and r.id > :resolverId order by r.id asc";
		log.debug(queryString);

		// Generate the query
		TypedQuery<ResolverInfo> query = this.entityManager.createQuery(queryString, ResolverInfo.class);

		// Set the query parameters
		query.setParameter("status", resolverInfoStatus);
		query.setParameter("resolverId", resolverId);
		query.setMaxResults(1);

		try {
			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public ResolverInfo getNextForStatusAfterForHost(ResolverInfoStatus resolverInfoStatus, Long resolverId, String host) {
		// Build up query string from parameters
		String queryString = "select r from ResolverInfo r where status = :status and r.id > :resolverId and r.resolverHost = :host order by r.id asc";
		log.debug(queryString);

		// Generate the query
		TypedQuery<ResolverInfo> query = this.entityManager.createQuery(queryString, ResolverInfo.class);

		// Set the query parameters
		query.setParameter("status", resolverInfoStatus);
		query.setParameter("resolverId", resolverId);
		query.setParameter("host", host);
		query.setMaxResults(1);

		try {
			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public ResolverInfo getNextForStatusForHost(ResolverInfoStatus resolverInfoStatus, String host) {
		// Build up query string from parameters
		String queryString = "select r from ResolverInfo r where status = :status and r.resolverHost = :host order by r.id asc";
		log.debug(queryString);

		// Generate the query
		TypedQuery<ResolverInfo> query = this.entityManager.createQuery(queryString, ResolverInfo.class);

		// Set the query parameters
		query.setParameter("status", resolverInfoStatus);
		query.setParameter("host", host);
		query.setMaxResults(1);

		try {
			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	public List<ResolverInfo> getAllForStatuses(List<ResolverInfoStatus> resolverInfoStatuses) {

		// Build up query string from parameters
		String queryString = "select r from ResolverInfo r where status in :statuses order by r.id asc";
		log.debug(queryString);

		// Generate the query
		TypedQuery<ResolverInfo> query = this.entityManager.createQuery(queryString, ResolverInfo.class);

		// Set the query parameters
		query.setParameter("statuses", resolverInfoStatuses);

		return query.getResultList();
	}
}
