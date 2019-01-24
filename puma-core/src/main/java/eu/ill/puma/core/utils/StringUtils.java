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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StringUtils {

	public static String join(Collection collection, String joinValue) {
		StringBuilder outputBuilder = new StringBuilder("");

		int size = collection.size();
		int index = 0;
		for (Object object : collection) {
			outputBuilder.append(object);
			if (index < size - 1) {
				outputBuilder.append(joinValue);
			}
			index++;
		}

		return outputBuilder.toString();
	}

	public static String join(Collection collection) {
		return join(collection, ", ");
	}

	public static String rejoin(String word, String oldJoinValue, String newJoinValue) {
		word = word.replaceAll(oldJoinValue, newJoinValue);
		String[] parts = word.split(newJoinValue);
		List<String> validParts = new ArrayList<>();
		for (String part : parts) {
			if (part.length() > 0) {
				validParts.add(part);
			}
		}

		return join(validParts, newJoinValue);
	}

	public static String limitText(String text, int length) {
		return text == null ? null : text.substring(0, Math.min(length - 1, text.length() - 1)) + (text.length() > length ? "..." : "");
	}
}
