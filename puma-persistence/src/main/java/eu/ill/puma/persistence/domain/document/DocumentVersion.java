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
package eu.ill.puma.persistence.domain.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionAnalysisState;
import eu.ill.puma.persistence.domain.document.enumeration.DocumentVersionSubType;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "document_version", indexes = {
		@Index(name = "document_version_document_index", columnList = "document_id")
})
public class DocumentVersion {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Column(name = "release_date")
	private Date releaseDate;

	@Column(name = "doi", length = 1000)
	private String doi;

	@Column(name = "short_hash", length = 1000)
	private String shortHash;

	@Column(name = "sub_type", length = 1000)
	@Enumerated(EnumType.STRING)
	private DocumentVersionSubType subType;

	@Column(name = "short_name", length = 1000)
	private String shortName;

	@Column(name = "title", length = 10000)
	private String title;

	@Column(name = "abstract", length = 100000)
	private String abstractText;

	@Column(name = "indexation_date")
	private Date indexationDate;

	@OneToMany(mappedBy = "documentVersion", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	private List<DocumentVersionSource> sources = new ArrayList<>();

	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name = "DOCUMENT_ID", nullable = false, foreignKey = @ForeignKey(name = "fk_document_version_document_id"))
	private Document document;

	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name = "ORIGINAL_DOCUMENT_ID", nullable = true, foreignKey = @ForeignKey(name = "fk_document_version_original_document_id"))
	private Document originalDocument;

	@OneToMany(mappedBy = "citingDocumentVersion", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@Where(clause = "obsolete = false")
	private List<Reference> references = new ArrayList();

	@JsonIgnore
	@OneToMany(mappedBy = "documentVersion", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@Where(clause = "obsolete = false")
	private List<PumaFile> files = new ArrayList();

	@JsonIgnore
	@OneToMany(mappedBy = "documentVersion", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	private List<ResolverInfo> resolverInfos = new ArrayList();

	@OneToMany(mappedBy = "documentVersion", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@Where(clause = "obsolete = false")
	private List<PersonLaboratoryAffiliation> personLaboratoryAffiliations = new ArrayList();

	@OneToMany(mappedBy = "documentVersion", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@Where(clause = "obsolete = false")
	private List<JournalPublisherAffiliation> journalPublisherAffiliations = new ArrayList();

	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinTable(name = "version_instrument", joinColumns = {
			@JoinColumn(name = "DOCUMENT_VERSION_ID", foreignKey = @ForeignKey(name = "fk_version_instrument_document_version_id"))}, inverseJoinColumns = {
			@JoinColumn(name = "INSTRUMENT_ID", foreignKey = @ForeignKey(name = "fk_version_instrument_instrument_id"))},
			indexes = {
					@Index(name = "version_instrument_instrument_id", columnList = "instrument_id"),
					@Index(name = "version_instrument_document_version_id", columnList = "document_version_id")},
			uniqueConstraints = {@UniqueConstraint(columnNames = {"document_version_id", "instrument_id"}, name = "unique_document_version_id_instrument_id")})
	@Where(clause = "obsolete = false")
	private List<Instrument> instruments = new ArrayList();

	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinTable(name = "version_keyword", joinColumns = {
			@JoinColumn(name = "DOCUMENT_VERSION_ID", foreignKey = @ForeignKey(name = "fk_version_keyword_document_version_id"))}, inverseJoinColumns = {
			@JoinColumn(name = "KEYWORD_ID", foreignKey = @ForeignKey(name = "fk_version_keyword_keyword_id"))},
			indexes = {
					@Index(name = "version_keyword_keyword_id", columnList = "keyword_id"),
					@Index(name = "version_keyword_document_version_id", columnList = "document_version_id")},
			uniqueConstraints = {@UniqueConstraint(columnNames = {"document_version_id", "keyword_id"}, name = "unique_document_version_id_keyword_id")})
	@Where(clause = "obsolete = false")
	private List<Keyword> keywords = new ArrayList();

	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinTable(name = "version_formula", joinColumns = {
			@JoinColumn(name = "DOCUMENT_VERSION_ID", foreignKey = @ForeignKey(name = "fk_version_formula_document_version_id"))}, inverseJoinColumns = {
			@JoinColumn(name = "FORMULA_ID", foreignKey = @ForeignKey(name = "fk_version_formula_formula_id"))},
			indexes = {
					@Index(name = "version_formula_formula_id", columnList = "formula_id"),
					@Index(name = "version_formula_document_version_id", columnList = "document_version_id")},
			uniqueConstraints = {@UniqueConstraint(columnNames = {"document_version_id", "formula_id"}, name = "unique_document_version_id_formula_id")})
	@Where(clause = "obsolete = false")
	private List<Formula> formulas = new ArrayList();

	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinTable(name = "version_research_domain", joinColumns = {
			@JoinColumn(name = "DOCUMENT_VERSION_ID", foreignKey = @ForeignKey(name = "fk_version_research_domain_document_version_id"))}, inverseJoinColumns = {
			@JoinColumn(name = "RESEARCH_DOMAIN_ID", foreignKey = @ForeignKey(name = "fk_version_research_domain_research_domain_id"))},
			indexes = {
					@Index(name = "version_research_domain_research_domain_id", columnList = "research_domain_id"),
					@Index(name = "version_research_domain_document_version_id", columnList = "document_version_id")},
			uniqueConstraints = {@UniqueConstraint(columnNames = {"document_version_id", "research_domain_id"}, name = "unique_document_version_id_research_domain_id")})
	@Where(clause = "obsolete = false")
	private List<ResearchDomain> researchDomains = new ArrayList();

	@OneToMany(mappedBy = "documentVersion", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@Where(clause = "obsolete = false")
	private List<AdditionalText> additionalTexts = new ArrayList<>();

	@JsonIgnore
	@OneToOne(mappedBy = "documentVersion", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	private DocumentVersionAnalysisState analysisState;

	@Column(name = "obsolete", columnDefinition = "boolean default false")
	private Boolean obsolete = false;

	@Column(name = "last_modification_date", nullable = true)
	Date lastModificationDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getShortHash() {
		return shortHash;
	}

	public void setShortHash(String shortHash) {
		this.shortHash = shortHash;
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
		this.title = title == null ? null : title.toLowerCase();
	}

	public String getAbstractText() {
		return abstractText;
	}

	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText == null ? null : abstractText.toLowerCase();
	}

	public Date getIndexationDate() {
		return indexationDate;
	}

	public void setIndexationDate(Date indexationDate) {
		this.indexationDate = indexationDate;
	}

	public List<DocumentVersionSource> getSources() {
		return sources;
	}

	public void addSource(DocumentVersionSource source) {
		this.sources.add(source);
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public Document getOriginalDocument() {
		return originalDocument;
	}

	public void setOriginalDocument(Document originalDocument) {
		this.originalDocument = originalDocument;
	}

	public Boolean isObsolete() {
		return obsolete;
	}

	public void setObsolete(Boolean obsolete) {
		this.obsolete = obsolete;
	}

	public Date getLastModificationDate() {
		return lastModificationDate;
	}

	public void setLastModificationDate(Date lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
	}


	public List<Reference> getReferences() {
		return references;
	}

	public Optional<Reference> getReferenceById(Long id) {
		return this.references.stream()
				.filter(reference -> reference != null &&
						reference.getId().equals(id))
				.findFirst();
	}

	public void addReference(Reference reference) {
		this.references.add(reference);
	}

	public boolean removeReference(Reference reference) {
		return this.references.remove(reference);
	}

	public List<PumaFile> getFiles() {
		return files;
	}

	public Optional<PumaFile> getFileById(Long id) {
		return this.files.stream()
				.filter(file -> file != null &&
						file.getId().equals(id))
				.findFirst();
	}

	public void addFile(PumaFile file) {
		this.files.add(file);
	}

	public void removeFile(PumaFile file) {
		this.files.remove(file);
	}

	public void removeAllFiles() {
		this.files.clear();
	}

	public List<ResolverInfo> getResolverInfos() {
		return resolverInfos;
	}

	public void addResolverInfo(ResolverInfo resolverInfo) {
		this.resolverInfos.add(resolverInfo);
	}

	public void removeAllResolverInfos() {
		this.resolverInfos.clear();
	}

	public List<JournalPublisherAffiliation> getJournalPublisherAffiliations() {
		return journalPublisherAffiliations;
	}

	public List<JournalPublisherAffiliation> getJournalPublisherAffiliationsByJournalId(Long id) {
		return this.journalPublisherAffiliations.stream()
				.filter(affiliation -> affiliation.getJournal() != null &&
						affiliation.getJournal().getId().equals(id))
				.collect(Collectors.toList());
	}

	public List<JournalPublisherAffiliation> getJournalPublisherAffiliationsByPublisherId(Long id) {
		return this.journalPublisherAffiliations.stream()
				.filter(affiliation -> affiliation.getPublisher() != null &&
						affiliation.getPublisher().getId().equals(id))
				.collect(Collectors.toList());
	}

	public Optional<JournalPublisherAffiliation> getJournalPublisherAffiliationByJournalIdAndPublisherId(Long jId, Long pId) {
		return this.journalPublisherAffiliations.stream()
				.filter(affiliation -> affiliation.getJournal() != null &&
						affiliation.getPublisher() != null &&
						affiliation.getJournal().getId().equals(jId) &&
						affiliation.getPublisher().getId().equals(pId))
				.findFirst();
	}

	public void addJournalPublisherAffiliation(JournalPublisherAffiliation journalPublisherAffiliation) {
		this.journalPublisherAffiliations.add(journalPublisherAffiliation);
	}

	public boolean removeJournalPublisherAffiliation(JournalPublisherAffiliation journalPublisherAffiliation) {
		return this.journalPublisherAffiliations.remove(journalPublisherAffiliation);
	}

	public List<PersonLaboratoryAffiliation> getPersonLaboratoryAffiliations() {
		return personLaboratoryAffiliations;
	}

	public List<PersonLaboratoryAffiliation> getPersonLaboratoryAffiliationsByPersonId(Long id) {
		return this.personLaboratoryAffiliations.stream()
				.filter(affiliation -> affiliation.getPerson() != null &&
						affiliation.getPerson().getId().equals(id))
				.collect(Collectors.toList());
	}

	public List<PersonLaboratoryAffiliation> getPersonLaboratoryAffiliationsByLaboratoryId(Long id) {
		return this.personLaboratoryAffiliations.stream()
				.filter(affiliation -> affiliation.getLaboratory() != null &&
						affiliation.getLaboratory().getId().equals(id))
				.collect(Collectors.toList());
	}

	public Optional<PersonLaboratoryAffiliation> getPersonLaboratoryAffiliationByPersonIdAndLaboratoryId(Long pId, Long lId) {
		return this.personLaboratoryAffiliations.stream()
				.filter(affiliation -> affiliation.getPerson() != null &&
						affiliation.getLaboratory() != null && affiliation.getPerson().getId().equals(pId) &&
						affiliation.getLaboratory().getId().equals(lId))
				.findFirst();
	}

	public void addPersonLaboratoryAffiliation(PersonLaboratoryAffiliation personLaboratoryAffiliation) {
		this.personLaboratoryAffiliations.add(personLaboratoryAffiliation);
	}

	public boolean removePersonLaboratoryAffiliation(PersonLaboratoryAffiliation personLaboratoryAffiliation) {
		return this.personLaboratoryAffiliations.remove(personLaboratoryAffiliation);
	}

	public List<Instrument> getInstruments() {
		return instruments;
	}

	public Optional<Instrument> getInstrumentById(Long id) {
		return this.instruments.stream()
				.filter(instrument -> instrument != null &&
						instrument.getId().equals(id))
				.findFirst();
	}

	public boolean removeInstrument(Instrument instrument) {
		return this.instruments.remove(instrument);
	}

	public void addInstrument(Instrument instrument) {
		this.instruments.add(instrument);
	}

	public List<Keyword> getKeywords() {
		return keywords;
	}

	public Optional<Keyword> getKeywordById(Long id) {
		return this.keywords.stream()
				.filter(keyword -> keyword != null &&
						keyword.getId().equals(id))
				.findFirst();
	}

	public void addKeyword(Keyword keyword) {
		this.keywords.add(keyword);
	}

	public boolean removeKeyword(Keyword keyword) {
		return this.keywords.remove(keyword);
	}

	public List<Formula> getFormulas() {
		return formulas;
	}

	public Optional<Formula> getFormulaById(Long id) {
		return this.formulas.stream()
				.filter(formula -> formula != null &&
						formula.getId().equals(id))
				.findFirst();
	}

	public void addFormula(Formula formula) {
		this.formulas.add(formula);
	}

	public boolean removeFormula(Formula formula) {
		return this.formulas.remove(formula);
	}

	public List<ResearchDomain> getResearchDomains() {
		return researchDomains;
	}

	public Optional<ResearchDomain> getResearchDomainById(Long id) {
		return this.researchDomains.stream()
				.filter(researchDomain -> researchDomain != null &&
						researchDomain.getId().equals(id))
				.findFirst();
	}

	public void addResearchDomain(ResearchDomain researchDomain) {
		this.researchDomains.add(researchDomain);
	}

	public boolean removeResearchDomain(ResearchDomain researchDomain) {
		return this.researchDomains.remove(researchDomain);
	}

	public List<AdditionalText> getAdditionalTexts() {
		return additionalTexts;
	}

	public Optional<AdditionalText> getAdditionalTextById(Long id) {
		return this.additionalTexts.stream()
				.filter(additionalText -> additionalText != null &&
						additionalText.getId().equals(id))
				.findFirst();
	}

	public void addAdditionalText(AdditionalText additionalText) {
		this.additionalTexts.add(additionalText);
	}

	public DocumentVersionAnalysisState getAnalysisState() {
		return analysisState;
	}

	public void setAnalysisState(DocumentVersionAnalysisState analysisState) {
		this.analysisState = analysisState;
	}

	public void buildShortHash() {
		// Convert hash to string
		this.shortHash = Integer.toHexString(this.hashCode());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", id)
				.append("doi", doi)
				.append("title", title)
				.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DocumentVersion that = (DocumentVersion) o;

		if (doi != null ? !doi.equals(that.doi) : that.doi != null) return false;
		if (subType != that.subType) return false;
		if (shortName != null ? !shortName.equals(that.shortName) : that.shortName != null) return false;
		if (title != null ? !title.equals(that.title) : that.title != null) return false;
		return abstractText != null ? abstractText.equals(that.abstractText) : that.abstractText == null;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + (doi != null ? doi.hashCode() : 0);
		result = 31 * result + (subType != null ? subType.hashCode() : 0);
		result = 31 * result + (shortName != null ? shortName.hashCode() : 0);
		result = 31 * result + (title != null ? title.hashCode() : 0);
		result = 31 * result + (abstractText != null ? abstractText.hashCode() : 0);
		return result;
	}
}
