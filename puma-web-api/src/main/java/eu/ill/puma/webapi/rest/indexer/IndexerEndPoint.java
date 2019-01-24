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
package eu.ill.puma.webapi.rest.indexer;

import eu.ill.puma.indexer.manager.IndexationState;
import eu.ill.puma.indexer.manager.IndexerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Component
@Path("/indexer")
public class IndexerEndPoint {

	private static final Logger log = LoggerFactory.getLogger(IndexerEndPoint.class);

	@Autowired
	private IndexerManager indexerManager;

	@POST
	@Path("/{id}")
	public Response start(@PathParam("id") Long documentVersionId) {
		log.info("Received command to start indexing for document " + documentVersionId);

		this.indexerManager.indexAsync(documentVersionId);

		return Response.ok().build();
	}

	@POST
	@Path("/ids")
	public Response start(List<Long> documentVersionIds) {
		log.info("Received command to start indexing for " + documentVersionIds.size() + " documents");

		this.indexerManager.indexAsync(documentVersionIds);

		return Response.ok().build();
	}

	@POST
	@Path("/reindex")
	public Response reindex() {
		log.info("Received command to start indexation");

		this.indexerManager.indexAllAsync();

		return Response.ok().build();
	}

	@POST
	@Path("/remaining")
	public Response indexRemaining() {
		log.info("Received command to start indexation");

		this.indexerManager.indexRemainingForIndexationAsync();

		return Response.ok().build();
	}

	@GET
	@Path("/info")
	@Produces({"application/json"})
	public Response getIndexedCount() {
		log.info("Getting indexer info");

		long numberRemainingForIndexation = this.indexerManager.getNumberRemainingForIndexation();
		long numberIndexed = this.indexerManager.getNumberIndexed();
		IndexationState state = this.indexerManager.getState();
		RestIndexerResponse response = new RestIndexerResponse();
		response.setTotalNumberIndexed(numberIndexed);
		response.setTotalNumberToIndex(numberRemainingForIndexation);
		response.setState(state);

		return Response.ok(response).build();
	}

	@POST
	@Path("/pause")
	public Response pause() {
		log.info("Received command to pause indexation");

		this.indexerManager.pause();

		return Response.ok().build();
	}

	@POST
	@Path("/resume")
	public Response resume() {
		log.info("Received command to resume indexation");

		this.indexerManager.resume();

		return Response.ok().build();
	}

	@POST
	@Path("/cancelAll")
	public Response cancelAll() {
		log.info("Received command to cancel indexation");

		this.indexerManager.cancelAll();

		return Response.ok().build();
	}
}
