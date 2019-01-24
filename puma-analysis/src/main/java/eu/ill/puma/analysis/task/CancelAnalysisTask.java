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

import eu.ill.puma.analysis.manager.AnalyserManager;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.taskmanager.Task;
import eu.ill.puma.taskmanager.TaskPriority;

public class CancelAnalysisTask extends Task<DocumentVersion>  {

	private AnalyserManager analyserManager;
	private DocumentVersion documentVersion;

	public CancelAnalysisTask(AnalyserManager analyserManager, DocumentVersion documentVersion) {
		this.analyserManager = analyserManager;
		this.documentVersion = documentVersion;
		this.setPriority(TaskPriority.ASAP);
	}

	@Override
	public DocumentVersion execute() throws Exception {
		this.analyserManager.cancelAnalysis(this.documentVersion);

		return this.documentVersion;
	}
}
