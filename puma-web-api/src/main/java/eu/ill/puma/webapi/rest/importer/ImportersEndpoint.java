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
package eu.ill.puma.webapi.rest.importer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ill.puma.importermanager.ImporterManager;
import eu.ill.puma.persistence.domain.importer.Importer;
import eu.ill.puma.persistence.domain.importer.ImporterOperation;
import eu.ill.puma.persistence.domain.jobscheduler.Job;
import eu.ill.puma.persistence.service.importer.ImporterService;
import eu.ill.puma.persistence.service.jobscheduler.JobService;
import eu.ill.puma.scheduler.Scheduler;
import eu.ill.puma.scheduler.domain.ImporterOperationData;
import eu.ill.puma.webapi.rest.error.RestError;
import eu.ill.puma.webapi.rest.scheduler.RestJob;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Path("/importers")
@Api(description = "the importer API")
public class ImportersEndpoint {

	private static final Logger log = LoggerFactory.getLogger(ImportersEndpoint.class);

	@Autowired
	private ImporterService importerService;

	@Autowired
	private ImporterManager importerManager;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private JobService jobService;

	private ObjectMapper mapper = new ObjectMapper();

	@PostConstruct
	private void init(){
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}

	/**
	 * Returns all importers including their details and status
	 *
	 * @return array of all importers including their details and status
	 */
	@GET
	@Produces({"application/json"})
	@ApiOperation(value = "", notes = "Returns all importers including their details and status", response = RestImporter.class, responseContainer = "List", tags = {})
	@ApiResponse(code = 200, message = "Details of all importers", response = RestImporter.class, responseContainer = "List")
	public Response getImporters() {
		List<Importer> allImporters = importerService.getAll();

		// Convert to rest objects
		List<RestImporter> allRestImporters = new ArrayList<>();
		for (Importer importer : allImporters) {
			allRestImporters.add(new RestImporter(importer));
		}

		return Response.ok(allRestImporters).build();
	}

	/**
	 * Creates a new importer with a specific URL. Duplicate URLs are not allowed.
	 *
	 * @param importer The new importer
	 * @return Details of the created importer
	 */
	@POST
	@Produces({"application/json"})
	@Consumes({"application/json"})
	@ApiOperation(value = "", notes = "Creates a new importer with a specific URL. Duplicate URLs are not allowed.", response = RestImporter.class, tags = {})
	@ApiResponse(code = 201, message = "The created importer details", response = RestImporter.class)
	public Response addImporter(RestImporter importer) {
		log.info("add import : " + importer.getName() + " with url " + importer.getUrl());

		Importer integratedImporter = importerManager.addImporter(importer.getImporter());
		if (integratedImporter != null) {
			return Response.ok(new RestImporter(integratedImporter)).status(201).build();
		}

		// error
		return RestError.buildResponse(Response.Status.INTERNAL_SERVER_ERROR, "Failed to create the importer");
	}


	/**
	 * Returns an importer corresponding to the given Id.
	 *
	 * @param importerId ID of the importer to fetch
	 * @return The specified importer details
	 */
	@GET
	@Path("/{importerId}")
	@Produces({"application/json"})
	@ApiOperation(value = "", notes = "Returns an importer corresponding to the given Id.", response = RestImporter.class, tags = {})
	@ApiResponse(code = 200, message = "The specified importer details", response = RestImporter.class)
	public Response getImporter(@PathParam("importerId") @ApiParam("ID of the importer to fetch") Long importerId) {

		Importer importer = importerService.getById(importerId);
		if (importer != null) {
			return Response.ok(new RestImporter(importer)).build();
		}

		// error
		return RestError.buildResponse(Response.Status.NOT_FOUND, "The importer with Id " + importerId + " does not exist");
	}


	/**
	 * Updates details of an importer corresponding to the given Id.
	 *
	 * @param importerId ID of the importer to update
	 * @return The specified importer details
	 */
	@PUT
	@Path("/{importerId}")
	@Produces({"application/json"})
	@ApiOperation(value = "", notes = "Updates details of an importer corresponding to the given Id.", response = RestImporter.class, tags = {})
	@ApiResponse(code = 200, message = "The specified importer details", response = RestImporter.class)
	public Response updateImporter(@PathParam("importerId") @ApiParam("ID of the importer to update") Long importerId, RestImporter importer) {

		if (importer.getId() != null && importer.getId().equals(importerId)) {
			Importer integratedImporter = importerService.update(importer.getImporter());

			if (integratedImporter != null) {
				return Response.ok(new RestImporter(integratedImporter)).build();

			} else {
				// error
				return RestError.buildResponse(Response.Status.INTERNAL_SERVER_ERROR, "Failed to update the importer");
			}

		} else {
			// error
			return RestError.buildResponse(Response.Status.BAD_REQUEST, "The importer ID does not correspond to the request parameter");
		}
	}


	/**
	 * Deletes an importer corresponding to the given Id.
	 *
	 * @param importerId ID of the importer to delete
	 * @return 200 if importer deleted
	 */
	@DELETE
	@Path("/{importerId}")
	@Produces({"application/json"})
	@ApiOperation(value = "", notes = "Deletes an importer corresponding to the given Id.", response = void.class, tags = {})
	@ApiResponse(code = 200, message = "importer deleted", response = void.class)
	public Response deleteImporter(@PathParam("importerId") @ApiParam("ID of the importer to delete") Long importerId) {

		Importer importer = importerService.getById(importerId);
		if (importer != null) {
			importerService.delete(importer);
			return Response.ok().build();

		} else {
			// error
			return RestError.buildResponse(Response.Status.NOT_FOUND, "The importer with Id " + importerId + " does not exist");
		}
	}


	/**
	 * Returns all current operations on an importer, specified by the given Id.
	 *
	 * @param importerId ID of the importer
	 * @return Operations currently running on the importer
	 */
	@GET
	@Path("/{importerId}/operations")
	@Produces({"application/json"})
	@ApiOperation(value = "", notes = "Returns all current operations on an importer, specified by the given Id.", response = RestImporterOperation.class, responseContainer = "List", tags = {})
	@ApiResponse(code = 200, message = "Operations currently running on the importer", response = RestImporterOperation.class, responseContainer = "List")
	public Response getImporterOperations(@PathParam("importerId") @ApiParam("ID of the importer") Long importerId) {

		// Get the importer
		Importer importer = importerService.getById(importerId);
		if (importer != null) {
			RestImporter restImporter = new RestImporter(importer);

			return Response.ok(restImporter.getOperations()).build();

		} else {
			// error
			return RestError.buildResponse(Response.Status.NOT_FOUND, "The importer with Id " + importerId + " does not exist");
		}
	}

	/**
	 * Returns all current operations on an importer, specified by the given Id.
	 *
	 * @param importerId ID of the importer
	 * @return Operations currently running on the importer
	 */
	@GET
	@Path("/{importerId}/operations/history")
	@Produces({"application/json"})
	public Response getImporterOperationsHistory(@PathParam("importerId") @ApiParam("ID of the importer") Long importerId) {

		// Get the importer
		Importer importer = importerService.getById(importerId);
		if (importer != null) {

			List<ImporterOperation> importerOperations = importerService.getPreviousOperationsForImporter(importer);
			List<RestImporterOperation> operations = new ArrayList<>();

			for (ImporterOperation importerOperation : importerOperations) {
				operations.add(new RestImporterOperation(importerOperation));
			}

			return Response.ok(operations).build();

		} else {
			// error
			return RestError.buildResponse(Response.Status.NOT_FOUND, "The importer with Id " + importerId + " does not exist");
		}
	}


	/**
	 * Create a new import operation on an importer, specified by the given Id.
	 *
	 * @param importerId        ID of the importer
	 * @param importerOperation The new importer operation
	 * @return Full details of the created importer operation
	 */
	@POST
	@Path("/{importerId}/operations")
	@Produces({"application/json"})
	@ApiOperation(value = "", notes = "Create a new import operation on an importer, specified by the given Id.", response = void.class, tags = {})
	@ApiResponse(code = 201, message = "Full details of the created importer operation", response = ImporterOperation.class)
	public Response addImporterOperation(@PathParam("importerId") @ApiParam("ID of the importer") Long importerId, RestImporterOperation importerOperation) throws Exception {


		// Get the importer
		Importer importer = importerService.getById(importerId);
		if (importer != null) {
			ImporterOperation integratedImporterOperation = importerService.addImporterOperation(importer, importerOperation.getImporterOperation());

			if (integratedImporterOperation != null) {
				// Send to Data Source manager
				try {
					this.importerManager.performImport(integratedImporterOperation);

					// Return buildResponse with operations
					RestImporterOperation restImporterOperation = new RestImporterOperation(integratedImporterOperation);
					return Response.ok(restImporterOperation).status(201).build();

				} catch (ImporterManager.PumaImporterOperationException exception) {
					importerService.deleteOperation(integratedImporterOperation);

					// error
					return RestError.buildResponse(Response.Status.FORBIDDEN, "The importer operation is already running");
				}

			} else {
				// error
				return RestError.buildResponse(Response.Status.INTERNAL_SERVER_ERROR, "Failed to create the importer operation");
			}

		} else {
			// error
			return RestError.buildResponse(Response.Status.NOT_FOUND, "The importer with Id " + importerId + " does not exist");
		}
	}

	/**
	 * @param job The new job
	 * @return Details of the created importer
	 */
	@POST
	@Path("/{importerId}/jobs")
	@Produces({"application/json"})
	@Consumes({"application/json"})
	@ApiOperation(value = "", notes = "add a new job to the scheduler", response = RestJob.class, tags = {})
	@ApiResponse(code = 201, message = "", response = RestJob.class)
	public Response addJob(RestJob job, @PathParam("importerId") Long importerId) {
		//check if importer exist
		if(importerService.getById(importerId) == null){
			return RestError.buildResponse(Response.Status.NOT_FOUND, "The importer with Id " + importerId + " does not exist");
		}
		try {
			if (job.getId() != null) job.setId(null);

			ImporterOperationData importerOperationData = mapper.readValue(job.getJobData(), ImporterOperationData.class);
			importerOperationData.setImporterId(importerId);
			job.setJobData(mapper.writeValueAsString(importerOperationData));

			Job integratedJob = scheduler.addJob(job.getJob());
			if (integratedJob != null) {
				return Response.ok(new RestJob(integratedJob)).status(201).build();
			}
		} catch (IOException e) {
			log.error("can not add job to the importer " + importerId, e);
		}
		// error
		return RestError.buildResponse(Response.Status.BAD_REQUEST, "Failed to add the job");
	}

	/**
	 * Returns the job corresponding to the given Id.
	 *
	 * @param importerId the ID of the importer to fetch
	 * @return The specified job details
	 */
	@GET
	@Path("/{importerId}/jobs")
	@Produces({"application/json"})
	@ApiOperation(value = "", notes = "Returns the jobs corresponding to the given ImporterId.", response = RestJob.class, tags = {})
	@ApiResponse(code = 200, message = "The specified job details", response = RestJob.class)
	public Response getJobs(@PathParam("importerId") Long importerId) {
		//check if importer exist
		if(importerService.getById(importerId) == null){
			return RestError.buildResponse(Response.Status.NOT_FOUND, "The importer with Id " + importerId + " does not exist");
		}

		List<Job> jobs = jobService.getByImporterId(importerId);
		if (jobs != null) {
			return Response.ok(jobs).build();
		}

		// error
		return RestError.buildResponse(Response.Status.BAD_REQUEST, "Failed to get jobs");
	}


}
