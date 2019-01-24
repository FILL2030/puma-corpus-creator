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
package eu.ill.puma.analysis.factory;

import eu.ill.puma.analysis.analyser.DocumentAnalyser;
import eu.ill.puma.analysis.annotation.Analyser;
import eu.ill.puma.analysis.exception.AnalyserInstantiationException;
import eu.ill.puma.core.utils.CpuUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyserPool {

	private int physicalCore = CpuUtils.getNumberOfCPUCores();

	private static final Logger log = LoggerFactory.getLogger(AnalyserPool.class);

	private List<DocumentAnalyser> availableAnalysers = new ArrayList<>();
	private List<DocumentAnalyser> busyAnalysers = new ArrayList<>();

	private Map<String, String> analyserProperties = new HashMap<>();

	private Class<? extends DocumentAnalyser> analyserClass;
	private Analyser analyser;

	private int maxPoolSize;

	public AnalyserPool(Analyser analyser, Class<? extends DocumentAnalyser> analyserClass, Map<String, String> analyserProperties) {
		this.analyser = analyser;
		this.analyserClass = analyserClass;
		if (analyserProperties != null) {
			this.analyserProperties = analyserProperties;
		}

		//set pool size
		if(analyser.limitMaxInstanceToPhysicalCpuCore() && physicalCore < analyser.maxInstances()){
			maxPoolSize = physicalCore;
		}else if(analyser.maxInstances() > 0){
			maxPoolSize = analyser.maxInstances();
		}else{
			maxPoolSize = Integer.MAX_VALUE;
		}
	}

	/**
	 * return the first available analyser
	 *
	 * @return
	 */
	public synchronized DocumentAnalyser get() throws AnalyserInstantiationException {
		DocumentAnalyser analyser = null;

		// If none available try to create one
		if (availableAnalysers.size() == 0) {
			this.createAnalyser();
		}

		// Get first available analyser if one exists
		if (availableAnalysers.size() > 0) {
			analyser = availableAnalysers.get(0);

			// Remove from available
			availableAnalysers.remove(analyser);

			// Add to busy
			this.busyAnalysers.add(analyser);
		}

		// If no available analyser then return first busy one
		if (analyser == null) {
			analyser = this.busyAnalysers.get(0);
		}

		return analyser;
	}

	public synchronized void release(DocumentAnalyser documentAnalyser) {
		// Add to available if not already there
		if (!this.availableAnalysers.contains(documentAnalyser)) {
			this.availableAnalysers.add(documentAnalyser);
		}

		// Remove from busy
		if (this.busyAnalysers.contains(documentAnalyser)) {
			this.busyAnalysers.remove(documentAnalyser);
		}
	}

	/**
	 * Get the number of instances
	 */
	public int getSize() {
		return this.availableAnalysers.size() + this.busyAnalysers.size();
	}

	/**
	 * Creates a new instance of the analyser if the maxInstances condition allows it
	 *
	 * @throws AnalyserInstantiationException
	 */
	private void createAnalyser() throws AnalyserInstantiationException {
		if (this.getSize() < maxPoolSize) {
			try {
				//instantiate analyser
				DocumentAnalyser analyser = this.analyserClass.getConstructor(Integer.TYPE, Map.class).newInstance(this.getSize(), this.analyserProperties);

				// Add to front of instances so that it is the first to be obtained
				this.availableAnalysers.add(0, analyser);

				log.debug("analyser created : \"" + this.analyser.name() + "\"");

			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				throw new AnalyserInstantiationException(e);
			}

		} else {
			log.debug("Cannot create any more \"" + this.analyser.name() + "\" availableAnalysers - maximum of " + this.getSize() + " has been reached");
		}
	}

	/**
	 * Destroys and removes all availableAnalysers
	 */
	public void destroyAllAnalysers() {
		for (DocumentAnalyser analyser : this.availableAnalysers) {
			analyser.destroyAnalyser();
		}

		this.availableAnalysers.clear();
	}

	public Analyser getAnalyser() {
		return this.analyser;
	}

	public Map<String, String> getAnalyserProperties() {
		return analyserProperties;
	}
}
