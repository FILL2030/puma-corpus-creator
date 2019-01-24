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

import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.ResolverInfo;
import eu.ill.puma.persistence.domain.document.enumeration.ResolverInfoStatus;
import eu.ill.puma.persistence.repository.document.ResolverInfoRepository;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ResolverInfoService {

	private static final Logger log = LoggerFactory.getLogger(ResolverInfoService.class);

	@Autowired
	private ResolverInfoRepository resolverInfoRepository;

	/**
	 * Returns all ResolverInfos
	 * @return
	 */
	public List<ResolverInfo> getAll() {
		return this.resolverInfoRepository.getAll();
	}

	/**
	 * Returns the ResolverInfo by its Id
	 * @param id The id of the ResolverInfo
	 * @return the ResolverInfo with the given Id
	 */
	public ResolverInfo getById(Long id) {
		return this.resolverInfoRepository.getById(id);
	}

	/**
	 * Returns the ResolverInfo by its Id
	 * @param id The id of the ResolverInfo
	 * @return the ResolverInfo with the given Id
	 */
	public ResolverInfo getByIdWithDocumentVersion(Long id) {
		ResolverInfo resolverInfo = this.resolverInfoRepository.getById(id);
		Hibernate.initialize(resolverInfo.getDocumentVersion());

		return resolverInfo;
	}

	/**
	 * Returns all files for a document version
	 * @param documentVersion The document version containing files
	 * @return The files of the document version
	 */
	public List<ResolverInfo> getAllForDocumentVersion(DocumentVersion documentVersion) {
		return this.resolverInfoRepository.getAllForDocumentVersion(documentVersion);
	}

	/**
	 * Persists the given ResolverInfo and ensures we do not create duplicates.
	 *  - if the id is set we assume we have an already persisted object and the object is updated
	 *  - If the ResolverInfo data match an existing ResolverInfo then we know it is already persisted
	 * @param resolverInfo The ResolverInfo to be persisted
	 * @return The persisted ResolverInfo
	 */
	public synchronized ResolverInfo save(ResolverInfo resolverInfo) {
		ResolverInfo integratedResolverInfo = null;

		// Check if it is a new object
		if (resolverInfo.getId() == null) {
			// Determine if the object already exists
			integratedResolverInfo = this.resolverInfoRepository.getByOriginUrlAndDocumentVersion(resolverInfo.getOriginUrl(), resolverInfo.getDocumentVersion());
			if (integratedResolverInfo != null) {
				log.debug("resolverInfo " + resolverInfo.getOriginUrl() + " already present in the db under the id " + integratedResolverInfo.getId() + " : merging");

			} else {
				// persist
				integratedResolverInfo = this.resolverInfoRepository.persist(resolverInfo);
			}

		} else {
			// merge
			integratedResolverInfo = this.resolverInfoRepository.merge(resolverInfo);
		}

		return integratedResolverInfo;
	}

	public List<ResolverInfo> getLatestResolved(int number) {
		return this.resolverInfoRepository.getLatestResolved(number);
	}

	public List<ResolverInfo> getAllPending() {
		return this.resolverInfoRepository.getAllForStatus(ResolverInfoStatus.PENDING);
	}

	public List<ResolverInfo> getAllFailed() {
		List<ResolverInfo> resolverInfos = this.resolverInfoRepository.getAllForStatus(ResolverInfoStatus.RESOLVE_NOT_SUPPORTED);
		resolverInfos.addAll(this.resolverInfoRepository.getAllForStatus(ResolverInfoStatus.RESOLVE_ERROR));
		resolverInfos.addAll(this.resolverInfoRepository.getAllForStatus(ResolverInfoStatus.RESOLVE_FAILED));

		return resolverInfos;
	}

	public List<ResolverInfo> getAllRequiringResolve() {
		List<ResolverInfo> resolverInfos = this.resolverInfoRepository.getAllForStatus(ResolverInfoStatus.PENDING);
		resolverInfos.addAll(this.resolverInfoRepository.getAllForStatus(ResolverInfoStatus.RESOLVE_NOT_SUPPORTED));
		resolverInfos.addAll(this.resolverInfoRepository.getAllForStatus(ResolverInfoStatus.RESOLVE_ERROR));
		resolverInfos.addAll(this.resolverInfoRepository.getAllForStatus(ResolverInfoStatus.RESOLVE_FAILED));

		return resolverInfos;
	}

	public List<ResolverInfo> getAllRequiringResolveForHost(String host) {
		List<ResolverInfo> resolverInfos = this.resolverInfoRepository.getAllForStatusAndHost(ResolverInfoStatus.RESOLVE_NOT_SUPPORTED, host);
		resolverInfos.addAll(this.resolverInfoRepository.getAllForStatusAndHost(ResolverInfoStatus.RESOLVE_ERROR, host));
		resolverInfos.addAll(this.resolverInfoRepository.getAllForStatusAndHost(ResolverInfoStatus.RESOLVE_FAILED, host));

		return resolverInfos;
	}

	public ResolverInfo getNextRequiringResolve() {
		ResolverInfo resolverInfo = this.resolverInfoRepository.getNextForStatus(ResolverInfoStatus.PENDING);
		if (resolverInfo == null) {
			resolverInfo = this.resolverInfoRepository.getNextForStatus(ResolverInfoStatus.RESOLVE_NOT_SUPPORTED);
		}
		if (resolverInfo == null) {
			resolverInfo = this.resolverInfoRepository.getNextForStatus(ResolverInfoStatus.RESOLVE_ERROR);
		}
		if (resolverInfo == null) {
			resolverInfo = this.resolverInfoRepository.getNextForStatus(ResolverInfoStatus.RESOLVE_FAILED);
		}

		if (resolverInfo != null) {
			Hibernate.initialize(resolverInfo.getDocumentVersion());
		}

		return resolverInfo;
	}

	public ResolverInfo getNextRequiringResolveForHost(String host) {
		ResolverInfo resolverInfo = this.resolverInfoRepository.getNextForStatusForHost(ResolverInfoStatus.PENDING, host);
		if (resolverInfo == null) {
			resolverInfo = this.resolverInfoRepository.getNextForStatusForHost(ResolverInfoStatus.RESOLVE_NOT_SUPPORTED, host);
		}
		if (resolverInfo == null) {
			resolverInfo = this.resolverInfoRepository.getNextForStatusForHost(ResolverInfoStatus.RESOLVE_ERROR, host);
		}
		if (resolverInfo == null) {
			resolverInfo = this.resolverInfoRepository.getNextForStatusForHost(ResolverInfoStatus.RESOLVE_FAILED, host);
		}

		if (resolverInfo != null) {
			Hibernate.initialize(resolverInfo.getDocumentVersion());
		}

		return resolverInfo;
	}

	public ResolverInfo getNextRequiringResolveAfter(Long resolverId) {
		ResolverInfo resolverInfo = this.resolverInfoRepository.getNextForStatusAfter(ResolverInfoStatus.PENDING, resolverId);
		if (resolverInfo == null) {
			resolverInfo = this.resolverInfoRepository.getNextForStatusAfter(ResolverInfoStatus.RESOLVE_NOT_SUPPORTED, resolverId);
		}
		if (resolverInfo == null) {
			resolverInfo = this.resolverInfoRepository.getNextForStatusAfter(ResolverInfoStatus.RESOLVE_ERROR, resolverId);
		}
		if (resolverInfo == null) {
			resolverInfo = this.resolverInfoRepository.getNextForStatusAfter(ResolverInfoStatus.RESOLVE_FAILED, resolverId);
		}

		if (resolverInfo != null) {
			Hibernate.initialize(resolverInfo.getDocumentVersion());
		}

		return resolverInfo;
	}

	public ResolverInfo getNextRequiringResolveAfterForHost(Long resolverId, String host) {
		ResolverInfo resolverInfo = this.resolverInfoRepository.getNextForStatusAfterForHost(ResolverInfoStatus.PENDING, resolverId, host);
		if (resolverInfo == null) {
			resolverInfo = this.resolverInfoRepository.getNextForStatusAfterForHost(ResolverInfoStatus.RESOLVE_NOT_SUPPORTED, resolverId, host);
		}
		if (resolverInfo == null) {
			resolverInfo = this.resolverInfoRepository.getNextForStatusAfterForHost(ResolverInfoStatus.RESOLVE_ERROR, resolverId, host);
		}
		if (resolverInfo == null) {
			resolverInfo = this.resolverInfoRepository.getNextForStatusAfterForHost(ResolverInfoStatus.RESOLVE_FAILED, resolverId, host);
		}

		if (resolverInfo != null) {
			Hibernate.initialize(resolverInfo.getDocumentVersion());
		}

		return resolverInfo;
	}

	public List<ResolverInfo> getAllForStatus(ResolverInfoStatus resolverInfoStatus) {
		return this.resolverInfoRepository.getAllForStatus(resolverInfoStatus);
	}

	public void delete(ResolverInfo resolverInfo){
		this.resolverInfoRepository.delete(resolverInfo);
	}

}
