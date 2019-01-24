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
package eu.ill.puma.persistence.service.analysis;

import eu.ill.puma.persistence.PumaTest;
import eu.ill.puma.core.domain.analysis.AnalysisState;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionAnalysisState;
import eu.ill.puma.persistence.domain.document.Document;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.DocumentVersionSource;
import eu.ill.puma.persistence.domain.document.enumeration.DocumentType;
import eu.ill.puma.persistence.service.document.DocumentVersionService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

public class DocumentVersionAnalysisStateServiceTest extends PumaTest {

	@Autowired
	private DocumentVersionService documentVersionService;

	@Autowired
	private DocumentVersionAnalysisStateService analysisStateService;

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
	public void testCreateAndRetrieve() throws Exception {
		DocumentVersion documentVersion = this.createDocumentVersion();

		this.documentVersionService.save(documentVersion);

		DocumentVersionAnalysisState analysisState = new DocumentVersionAnalysisState();
		analysisState.setDocumentVersion(documentVersion);
		analysisState.setPublisher(AnalysisState.TO_ANALYSE);
		analysisState.setJournal(AnalysisState.ANALYSED);
		analysisState.setInstrument(AnalysisState.ANALYSED);
		analysisState.setLaboratory(AnalysisState.CLOSED);

		this.analysisStateService.save(analysisState);

		DocumentVersionAnalysisState integrated = this.analysisStateService.getByDocumentVersion(documentVersion);

		Assert.assertTrue(integrated.getPublisher().equals(AnalysisState.TO_ANALYSE));
		Assert.assertTrue(integrated.getJournal().equals(AnalysisState.ANALYSED));
		Assert.assertTrue(integrated.getInstrument().equals(AnalysisState.ANALYSED));
		Assert.assertTrue(integrated.getLaboratory().equals(AnalysisState.CLOSED));

	}

}
