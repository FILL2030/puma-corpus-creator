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
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.taskmanager.Task;
import eu.ill.puma.taskmanager.TaskPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalysisTask extends Task<AnalyserResponse> {

	private static final Logger log = LoggerFactory.getLogger(AnalysisTask.class);

	private DocumentVersion documentVersion;
	private DocumentAnalyser analyser;

	public AnalysisTask(DocumentVersion documentVersion, DocumentAnalyser analyser) {
		this.documentVersion = documentVersion;
		this.analyser = analyser;
		this.setPriority(TaskPriority.LOW);
	}

	@Override
	public AnalyserResponse execute() throws Exception {

		log.debug("Start analyser \"" + this.analyser.getName() + "\" for document " + this.documentVersion.getId());
		AnalyserResponse response = this.analyser.analyse(documentVersion);
		log.debug("End analyser \"" + this.analyser.getName() + "\" for document " + this.documentVersion.getId());

		return response;
	}

	public DocumentVersion getDocumentVersion() {
		return documentVersion;
	}

	public DocumentAnalyser getAnalyser() {
		return analyser;
	}
}
