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
package eu.ill.puma.persistence.service.converterV2.integrater;

import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.core.domain.document.entities.BaseJournal;
import eu.ill.puma.core.domain.document.entities.BasePublisher;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.Journal;
import eu.ill.puma.persistence.domain.document.JournalPublisherAffiliation;
import eu.ill.puma.persistence.domain.document.Publisher;
import eu.ill.puma.persistence.service.converterV2.EntityOriginStore;
import eu.ill.puma.persistence.service.converterV2.entitityconverter.JournalConverter;
import eu.ill.puma.persistence.service.converterV2.entitityconverter.PublisherConverter;
import eu.ill.puma.persistence.service.document.JournalService;
import eu.ill.puma.persistence.service.document.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JournalPublisherIntegrator {

	@Autowired
	private JournalService journalService;

	@Autowired
	private PublisherService publisherService;

	public void convert(DocumentVersion documentVersion, BaseDocument baseDocument, EntityOriginStore entityOriginStore) {

		if (baseDocument.getJournal() != null) {

			BaseJournal baseJournal = baseDocument.getJournal();

			//delete journal (update & delete operation)
			if (baseJournal != null && baseJournal.getPumaId() != null && documentVersion.getJournalPublisherAffiliations().size() > 0) {

				List<JournalPublisherAffiliation> affiliationList = documentVersion.getJournalPublisherAffiliations();

				//delete
				for (JournalPublisherAffiliation affiliation : affiliationList) {
					affiliation.setObsolete(true);

					//delete journal
					if (affiliation.getJournal() != null) {
						entityOriginStore.deleteEntity(affiliation.getJournal().getId(), EntityType.JOURNAL, baseJournal.getConfidence());
					}

					//delete publisher
					if (affiliation.getPublisher() != null) {
						entityOriginStore.deleteEntity(affiliation.getPublisher().getId(), EntityType.PUBLISHER, baseJournal.getConfidence());
					}
				}
			}

			//create journal (add & update operation)
			if (baseJournal.getPumaId() == null || baseJournal.getPumaId() >= 0) {
				//new Journal
				Journal journal = JournalConverter.convert(baseJournal);
				journal = journalService.save(journal);

				boolean addedJournal = false;

				//affiliations
				if (baseDocument.getPublishers().size() == 0) {
					JournalPublisherAffiliation journalPublisherAffiliation = new JournalPublisherAffiliation();
					journalPublisherAffiliation.setDocumentVersion(documentVersion);
					journalPublisherAffiliation.setJournal(journal);

					if (!documentVersion.getJournalPublisherAffiliations().contains(journalPublisherAffiliation)) {
						documentVersion.addJournalPublisherAffiliation(journalPublisherAffiliation);
						addedJournal = true;
					}

				} else {
					for (BasePublisher basePublisher : baseDocument.getPublishers()) {
						Publisher publisher = PublisherConverter.convert(basePublisher);
						publisher = publisherService.save(publisher);


						JournalPublisherAffiliation journalPublisherAffiliation = new JournalPublisherAffiliation();
						journalPublisherAffiliation.setDocumentVersion(documentVersion);
						journalPublisherAffiliation.setJournal(journal);
						journalPublisherAffiliation.setPublisher(publisher);

						if (!documentVersion.getJournalPublisherAffiliations().contains(journalPublisherAffiliation)) {
							documentVersion.addJournalPublisherAffiliation(journalPublisherAffiliation);

							//publisher history
							entityOriginStore.foundEntity(publisher.getId(), EntityType.PUBLISHER, basePublisher.getConfidence());

							addedJournal = true;
						}
					}
				}

				if (addedJournal) {
					// journal history
					entityOriginStore.foundEntity(journal.getId(), EntityType.JOURNAL, baseJournal.getConfidence());
				}
			}
		}
	}
}
