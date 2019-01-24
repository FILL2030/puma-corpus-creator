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
package eu.ill.puma.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@ComponentScan(basePackages = {"eu.ill.puma"})
@EntityScan(basePackages = {"eu.ill.puma"})
@PropertySource(value = {
		"classpath:application.properties",
		"classpath:puma-persistence-${spring.profiles.active}.properties",
		"classpath:puma-task-manager-${spring.profiles.active}.properties",
		"classpath:puma-importer-manager-${spring.profiles.active}.properties",
		"classpath:puma-analysis-${spring.profiles.active}.properties"})
public class PumaApplication {

	private static final Logger log = LoggerFactory.getLogger(PumaApplication.class);

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext ctx = SpringApplication.run(PumaApplication.class, args);
	}

}
