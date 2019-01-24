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

import eu.ill.puma.core.utils.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;

@Service
public class InstrumentLoaderService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Value("${puma.persistence.files.instrument.inject}")
	private Boolean inject;

	@PostConstruct
	public void loadInitData() throws UnsupportedEncodingException {

		String countQuery = "select count(*) from instrument;";
		String sql = ResourceLoader.readString("instrument-data.sql");

		Integer count = this.jdbcTemplate.queryForObject(countQuery, Integer.class);

		if(count == 0 && this.inject){
			this.jdbcTemplate.execute(sql);
		}
	}
}
