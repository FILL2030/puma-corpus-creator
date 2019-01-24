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

import eu.ill.puma.persistence.domain.document.Instrument;
import eu.ill.puma.persistence.domain.document.Laboratory;
import eu.ill.puma.persistence.repository.PumaDocumentEntityRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;

@Repository
public class InstrumentRepository extends PumaDocumentEntityRepository<Instrument> {

	public Instrument getByInstrumentDetails(String code, Laboratory laboratory) {
		Instrument instrument = this.getFirstEntity(Arrays.asList("code", "laboratory"), code, laboratory);

		if (instrument == null) {
			String queryString = "select i from Instrument i " +
				" join i.aliases a " +
				" where a = :code" +
				" and i.laboratory = :laboratory";

			TypedQuery<Instrument> query = entityManager.createQuery(queryString, Instrument.class);

			query.setParameter("laboratory", laboratory);
			query.setParameter("code", code);
			query.setMaxResults(1);

			try {
				instrument = query.getSingleResult();

			} catch (NoResultException e) {
			}
		}

		return instrument;
	}

	public List<Instrument> getAllForLaboratory(Laboratory laboratory) {
		return this.getEntities("laboratory", laboratory);
	}

	public Long getCountForLaboratory(Laboratory laboratory){
		String queryString = "select count(i) from  Instrument i where i.laboratory = :laboratory";

		TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);

		query.setParameter("laboratory", laboratory);
		query.setMaxResults(1);

		return query.getSingleResult();
	}

}