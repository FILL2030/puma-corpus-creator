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
package eu.ill.puma.analysis.utils;

import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.core.domain.importer.ImporterInfo;
import eu.ill.puma.core.domain.importer.MetaDataAnalysisState;
import eu.ill.puma.importermanager.downloader.PumaFileDownloader;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionAnalysisState;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.importer.Importer;
import eu.ill.puma.persistence.service.analysis.DocumentVersionAnalysisStateService;
import eu.ill.puma.persistence.service.converterV2.DocumentConverter;
import eu.ill.puma.persistence.service.importer.ImporterService;
import eu.ill.puma.taskmanager.TaskManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImporterCreator {
	@Autowired
	private ImporterService importerService;

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private PumaFileDownloader fileDownloader;

	@Autowired
	private DocumentVersionAnalysisStateService analysisStateService;

	@Autowired
	private DocumentConverter documentConverter;

	public Importer createImporter(String importerName) {
		// Create importer
		String importerFullName = importerName + " Importer";
		String shortName = importerName;
		String importerUrl = "http://importer-" + importerName + ".puma.ill.fr";

		Importer importer = new Importer();
		importer.setName(importerFullName);
		importer.setShortName(shortName);
		importer.setUrl(importerUrl);
		importerService.save(importer);

		return importerService.getById(importer.getId());
	}

	public DocumentVersion importDocument(BaseDocument baseDocument, Importer importer, ImporterInfo importerInfo) throws Exception {
		this.taskManager.enable();

		DocumentVersion documentVersion = this.documentConverter.convert(baseDocument, importer.getShortName());

		this.initialiseAnalysisState(documentVersion, importerInfo.getMetaDataAnalysisState());

		this.fileDownloader.downloadFilesForDocumentVersion(documentVersion);

		return documentVersion;
	}

	private void initialiseAnalysisState(DocumentVersion documentVersion, MetaDataAnalysisState metaDataAnalysisState) {
		DocumentVersionAnalysisState analysisState = new DocumentVersionAnalysisState();

		analysisState.setDocumentVersion(documentVersion);
		analysisState.setDoi(metaDataAnalysisState.getDoi());
		analysisState.setTitle(metaDataAnalysisState.getTitle());
		analysisState.setAbstractText(metaDataAnalysisState.getAbstract());
		analysisState.setReleaseDate(metaDataAnalysisState.getDate());
		analysisState.setPerson(metaDataAnalysisState.getPerson());
		analysisState.setInstrument(metaDataAnalysisState.getInstrument());
		analysisState.setLaboratory(metaDataAnalysisState.getLaboratory());
		analysisState.setKeyword(metaDataAnalysisState.getKeyword());
		analysisState.setFormula(metaDataAnalysisState.getFormula());
		analysisState.setReference(metaDataAnalysisState.getReference());
		analysisState.setCitation(metaDataAnalysisState.getCitation());
		analysisState.setResearchDomain(metaDataAnalysisState.getResearchDomain());
		analysisState.setJournal(metaDataAnalysisState.getJournal());
		analysisState.setPublisher(metaDataAnalysisState.getPublisher());
		analysisState.setExtractedImage(metaDataAnalysisState.getExtractedImage());
		analysisState.setAdditionalText(metaDataAnalysisState.getAdditionalText());

		documentVersion.setAnalysisState(analysisState);

		this.analysisStateService.save(analysisState);
	}
}
