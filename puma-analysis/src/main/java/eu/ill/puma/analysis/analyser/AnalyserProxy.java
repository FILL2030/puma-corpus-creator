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

import eu.ill.puma.analysis.exception.AnalyserInstantiationException;
import eu.ill.puma.analysis.exception.AnalysisException;
import eu.ill.puma.analysis.exception.UnPreparedAnalyserException;
import eu.ill.puma.analysis.factory.AnalyserPool;
import eu.ill.puma.core.domain.analysis.AnalyserResponse;
import eu.ill.puma.persistence.domain.document.DocumentVersion;

public class AnalyserProxy extends DocumentAnalyser {

	private AnalyserPool analyserPool;

	public AnalyserProxy(AnalyserPool analyserPool) {
		super(-1, analyserPool.getAnalyserProperties(), analyserPool.getAnalyser());

		this.analyserPool = analyserPool;
	}

	@Override
	public AnalyserResponse analyse(DocumentVersion documentVersion) throws UnPreparedAnalyserException, AnalysisException {
		DocumentAnalyser documentAnalyser = null;
		try {
			documentAnalyser = this.analyserPool.get();

		} catch (AnalyserInstantiationException e) {
			throw new UnPreparedAnalyserException(e);
		}

		AnalyserResponse analyserResponse = documentAnalyser.analyse(documentVersion);

		this.analyserPool.release(documentAnalyser);

		return analyserResponse;
	}

	@Override
	protected AnalyserResponse doAnalyse(DocumentVersion document) {
		// Never called
		return null;
	}

	@Override
	protected boolean prepareAnalyser() {
		// Never called
		return false;
	}

	@Override
	public void destroyAnalyser() {
		// Never called
	}

	@Override
	public String getFilePrefix() {
		// Never called
		return null;
	}
}
