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

import eu.ill.puma.analysis.factory.AnalyserFactory;
import eu.ill.puma.analysis.manager.AnalyserManager;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionEntityOrigin;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.service.analysis.DocumentVersionAnalysisHistoryService;
import eu.ill.puma.persistence.service.analysis.DocumentVersionEntityOriginService;
import eu.ill.puma.persistence.service.document.DocumentVersionService;
import eu.ill.puma.taskmanager.TaskManager;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Ignore
public class WosPDFPublicationAnalysisTest1 extends FullAnalysisTest {

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private DocumentVersionAnalysisHistoryService analysisHistoryService;

	@Autowired
	private DocumentVersionEntityOriginService entityOriginService;

	@Autowired
	private DocumentVersionService documentVersionService;

	@Autowired
	private AnalyserFactory analyserFactory;

	@Autowired
	private AnalyserManager analyserManager;

	@Test
	public void importDocument() throws Exception {
		this.taskManager.enable();

		DocumentVersion documentVersion = this.getDocumentVersion("wos", "private/importers/wos/PDFPublicationDocument1.json", "private/importers/wos/ImporterInfo.json");

		List<DocumentVersionEntityOrigin> allOrigins = this.entityOriginService.getAllForDocumentVersion(documentVersion);

		Assert.assertEquals(1, this.entityOriginService.getAllForDocumentVersionAndEntityType(documentVersion, EntityType.DOI).size());
		Assert.assertEquals(1, this.entityOriginService.getAllForDocumentVersionAndEntityType(documentVersion, EntityType.TITLE).size());
		Assert.assertEquals(1, this.entityOriginService.getAllForDocumentVersionAndEntityType(documentVersion, EntityType.ABSTRACT).size());
		Assert.assertEquals(1, this.entityOriginService.getAllForDocumentVersionAndEntityType(documentVersion, EntityType.RELEASE_DATE).size());
		Assert.assertEquals(10, this.entityOriginService.getAllForDocumentVersionAndEntityType(documentVersion, EntityType.KEYWORD).size());
		Assert.assertEquals(8, this.entityOriginService.getAllForDocumentVersionAndEntityType(documentVersion, EntityType.RESEARCH_DOMAIN).size());
		Assert.assertEquals(2, this.entityOriginService.getAllForDocumentVersionAndEntityType(documentVersion, EntityType.LABORATORY).size());
		Assert.assertEquals(1, this.entityOriginService.getAllForDocumentVersionAndEntityType(documentVersion, EntityType.PERSON).size());
		Assert.assertEquals(1, this.entityOriginService.getAllForDocumentVersionAndEntityType(documentVersion, EntityType.PUBLISHER).size());
		Assert.assertEquals(1, this.entityOriginService.getAllForDocumentVersionAndEntityType(documentVersion, EntityType.JOURNAL).size());

		Assert.assertEquals(27, allOrigins.size());
	}


	@Test
	public void verifyAnalyserOrder() throws Exception {
		DocumentVersion documentVersion = this.getDocumentVersion("wos", "private/importers/wos/PDFPublicationDocument1.json", "private/importers/wos/ImporterInfo.json");
		String[] analyserNames = {"abby"};
		this.doFakeAnalysis(documentVersion, analyserNames);
	}

}
