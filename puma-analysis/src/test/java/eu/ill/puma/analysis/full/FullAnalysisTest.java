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
package eu.ill.puma.analysis.full;

import eu.ill.puma.core.domain.analysis.AnalyserResponse;
import eu.ill.puma.analysis.analyser.DocumentAnalyser;
import eu.ill.puma.analysis.factory.AnalyserFactory;
import eu.ill.puma.analysis.manager.AnalysisCalculator;
import eu.ill.puma.analysis.utils.AnalysisUtils;
import eu.ill.puma.analysis.utils.ImporterCreator;
import eu.ill.puma.core.domain.analysis.AnalysisState;
import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.core.domain.importer.ImporterInfo;
import eu.ill.puma.core.utils.ResourceLoader;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionAnalysisHistory;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionAnalysisState;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileType;
import eu.ill.puma.persistence.domain.importer.Importer;
import eu.ill.puma.persistence.service.analysis.DocumentVersionAnalysisHistoryService;
import eu.ill.puma.persistence.service.analysis.DocumentVersionAnalysisStateService;
import eu.ill.puma.persistence.service.document.DocumentVersionService;
import eu.ill.puma.persistence.service.document.PumaFileService;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/applicationContext-test.xml"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class, DirtiesContextTestExecutionListener.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Ignore
public class FullAnalysisTest {

	@Autowired
	private ImporterCreator importerCreator;

	@Autowired
	private DocumentVersionAnalysisStateService analysisStateService;

	@Autowired
	private DocumentVersionService documentVersionService;

	@Autowired
	private DocumentVersionAnalysisHistoryService analysisHistoryService;

	@Autowired
	private PumaFileService fileService;

	@Autowired
	private AnalyserFactory analyserFactory;

	private AnalysisCalculator analysisCalculator;

	@PostConstruct
	public void initFactory() {
		this.analysisCalculator = new AnalysisCalculator(this.analyserFactory);
	}

	protected DocumentVersion getDocumentVersion(String importerName, String fileName, String infoFileName) throws Exception {
		Importer importer = this.importerCreator.createImporter(importerName);

		BaseDocument importerDocument = ResourceLoader.readType(fileName, BaseDocument.class);
		ImporterInfo importerInfo = ResourceLoader.readType(infoFileName, ImporterInfo.class);

		DocumentVersion documentVersion = this.importerCreator.importDocument(importerDocument, importer, importerInfo);

		List<DocumentVersion> documentVersions = this.documentVersionService.getAll();
		Assert.assertEquals(1, documentVersions.size());

		List<DocumentVersionAnalysisState> allAnalysisStates = this.analysisStateService.getAll();
		Assert.assertEquals(1, allAnalysisStates.size());

		this.documentVersionService.getAllEntities(documentVersion, true);
		return documentVersion;
	}

	protected void doFakeAnalysis(DocumentVersion documentVersion, String[] analyserNames) throws Exception {
		int loopIndex = 0;
		String analyserName = null;

		do {
			List<String> analysisHistory = this.analysisHistoryService.getAllSuccessfulAnalysersForDocumentVersion(documentVersion);

			analyserName = this.analysisCalculator.determineAnalysis(documentVersion, analysisHistory);

			if (analyserName != null) {
				Assert.assertTrue(analyserNames.length > loopIndex);

				// Get the analysers from the factory
				DocumentAnalyser documentAnalyser = this.analyserFactory.getAnalyserForName(analyserName);

				Assert.assertEquals(analyserNames[loopIndex], documentAnalyser.getName());

				// Fake response
				AnalyserResponse analyserResponse = new AnalyserResponse();
				analyserResponse.setDuration(50l);

				// Add analysis history
				DocumentVersionAnalysisHistory history = AnalysisUtils.createSuccessfulAnalysisHistory(documentVersion, documentAnalyser, analyserResponse);
				this.analysisHistoryService.save(history);

				// Clean up result : remove any unwanted entities
				DocumentVersionAnalysisState analysisState = documentVersion.getAnalysisState();

				// Update analysis state
				AnalysisUtils.updateAnalysisState(analysisState, documentAnalyser);
				List<EntityType> producedEntities = documentAnalyser.getProducedEntities();
				for (EntityType entityType : producedEntities) {
					Assert.assertNotEquals(AnalysisState.TO_ANALYSE, analysisState.getEntityState(entityType));
				}

				analysisState.setAnalysisDate(new Date());
				this.analysisStateService.save(analysisState);

				// create dummy fulltext file
				if (producedEntities.contains(EntityType.FULL_TEXT)) {
					PumaFile pumaFile = new PumaFile();
					pumaFile.setDocumentType(PumaFileType.EXTRACTED_FULL_TEXT);
					pumaFile.setMimeType("application/json");
					pumaFile.setDocumentVersion(documentVersion);

					pumaFile.setData(StringUtils.getBytesUtf8("todo"));

					documentVersion.addFile(pumaFile);
					fileService.save(pumaFile);
				}

				loopIndex++;
			}

		} while (analyserName != null);

		Assert.assertEquals(analyserNames.length, loopIndex);
	}


}
