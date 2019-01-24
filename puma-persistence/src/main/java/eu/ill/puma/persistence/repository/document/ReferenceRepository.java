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

import eu.ill.puma.persistence.domain.document.Document;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.Reference;
import eu.ill.puma.persistence.repository.PumaDocumentEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;

@Repository
public class ReferenceRepository extends PumaDocumentEntityRepository<Reference> {

	public Reference getByCitationStringAndCitingDocumentVersion(String citationString, DocumentVersion citingDocumentVersion) {
		return this.getFirstEntity(Arrays.asList("citationString", "citingDocumentVersion"), citationString, citingDocumentVersion);
	}

	public Reference getByCitingDocumentVersionSourceIdAndCitedDocument(String citingDocumentVersionSourceId, Document citedDocument) {
		return this.getFirstEntity(Arrays.asList("citingDocumentVersionSourceId", "citedDocument"), citingDocumentVersionSourceId, citedDocument);
	}

}
