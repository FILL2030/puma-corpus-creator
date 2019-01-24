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
package eu.ill.puma.webapi.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;

public class PumaErrorResponse {

	private Date timestamp = new Date();
	private String path = null;
	private String statusMessage = null;
	private int statusCode;

	public PumaErrorResponse(Response.Status status, String path) {
		this.statusCode = status.getStatusCode();
		this.statusMessage = status.getReasonPhrase();
		this.path = path;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getPath() {
		return path;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getStatusMessage() {
		return this.statusMessage;
	}

	public static Response build(String uri) {
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new PumaErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, uri)).type(MediaType.APPLICATION_JSON).build();

	}

}
