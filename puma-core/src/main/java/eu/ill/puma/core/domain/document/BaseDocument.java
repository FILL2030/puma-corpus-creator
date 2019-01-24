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
package eu.ill.puma.core.domain.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.ill.puma.core.domain.document.entities.*;
import eu.ill.puma.core.domain.document.enumeration.BaseDocumentType;
import eu.ill.puma.core.domain.document.enumeration.BaseDocumentVersionSubType;

import java.util.ArrayList;
import java.util.List;

public class BaseDocument {

	private Long id = null;
	private String sourceId = null;
	private boolean isModifiedAtSource = false;

	@JsonIgnore
	private Long pumaId;

	private BaseStringEntity doi;
	private BaseStringEntity title = null;
	private BaseStringEntity abstractText = null;
	private BaseDateEntity releaseDate = null;

	private BaseStringEntity shortName = null;
	private BaseDocumentType type = null;
	private BaseDocumentVersionSubType subType = null;

	private BaseJournal journal = null;

	private List<BaseStringEntity> keywords = new ArrayList();
	private List<BaseStringEntity> references = new ArrayList();
	private List<BaseStringEntity> citations = new ArrayList();
	private List<BaseStringEntity> researchDomains = new ArrayList();

	private List<BaseLaboratory> laboratories = new ArrayList();
	private List<BasePerson> persons = new ArrayList();
	private List<BaseInstrument> instruments = new ArrayList();
	private List<BaseFormula> formulas = new ArrayList();
	private List<BasePublisher> publishers = new ArrayList();
	private List<BaseFile> files = new ArrayList();
	private List<BaseFileToDownload> filesToDownload = new ArrayList();
	private List<BaseAdditionalText> additionalTexts = new ArrayList();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public boolean isModifiedAtSource() {
		return isModifiedAtSource;
	}

	public void setModifiedAtSource(boolean modifiedAtSource) {
		isModifiedAtSource = modifiedAtSource;
	}

	@JsonIgnore
	public Long getPumaId() {
		return pumaId;
	}

	@JsonIgnore
	public void setPumaId(Long pumaId) {
		this.pumaId = pumaId;
	}

	public BaseStringEntity getDoi() {
		return doi;
	}

	public void setDoi(BaseStringEntity doi) {
		this.doi = doi;
	}

	public BaseStringEntity getShortName() {
		return shortName;
	}

	public void setShortName(BaseStringEntity shortName) {
		this.shortName = shortName;
	}

	public BaseStringEntity getTitle() {
		return title;
	}

	public void setTitle(BaseStringEntity title) {
		this.title = title;
	}

	public BaseStringEntity getAbstract() {
		return abstractText;
	}

	public void setAbstract(BaseStringEntity abstractText) {
		this.abstractText = abstractText;
	}

	public BaseDocumentType getType() {
		return type;
	}

	public void setType(BaseDocumentType type) {
		this.type = type;
	}

	public BaseDocumentVersionSubType getSubType() {
		return subType;
	}

	public void setSubType(BaseDocumentVersionSubType subType) {
		this.subType = subType;
	}

	public BaseDateEntity getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(BaseDateEntity releaseDate) {
		this.releaseDate = releaseDate;
	}

	public BaseJournal getJournal() {
		return journal;
	}

	public void setJournal(BaseJournal journal) {
		this.journal = journal;
	}

	public List<BaseStringEntity> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<BaseStringEntity> keywords) {
		this.keywords = keywords;
	}

	public void addKeyword(BaseStringEntity keyword) {
		this.keywords.add(keyword);
	}

	public void removeAllKeywords() {
		this.keywords.clear();
	}

	public List<BaseStringEntity> getReferences() {
		return references;
	}

	public void setReferences(List<BaseStringEntity> references) {
		this.references = references;
	}

	public void addReference(BaseStringEntity reference) {
		this.references.add(reference);
	}

	public void removeAllReferences() {
		this.references.clear();
	}

	public List<BaseStringEntity> getCitations() {
		return citations;
	}

	public void setCitations(List<BaseStringEntity> citations) {
		this.citations = citations;
	}

	public void addCitation(BaseStringEntity citation) {
		this.citations.add(citation);
	}

	public void removeAllCitations() {
		this.citations.clear();
	}

	public List<BaseStringEntity> getResearchDomains() {
		return researchDomains;
	}

	public void setResearchDomains(List<BaseStringEntity> researchDomains) {
		this.researchDomains = researchDomains;
	}

	public void addResearchDomain(BaseStringEntity researchDomain) {
		this.researchDomains.add(researchDomain);
	}

	public void removeAllResearchDomains() {
		this.researchDomains.clear();
	}

	public List<BaseLaboratory> getLaboratories() {
		return laboratories;
	}

	public void setLaboratories(List<BaseLaboratory> laboratories) {
		this.laboratories = laboratories;
	}

	public void addLaboratory(BaseLaboratory laboratory) {
		this.laboratories.add(laboratory);
	}

	public void removeAllLaboratories() {
		this.laboratories.clear();
	}

	public List<BasePerson> getPersons() {
		return persons;
	}

	public void addPerson(BasePerson person) {
		this.persons.add(person);
	}

	public void setPersons(List<BasePerson> persons) {
		this.persons = persons;
	}

	public void removeAllPersons() {
		this.persons.clear();
	}

	public List<BaseInstrument> getInstruments() {
		return instruments;
	}

	public void setInstruments(List<BaseInstrument> instruments) {
		this.instruments = instruments;
	}

	public void addInstrument(BaseInstrument instrument) {
		this.instruments.add(instrument);
	}

	public void removeAllInstruments() {
		this.instruments.clear();
	}

	public List<BaseFormula> getFormulas() {
		return formulas;
	}

	public void setFormulas(List<BaseFormula> formulas) {
		this.formulas = formulas;
	}

	public void addFormula(BaseFormula formula) {
		this.formulas.add(formula);
	}

	public void removeAllFormulas() {
		this.formulas.clear();
	}

	public List<BasePublisher> getPublishers() {
		return publishers;
	}

	public void setPublishers(List<BasePublisher> publishers) {
		this.publishers = publishers;
	}

	public void addPublisher(BasePublisher publisher) {
		this.publishers.add(publisher);
	}

	public void removeAllPublishers() {
		this.publishers.clear();
	}

	public List<BaseFile> getFiles() {
		return files;
	}

	public void setFiles(List<BaseFile> files) {
		this.files = files;
	}

	public void addFile(BaseFile file) {
		this.files.add(file);
	}

	public void removeFile(BaseFile file) {
		this.files.remove(file);
	}

	public List<BaseFileToDownload> getFilesToDownload() {
		return filesToDownload;
	}

	public void setFilesToDownload(List<BaseFileToDownload> filesToDownload) {
		this.filesToDownload = filesToDownload;
	}

	public void addFileToDownload(BaseFileToDownload fileToDownload) {
		this.filesToDownload.add(fileToDownload);
	}

	public List<BaseAdditionalText> getAdditionalTexts() {
		return additionalTexts;
	}

	public void setAdditionalTexts(List<BaseAdditionalText> additionalTexts) {
		this.additionalTexts = additionalTexts;
	}

	public void addAdditionalText(BaseAdditionalText additionalText) {
		this.additionalTexts.add(additionalText);
	}

}

