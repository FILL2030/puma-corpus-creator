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
package eu.ill.puma.persistence.domain.analysis;

import eu.ill.puma.core.domain.analysis.AnalysisState;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "document_version_analysis_state", indexes = {
	@Index(name = "analysis_state_document_version_id_index", columnList = "document_version_id")
})
public class DocumentVersionAnalysisState {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@OneToOne(fetch=FetchType.EAGER)
	@JoinColumn(unique = true, name = "DOCUMENT_VERSION_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "fk_entity_confidence_document_version_id"))
	private DocumentVersion documentVersion;

	@Column(name = "doi", nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private AnalysisState doi = AnalysisState.TO_ANALYSE;

	@Column(name = "title", nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private AnalysisState title = AnalysisState.TO_ANALYSE;

	@Column(name = "abstract", nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private AnalysisState abstractText = AnalysisState.TO_ANALYSE;

	@Column(name = "full_text", nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private AnalysisState fullText = AnalysisState.TO_ANALYSE;

	@Column(name = "release_date", nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private AnalysisState releaseDate = AnalysisState.TO_ANALYSE;

	@Column(name = "person", nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private AnalysisState person = AnalysisState.TO_ANALYSE;

	@Column(name = "instrument", nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private AnalysisState instrument = AnalysisState.TO_ANALYSE;

	@Column(name = "laboratory", nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private AnalysisState laboratory = AnalysisState.TO_ANALYSE;

	@Column(name = "keyword", nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private AnalysisState keyword = AnalysisState.TO_ANALYSE;

	@Column(name = "formula", nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private AnalysisState formula = AnalysisState.TO_ANALYSE;

	@Column(name = "reference", nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private AnalysisState reference = AnalysisState.TO_ANALYSE;

	@Column(name = "citation", nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private AnalysisState citation = AnalysisState.TO_ANALYSE;

	@Column(name = "research_domain", nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private AnalysisState researchDomain = AnalysisState.TO_ANALYSE;

	@Column(name = "journal", nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private AnalysisState journal = AnalysisState.TO_ANALYSE;

	@Column(name = "publisher", nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private AnalysisState publisher = AnalysisState.TO_ANALYSE;

	@Column(name = "extracted_image", nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private AnalysisState extractedImage = AnalysisState.TO_ANALYSE;

	@Column(name = "additional_text", nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private AnalysisState additionalText = AnalysisState.TO_ANALYSE;

	@Column(name = "analysis_setup")
	private String analysisSetup;

	@Column(name = "analysis_date")
	private Date analysisDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DocumentVersion getDocumentVersion() {
		return documentVersion;
	}

	public void setDocumentVersion(DocumentVersion documentVersion) {
		this.documentVersion = documentVersion;
	}

	public AnalysisState getDoi() {
		return doi;
	}

	public void setDoi(AnalysisState doi) {
		this.doi = doi;
	}

	public AnalysisState getTitle() {
		return title;
	}

	public void setTitle(AnalysisState title) {
		this.title = title;
	}

	public AnalysisState getAbstractText() {
		return abstractText;
	}

	public void setAbstractText(AnalysisState abstractText) {
		this.abstractText = abstractText;
	}

	public AnalysisState getFullText() {
		return fullText;
	}

	public void setFullText(AnalysisState fullText) {
		this.fullText = fullText;
	}

	public AnalysisState getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(AnalysisState releaseDate) {
		this.releaseDate = releaseDate;
	}

	public AnalysisState getPerson() {
		return person;
	}

	public void setPerson(AnalysisState person) {
		this.person = person;
	}

	public AnalysisState getInstrument() {
		return instrument;
	}

	public void setInstrument(AnalysisState instrument) {
		this.instrument = instrument;
	}

	public AnalysisState getLaboratory() {
		return laboratory;
	}

	public void setLaboratory(AnalysisState laboratory) {
		this.laboratory = laboratory;
	}

	public AnalysisState getKeyword() {
		return keyword;
	}

	public void setKeyword(AnalysisState keyword) {
		this.keyword = keyword;
	}

	public AnalysisState getFormula() {
		return formula;
	}

	public void setFormula(AnalysisState formula) {
		this.formula = formula;
	}

	public AnalysisState getReference() {
		return reference;
	}

	public void setReference(AnalysisState reference) {
		this.reference = reference;
	}

	public AnalysisState getCitation() {
		return citation;
	}

	public void setCitation(AnalysisState citation) {
		this.citation = citation;
	}

	public AnalysisState getResearchDomain() {
		return researchDomain;
	}

	public void setResearchDomain(AnalysisState researchDomain) {
		this.researchDomain = researchDomain;
	}

	public AnalysisState getJournal() {
		return journal;
	}

	public void setJournal(AnalysisState journal) {
		this.journal = journal;
	}

	public AnalysisState getPublisher() {
		return publisher;
	}

	public void setPublisher(AnalysisState publisher) {
		this.publisher = publisher;
	}

	public AnalysisState getExtractedImage() {
		return extractedImage;
	}

	public void setExtractedImage(AnalysisState extractedImage) {
		this.extractedImage = extractedImage;
	}

	public AnalysisState getAdditionalText() {
		return additionalText;
	}

	public void setAdditionalText(AnalysisState additionalText) {
		this.additionalText = additionalText;
	}

	public String getAnalysisSetup() {
		return analysisSetup;
	}

	public void setAnalysisSetup(String analysisSetup) {
		this.analysisSetup = analysisSetup;
	}

	public Date getAnalysisDate() {
		return analysisDate;
	}

	public void setAnalysisDate(Date analysisDate) {
		this.analysisDate = analysisDate;
	}

	public AnalysisState getEntityState(EntityType entityType) {
		if (entityType.equals(EntityType.DOI)) {
			return this.doi;

		} else if (entityType.equals(EntityType.TITLE)) {
			return this.title;

		} else if (entityType.equals(EntityType.ABSTRACT)) {
			return this.abstractText;

		} else if (entityType.equals(EntityType.FULL_TEXT)) {
			return this.fullText;

		} else if (entityType.equals(EntityType.RELEASE_DATE)) {
			return this.releaseDate;

		} else if (entityType.equals(EntityType.PERSON)) {
			return this.person;

		} else if (entityType.equals(EntityType.INSTRUMENT)) {
			return this.instrument;

		} else if (entityType.equals(EntityType.LABORATORY)) {
			return this.laboratory;

		} else if (entityType.equals(EntityType.KEYWORD)) {
			return this.keyword;

		} else if (entityType.equals(EntityType.FORMULA)) {
			return this.formula;

		} else if (entityType.equals(EntityType.REFERENCE)) {
			return this.reference;

		} else if (entityType.equals(EntityType.CITATION)) {
			return this.citation;

		} else if (entityType.equals(EntityType.RESEARCH_DOMAIN)) {
			return this.researchDomain;

		} else if (entityType.equals(EntityType.JOURNAL)) {
			return this.journal;

		} else if (entityType.equals(EntityType.PUBLISHER)) {
			return this.publisher;

		} else if (entityType.equals(EntityType.EXTRACTED_IMAGE)) {
			return this.extractedImage;

		} else if (entityType.equals(EntityType.ADDITIONAL_TEXT)) {
			return this.additionalText;
		}

		return null;
	}


	public void setEntityState(EntityType entityType, AnalysisState analysisState) {
		if (entityType.equals(EntityType.DOI)) {
			this.doi = analysisState;

		} else if (entityType.equals(EntityType.TITLE)) {
			this.title = analysisState;

		} else if (entityType.equals(EntityType.ABSTRACT)) {
			this.abstractText = analysisState;

		} else if (entityType.equals(EntityType.FULL_TEXT)) {
			this.fullText = analysisState;

		} else if (entityType.equals(EntityType.RELEASE_DATE)) {
			this.releaseDate = analysisState;

		} else if (entityType.equals(EntityType.PERSON)) {
			this.person = analysisState;

		} else if (entityType.equals(EntityType.INSTRUMENT)) {
			this.instrument = analysisState;

		} else if (entityType.equals(EntityType.LABORATORY)) {
			this.laboratory = analysisState;

		} else if (entityType.equals(EntityType.KEYWORD)) {
			this.keyword = analysisState;

		} else if (entityType.equals(EntityType.FORMULA)) {
			this.formula = analysisState;

		} else if (entityType.equals(EntityType.REFERENCE)) {
			this.reference = analysisState;

		} else if (entityType.equals(EntityType.CITATION)) {
			this.citation = analysisState;

		} else if (entityType.equals(EntityType.RESEARCH_DOMAIN)) {
			this.researchDomain = analysisState;

		} else if (entityType.equals(EntityType.JOURNAL)) {
			this.journal = analysisState;

		} else if (entityType.equals(EntityType.PUBLISHER)) {
			this.publisher = analysisState;

		} else if (entityType.equals(EntityType.EXTRACTED_IMAGE)) {
			this.extractedImage = analysisState;

		} else if (entityType.equals(EntityType.ADDITIONAL_TEXT)) {
			this.additionalText = analysisState;
		}
	}

	public void copyFrom(DocumentVersionAnalysisState other) {
		this.doi = other.doi;
		this.title = other.title;
		this.abstractText = other.abstractText;
		this.fullText = other.fullText;
		this.releaseDate = other.releaseDate;
		this.person = other.person;
		this.instrument = other.instrument;
		this.laboratory = other.laboratory;
		this.keyword = other.keyword;
		this.formula = other.formula;
		this.reference = other.reference;
		this.citation = other.citation;
		this.researchDomain = other.researchDomain;
		this.journal = other.journal;
		this.publisher = other.publisher;
		this.extractedImage = other.extractedImage;
		this.additionalText = other.additionalText;
		this.analysisSetup = other.analysisSetup;
		this.analysisDate = other.analysisDate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		DocumentVersionAnalysisState that = (DocumentVersionAnalysisState) o;

		return new EqualsBuilder()
			.append(id, that.id)
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(id)
			.toHashCode();
	}
}
