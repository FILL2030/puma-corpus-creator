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
package eu.ill.puma.webapi.rest.scheduler;

import eu.ill.puma.persistence.domain.jobscheduler.Job;
import eu.ill.puma.persistence.service.jobscheduler.JobService;
import eu.ill.puma.scheduler.Scheduler;
import eu.ill.puma.webapi.rest.error.RestError;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * Created by letreguilly on 14/06/17.
 */
@Component
@Path("/jobs")
@Api(description = "the scheduler API")
public class SchedulerEndpoint {

	private static final Logger log = LoggerFactory.getLogger(SchedulerEndpoint.class);

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private JobService jobService;

	/**
	 * Returns all importers including their details and status
	 *
	 * @return array of all importers including their details and status
	 */
	@GET
	@Produces({"application/json"})
	@ApiOperation(value = "", notes = "Returns all scheduler jobs including their details and status", response = Job.class, responseContainer = "List")
	@ApiResponse(code = 200, message = "Details of all Jobs", response = Job.class, responseContainer = "List")
	public Response getJobs() {
		return Response.ok(jobService.getAll()).build();
	}


	/**
	 * Returns the job corresponding to the given Id.
	 *
	 * @param jobId the ID of the job to fetch
	 * @return The specified job details
	 */
	@GET
	@Path("/{jobId}")
	@Produces({"application/json"})
	@ApiOperation(value = "", notes = "Returns the job corresponding to the given Id.", response = RestJob.class, tags = {})
	@ApiResponse(code = 200, message = "The specified job details", response = RestJob.class)
	public Response getJob(@PathParam("jobId") @ApiParam("ID of the job to fetch") Long jobId) {

		Job job = jobService.getById(jobId);
		if (job != null) {
			return Response.ok(new RestJob(job)).build();
		}

		// error
		return RestError.buildResponse(Response.Status.NOT_FOUND, "The importer with Id " + jobId + " does not exist");
	}

	/**
	 * @param job The new job
	 * @return Details of the created importer
	 */
	@POST
	@Produces({"application/json"})
	@Consumes({"application/json"})
	@ApiOperation(value = "", notes = "add a new job to the scheduler", response = RestJob.class, tags = {})
	@ApiResponse(code = 201, message = "", response = RestJob.class)
	public Response addJob(RestJob job) {
		if (job.getId() != null) job.setId(null);

		Job integratedJob = scheduler.addJob(job.getJob());
		if (integratedJob != null) {
			return Response.ok(new RestJob(integratedJob)).status(201).build();
		}

		// error
		return RestError.buildResponse(Response.Status.BAD_REQUEST, "Failed to add the job");
	}

	/**
	 * @param job The job to update
	 * @return Details of the created importer
	 */
	@PUT
	@Produces({"application/json"})
	@Consumes({"application/json"})
	@ApiResponse(code = 201, message = "", response = RestJob.class)
	public Response updateJob(RestJob job) {
		Job integratedJob = scheduler.updateJob(job.getJob());
		if (integratedJob != null) {
			return Response.ok(new RestJob(integratedJob)).status(201).build();
		}

		// error
		return RestError.buildResponse(Response.Status.BAD_REQUEST, "Failed to add the job");
	}

	/**
	 * Deletes the job corresponding to the given Id.
	 *
	 * @param jobId ID of the importer to delete
	 * @return 200 if importer deleted
	 */
	@DELETE
	@Path("/{jobId}")
	@Produces({"application/json"})
	public Response deleteJobWithDeleteVerb(@PathParam("jobId") @ApiParam("ID of the importer to delete") Long jobId) {
		return this.deleteJob(jobId);
	}

	private Response deleteJob(Long jobId) {
		Job job = jobService.getById(jobId);
		if (job != null) {
			scheduler.deleteJob(job);
			log.info("delete job with id : " + jobId);
			return Response.ok().build();
		} else {
			// error
			return RestError.buildResponse(Response.Status.NOT_FOUND, "The job with Id " + jobId + " does not exist");
		}
	}

	/**
	 * Deletes the job corresponding to the given Id.
	 *
	 * @param jobId ID of the importer to delete
	 * @return 200 if importer deleted
	 */
	@POST
	@Path("/{jobId}/disable")
	@Produces({"application/json"})
	public Response disableJob(@PathParam("jobId") @ApiParam("ID of the importer to delete") Long jobId) {
		Job job = jobService.getById(jobId);
		if (job != null) {
			scheduler.disableJob(job, false);
			return Response.ok(job).build();
		} else {
			// error
			return RestError.buildResponse(Response.Status.NOT_FOUND, "The job with Id " + jobId + " does not exist");
		}
	}


	/**
	 * Deletes the job corresponding to the given Id.
	 *
	 * @param jobId ID of the importer to delete
	 * @return 200 if importer deleted
	 */
	@POST
	@Path("/{jobId}/enable")
	@Produces({"application/json"})
	public Response enableJob(@PathParam("jobId") @ApiParam("ID of the importer to delete") Long jobId) {

		Job job = jobService.getById(jobId);
		if (job != null) {
			scheduler.enableJob(job);
			return Response.ok(job).build();
		} else {
			// error
			return RestError.buildResponse(Response.Status.NOT_FOUND, "The job with Id " + jobId + " does not exist");
		}
	}

	/**
	 * Returns a list of all job runners.
	 *
	 * @return job runner name list
	 */
	@GET
	@Path("/runners")
	@Produces({"application/json"})
	@ApiResponse(code = 200, message = "job runner names", response = void.class)
	public Response getJobRunnerName() {
		return Response.ok(scheduler.getJobRunnerNames()).build();
	}


}
