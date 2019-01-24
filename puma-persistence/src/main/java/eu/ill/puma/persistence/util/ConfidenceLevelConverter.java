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
package eu.ill.puma.persistence.util;

import eu.ill.puma.persistence.domain.analysis.ConfidenceLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class ConfidenceLevelConverter implements AttributeConverter<ConfidenceLevel, Integer> {

	private static final Logger log = LoggerFactory.getLogger(ConfidenceLevelConverter.class);


	@Override
	public Integer convertToDatabaseColumn(ConfidenceLevel confidenceLevel) {
		return confidenceLevel.getValue();
	}

	@Override
	public ConfidenceLevel convertToEntityAttribute(Integer dbData) {
		return ConfidenceLevel.getForValue(dbData);
	}

}
