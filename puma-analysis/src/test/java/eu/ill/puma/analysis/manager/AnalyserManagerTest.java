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
package eu.ill.puma.analysis.manager;

import eu.ill.puma.analysis.factory.AnalyserFactory;
import eu.ill.puma.analysis.utils.ImporterCreator;
import eu.ill.puma.core.domain.analysis.AnalysisState;
import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.core.domain.importer.ImporterInfo;
import eu.ill.puma.core.utils.ResourceLoader;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionAnalysisHistory;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionAnalysisState;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionEntityOrigin;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.importer.Importer;
import eu.ill.puma.persistence.service.analysis.DocumentVersionAnalysisHistoryService;
import eu.ill.puma.persistence.service.analysis.DocumentVersionAnalysisStateService;
import eu.ill.puma.persistence.service.analysis.DocumentVersionEntityOriginService;
import eu.ill.puma.persistence.service.document.DocumentVersionService;
import eu.ill.puma.taskmanager.TaskManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
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

import java.util.List;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:/applicationContext-test.xml"
})
@TestExecutionListeners({
		DependencyInjectionTestExecutionListener.class,
		TransactionalTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Ignore
public class AnalyserManagerTest {

	@Autowired
	private ImporterCreator importerCreator;

	@Autowired
	private AnalyserManager analyserManager;

	@Autowired
	private DocumentVersionAnalysisStateService analysisStateService;

	@Autowired
	private DocumentVersionService documentVersionService;

	@Autowired
	private DocumentVersionAnalysisHistoryService analysisHistoryService;

	@Autowired
	private DocumentVersionEntityOriginService entityOriginService;

	@Autowired
	private AnalyserFactory analyserFactory;

	@Autowired
	private TaskManager taskManager;

	@Before
	public void importData() throws Exception {
		this.taskManager.enable();

		Importer importer = this.importerCreator.createImporter("wos");

		BaseDocument[] importerDocuments = ResourceLoader.readType("private/ImporterDocuments.json", BaseDocument[].class);
		ImporterInfo importerInfo = ResourceLoader.readType("private/ImporterInfo.json", ImporterInfo.class);

		for (BaseDocument importerDocument : importerDocuments) {
			this.importerCreator.importDocument(importerDocument, importer, importerInfo);
		}

		List<DocumentVersion> documentVersions = this.documentVersionService.getAll();
		Assert.assertEquals(importerDocuments.length, documentVersions.size());

		List<DocumentVersionAnalysisState> allAnalysisStates = this.analysisStateService.getAll();
		Assert.assertEquals(10, allAnalysisStates.size());
	}

	@Test
	public void verifyRequireAnalysisAfterImport() throws Exception {
		this.taskManager.disable();

		this.analyserManager.activatePendingAnalysis(null);

		long numberOfTasksToAnalyse = this.analyserManager.getNumberOfDocumentsActiveOrPendingAnalysis();
		Assert.assertEquals(10, numberOfTasksToAnalyse);

	}

	@Test
	public void verifyCancel() throws Exception {
		this.taskManager.disable();

		this.analyserManager.activatePendingAnalysis(null);

		List<DocumentVersion> documentVersions = this.documentVersionService.getAll();
		for (int i = 0; i < documentVersions.size(); i++) {
			this.analyserManager.cancelAnalysis(documentVersions.get(i));

			long numberOfTasksToAnalyse = this.analyserManager.getNumberOfDocumentsActiveOrPendingAnalysis();
			Assert.assertEquals(documentVersions.size() - i - 1, numberOfTasksToAnalyse);
		}
	}

	@Test
	public void verifyAnalysisStateUpdated() {
		this.analyserManager.setAnalysisCalculator(new DummyAnalysisCalculator(analyserFactory));

		List<DocumentVersion> documentVersions = this.documentVersionService.getAll();
		for (DocumentVersion documentVersion : documentVersions) {
			DocumentVersionAnalysisState analysisState = this.analysisStateService.getByDocumentVersion(documentVersion);
			Assert.assertEquals(AnalysisState.TO_ANALYSE, analysisState.getDoi());
		}

		this.analyserManager.activatePendingAnalysis(null);

		for (DocumentVersion documentVersion : documentVersions) {
			DocumentVersionAnalysisState analysisState = this.analysisStateService.getByDocumentVersion(documentVersion);
			Assert.assertEquals(AnalysisState.ANALYSED, analysisState.getDoi());
		}
	}

	@Test
	public void verifyAnalysisHistoryUpdated() {
		this.analyserManager.setAnalysisCalculator(new DummyAnalysisCalculator(analyserFactory));

		List<DocumentVersion> documentVersions = this.documentVersionService.getAll();
		for (DocumentVersion documentVersion : documentVersions) {
			DocumentVersionAnalysisState analysisState = this.analysisStateService.getByDocumentVersion(documentVersion);
			Assert.assertEquals(AnalysisState.TO_ANALYSE, analysisState.getDoi());
		}

		this.analyserManager.activatePendingAnalysis(null);

		for (DocumentVersion documentVersion : documentVersions) {
			DocumentVersionAnalysisHistory analysisHistory = this.analysisHistoryService.getForDocumentVersionAndSuccessfulAnalyser(documentVersion, "test");
			Assert.assertNotNull(analysisHistory);
			Assert.assertTrue(analysisHistory.isSuccessful());
		}
	}

	@Test
	public void verifyEntityOriginUpdated() {
		this.analyserManager.setAnalysisCalculator(new DummyAnalysisCalculator(analyserFactory));

		List<DocumentVersion> documentVersions = this.documentVersionService.getAll();
		for (DocumentVersion documentVersion : documentVersions) {
			DocumentVersionAnalysisState analysisState = this.analysisStateService.getByDocumentVersion(documentVersion);
			Assert.assertEquals(AnalysisState.TO_ANALYSE, analysisState.getDoi());
		}

		this.analyserManager.activatePendingAnalysis(null);

		for (DocumentVersion documentVersion : documentVersions) {
			List<DocumentVersionEntityOrigin> entityOrigins = this.entityOriginService.getAllForDocumentVersionAndEntityType(documentVersion, EntityType.DOI);

			Assert.assertTrue(entityOrigins.size() > 0);
			boolean found = false;
			for (DocumentVersionEntityOrigin entityOrigin : entityOrigins) {
				if (entityOrigin.getEntityOrigin().equals("test")) {
					found = true;
				}
			}
			Assert.assertTrue(found);
		}
	}



}
