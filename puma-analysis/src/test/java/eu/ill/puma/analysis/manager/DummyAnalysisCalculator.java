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
import eu.ill.puma.core.domain.analysis.AnalysisState;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionAnalysisState;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DummyAnalysisCalculator extends AnalysisCalculator {

	private static final Logger log = LoggerFactory.getLogger(DummyAnalysisCalculator.class);


	public DummyAnalysisCalculator(AnalyserFactory factory) {
		super(factory);
	}

	@Override
	public String determineAnalysis(DocumentVersion documentVersion, List<String> analysisHistory) {

		DocumentVersionAnalysisState analysisState = documentVersion.getAnalysisState();
		if (analysisState.getDoi().equals(AnalysisState.TO_ANALYSE)) {
			return "test";
		} else {
			log.info("DOI has already been added to document " + documentVersion);
		}

		return null;
	}
}
