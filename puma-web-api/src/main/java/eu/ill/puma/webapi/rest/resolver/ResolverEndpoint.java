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
package eu.ill.puma.webapi.rest.resolver;

import eu.ill.puma.importermanager.resolver.PumaFileUrlResolver;
import eu.ill.puma.importermanager.resolver.domain.ResolverResponseDownloadData;
import eu.ill.puma.importermanager.resolver.domain.ResolverResponseUrl;
import eu.ill.puma.persistence.domain.document.ResolverInfo;
import eu.ill.puma.persistence.service.document.PumaFileService;
import eu.ill.puma.persistence.service.document.ResolverInfoService;
import eu.ill.puma.webapi.rest.error.RestError;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Component
@Path("/resolvers")
@Api(description = "the importer API")
public class ResolverEndpoint {

	private static final Logger log = LoggerFactory.getLogger(ResolverEndpoint.class);

	@Autowired
	private PumaFileUrlResolver pumaFileUrlResolver;

	@Autowired
	private ResolverInfoService resolverInfoService;

	@GET
	@Produces({"application/json"})
	@ApiOperation(value = "", notes = "Returns all resolver infos that are either active or pending", response = RestResolverInfoResponse.class)
	@ApiResponse(code = 200, message = "Details of all resovler info", response = RestResolverInfoResponse.class)
	public Response getRemaining() {
		log.info("Getting list of all resolvers");
		List<ResolverInfo> resolverInfos = this.pumaFileUrlResolver.getRemainingResolvers();

		RestResolverInfoResponse response = new RestResolverInfoResponse();
		for (ResolverInfo resolverInfo : resolverInfos) {
			response.addResolverInfo(new RestResolverInfo(resolverInfo));
		}

		return Response.ok(response).build();
	}

	@GET
	@Path("/active")
	@Produces({"application/json"})
	public Response getActive() {
		log.info("Getting list of active resolvers");
		List<ResolverInfo> resolverInfos = this.pumaFileUrlResolver.getActiveResolvers();

		RestResolverInfoResponse response = new RestResolverInfoResponse();
		for (ResolverInfo resolverInfo : resolverInfos) {
			response.addResolverInfo(new RestResolverInfo(resolverInfo));
		}

		return Response.ok(response).build();
	}

	@GET
	@Path("/history/{number}")
	@Produces({"application/json"})
	public Response getHistory(@PathParam("number") Integer number) {
		log.info("Getting history of resolvers, size = " + number);
		List<ResolverInfo> resolverInfos = this.pumaFileUrlResolver.getHistory(number);

		RestResolverInfoResponse response = new RestResolverInfoResponse();
		for (ResolverInfo resolverInfo : resolverInfos) {
			response.addResolverInfo(new RestResolverInfo(resolverInfo));
		}

		return Response.ok(response).build();
	}

	@GET
	@Path("/pending")
	@Produces({"application/json"})
	public Response getPending() {
		log.info("Getting list of pending resolvers");
		List<ResolverInfo> resolverInfos = this.pumaFileUrlResolver.getPendingResolvers();

		RestResolverInfoResponse response = new RestResolverInfoResponse();
		for (ResolverInfo resolverInfo : resolverInfos) {
			response.addResolverInfo(new RestResolverInfo(resolverInfo));
		}

		return Response.ok(response).build();
	}

	@GET
	@Path("/failed")
	@Produces({"application/json"})
	public Response getFailed() {
		log.info("Getting list of pending resolvers");
		List<ResolverInfo> resolverInfos = this.pumaFileUrlResolver.getFailedResolvers();

		RestResolverInfoResponse response = new RestResolverInfoResponse();
		for (ResolverInfo resolverInfo : resolverInfos) {
			response.addResolverInfo(new RestResolverInfo(resolverInfo));
		}

		return Response.ok(response).build();
	}

	@POST
	@Consumes({"application/json"})
	@Produces({"application/json"})
	@ApiOperation(value = "", notes = "Activates any pending resolvers.", tags = {})
	public Response resolve(RestResolverCommand command) {
		int maxNumberToResolve = command.getMaxNumber();
		log.info("Executing resolve command for " + maxNumberToResolve + " resolvers");

		this.pumaFileUrlResolver.activatePendingResolversAsync(maxNumberToResolve);;

		return Response.ok().build();
	}

	@POST
	@Path("/{id}")
	@Consumes({"application/json"})
	@Produces({"application/json"})
	public Response resolve(@PathParam("id") Long resolverId) {
		ResolverInfo resolverInfo = resolverInfoService.getById(resolverId);
		if (resolverInfo != null) {
			log.info("Resolving for  " + resolverInfo);

			this.pumaFileUrlResolver.resolve(resolverInfo);

			return Response.ok().build();
		}

		return RestError.buildResponse(Response.Status.NOT_FOUND, "The resolver with Id " + resolverId + " does not exist");
	}

	@POST
	@Path("/ids")
	@Produces({"application/json"})
	public Response resolve(List<Long> resolverIds) {
		List<ResolverInfo> resolverInfos = new ArrayList<>();
		resolverIds.stream().forEach(resolverId -> {
			ResolverInfo resolverInfo = resolverInfoService.getById(resolverId);
			if (resolverInfo != null) {
				resolverInfos.add(resolverInfo);
			}
		});

		RestResolverInfoResponse response = new RestResolverInfoResponse();
		this.pumaFileUrlResolver.resolve(resolverInfos).stream().forEach(resolverInfo -> {
			response.addResolverInfo(new RestResolverInfo(resolverInfo));
		});

		log.info("Resolving " + response.getResolverInfos().size() + " files");


		return Response.ok(response).build();
	}

	@POST
	@Path("/host/{host}")
	@Consumes({"application/json"})
	@Produces({"application/json"})
	public Response resolveForHost(@PathParam("host") String host) {
		log.info("Resolving for host " + host);

		this.pumaFileUrlResolver.resolveForHostAsync(host);

		return Response.ok().build();
	}

	@GET
	@Path("/{id}")
	@Consumes({"application/json"})
	@Produces({"application/json"})
	public Response get(@PathParam("id") Long resolverId) {
		ResolverInfo resolverInfo = resolverInfoService.getById(resolverId);
		if (resolverInfo != null) {
			RestResolverInfo restResolverInfo = new RestResolverInfo(resolverInfo);

			return Response.ok(restResolverInfo).build();
		}

		return RestError.buildResponse(Response.Status.NOT_FOUND, "The resolver with Id " + resolverId + " does not exist");
	}

	@POST
	@Path("/{id}/upload")
	@Produces({"application/json"})
	public Response upload(@PathParam("id") Long resolverId, ResolverUploadFile uploadFile) {
		log.info("Upload file for resolve id " + resolverId);
		ResolverInfo resolverInfo = resolverInfoService.getByIdWithDocumentVersion(resolverId);
		if (resolverInfo != null) {
			log.info("Uploading file for  " + resolverInfo);
			try {
				// Convert to Resolver download
				ResolverResponseDownloadData downloadData = new ResolverResponseDownloadData();
				downloadData.setData(uploadFile.getData());
				downloadData.setMd5Checksum(uploadFile.getMd5());
				downloadData.setUrl(new ResolverResponseUrl(uploadFile.getUrl()));
				downloadData.setMimeType(uploadFile.getMimeType());

				if (this.pumaFileUrlResolver.setFileForResolverInfo(resolverInfo, downloadData)) {
					ResolverUploadResponse response = new ResolverUploadResponse();
					response.setUploadOk(true);

					return Response.ok(response).build();

				} else {
					log.error("Failed to convert file data for resoler " + resolverId);
					return RestError.buildResponse(Response.Status.INTERNAL_SERVER_ERROR, "Failed to convert file data for resolver " + resolverId);
				}

			} catch (PumaFileService.PumaFilePersistenceException e) {
				log.error("Failed to save file : " + e.getMessage());
				return RestError.buildResponse(Response.Status.INTERNAL_SERVER_ERROR, "Failed to save file for resolver " + resolverId);
			}
		}

		return RestError.buildResponse(Response.Status.NOT_FOUND, "The resolver with Id " + resolverId + " does not exist");
	}

	@GET
	@Path("/{id}/next")
	@Consumes({"application/json"})
	@Produces({"application/json"})
	public Response getNextAfterId(@PathParam("id") Long resolverId) {
		log.info("Getting next resolver after " + resolverId);
		ResolverInfo resolverInfo = this.resolverInfoService.getNextRequiringResolveAfter(resolverId);
		if (resolverInfo != null) {
			RestResolverInfo restResolverInfo = new RestResolverInfo(resolverInfo);

			return Response.ok(restResolverInfo).build();
		}

		log.info("No more resolvers");
		return Response.ok().build();
	}

	@GET
	@Path("/{id}/next/host/{host}")
	@Consumes({"application/json"})
	@Produces({"application/json"})
	public Response getNextAfterIdForHost(@PathParam("id") Long resolverId, @PathParam("host") String host) {
		log.info("Getting next resolver after " + resolverId + " for host " + host);
		ResolverInfo resolverInfo = this.resolverInfoService.getNextRequiringResolveAfterForHost(resolverId, host);
		if (resolverInfo != null) {
			RestResolverInfo restResolverInfo = new RestResolverInfo(resolverInfo);

			return Response.ok(restResolverInfo).build();
		}

		log.info("No more resolvers");
		return Response.ok().build();
	}

	@GET
	@Path("/next")
	@Produces({"application/json"})
	public Response getNext() {
		log.info("Getting next resolver");
		ResolverInfo resolverInfo = this.resolverInfoService.getNextRequiringResolve();
		if (resolverInfo != null) {
			RestResolverInfo restResolverInfo = new RestResolverInfo(resolverInfo);

			return Response.ok(restResolverInfo).build();
		}

		log.info("No more resolvers");
		return Response.ok().build();
	}

	@GET
	@Path("/next/host/{host}")
	@Produces({"application/json"})
	public Response getNextForHost(@PathParam("host") String host) {
		log.info("Getting next resolver for host " + host);
		ResolverInfo resolverInfo = this.resolverInfoService.getNextRequiringResolveForHost(host);
		if (resolverInfo != null) {
			RestResolverInfo restResolverInfo = new RestResolverInfo(resolverInfo);

			return Response.ok(restResolverInfo).build();
		}

		log.info("No more resolvers");
		return Response.ok().build();
	}
}
