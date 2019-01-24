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
package eu.ill.puma.webapi.rest.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RestError {

	private static final Logger log = LoggerFactory.getLogger(RestError.class);

	private String message = null;
	private int statusCode = 0;
	private String statusMessage = null;

	public RestError(Response.Status status, String message) {
		this.statusCode = status.getStatusCode();
		this.statusMessage = status.getReasonPhrase();
		this.message = message;
	}

	public Integer getStatusCode() {
		return this.statusCode;
	}

	public String getStatusMessage() {
		return this.statusMessage;
	}

	public String getMessage() {
		return this.message;
	}


	public static Response buildResponse(Response.Status status, String message) {
		return Response.status(status).entity(new RestError(status, message)).type(MediaType.APPLICATION_JSON).build();
	}

}
