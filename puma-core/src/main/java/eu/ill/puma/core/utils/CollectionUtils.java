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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionUtils {

	public static <T, R> T getMaxAttributeOccurrence(Collection<R> collection, Function<R, T> attributeGetter) {
		Map<T, Long> attributeCounts = collection.stream()
			.map(attributeGetter).filter(Objects::nonNull) // map object type to attribute & filter null attributes out
			.collect(Collectors.groupingBy(Function.identity(), Collectors.counting())); // summarize attribute

		T mostCommon = attributeCounts
			.entrySet().stream().max(Map.Entry.comparingByValue()) // fetch the max entry
			.map(Map.Entry::getKey).orElse(null); // map to attribute

		return mostCommon;
	}

	public static <R> String getMaxAttributeOccurrenceOrLongest(Collection<R> collection, Function<R, String> attributeGetter) {
		Map<String, Long> attributeCounts = collection.stream()
			.map(attributeGetter).filter(Objects::nonNull) // map object type to attribute & filter null attributes out
			.collect(Collectors.groupingBy(Function.identity(), Collectors.counting())); // summarize attribute

		if (attributeCounts.size() == collection.size()) {
			Optional<String> longest = attributeCounts
				.keySet().stream().max(Comparator.comparingInt(String::length));
			return longest.isPresent() ? longest.get() : null;

		} else {
			String mostCommon = attributeCounts
				.entrySet().stream().max(Map.Entry.comparingByValue()) // fetch the max entry
				.map(Map.Entry::getKey).orElse(null); // map to attribute

			return mostCommon;
		}
	}
}
