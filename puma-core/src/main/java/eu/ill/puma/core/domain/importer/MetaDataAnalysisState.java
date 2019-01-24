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
package eu.ill.puma.core.domain.importer;


import eu.ill.puma.core.domain.analysis.AnalysisState;

public class MetaDataAnalysisState {

	private AnalysisState doi = AnalysisState.TO_ANALYSE;
	private AnalysisState title = AnalysisState.TO_ANALYSE;
	private AnalysisState abstractText = AnalysisState.TO_ANALYSE;
	private AnalysisState date = AnalysisState.TO_ANALYSE;
	private AnalysisState person = AnalysisState.TO_ANALYSE;
	private AnalysisState laboratory = AnalysisState.TO_ANALYSE;
	private AnalysisState instrument = AnalysisState.TO_ANALYSE;
	private AnalysisState experimentTechnique = AnalysisState.TO_ANALYSE;
	private AnalysisState keyword = AnalysisState.TO_ANALYSE;
	private AnalysisState formula = AnalysisState.TO_ANALYSE;
	private AnalysisState reference = AnalysisState.TO_ANALYSE;
	private AnalysisState citation = AnalysisState.TO_ANALYSE;
	private AnalysisState researchDomain = AnalysisState.TO_ANALYSE;
	private AnalysisState journal = AnalysisState.TO_ANALYSE;
	private AnalysisState publisher = AnalysisState.TO_ANALYSE;
	private AnalysisState extractedImage = AnalysisState.TO_ANALYSE;
	private AnalysisState additionalText = AnalysisState.TO_ANALYSE;
	private AnalysisState fullText = AnalysisState.TO_ANALYSE;

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

	public AnalysisState getAbstract() {
		return abstractText;
	}

	public void setAbstract(AnalysisState abstractText) {
		this.abstractText = abstractText;
	}

	public AnalysisState getDate() {
		return date;
	}

	public void setDate(AnalysisState date) {
		this.date = date;
	}

	public AnalysisState getPerson() {
		return person;
	}

	public void setPerson(AnalysisState person) {
		this.person = person;
	}

	public AnalysisState getLaboratory() {
		return laboratory;
	}

	public void setLaboratory(AnalysisState laboratory) {
		this.laboratory = laboratory;
	}

	public AnalysisState getInstrument() {
		return instrument;
	}

	public void setInstrument(AnalysisState instrument) {
		this.instrument = instrument;
	}

	public AnalysisState getExperimentTechnique() {
		return experimentTechnique;
	}

	public void setExperimentTechnique(AnalysisState experimentTechnique) {
		this.experimentTechnique = experimentTechnique;
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

	public AnalysisState getFullText() {
		return fullText;
	}

	public void setFullText(AnalysisState fullText) {
		this.fullText = fullText;
	}
}
