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
import eu.ill.puma.core.domain.document.entities.BaseStringEntity;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.Keyword;
import eu.ill.puma.persistence.service.converterV2.EntityOriginStore;
import eu.ill.puma.persistence.service.converterV2.entitityconverter.KeywordConverter;
import eu.ill.puma.persistence.service.document.KeywordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class KeywordIntegrator {

	@Autowired
	private KeywordService keywordService;

	public void convert(DocumentVersion documentVersion, BaseDocument baseDocument, EntityOriginStore entityOriginStore) {

		for (BaseStringEntity baseKeyword : baseDocument.getKeywords()) {

			//delete keyword (update & delete operation)
			if (baseKeyword.getPumaId() != null) {
				Long keywordId = Math.abs(baseKeyword.getPumaId());

				Optional<Keyword> optionalKeywordToDelete = documentVersion.getKeywordById(keywordId);

				if (optionalKeywordToDelete.isPresent()) {
					documentVersion.getKeywords().remove(optionalKeywordToDelete.get());

					//create entity origin
					entityOriginStore.deleteEntity(optionalKeywordToDelete.get().getId(), EntityType.KEYWORD, baseKeyword.getConfidence());
				}
			}

			//create keyword (add & update operation)
			if (baseKeyword.getPumaId() == null || baseKeyword.getPumaId() >= 0) {
				//new keyword
				Keyword keywordToAdd = KeywordConverter.convert(baseKeyword);
				keywordToAdd = keywordService.save(keywordToAdd);

				if (!documentVersion.getKeywords().contains(keywordToAdd)) {
					documentVersion.addKeyword(keywordToAdd);

					//create entity origin
					entityOriginStore.foundEntity(keywordToAdd.getId(), EntityType.KEYWORD, baseKeyword.getConfidence());
				}
			}
		}
	}
}
