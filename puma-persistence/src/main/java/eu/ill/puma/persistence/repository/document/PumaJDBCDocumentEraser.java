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
package eu.ill.puma.persistence.repository.document;

import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.repository.PumaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PumaJDBCDocumentEraser extends PumaRepository<DocumentVersion> {

	private static final Logger log = LoggerFactory.getLogger(PumaJDBCDocumentEraser.class);

	@Autowired
	JdbcTemplate jdbcTemplate;

	public void eraseDocumentEntities(DocumentVersion documentVersion) {
		log.debug("Erasing document entities for document version " + documentVersion.getId());
		Long documentVersionId = documentVersion.getId();

		try {

			this.deleteAdditionalTextData(documentVersionId);
			this.deleteAnalysisHistoryData(documentVersionId);
			this.deleteAnalysisStateData(documentVersionId);
			this.deleteEntityOriginData(documentVersionId);
			this.deleteInstrumentScientificTechniqueAffiliationData(documentVersionId);
			this.deletePersonLaboratoryAffiliationData(documentVersionId);
			this.deleteJournalPublisherAffiliationData(documentVersionId);
			this.deleteReferencesData(documentVersionId);
			this.deleteFormulasData(documentVersionId);
			this.deleteKeywordsData(documentVersionId);
			this.deleteResearchDomainsData(documentVersionId);

		} catch (Exception e) {
			log.error("Error erasing elements for document version " + documentVersionId + ": " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void deleteResearchDomainsData(Long documentVersionId) {
		long researchDomainsCount = this.countResearchDomains(documentVersionId);
		log.debug("Deleting " + researchDomainsCount + " research domains");
		this.deleteResearchDomains(documentVersionId);

		long orphanedResearchDomainCount = this.countOrphanedResearchDomains();
		log.debug("Deleting " + orphanedResearchDomainCount + " orphaned research domains");
		this.deleteOrphanedResearchDomains();
	}

	private long countResearchDomains(Long documentVersionId) {
		String queryString =  "" +
			"select count(*) from version_research_domain" +
			"  where document_version_id = ?";

		Long count = jdbcTemplate.queryForObject(queryString, new Object[]{documentVersionId}, Long.class);

		return count;
	}

	private void deleteResearchDomains(Long documentVersionId) {
		String queryString =  "" +
			"delete from version_research_domain" +
			"  where document_version_id = ?";

		jdbcTemplate.update(queryString, documentVersionId);
	}

	private long countOrphanedResearchDomains() {
		String queryString =  "" +
			"select count(t.id) from research_domain t" +
			"  left outer join version_research_domain a on a.research_domain_id = t.id" +
			"  where a.document_version_id is null";

		Long count = jdbcTemplate.queryForObject(queryString, Long.class);

		return count;
	}

	private void deleteOrphanedResearchDomains() {
		String queryString =  "" +
			"delete from research_domain t" +
			"  where t.id in (" +
			"    select t.id from research_domain t " +
			"    left outer join version_research_domain a on a.research_domain_id = t.id " +
			"    where a.document_version_id is null)";

		jdbcTemplate.update(queryString);
	}

	private void deleteKeywordsData(Long documentVersionId) {
		long keywordsCount = this.countKeywords(documentVersionId);
		log.debug("Deleting " + keywordsCount + " keywords");
		this.deleteKeywords(documentVersionId);

		long orphanedKeywordCount = this.countOrphanedKeywords();
		log.debug("Deleting " + orphanedKeywordCount + " orphaned keywords");
		this.deleteOrphanedKeywords();
	}

	private long countKeywords(Long documentVersionId) {
		String queryString =  "" +
			"select count(*) from version_keyword" +
			"  where document_version_id = ?";

		Long count = jdbcTemplate.queryForObject(queryString, new Object[]{documentVersionId}, Long.class);

		return count;
	}

	private void deleteKeywords(Long documentVersionId) {
		String queryString =  "" +
			"delete from version_keyword" +
			"  where document_version_id = ?";

		jdbcTemplate.update(queryString, documentVersionId);
	}

	private long countOrphanedKeywords() {
		String queryString =  "" +
			"select count(t.id) from keyword t" +
			"  left outer join version_keyword a on a.keyword_id = t.id" +
			"  where a.document_version_id is null";

		Long count = jdbcTemplate.queryForObject(queryString, Long.class);

		return count;
	}

	private void deleteOrphanedKeywords() {
		String queryString =  "" +
			"delete from keyword t" +
			"  where t.id in (" +
			"    select t.id from keyword t " +
			"    left outer join version_keyword a on a.keyword_id = t.id " +
			"    where a.document_version_id is null)";

		jdbcTemplate.update(queryString);
	}

	private void deleteFormulasData(Long documentVersionId) {
		long formulasCount = this.countFormulas(documentVersionId);
		log.debug("Deleting " + formulasCount + " formulas");
		this.deleteFormulas(documentVersionId);

		long orphanedFormulaCount = this.countOrphanedFormulas();
		log.debug("Deleting " + orphanedFormulaCount + " orphaned formulas");
		this.deleteOrphanedFormulas();
	}

	private long countFormulas(Long documentVersionId) {
		String queryString =  "" +
			"select count(*) from version_formula" +
			"  where document_version_id = ?";

		Long count = jdbcTemplate.queryForObject(queryString, new Object[]{documentVersionId}, Long.class);

		return count;
	}

	private void deleteFormulas(Long documentVersionId) {
		String queryString =  "" +
			"delete from version_formula" +
			"  where document_version_id = ?";

		jdbcTemplate.update(queryString, documentVersionId);
	}

	private long countOrphanedFormulas() {
		String queryString =  "" +
			"select count(t.id) from formula t" +
			"  left outer join version_formula a on a.formula_id = t.id" +
			"  where a.document_version_id is null";

		Long count = jdbcTemplate.queryForObject(queryString, Long.class);

		return count;
	}

	private void deleteOrphanedFormulas() {
		String queryString =  "" +
			"delete from formula t" +
			"  where t.id in (" +
			"    select t.id from formula t " +
			"    left outer join version_formula a on a.formula_id = t.id " +
			"    where a.document_version_id is null)";

		jdbcTemplate.update(queryString);
	}

	private void deleteReferencesData(Long documentVersionId) {
		long referencesCount = this.countReferences(documentVersionId);
		log.debug("Deleting " + referencesCount + " references");
		this.deleteReferences(documentVersionId);
	}

	private long countReferences(Long documentVersionId) {
		String queryString =  "" +
			"select count(id) from reference" +
			"  where citing_document_version_id = ?";

		Long count = jdbcTemplate.queryForObject(queryString, new Object[]{documentVersionId}, Long.class);

		return count;
	}

	private void deleteReferences(Long documentVersionId) {
		String queryString =  "" +
			"delete from reference" +
			"  where citing_document_version_id = ?";

		jdbcTemplate.update(queryString, documentVersionId);
	}

	private void deleteJournalPublisherAffiliationData(Long documentVersionId) {
		long journalPublisherAffiliationCount = this.countJournalPublisherAffiliations(documentVersionId);
		log.debug("Deleting " + journalPublisherAffiliationCount + " journal publisher affiliations");
		this.deleteJournalPublisherAffiliations(documentVersionId);

		long orphanedJournalCount = this.countOrphanedJournals();
		log.debug("Deleting " + orphanedJournalCount + " orphaned journals");
		this.deleteOrphanedJournals();

		long orphanedPublisherCount = this.countOrphanedPublishers();
		log.debug("Deleting " + orphanedPublisherCount + " orphaned publishers");
		this.deleteOrphanedPublishers();

	}

	private long countJournalPublisherAffiliations(Long documentVersionId) {
		String queryString =  "" +
			"select count(id) from journal_publisher_affiliation" +
			"  where document_version_id = ?";

		Long count = jdbcTemplate.queryForObject(queryString, new Object[]{documentVersionId}, Long.class);

		return count;
	}

	private void deleteJournalPublisherAffiliations(Long documentVersionId) {
		String queryString =  "" +
			"delete from journal_publisher_affiliation" +
			"  where document_version_id = ?";

		jdbcTemplate.update(queryString, documentVersionId);
	}

	private long countOrphanedJournals() {
		String queryString =  "" +
			"select count(t.id) from journal t" +
			"  left outer join journal_publisher_affiliation a on a.journal_id = t.id" +
			"  where a.id is null";

		Long count = jdbcTemplate.queryForObject(queryString, Long.class);

		return count;
	}

	private void deleteOrphanedJournals() {
		String queryString =  "" +
			"delete from journal t" +
			"  where t.id in (" +
			"    select t.id from journal t " +
			"    left outer join journal_publisher_affiliation a on a.journal_id = t.id " +
			"    where a.id is null)";

		jdbcTemplate.update(queryString);
	}
	
	private long countOrphanedPublishers() {
		String queryString =  "" +
			"select count(t.id) from publisher t" +
			"  left outer join journal_publisher_affiliation a on a.publisher_id = t.id" +
			"  where a.id is null";

		Long count = jdbcTemplate.queryForObject(queryString, Long.class);

		return count;
	}

	private void deleteOrphanedPublishers() {
		String queryString =  "" +
			"delete from publisher t" +
			"  where t.id in (" +
			"    select t.id from publisher t " +
			"    left outer join journal_publisher_affiliation a on a.publisher_id = t.id " +
			"    where a.id is null)";

		jdbcTemplate.update(queryString);
	}

	private void deletePersonLaboratoryAffiliationData(Long documentVersionId) {
		long personLaboratoryAffiliationCount = this.countPersonLaboratoryAffiliations(documentVersionId);
		log.debug("Deleting " + personLaboratoryAffiliationCount + " person laboratory affiliations");
		this.deletePersonLaboratoryAffiliationRoles(documentVersionId);
		this.deletePersonLaboratoryAffiliations(documentVersionId);

		long orphanedLaboratoryCount = this.countOrphanedLaboratories();
		log.debug("Deleting " + orphanedLaboratoryCount + " orphaned laboratories");
		this.deleteOrphanedLaboratories();

		long orphanedPersonCount = this.countOrphanedPersons();
		log.debug("Deleting " + orphanedPersonCount + " orphaned persons");
		this.deleteOrphanedPersons();
	}

	private long countPersonLaboratoryAffiliations(Long documentVersionId) {
		String queryString =  "" +
			"select count(id) from person_laboratory_affiliation" +
			"  where document_version_id = ?";

		Long count = jdbcTemplate.queryForObject(queryString, new Object[]{documentVersionId}, Long.class);

		return count;
	}

	private void deletePersonLaboratoryAffiliationRoles(Long documentVersionId) {
		String queryString =  "" +
			"delete from person_laboratory_affiliation_role" +
			"  where person_laboratory_affiliation_id in (" +
			"    select id from person_laboratory_affiliation" +
			"    where document_version_id = ?)";

		jdbcTemplate.update(queryString, documentVersionId);
	}

	private void deletePersonLaboratoryAffiliations(Long documentVersionId) {
		String queryString =  "" +
			"delete from person_laboratory_affiliation" +
			"  where document_version_id = ?";

		jdbcTemplate.update(queryString, documentVersionId);
	}

	private long countOrphanedLaboratories() {
		String queryString =  "" +
			"select count(t.id) from laboratory t" +
			"  left outer join person_laboratory_affiliation a on a.laboratory_id = t.id" +
			"  left outer join instrument i on i.laboratory_id = t.id " +
			"  where a.id is null" +
			"  and i.id is null";

		Long count = jdbcTemplate.queryForObject(queryString, Long.class);

		return count;
	}

	private void deleteOrphanedLaboratories() {
		String queryString =  "" +
			"delete from laboratory t" +
			"  where t.id in (" +
			"    select t.id from laboratory t " +
			"    left outer join person_laboratory_affiliation a on a.laboratory_id = t.id " +
			"    left outer join instrument i on i.laboratory_id = t.id " +
			"    where a.id is null " +
			"    and i.id is null)";

		jdbcTemplate.update(queryString);
	}
	
	private long countOrphanedPersons() {
		String queryString =  "" +
			"select count(t.id) from person t" +
			"  left outer join person_laboratory_affiliation a on a.person_id = t.id" +
			"  where a.id is null";

		Long count = jdbcTemplate.queryForObject(queryString, Long.class);

		return count;
	}

	private void deleteOrphanedPersons() {
		String queryString =  "" +
			"delete from person t" +
			"  where t.id in (" +
			"    select t.id from person t " +
			"    left outer join person_laboratory_affiliation a on a.person_id = t.id " +
			"    where a.id is null)";

		jdbcTemplate.update(queryString);
	}

	private void deleteInstrumentScientificTechniqueAffiliationData(Long documentVersionId) {
		long versionInstrumentCount = this.countVersionInstruments(documentVersionId);
		log.debug("Deleting " + versionInstrumentCount + " version instruments");
		this.deleteVersionInstruments(documentVersionId);
		this.deleteOrphanInstrumentScientificTechniques();
		this.deleteOrphanInstrumentAliases();
		this.deleteOrphanInstruments();
		this.deleteOrphanScientificTechniques();
	}

	private long countVersionInstruments(Long documentVersionId) {
		String queryString =  "" +
			"select count(instrument_id) from version_instrument" +
			"  where document_version_id = ?";

		Long count = jdbcTemplate.queryForObject(queryString, new Object[]{documentVersionId}, Long.class);

		return count;
	}

	private void deleteVersionInstruments(Long documentVersionId) {
		String queryString =  "" +
			"delete from version_instrument" +
			"  where document_version_id = ?";

		jdbcTemplate.update(queryString, documentVersionId);
	}

	private void deleteOrphanInstruments() {
		String queryString =  "" +
			"delete from instrument where id in " +
			"  (select i.id from instrument i " +
			"   left outer join version_instrument vi on vi.instrument_id = i.id" +
			"   where vi.instrument_id is null" +
			"   and i.fixed = false)";

		jdbcTemplate.update(queryString);
	}

	private void deleteOrphanInstrumentScientificTechniques() {
		String queryString =  "" +
			"delete from instrument_scientific_technique where instrument_id in " +
			"  (select i.id from instrument i " +
			"   left outer join version_instrument vi on vi.instrument_id = i.id" +
			"   where vi.instrument_id is null" +
			"   and i.fixed = false)";

		jdbcTemplate.update(queryString);
	}

	private void deleteOrphanInstrumentAliases() {
		String queryString =  "" +
			"delete from instrument_alias where instrument_id in " +
			"  (select i.id from instrument i " +
			"   left outer join version_instrument vi on vi.instrument_id = i.id" +
			"   where vi.instrument_id is null" +
			"   and i.fixed = false)";

		jdbcTemplate.update(queryString);
	}

	private void deleteOrphanScientificTechniques() {
		String queryString =  "" +
			"delete from scientific_technique where id in " +
			"  (select st.id from scientific_technique st " +
			"   left outer join instrument_scientific_technique ist on ist.scientific_technique_id = st.id" +
			"   where ist.scientific_technique_id is null)";

		jdbcTemplate.update(queryString);
	}

	private void deleteEntityOriginData(Long documentVersionId) {
		long entityOriginCount = this.countEntityOrigin(documentVersionId);
		log.debug("Deleting " + entityOriginCount + " entity origins");
		this.deleteEntityOrigin(documentVersionId);
	}

	private long countEntityOrigin(Long documentVersionId) {
		String queryString =  "" +
			"select count(id) from document_version_entity_origin" +
			"  where document_version_id = ?" +
			"  and entity_id is not null";

		Long count = jdbcTemplate.queryForObject(queryString, new Object[]{documentVersionId}, Long.class);

		return count;
	}

	private void deleteEntityOrigin(Long documentVersionId) {
		String queryString =  "" +
			"delete from document_version_entity_origin" +
			"  where document_version_id = ?" +
			"  and entity_id is not null";

		jdbcTemplate.update(queryString, documentVersionId);
	}

	private void deleteAnalysisStateData(Long documentVersionId) {
		long analysisStateCount = this.countAnalysisState(documentVersionId);
		log.debug("Deleting " + analysisStateCount + " analysis states");
		this.deleteAnalysisState(documentVersionId);
	}

	private long countAnalysisState(Long documentVersionId) {
		String queryString =  "" +
			"select count(id) from document_version_analysis_state" +
			"  where document_version_id = ?";

		Long count = jdbcTemplate.queryForObject(queryString, new Object[]{documentVersionId}, Long.class);

		return count;
	}

	private void deleteAnalysisState(Long documentVersionId) {
		String queryString =  "" +
			"delete from document_version_analysis_state" +
			"  where document_version_id = ?";

		jdbcTemplate.update(queryString, documentVersionId);
	}

	private void deleteAnalysisHistoryData(Long documentVersionId) {
		long analysisHistoryCount = this.countAnalysisHistory(documentVersionId);
		log.debug("Deleting " + analysisHistoryCount + " analysis histories");
		this.deleteAnalysisHistory(documentVersionId);
	}

	private long countAnalysisHistory(Long documentVersionId) {
		String queryString =  "" +
			"select count(id) from document_version_analysis_history" +
			"  where document_version_id = ?";

		Long count = jdbcTemplate.queryForObject(queryString, new Object[]{documentVersionId}, Long.class);

		return count;
	}

	private void deleteAnalysisHistory(Long documentVersionId) {
		String queryString =  "" +
			"delete from document_version_analysis_history" +
			"  where document_version_id = ?";

		jdbcTemplate.update(queryString, documentVersionId);
	}

	private void deleteAdditionalTextData(Long documentVersionId) {
		long additionalTextsCount = this.countAdditionalTexts(documentVersionId);
		log.debug("Deleting " + additionalTextsCount + " additional texts");
		this.deleteAdditionalTextSearchableDataTypes(documentVersionId);
		this.deleteAdditionalTexts(documentVersionId);
	}

	private long countAdditionalTexts(Long documentVersionId) {
		String queryString =  "" +
			"select count(id) from additional_text" +
			"  where document_version_id = ?";

		Long count = jdbcTemplate.queryForObject(queryString, new Object[]{documentVersionId}, Long.class);

		return count;
	}

	private void deleteAdditionalTextSearchableDataTypes(Long documentVersionId) {
		String queryString =  "" +
			"delete from additional_text_searchable_data_types at" +
			"  using additional_text a" +
			"  where at.additional_text_id = a.id" +
			"  and a.document_version_id = ?";

		jdbcTemplate.update(queryString, documentVersionId);
	}

	private void deleteAdditionalTexts(Long documentVersionId) {
		String queryString =  "" +
			"delete from additional_text" +
			"  where document_version_id = ?";

		jdbcTemplate.update(queryString, documentVersionId);
	}


}
