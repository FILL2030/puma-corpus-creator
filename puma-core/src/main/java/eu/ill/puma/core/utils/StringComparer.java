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
package eu.ill.puma.core.utils;

public class StringComparer {
	private static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";

	public static boolean nullOrEmpty(String value) {
		return (value == null || value.isEmpty());
	}

	public static boolean nullOrShorterThan(String value, int length) {
		return (value == null || value.length() < length);
	}

	public static boolean isEmail(String value) {
		return (value == null || value.isEmpty()) || value.matches(EMAIL_PATTERN);
	}

	public static boolean startsOrEndsWith(String value, String part) {
		return (value.startsWith(part) || value.endsWith(part));
	}

	public static boolean areEqual(String value1, String value2) {
		if (nullOrEmpty(value1)) {
			return false;
		}

		if (nullOrEmpty(value2)) {
			return false;
		}

		return value1.equals(value2);
	}

	public static boolean areEqualOrNull(String value1, String value2) {
		if (value1 == null && value2 == null) {
			return true;
		}

		if (value1 != null && value2 != null) {
			return value1.equals(value2);
		}

		return false;
	}

	public static boolean areAllNullOrEmpty(String... values) {
		for (String value : values) {
			if (!nullOrEmpty(value)) {
				return false;
			}
		}

		return true;
	}

	public static boolean areAnyNullOrEmpty(String... values) {
		for (String value : values) {
			if (nullOrEmpty(value)) {
				return true;
			}
		}

		return false;
	}

	public static boolean areNotNullAndNotEmptyAndFirstLetterEqual(String... values) {
		// Verify any are not null or empty
		for (String value : values) {
			if (nullOrEmpty(value)) {
				return false;
			}
		}

		char firstChar = values[0].charAt(0);
		boolean isFirst = true;
		for (String value : values) {
			if (isFirst) {
				isFirst = false;

			} else {
				if (value.charAt(0) != firstChar) {
					return false;
				}
			}
		}

		return true;
	}
}
