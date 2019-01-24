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

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.ill.puma.persistence.domain.document.enumeration.DocumentType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "document")
public class Document {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Column(name = "document_type", nullable = false, length = 1000)
	@Enumerated(EnumType.STRING)
	private DocumentType documentType;

	@JsonIgnore
	@OneToMany(mappedBy = "document", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	private List<DocumentVersion> documentVersions = new ArrayList();

	@JsonIgnore
	@OneToMany(mappedBy = "citedDocument", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	private List<Reference> references = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DocumentType getDocumentType() {
		return documentType;
	}

	public void setDocumentType(DocumentType documentType) {
		this.documentType = documentType;
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

	public List<Reference> getReferences() {
		return references;
	}

	public Optional<Reference> getReferenceById(Long id){
		return this.references.stream().filter(
				reference -> reference.getId().equals(id))
				.findFirst();
	}

	public void addReference(Reference reference) {
		this.references.add(reference);
	}
}
