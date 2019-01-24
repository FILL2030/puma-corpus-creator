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

import java.util.HashMap;
import java.util.Map;

public class ThrottleStore {

	private Map<String, Throttle> throttles = new HashMap<>();

	private static ThrottleStore instance = null;
	private static Object mutex= new Object();

	public static ThrottleStore getInstance() {
		if (instance == null) {
			synchronized (mutex){
				if (instance == null) {
					instance = new ThrottleStore();
				}
			}
		}

		return instance;
	}

	public synchronized Throttle get(String key) {
		Throttle throttle = this.throttles.get(key);

		if (throttle == null) {
			Throttle newThrottle = new Throttle(key);
			this.throttles.put(key, newThrottle);

			throttle = newThrottle;
		}

		return throttle;
	}

}
