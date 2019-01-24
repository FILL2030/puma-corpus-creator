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

import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.util.ConfidenceLevelConverter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "document_version_entity_origin", indexes = {
		@Index(name = "document_version_entity_origin_index", columnList = "document_version_id,entity_id,entity_type,entity_origin,action"),
		@Index(name = "document_version_entity_origin_entity_type_index", columnList = "entity_type"),
		@Index(name = "document_version_entity_origin_entity_id_index", columnList = "entity_id"),
		@Index(name = "document_version_entity_origin_entity_origin_index", columnList = "entity_origin"),
		@Index(name = "document_version_entity_origin_action_index", columnList = "action"),
		@Index(name = "document_version_entity_confidence_index", columnList = "confidence"),
		@Index(name = "document_version_entity_origin_document_version_id_index", columnList = "document_version_id")
})
public class DocumentVersionEntityOrigin {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "DOCUMENT_VERSION_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "fk_entity_origin_document_version_id"))
	private DocumentVersion documentVersion;

	@Column(name = "entity_id", nullable = true)
	private Long entityId;

	@Column(name = "entity_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private EntityType entityType;

	@Column(name = "entity_origin", nullable = false)
	private String entityOrigin;

	@Column(name = "action", nullable = false)
	@Enumerated(EnumType.STRING)
	private OriginAction action;

	@Column(name = "action_date", nullable = false)
	private Date actionDate;

	@Column(name = "confidence", nullable = false)
	@Convert(converter = ConfidenceLevelConverter.class)
	private ConfidenceLevel confidenceLevel = ConfidenceLevel.TODO;

	public DocumentVersionEntityOrigin() {
	}

	public DocumentVersionEntityOrigin(DocumentVersion documentVersion, Long entityId, EntityType entityType, String origin, ConfidenceLevel confidenceLevel, OriginAction action) {
		this.documentVersion = documentVersion;
		this.entityId = entityId;
		this.entityType = entityType;
		this.entityOrigin = origin;
		this.confidenceLevel = confidenceLevel;
		this.action = action;
		this.actionDate = new Date();
	}

	public static DocumentVersionEntityOrigin Found(DocumentVersion documentVersion, Long entityId, EntityType entityType, String origin, ConfidenceLevel confidenceLevel) {
		DocumentVersionEntityOrigin entityOrigin = new DocumentVersionEntityOrigin(documentVersion, entityId, entityType, origin, confidenceLevel, OriginAction.FOUND);

		return entityOrigin;
	}

	public static DocumentVersionEntityOrigin Ignored(DocumentVersion documentVersion, Long entityId, EntityType entityType, String origin, ConfidenceLevel confidenceLevel) {
		DocumentVersionEntityOrigin entityOrigin = new DocumentVersionEntityOrigin(documentVersion, entityId, entityType, origin, confidenceLevel, OriginAction.IGNORED);

		return entityOrigin;
	}

	public static DocumentVersionEntityOrigin Deleted(DocumentVersion documentVersion, Long entityId, EntityType entityType, String origin, ConfidenceLevel confidenceLevel) {
		DocumentVersionEntityOrigin entityOrigin = new DocumentVersionEntityOrigin(documentVersion, entityId, entityType, origin, confidenceLevel, OriginAction.DELETED);

		return entityOrigin;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DocumentVersion getDocumentVersion() {
		return documentVersion;
	}

	public void setDocumentVersion(DocumentVersion documentVersion) {
		this.documentVersion = documentVersion;
	}

	public Long getEntityId() {
		return entityId;
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	public EntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(EntityType entityType) {
		this.entityType = entityType;
	}

	public String getEntityOrigin() {
		return entityOrigin;
	}

	public void setEntityOrigin(String entityOrigin) {
		this.entityOrigin = entityOrigin;
	}

	public OriginAction getAction() {
		return action;
	}

	public void setAction(OriginAction action) {
		this.action = action;
	}

	public ConfidenceLevel getConfidenceLevel() {
		return confidenceLevel;
	}

	public void setConfidenceLevel(ConfidenceLevel confidenceLevel) {
		this.confidenceLevel = confidenceLevel;
	}

	public Date getActionDate() {
		return actionDate;
	}

	public void setActionDate(Date actionDate) {
		this.actionDate = actionDate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		DocumentVersionEntityOrigin that = (DocumentVersionEntityOrigin) o;

		return new EqualsBuilder()
				.append(documentVersion, that.documentVersion)
				.append(entityId, that.entityId)
				.append(entityType, that.entityType)
				.append(entityOrigin, that.entityOrigin)
				.append(action, that.action)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(documentVersion)
				.append(entityId)
				.append(entityType)
				.append(entityOrigin)
				.append(action)
				.toHashCode();
	}

	@Override
	public String toString() {
		return "DocumentVersionEntityOrigin{" +
				"id=" + id +
				", documentVersionID=" + documentVersion.getId() +
				", entityId=" + entityId +
				", entityType=" + entityType +
				", entityOrigin='" + entityOrigin + '\'' +
				", action=" + action +
				", actionDate=" + actionDate +
				", confidenceLevel=" + confidenceLevel +
				'}';
	}
}
