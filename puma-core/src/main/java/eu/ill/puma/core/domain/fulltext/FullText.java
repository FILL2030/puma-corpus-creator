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
package eu.ill.puma.core.domain.fulltext;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by letreguilly on 03/08/17.
 */
public class FullText {


	private String title;
	private String abstractText;

	private List<Section> sections = new ArrayList();

	private List<Figure> figures = new ArrayList();
	private List<String> references = new ArrayList();

	public FullText() {
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAbstract() {
		return abstractText;
	}

	public void setAbstract(String abstractText) {
		this.abstractText = abstractText;
	}

	public List<Section> getSections() {
		return sections;
	}

	public void setSections(List<Section> sections) {
		this.sections = sections;
	}

	public void addSection(Section section) {
		this.sections.add(section);
	}

	public List<Figure> getFigures() {
		return figures;
	}

	public void setFigures(List<Figure> figures) {
		this.figures = figures;
	}

	public void addFigure(Figure figure) {
		this.figures.add(figure);
	}

	public Figure getFigureWithId(String id) {
		for (Figure figure : this.figures) {
			if (figure.getId().equals(id)) {
				return figure;
			}
		}
		return null;
	}

	public void addReference(String reference) {
		this.references.add(reference);
	}

	public List<String> getReferences() {
		return this.references;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		FullText fullText = (FullText) o;

		return new EqualsBuilder()
				.append(sections, fullText.sections)
				.append(figures, fullText.figures)
				.append(references, fullText.references)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(sections)
				.append(figures)
				.append(references)
				.toHashCode();
	}

}
