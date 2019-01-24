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
package eu.ill.puma.core.domain.analysis;

import eu.ill.puma.core.domain.document.BaseDocument;

/**
 * Created by letreguilly on 25/07/17.
 */
public class AnalyserResponse {
	private BaseDocument baseDocument = new BaseDocument();

	private Long duration;

	private boolean successful = true;

	private String message;


	public BaseDocument getBaseDocument() {
		return baseDocument;
	}

	public void setBaseDocument(BaseDocument baseDocument) {
		this.baseDocument = baseDocument;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
