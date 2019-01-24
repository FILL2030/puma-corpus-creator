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
package eu.ill.puma.core.utils.throttle;

import eu.ill.puma.core.utils.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ThrottleHostsParser {

	private static final Logger log = LoggerFactory.getLogger(ThrottleHostsParser.class);

	public Map<String, Integer> read(String fileName) {
		Map<String, Integer> throttleHosts = new HashMap<>();

		try {
			throttleHosts = ResourceLoader.readType(fileName, HashMap.class);

		} catch (Exception e) {
			log.error("Could not read throttle file " + fileName);
		}


		return throttleHosts;
	}
}
