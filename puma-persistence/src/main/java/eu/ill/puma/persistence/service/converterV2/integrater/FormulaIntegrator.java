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
package eu.ill.puma.persistence.service.converterV2.integrater;

import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.core.domain.document.entities.BaseFormula;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.Formula;
import eu.ill.puma.persistence.service.converterV2.EntityOriginStore;
import eu.ill.puma.persistence.service.converterV2.entitityconverter.FormulaConverter;
import eu.ill.puma.persistence.service.document.FormulaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FormulaIntegrator {

	@Autowired
	private FormulaService formulaService;

	public void convert(DocumentVersion documentVersion, BaseDocument baseDocument, EntityOriginStore entityOriginStore) {

		if (baseDocument.getFormulas() != null) {

			for (BaseFormula baseFormula : baseDocument.getFormulas()) {

				//delete formula (update & delete operation)
				if (baseFormula.getPumaId() != null) {
					Long formulaId = Math.abs(baseFormula.getPumaId());

					Optional<Formula> optionalFormulaToDelete = documentVersion.getFormulaById(formulaId);

					if (optionalFormulaToDelete.isPresent()) {
						documentVersion.getFormulas().remove(optionalFormulaToDelete.get());

						//create entity origin
						entityOriginStore.deleteEntity(optionalFormulaToDelete.get().getId(), EntityType.FORMULA, baseFormula.getConfidence());
					}
				}

				//create formula (add & update operation)
				if (baseFormula.getPumaId() == null || baseFormula.getPumaId() >= 0) {
					//new formula
					Formula formulaToAdd = FormulaConverter.convert(baseFormula);
					formulaToAdd = formulaService.save(formulaToAdd);

					if (!documentVersion.getFormulas().contains(formulaToAdd)) {
						documentVersion.addFormula(formulaToAdd);

						//create entity origin
						entityOriginStore.foundEntity(formulaToAdd.getId(), EntityType.FORMULA, baseFormula.getConfidence());
					}
				}
			}
		}
	}
}
