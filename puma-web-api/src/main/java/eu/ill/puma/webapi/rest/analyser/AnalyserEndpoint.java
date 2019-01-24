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
package eu.ill.puma.webapi.rest.analyser;

import eu.ill.puma.analysis.manager.AnalyserManager;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.service.analysis.DocumentVersionAnalysisStateService;
import eu.ill.puma.persistence.service.document.DocumentVersionService;
import eu.ill.puma.webapi.rest.error.RestError;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Component
@Path("/analysers")
@Api(description = "the analyser API")
public class AnalyserEndpoint {

	private static int PAGE_SIZE = 100;
	private static final Logger log = LoggerFactory.getLogger(AnalyserEndpoint.class);

	@Autowired
	private DocumentVersionService documentVersionService;

	@Autowired
	private DocumentVersionAnalysisStateService analysisStateService;

	@Autowired
	private AnalyserManager analyserManager;


	@GET
	@Produces({"application/json"})
	public Response getPending() {
		log.info("Getting list of next " + PAGE_SIZE + " documents requiring analysis");

		String analysisSetup = this.analyserManager.getAnalyserSetup();

		long numberOfDocumentsRequiringAnalysis = this.analysisStateService.getNumberOfDocumentsRequiringAnalysis(analysisSetup);
		List<DocumentVersion> documentVersions = this.analysisStateService.getNextDocumentVersionsRequiringAnalysis(analysisSetup, PAGE_SIZE, false);

		RestAnalyserResponse response = new RestAnalyserResponse(numberOfDocumentsRequiringAnalysis);
		for (DocumentVersion documentVersion : documentVersions) {
			response.addDocument(new RestAnalysisDocument(documentVersion));
		}

		return Response.ok(response).build();
	}
	@GET
	@Path("/{number}")
	@Produces({"application/json"})
	public Response getPendingWithSize(@PathParam("number") int size) {
		log.info("Getting list of next " + size + " documents requiring analysis");

		String analysisSetup = this.analyserManager.getAnalyserSetup();

		long numberOfDocumentsRequiringAnalysis = this.analysisStateService.getNumberOfDocumentsRequiringAnalysis(analysisSetup);
		List<DocumentVersion> documentVersions = this.analysisStateService.getNextDocumentVersionsRequiringAnalysis(analysisSetup, size, false);

		RestAnalyserResponse response = new RestAnalyserResponse(numberOfDocumentsRequiringAnalysis);
		for (DocumentVersion documentVersion : documentVersions) {
			response.addDocument(new RestAnalysisDocument(documentVersion));
		}

		return Response.ok(response).build();
	}

	@GET
	@Path("/active")
	@Produces({"application/json"})
	public Response getActive() {
		log.info("Getting active analysis");

		List<DocumentVersion> documentVersions = this.analyserManager.getActiveAnalysis();

		RestAnalyserResponse response = new RestAnalyserResponse(documentVersions.size());
		for (DocumentVersion documentVersion : documentVersions) {
			response.addDocument(new RestAnalysisDocument(documentVersion));
		}

		return Response.ok(response).build();
	}

	@GET
	@Path("/history/{number}")
	@Produces({"application/json"})
	public Response getHistory(@PathParam("number") int number) {
		log.info("Getting analysis history with " + number + " entries");

		List<DocumentVersion> documentVersions = this.analysisStateService.getAnalysisHistory(number, false);

		RestAnalyserResponse response = new RestAnalyserResponse(documentVersions.size());
		for (DocumentVersion documentVersion : documentVersions) {
			response.addDocument(new RestAnalysisDocument(documentVersion));
		}

		return Response.ok(response).build();
	}

	@POST
	@Consumes({"application/json"})
	@Produces({"application/json"})
	public Response analyse(RestAnalysisCommand command) {
		Integer maxNumberToResolve = command.getMaxNumber();
		if (maxNumberToResolve == null) {
			log.info("Executing analysis command for all documents");

		} else {
			log.info("Executing analysis command for " + maxNumberToResolve + " documents");
		}

		this.analyserManager.activatePendingAnalysisAsync(maxNumberToResolve);

		return Response.ok().build();
	}

	@POST
	@Path("/{id}")
	@Consumes({"application/json"})
	@Produces({"application/json"})
	public Response analyse(@PathParam("id") Long documentVersionId) {
		DocumentVersion documentVersion = this.documentVersionService.getById(documentVersionId);
		if (documentVersion != null) {
			log.info("Analysing document with id " + documentVersionId);
			this.analyserManager.performAnalysisAsync(documentVersionId);

			return Response.ok().build();

		} else {
			return RestError.buildResponse(Response.Status.NOT_FOUND, "The document with Id " + documentVersionId + " does not exist");
		}
	}

	@POST
	@Path("/ids")
	@Consumes({"application/json"})
	@Produces({"application/json"})
	public Response analyse(List<Long> documentVersionIds) {
		log.info("Analysing " + documentVersionIds.size() + " documents");

		this.analyserManager.performAnalysisListAsync(documentVersionIds);

		return Response.ok().build();
	}

	@DELETE
	@Produces({"application/json"})
	public Response cancelAllAnalysis() {
		log.info("Cancelling all analysis");
		this.analyserManager.cancelAllAnalysisAsync();

		return Response.ok().build();
	}

	@DELETE
	@Path("/{id}")
	@Produces({"application/json"})
	public Response cancelAnalysis(@PathParam("id") Long documentVersionId) {
		DocumentVersion documentVersion = this.documentVersionService.getById(documentVersionId);
		if (documentVersion != null) {
			log.info("Cancelling analysis of document with id " + documentVersionId);
			this.analyserManager.cancelAnalysisAsync(documentVersion);

			return Response.ok().build();

		} else {
			return RestError.buildResponse(Response.Status.NOT_FOUND, "The document with Id " + documentVersionId + " does not exist");
		}
	}

}
