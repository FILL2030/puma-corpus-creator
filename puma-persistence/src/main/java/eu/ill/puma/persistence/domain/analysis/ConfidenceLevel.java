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
package eu.ill.puma.persistence.domain.analysis;

import java.util.HashMap;
import java.util.Map;

public enum ConfidenceLevel {
	TODO(0),
	FOUND(10),
	CONFIDENT(20),
	SURE(30);

	private static final Map<Integer, ConfidenceLevel> typesByValue = new HashMap<>();

	static {
		for (ConfidenceLevel type : ConfidenceLevel.values()) {
			typesByValue.put(type.getValue(), type);
		}
	}

	private final int value;

	ConfidenceLevel(final int newValue) {
		value = newValue;
	}

	public int getValue() { return value; }

	public static ConfidenceLevel getForValue(int value) {
		return typesByValue.get(value);
	}
}
