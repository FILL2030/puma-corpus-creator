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
package eu.ill.puma.analysis.analyser;

import eu.ill.puma.analysis.annotation.Analyser;
import eu.ill.puma.core.domain.analysis.AnalyserResponse;
import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.core.domain.document.MetadataConfidence;
import eu.ill.puma.core.domain.document.entities.BaseStringEntity;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import eu.ill.puma.persistence.domain.document.DocumentVersion;

import java.util.Map;

/**
 * Created by letreguilly on 21/07/17.
 */
@Analyser(name = "test", maxInstances = 3, produces = {EntityType.DOI})
public class TestAnalyser extends DocumentAnalyser {

	public static final String DOI_BASE = "doi:test:";
	public static int DOI_ID_COUNT = 0;

	public TestAnalyser(int instanceIndex, Map<String, String> properties) {
		super(instanceIndex, properties);
	}

	@Override
	protected boolean prepareAnalyser() {
		return true;
	}

	@Override
	public void destroyAnalyser() {

	}

	@Override
	public AnalyserResponse doAnalyse(DocumentVersion document) {
		BaseDocument baseDocument = new BaseDocument();
		baseDocument.setDoi(new BaseStringEntity(DOI_BASE + DOI_ID_COUNT++, MetadataConfidence.FOUND));

		AnalyserResponse response = new AnalyserResponse();
		response.setBaseDocument(baseDocument);

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return response;
	}

	@Override
	public String getFilePrefix() {
		return null;
	}
}