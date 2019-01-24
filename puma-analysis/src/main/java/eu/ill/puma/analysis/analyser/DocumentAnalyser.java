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
import eu.ill.puma.analysis.exception.AnalysisException;
import eu.ill.puma.analysis.exception.UnPreparedAnalyserException;
import eu.ill.puma.core.domain.analysis.AnalyserResponse;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by letreguilly on 21/07/17.
 */
public abstract class DocumentAnalyser {

	private static final Logger log = LoggerFactory.getLogger(DocumentAnalyser.class);

	private String name = "";
	private List<EntityType> producedEntities = new ArrayList<>();

	private int instanceIndex;
	protected Map<String, String> properties;

	private boolean busy = false;
	private boolean initiated = false;

	public DocumentAnalyser(int instanceIndex, Map<String, String> properties) {
		this.instanceIndex = instanceIndex;
		this.properties = properties;
		if (this.getClass().getAnnotation(Analyser.class) != null) {
			this.name = this.getClass().getAnnotation(Analyser.class).name();
			this.producedEntities = Arrays.asList(this.getClass().getAnnotation(Analyser.class).produces());
		}
	}

	public DocumentAnalyser(int instanceIndex, Map<String, String> properties, Analyser analyser) {
		this.instanceIndex = instanceIndex;
		this.properties = properties;
		this.name = analyser.name();
		this.producedEntities = Arrays.asList(analyser.produces());
	}

	/**
	 * analyse the given document
	 *
	 * @param document
	 * @return
	 */
	public AnalyserResponse analyse(DocumentVersion document) throws UnPreparedAnalyserException, AnalysisException {
		//stats
		long startTime = System.currentTimeMillis();

		//analyse
		AnalyserResponse analyserResponse = null;

		if (document == null) {
			analyserResponse = new AnalyserResponse();
			analyserResponse.setSuccessful(false);
			analyserResponse.setMessage("Document sent for analysis to \"" + this.name + "\" is null");
			log.warn("Null document sent to analyser \"" + this.name + "\"");

		} else {
			//prepare
			if (!initiated) {
				if (!this.prepareAnalyser()) {
					throw new UnPreparedAnalyserException("Failed to prepare \"" + this.name + "\"");
				}
			}

			try {
				busy = true;
				analyserResponse = this.doAnalyse(document);
				busy = false;

			} catch (Throwable ex) {
				analyserResponse = new AnalyserResponse();
				analyserResponse.setSuccessful(false);
				analyserResponse.setMessage(ex.getMessage());
				log.error("analyser \"" + this.name + "\" has failed for document " + document.getId() + " : " + ex.getMessage(), ex);
			}

		}

		//stats results
		long duration = System.currentTimeMillis() - startTime;

		analyserResponse.setDuration(duration);
		analyserResponse.getBaseDocument().setPumaId(document.getId());

		return analyserResponse;
	}

	/**
	 * perform the analysis
	 *
	 * @param document
	 * @return
	 */
	protected abstract AnalyserResponse doAnalyse(DocumentVersion document) throws AnalysisException;

	/**
	 * prepare the analyser after construct, write here the initialisation code
	 *
	 * @return
	 */
	protected abstract boolean prepareAnalyser();

	public abstract void destroyAnalyser();

	public boolean isBusy() {
		return busy;
	}

	public String getName() {
		return this.name;
	}

	public List<EntityType> getProducedEntities() {
		return producedEntities;
	}

	public int getInstanceIndex() {
		return instanceIndex;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public abstract String getFilePrefix();
}