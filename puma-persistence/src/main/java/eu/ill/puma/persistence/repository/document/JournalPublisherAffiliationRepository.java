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
import eu.ill.puma.persistence.domain.document.Journal;
import eu.ill.puma.persistence.domain.document.JournalPublisherAffiliation;
import eu.ill.puma.persistence.domain.document.Publisher;
import eu.ill.puma.persistence.repository.PumaDocumentEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class JournalPublisherAffiliationRepository extends PumaDocumentEntityRepository<JournalPublisherAffiliation> {

	public List<JournalPublisherAffiliation> getAllForJournal(Journal journal) {
		return this.getEntities("journal", journal);
	}

	public List<JournalPublisherAffiliation> getAllForPublisher(Publisher publisher) {
		return this.getEntities("publisher", publisher);
	}

	public List<JournalPublisherAffiliation> getAllForDocumentVersion(DocumentVersion documentVersion) {
		return this.getEntities("documentVersion", documentVersion);
	}

	public List<JournalPublisherAffiliation> getForJournalAndDocumentVersion(Journal journal, DocumentVersion documentVersion) {
		List<String> parameters = new ArrayList();
		parameters.add("journal");
		parameters.add("documentVersion");
		return this.getEntities(parameters, journal, documentVersion);
	}

	public List<JournalPublisherAffiliation> getForPublisherAndDocumentVersion(Publisher publisher, DocumentVersion documentVersion) {
		List<String> parameters = new ArrayList();
		parameters.add("publisher");
		parameters.add("documentVersion");
		return this.getEntities(parameters, publisher, documentVersion);
	}
}
