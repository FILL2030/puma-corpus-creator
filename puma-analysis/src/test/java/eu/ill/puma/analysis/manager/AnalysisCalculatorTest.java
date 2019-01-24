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
import eu.ill.puma.persistence.domain.analysis.DocumentVersionAnalysisState;
import eu.ill.puma.persistence.domain.document.Document;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.DocumentVersionSource;
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.domain.document.enumeration.DocumentType;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileType;
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

import javax.annotation.PostConstruct;
import java.util.ArrayList;
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
public class AnalysisCalculatorTest {

	@Autowired
	private AnalyserFactory analyserFactory;

	private AnalysisCalculator analysisCalculator;

	@PostConstruct
	public void initFactory() {
		this.analysisCalculator = new AnalysisCalculator(this.analyserFactory);
	}

	public DocumentVersion createDocumentVersion(DocumentType documentType) {
		Document document = new Document();
		document.setDocumentType(documentType);

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

	public PumaFile createFile(String originUrl, PumaFileType fileType, String mimeType) {
		PumaFile pumaFile = new PumaFile();
		pumaFile.setOriginUrl(originUrl);
		pumaFile.setDocumentType(fileType);
		pumaFile.setMimeType(mimeType);

		return pumaFile;
	}

	@Test
	public void testNoAnalysis() throws Exception {
		DocumentVersion documentVersion = this.createDocumentVersion(DocumentType.PUBLICATION);
		DocumentVersionAnalysisState analysisState = new DocumentVersionAnalysisState();
		documentVersion.setAnalysisState(analysisState);

		String analyserName = this.analysisCalculator.determineAnalysis(documentVersion, new ArrayList<String>());

		Assert.assertNull(analyserName);
	}

}
