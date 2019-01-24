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
package eu.ill.puma.persistence.domain.document;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "keyword", indexes = {
		@Index(name = "keyword_word_index", columnList = "word")})
public class Keyword {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Column(name = "word", nullable = false, length = 2000)
	private String word;

	@ManyToMany(mappedBy = "keywords")
	private List<DocumentVersion> documentVersions = new ArrayList<>();

	@Column(name = "obsolete", columnDefinition = "boolean default false")
	private Boolean obsolete = false;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word == null ? null : word.toLowerCase();
	}

	public List<DocumentVersion> getDocumentVersions() {
		return documentVersions;
	}

	public void addDocumentVersion(DocumentVersion documentVersion) {
		this.documentVersions.add(documentVersion);
	}

	public void removeAllDocumentVersions() {
		this.documentVersions.clear();
	}

	public Boolean getObsolete() {
		return obsolete;
	}

	public void setObsolete(Boolean obsolete) {
		this.obsolete = obsolete;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Keyword keyword = (Keyword) o;

		return word != null ? word.equals(keyword.word) : keyword.word == null;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + (word != null ? word.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("id", id)
			.append("word", word)
			.toString();
	}
}
