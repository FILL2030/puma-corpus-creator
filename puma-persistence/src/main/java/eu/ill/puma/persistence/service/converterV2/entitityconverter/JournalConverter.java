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
package eu.ill.puma.persistence.service.converterV2.entitityconverter;

import eu.ill.puma.core.domain.document.entities.BaseJournal;
import eu.ill.puma.persistence.domain.document.Journal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JournalConverter {

	private static Pattern volumePattern = Pattern.compile("([\\s\\w\\W]+), vol[\\s\\w\\W]+");

	public static Journal convert(BaseJournal importerJournal) {
		if (importerJournal != null) {
			Journal journal = new Journal();

			String cleanedName = cleanName(importerJournal.getName().toLowerCase());

			journal.setName(cleanedName);
//			if (importerJournal.getPumaId() != null) {
//				journal.setId(Math.abs(importerJournal.getPumaId()));
//			}
			return journal;

		} else {
			return null;
		}
	}

	private static String cleanName(String name) {
		String cleanedName = name;

		// Remove ', vol....'
		Matcher volumeMatcher = volumePattern.matcher(name);
		if (volumeMatcher.find()) {
			cleanedName = volumeMatcher.group(1);
		}

		return cleanedName;
	}


}
