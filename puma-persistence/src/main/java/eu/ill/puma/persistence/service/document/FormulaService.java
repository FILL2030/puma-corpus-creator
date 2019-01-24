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
package eu.ill.puma.persistence.service.document;

import eu.ill.puma.persistence.domain.document.Formula;
import eu.ill.puma.persistence.repository.document.FormulaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FormulaService {

	private static final Logger log = LoggerFactory.getLogger(FormulaService.class);

	@Autowired
	private FormulaRepository formulaRepository;

	/**
	 * returns the Formula specified by the Id
	 *
	 * @param id the Formula Id
	 * @return The desired Formula
	 */
	public Formula getById(Long id) {
		return this.formulaRepository.getById(id);
	}

	/**
	 * returns all formulas
	 *
	 * @return List of all formulas
	 */
	public List<Formula> getAll() {
		return this.formulaRepository.getAll();
	}

	/**
	 * Persists the given Formula and ensures we do not create duplicates.
	 * - if the id is set we assume we have an already persisted object and the object is updated
	 * - If the Formula data are identical then we assume it is already persisted
	 *
	 * @param formula The formula to be persisted
	 * @return The persisted Formula
	 */
	public synchronized Formula save(Formula formula) {
		Formula integratedFormula = null;

		// Check if it is a new object
		if (formula.getId() == null) {
			// Check if it is already persisted
			integratedFormula = this.formulaRepository.getByMatchingValues(formula.getCode(), formula.getConsistence(), formula.getTemperature(), formula.getPressure(), formula.getMagneticField());

			if (integratedFormula != null) {
				log.debug("formula " + formula.getCode() + " already present in the db under the id " + integratedFormula.getId());

			} else {
				integratedFormula = this.formulaRepository.persist(formula);
			}

		} else {
			// Merge
			integratedFormula = this.formulaRepository.merge(formula);
		}

		return integratedFormula;
	}

}
