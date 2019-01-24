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
package eu.ill.puma.webapi.rest;

import eu.ill.puma.persistence.service.jobscheduler.JobService;
import eu.ill.puma.webapi.rest.scheduler.RestJob;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by letreguilly on 23/06/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SchedulerRestApiTest {
	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private JobService jobService;

	@Test
	public void testAddJob() {
		this.addTestJob(true);
	}

	@Test
	public void testGetAllJobs() {
		this.addTestJob(true);

		ResponseEntity<RestJob[]> restJobReponse = restTemplate.getForEntity("/api/v1/jobs", RestJob[].class);

		RestJob[] restJobs = restJobReponse.getBody();

		//assert response list
		Assert.assertNotNull(restJobs);
		Assert.assertEquals(1, restJobs.length);

		RestJob responseRestJob = restJobs[0];

		//assert response job
		Assert.assertTrue(responseRestJob.isEnabled());
		Assert.assertEquals("", responseRestJob.getJobData());
		Assert.assertEquals("fake_job_runner", responseRestJob.getJobRunnerName());
		Assert.assertEquals("00 00 * * * *", responseRestJob.getScheduling());
		Assert.assertEquals("test rest job", responseRestJob.getName());
	}

	@Test
	public void testGetJobsById() {
		this.addTestJob(true);

		ResponseEntity<RestJob> restJobReponse = restTemplate.getForEntity("/api/v1/jobs/1", RestJob.class);

		RestJob responseRestJob = restJobReponse.getBody();

		//assert response list
		Assert.assertNotNull(responseRestJob);

		//assert response job
		Assert.assertTrue(responseRestJob.isEnabled());
		Assert.assertEquals("", responseRestJob.getJobData());
		Assert.assertEquals("fake_job_runner", responseRestJob.getJobRunnerName());
		Assert.assertEquals("00 00 * * * *", responseRestJob.getScheduling());
		Assert.assertEquals("test rest job", responseRestJob.getName());
	}

	@Test
	public void testDeleteJob() {
		this.addTestJob(true);
		restTemplate.delete("/api/v1/jobs/1");
		Assert.assertNull(jobService.getById(1L));
	}

	@Test
	public void testEnableJob() {
		this.addTestJob(false);
		ResponseEntity<Object> restJobReponse = restTemplate.postForEntity("/api/v1/jobs/1/enable", null, Object.class);
		Assert.assertNotNull(restJobReponse);
		Assert.assertTrue(jobService.getById(1L).isEnabled());
	}

	@Test
	public void testDisableJob() {
		this.addTestJob(true);
		ResponseEntity<Object> restJobReponse = restTemplate.postForEntity("/api/v1/jobs/1/disable", null, Object.class);
		Assert.assertNotNull(restJobReponse);
		Assert.assertTrue(!jobService.getById(1L).isEnabled());
	}

	private void addTestJob(boolean enabled) {
		//create rest job
		RestJob restJob = new RestJob();
		restJob.setEnabled(enabled);
		restJob.setJobData("");
		restJob.setJobRunnerName("fake_job_runner");
		restJob.setScheduling("00 00 * * * *");
		restJob.setName("test rest job");

		// Create entity to post
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<RestJob> entity = new HttpEntity(restJob, headers);

		//do post
		ResponseEntity<RestJob> response = restTemplate.postForEntity("/api/v1/jobs", entity, RestJob.class);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());

	}
}
