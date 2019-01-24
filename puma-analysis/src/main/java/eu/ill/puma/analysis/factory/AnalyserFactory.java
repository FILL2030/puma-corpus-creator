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

import eu.ill.puma.analysis.analyser.AnalyserProxy;
import eu.ill.puma.analysis.analyser.DocumentAnalyser;
import eu.ill.puma.analysis.annotation.Analyser;
import eu.ill.puma.analysis.exception.AnalyserInstantiationException;
import eu.ill.puma.analysis.exception.AnalyserNotFoundException;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by letreguilly on 21/07/17.
 */
@Service
public class AnalyserFactory {

	private static final Logger log = LoggerFactory.getLogger(AnalyserFactory.class);

	private Map<String, AnalyserPool> analyserPools = new ConcurrentHashMap();
	private List<String> enabledAnalysers = new ArrayList<>();

	@Autowired
	private AnalysisConfiguration analysisConfiguration;

	/**
	 * register all analyser at the application startup
	 */
	@PostConstruct
	private void registerAllAnalyser() {

		// Get configuration for analysers
		Map<String, Map<String, String>> analyserProperties = analysisConfiguration.getAnalyserConfig();

		//get all analyser class
		Reflections reflections = new Reflections("eu.ill.puma.analysis.analyser");

		//iterate
		for (Class analyserClass : reflections.getTypesAnnotatedWith(Analyser.class)) {

			//get annotation
			Analyser analyser = (Analyser) analyserClass.getAnnotation(Analyser.class);

			// create new pool for analyser
			if (analyser != null) {
				this.analyserPools.put(analyser.name(), new AnalyserPool(analyser, analyserClass, analyserProperties.get(analyser.name())));

				if (analyser.enabled()) {
					this.enabledAnalysers.add(analyser.name());
				}

			} else {
				log.error("Failed to get instance of analyser with class " + analyserClass);
			}
		}
	}

	/**
	 * closed all analyser at application stop
	 */
	@PreDestroy
	private void closeAnalyser() {
		log.info("Terminating all analysers");
		for (AnalyserPool analyserPool : analyserPools.values()) {
			analyserPool.destroyAllAnalysers();
		}
	}

	/**
	 * Returns an analyser for the given name
	 * @param analyserName
	 * @return
	 * @throws AnalyserNotFoundException
	 * @throws AnalyserInstantiationException
	 */
	public synchronized DocumentAnalyser getAnalyserForName(String analyserName) throws AnalyserNotFoundException, AnalyserInstantiationException {

		//check if the analyser exist
		if (this.analyserPools.containsKey(analyserName)) {
			// Get pool for analyser type
			AnalyserPool analyserPool = analyserPools.get(analyserName);

			// return new proxy
			return new AnalyserProxy(analyserPool);

		} else {
			//if not exist throw an exception
			throw new AnalyserNotFoundException("analyser " + analyserName + " not found");
		}
	}

	public Map<String, List<EntityType>> getAnalyserEntityProduction() {
		Map<String, List<EntityType>> production = new HashMap<>();
		for (String analyserName : this.analyserPools.keySet()) {
			if (this.enabledAnalysers.contains(analyserName)) {
				AnalyserPool pool = this.analyserPools.get(analyserName);

				List<EntityType> produces = Arrays.asList(pool.getAnalyser().produces());
				production.put(analyserName, produces);
			}
		}

		return production;
	}

	/**
	 * return the number of registered analyser
	 *
	 * @return
	 */
	public int getNumberOfRegisteredAnalysers() {
		return this.analyserPools.size();
	}

	/**
	 * return the current number of analyser instance
	 *
	 * @return
	 */
	public int getNumberOfInstantiatedAnalyser() {
		int instantiatedAnalyser = 0;

		for (AnalyserPool analyserPool : analyserPools.values()) {
			instantiatedAnalyser += analyserPool.getSize();
		}

		return instantiatedAnalyser;
	}

	/**
	 * return the current number of analyser instance
	 *
	 * @return
	 */
	public int getNumberOfInstantiatedAnalyser(String analyserName) {
		if (this.analyserPools.containsKey(analyserName)) {
			return this.analyserPools.get(analyserName).getSize();
		}

		return 0;
	}

	/**
	 * Returns a string identifier for the current analyser setup (all integrated analysers)
	 *
	 * @return
	 */
	public String getAnalyserSetup() {
		List<String> analyserNames = new ArrayList<>(this.enabledAnalysers);

		Collections.sort(analyserNames);

		return StringUtils.join(analyserNames, "_");
	}

	public int getMaxInstances(String analyserName) {
		if (this.analyserPools.containsKey(analyserName)) {
			return this.analyserPools.get(analyserName).getAnalyser().maxInstances();
		}

		return 0;
	}
}
