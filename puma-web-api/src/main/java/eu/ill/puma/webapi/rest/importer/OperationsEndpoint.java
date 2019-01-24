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

import eu.ill.puma.importermanager.ImporterManager;
import eu.ill.puma.persistence.domain.importer.ImporterOperation;
import eu.ill.puma.persistence.service.importer.ImporterService;
import eu.ill.puma.webapi.rest.error.RestError;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Component
@Path("/operations")
@Api(description = "the importer API")
public class OperationsEndpoint {

	@Autowired
	private ImporterService importerService;

	@Autowired
	private ImporterManager importerManager;


	/**
	 * Returns all current operations
	 * @return array of all Operations currently running
	 */
	@GET
	@Produces({ "application/json" })
	@ApiOperation(value = "", notes = "Returns all current operations", response = RestImporterOperation.class, responseContainer = "List", tags={  })
	@ApiResponse(code = 200, message = "Details of all current operations", response = RestImporterOperation.class, responseContainer = "List")
	public Response getOperations() {
		List<ImporterOperation> allOperations = importerService.getRunningOperations();

		// Convert to rest objects
		List<RestImporterOperation> allRestImporterOperations = new ArrayList<>();
		for (ImporterOperation importerOperation : allOperations) {
			allRestImporterOperations.add(new RestImporterOperation(importerOperation));
		}

		return Response.ok(allRestImporterOperations).build();
	}

	/**
	 * Returns all current operations
	 * @return array of all Operations currently running
	 */
	@GET
	@Path("/history")
	@Produces({ "application/json" })
	public Response getOperationsHistory() {
		List<ImporterOperation> allOperations = importerService.getPreviousOperations();

		// Convert to rest objects
		List<RestImporterOperation> allRestImporterOperations = new ArrayList<>();
		for (ImporterOperation importerOperation : allOperations) {
			allRestImporterOperations.add(new RestImporterOperation(importerOperation));
		}

		return Response.ok(allRestImporterOperations).build();
	}

	/**
	 * Returns an operation from an importer specified by both the importer Id and operation Id.
	 * @param operationId ID of the operation
	 * @return The specified operation details
	 */
	@GET
	@Path("/{operationId}")
	@Produces({ "application/json" })
	@ApiOperation(value = "", notes = "Returns an operation from an importer specified by the operation Id.", response = RestImporterOperation.class, tags={  })
	@ApiResponse(code = 200, message = "The specified operation details", response = RestImporterOperation.class)
	public Response getImporterOperation(@PathParam("operationId") @ApiParam("ID of the operation") Long operationId) {

		ImporterOperation importerOperation = importerService.getOperationById(operationId);
		if (importerOperation != null) {
			// Return the operation
			return Response.ok(new RestImporterOperation(importerOperation)).build();
		}

		// error
		return RestError.buildResponse(Response.Status.NOT_FOUND, "The importer operation with Id " + operationId + " does not exist");
	}


	/**
	 * Cancel an operation on an importer specified by both the importer Id and operation Id.
	 * @param operationId ID of the operation to delete
	 * @return status 200 if operation deleted
	 */
	@DELETE
	@Path("/{operationId}")
	@Produces({ "application/json" })
	@ApiOperation(value = "", notes = "Cancel an operation specified by its operation Id.", response = void.class, tags={  })
	@ApiResponse(code = 200, message = "operation deleted", response = void.class)
	public Response deleteImporterOperation(@PathParam("operationId") @ApiParam("ID of the operation to delete") Long operationId) {

		ImporterOperation importerOperation = importerService.getOperationById(operationId);
		if (importerOperation != null) {
			// Send to Task Manager
			this.importerManager.performCancel(importerOperation);

			return Response.ok().build();
		}

		// error
		return RestError.buildResponse(Response.Status.NOT_FOUND, "The importer operation with Id " + operationId + " does not exist");
	}

}
