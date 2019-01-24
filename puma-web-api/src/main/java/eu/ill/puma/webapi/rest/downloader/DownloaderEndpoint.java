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
package eu.ill.puma.webapi.rest.downloader;

import eu.ill.puma.importermanager.downloader.PumaFileDownloader;
import eu.ill.puma.persistence.domain.document.PumaFile;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Component
@Path("/downloads")
@Api(description = "the downloader API")
public class DownloaderEndpoint {

	private static final Logger log = LoggerFactory.getLogger(DownloaderEndpoint.class);

	@Autowired
	private PumaFileDownloader pumaFileDownloader;

	@GET
	@Produces({"application/json"})
	@ApiOperation(value = "", notes = "Returns all downloads that are either active or pending", response = RestDownloaderResponse.class)
	@ApiResponse(code = 200, message = "Details of all downloads", response = RestDownloaderResponse.class)
	public Response getActiveAndPending() {
		log.info("Getting list of all downloads");
		List<PumaFile> pumaFiles = this.pumaFileDownloader.getActiveAndPendingDownloads();

		RestDownloaderResponse response = new RestDownloaderResponse();
		for (PumaFile pumaFile: pumaFiles) {
			response.addPumaFile(new RestPumaFile(pumaFile));
		}

		return Response.ok(response).build();
	}

	@POST
	@Consumes({"application/json"})
	@Produces({"application/json"})
	@ApiOperation(value = "", notes = "Activates any pending downloads.", tags = {})
	public Response resolve(RestDownloaderCommand command) {
		int maxNumberToDownload = command.getMaxNumber();
		log.info("Executing downloads command for " + maxNumberToDownload + " downloads");

		this.pumaFileDownloader.activatePendingDownloadsAsync(maxNumberToDownload);;

		return Response.ok().build();
	}

}
