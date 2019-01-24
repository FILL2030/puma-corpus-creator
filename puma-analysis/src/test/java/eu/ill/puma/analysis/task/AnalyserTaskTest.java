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
package eu.ill.puma.analysis.task;

import eu.ill.puma.core.domain.analysis.AnalyserResponse;
import eu.ill.puma.analysis.analyser.DocumentAnalyser;
import eu.ill.puma.analysis.analyser.TestAnalyser;
import eu.ill.puma.analysis.factory.AnalyserFactory;
import eu.ill.puma.persistence.domain.document.Document;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.DocumentVersionSource;
import eu.ill.puma.persistence.domain.document.enumeration.DocumentType;
import eu.ill.puma.persistence.service.document.DocumentVersionService;
import eu.ill.puma.taskmanager.TaskManager;
import org.junit.Assert;
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

import java.util.Date;

/**
 * Created by letreguilly on 21/07/17.
 */
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
public class AnalyserTaskTest {

	@Autowired
	private DocumentVersionService documentVersionService;

	@Autowired
	private AnalyserFactory analyserFactory;

	@Autowired
	private TaskManager taskManager;


	public DocumentVersion createDocumentVersion() {
		Document document = new Document();
		document.setDocumentType(DocumentType.PUBLICATION);

		DocumentVersion documentVersion = new DocumentVersion();
		documentVersion.setDocument(document);

		DocumentVersionSource documentVersionSource = new DocumentVersionSource();
		documentVersionSource.setImporterShortName("TEST");
		documentVersionSource.setSourceId("testId");
		documentVersionSource.setImportDate(new Date());
		documentVersionSource.setDocumentVersion(documentVersion);

		documentVersion.addSource(documentVersionSource);

		return documentVersion;
	}

	@Test
	public void testAnalysisTask() throws Exception {
		DocumentVersion documentVersion = this.createDocumentVersion();

		this.documentVersionService.save(documentVersion);

		DocumentAnalyser analyser = this.analyserFactory.getAnalyserForName("test");
		Assert.assertNotNull(analyser.getName());

		AnalysisTask analysisTask = new AnalysisTask(documentVersion, analyser);

		this.taskManager.executeTask(analysisTask);

		AnalyserResponse response = analysisTask.get();

		Assert.assertTrue(response.getBaseDocument().getDoi().getValue().startsWith(TestAnalyser.DOI_BASE));
	}

}
