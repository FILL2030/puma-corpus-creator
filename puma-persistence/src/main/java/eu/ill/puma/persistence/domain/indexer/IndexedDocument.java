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
package eu.ill.puma.persistence.domain.indexer;

import eu.ill.puma.persistence.domain.document.*;
import eu.ill.puma.persistence.domain.document.enumeration.DocumentType;
import eu.ill.puma.persistence.domain.document.enumeration.DocumentVersionSubType;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileType;
import eu.ill.puma.persistence.service.document.PumaFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class IndexedDocument {
	private static final Logger log = LoggerFactory.getLogger(IndexedDocument.class);

	public final static String INDEXER_TYPE = "document";

	private Long id;
	private DocumentType documentType;
	private Date releaseDate;
	private String doi;
	private DocumentVersionSubType subType;
	private String shortName;
	private String title;
	private String abstractText;
	private Boolean obsolete;
	private List<DocumentVersionSource> sources = new ArrayList<>();
	private List<Reference> references = new ArrayList();

	private Set<Person> persons = new LinkedHashSet<>();
	private Set<Laboratory> laboratories = new LinkedHashSet<>();
	private List<Instrument> instruments = new ArrayList<>();
	private Set<Publisher> publishers = new LinkedHashSet<>();
	private Journal journal;
	private List<Keyword> keywords = new ArrayList();
	private List<Formula> formulas = new ArrayList();
	private List<ResearchDomain> researchDomains = new ArrayList();
	private List<AdditionalText> additionalTexts = new ArrayList<>();
	private String fullText;

	public IndexedDocument() {
	}

	public IndexedDocument(DocumentVersion documentVersion, PumaFileService pumaFileService) {
		this.id = documentVersion.getId();
		this.documentType = documentVersion.getDocument().getDocumentType();
		this.releaseDate = documentVersion.getReleaseDate();
		this.doi = documentVersion.getDoi();
		this.shortName = documentVersion.getShortName();
		this.title = documentVersion.getTitle();
		this.abstractText = documentVersion.getAbstractText();
		this.sources = documentVersion.getSources();
		this.references = documentVersion.getReferences();
		this.keywords = documentVersion.getKeywords();
		this.formulas = documentVersion.getFormulas();
		this.researchDomains = documentVersion.getResearchDomains();
		this.additionalTexts = documentVersion.getAdditionalTexts();
		this.obsolete = documentVersion.isObsolete();

		// Flatten affiliations
		for (PersonLaboratoryAffiliation affiliation : documentVersion.getPersonLaboratoryAffiliations()) {
			if (affiliation.getPerson() != null) {
				this.persons.add(affiliation.getPerson());
			}
			if (affiliation.getLaboratory() != null) {
				this.laboratories.add(affiliation.getLaboratory());
			}
		}

		for (JournalPublisherAffiliation affiliation : documentVersion.getJournalPublisherAffiliations()) {
			if (affiliation.getJournal() != null) {
				this.journal = affiliation.getJournal();
			}
			if (affiliation.getPublisher() != null) {
				this.publishers.add(affiliation.getPublisher());
			}
		}

		StringBuilder stringBuilder = new StringBuilder();
		// Get fulltext files
		for (PumaFile file : documentVersion.getFiles()) {
			if (file.getDocumentType().equals(PumaFileType.EXTRACTED_FULL_TEXT)) {
				pumaFileService.readFileData(file);
				if (file.getData() != null) {
					this.fullText = new String(file.getData());
				}
			}
		}

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DocumentType getDocumentType() {
		return documentType;
	}

	public void setDocumentType(DocumentType documentType) {
		this.documentType = documentType;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getDoi() {
		return doi;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public DocumentVersionSubType getSubType() {
		return subType;
	}

	public void setSubType(DocumentVersionSubType subType) {
		this.subType = subType;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAbstractText() {
		return abstractText;
	}

	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}

	public List<DocumentVersionSource> getSources() {
		return sources;
	}

	public void setSources(List<DocumentVersionSource> sources) {
		this.sources = sources;
	}

	public List<Reference> getReferences() {
		return references;
	}

	public void setReferences(List<Reference> references) {
		this.references = references;
	}

	public Set<Person> getPersons() {
		return persons;
	}

	public void setPersons(Set<Person> persons) {
		this.persons = persons;
	}

	public Set<Laboratory> getLaboratories() {
		return laboratories;
	}

	public void setLaboratories(Set<Laboratory> laboratories) {
		this.laboratories = laboratories;
	}

	public List<Instrument> getInstruments() {
		return instruments;
	}

	public void setInstruments(List<Instrument> instruments) {
		this.instruments = instruments;
	}

	public Set<Publisher> getPublishers() {
		return publishers;
	}

	public void setPublishers(Set<Publisher> publishers) {
		this.publishers = publishers;
	}

	public Journal getJournal() {
		return journal;
	}

	public void setJournal(Journal journal) {
		this.journal = journal;
	}

	public List<Keyword> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<Keyword> keywords) {
		this.keywords = keywords;
	}

	public List<Formula> getFormulas() {
		return formulas;
	}

	public void setFormulas(List<Formula> formulas) {
		this.formulas = formulas;
	}

	public List<ResearchDomain> getResearchDomains() {
		return researchDomains;
	}

	public void setResearchDomains(List<ResearchDomain> researchDomains) {
		this.researchDomains = researchDomains;
	}

	public List<AdditionalText> getAdditionalTexts() {
		return additionalTexts;
	}

	public void setAdditionalTexts(List<AdditionalText> additionalTexts) {
		this.additionalTexts = additionalTexts;
	}

	public String getFullText() {
		return fullText;
	}

	public void setFullText(String fullText) {
		this.fullText = fullText;
	}

	public Boolean getObsolete() {
		return obsolete;
	}

	public void setObsolete(Boolean obsolete) {
		this.obsolete = obsolete;
	}
}
