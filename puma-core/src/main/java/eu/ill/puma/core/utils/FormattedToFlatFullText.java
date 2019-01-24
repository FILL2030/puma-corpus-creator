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

import eu.ill.puma.core.domain.fulltext.Figure;
import eu.ill.puma.core.domain.fulltext.FullText;
import eu.ill.puma.core.domain.fulltext.Section;

public class FormattedToFlatFullText {

	public static String unformat(FullText fullText) {
		StringBuilder stringBuilder = new StringBuilder();

		if (fullText.getTitle() != null) {
			stringBuilder.append(fullText.getTitle()).append("\n");
		}

		if (fullText.getAbstract() != null) {
			stringBuilder.append(fullText.getAbstract()).append("\n");
		}

		for (Section section : fullText.getSections()) {
			stringBuilder.append(section.getTitle()).append("\n");
			stringBuilder.append(section.getText()).append("\n");
		}

		for (Figure figure : fullText.getFigures()) {
			stringBuilder.append(figure.getTitle()).append("\n");
			stringBuilder.append(figure.getDescription()).append("\n");
		}

		for (String reference : fullText.getReferences()) {
			stringBuilder.append(reference).append("\n");
		}

		return stringBuilder.toString();
	}
}
