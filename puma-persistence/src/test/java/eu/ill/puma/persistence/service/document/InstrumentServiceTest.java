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

import eu.ill.puma.persistence.PumaTest;
import eu.ill.puma.persistence.domain.document.Instrument;
import eu.ill.puma.persistence.domain.document.Laboratory;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class InstrumentServiceTest extends PumaTest {

	private static final Logger log = LoggerFactory.getLogger(InstrumentServiceTest.class);

	@Autowired
	private InstrumentService instrumentService;

	@Autowired
	private LaboratoryService laboratoryService;

	@Test
	public void contextLoads() throws Exception {
		Assert.assertNotNull(this.instrumentService);
		Assert.assertNotNull(this.laboratoryService);
	}


	@Test
	public void testLaboratoryInstrument() {
		Laboratory laboratory = new Laboratory();
		laboratory.setName("Test labo");
		laboratory.setAddress("1 Big Street");

		Instrument instrument = new Instrument();
		instrument.setName("Test Instr");

		// Only set labo in instr (do not need to set instr in labo)
		instrument.setLaboratory(laboratory);

		instrumentService.save(instrument);

		// Test getting person back from instrument db
		Instrument instrument2 = instrumentService.getByIdCompleted(instrument.getId());

		Assert.assertNotNull(instrument2.getLaboratory());

		Laboratory laboratory2 = laboratoryService.getByIdCompleted(laboratory.getId());
		Assert.assertEquals(1,laboratory2.getInstruments().size());
	}

}
