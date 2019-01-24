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
import eu.ill.puma.taskmanager.Task;
import eu.ill.puma.taskmanager.TaskPriority;

public class ActivatePendingAnalysisTask extends Task<Integer>  {

	private AnalyserManager analyserManager;
	private Integer maxNumberToAnalyse;

	public ActivatePendingAnalysisTask(AnalyserManager analyserManager, Integer maxNumberToAnalyse) {
		this.analyserManager = analyserManager;
		this.maxNumberToAnalyse = maxNumberToAnalyse;
		this.setPriority(TaskPriority.ASAP);
	}

	@Override
	public Integer execute() throws Exception {
		this.analyserManager.activatePendingAnalysis(this.maxNumberToAnalyse);

		return this.maxNumberToAnalyse;
	}

	public Integer getMaxNumberToAnalyse() {
		return this.maxNumberToAnalyse;
	}
}
