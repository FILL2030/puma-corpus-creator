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
package eu.ill.puma.webapi;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import eu.ill.puma.webapi.exceptions.PumaGenericExceptionMapper;
import eu.ill.puma.webapi.filters.PumaCORSFilter;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.ApplicationPath;

@Configuration
@ApplicationPath("/api/v1")
public class PumaJerseyConfig extends ResourceConfig {

	public PumaJerseyConfig() {
		// specify packages with API controllers
		packages("eu.ill.puma.webapi");
		configureFilters();
		configureExceptions();
		configureSwagger();
	}

	private void configureFilters() {
		register(PumaCORSFilter.class);
	}

	private void configureExceptions() {
		register(JacksonJaxbJsonProvider.class);
		register(PumaGenericExceptionMapper.class);
	}

	private void configureSwagger() {
		// Swagger settings
		register(ApiListingResource.class);
		register(SwaggerSerializers.class);

		BeanConfig beanConfig = new BeanConfig();
		beanConfig.setVersion("1.0");
		beanConfig.setSchemes(new String[]{"http"});
		beanConfig.setBasePath("/api/v1");
		beanConfig.setDescription("The Puma Corpus Creator API");
		beanConfig.setResourcePackage("eu.ill.puma");
		beanConfig.setPrettyPrint(true);
		beanConfig.setScan(true);
	}
}