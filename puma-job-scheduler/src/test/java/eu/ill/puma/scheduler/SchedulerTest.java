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
package eu.ill.puma.scheduler;

import eu.ill.puma.persistence.domain.jobscheduler.Job;
import eu.ill.puma.persistence.service.jobscheduler.JobService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

/**
 * Created by letreguilly on 23/06/17.
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:/applicationContext-test.xml"
})
@TestExecutionListeners({
		DependencyInjectionTestExecutionListener.class,
		TransactionalTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SchedulerTest {

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private JobService jobService;

	@Test
	public void testJobRunnerAutoRegistration() {
		Assert.assertTrue(scheduler.getJobRunnerNumber() >= 3L);
	}

	@Test
	public void testAddJob() throws InterruptedException {
		//create test job
		Job job = new Job();
		job.setEnabled(true);
		job.setScheduling("* * * * * *");
		job.setJobData("");
		job.setName("test job");
		job.setJobRunnerName("fake_job_runner");

		Long oldSize = scheduler.getPlannedJobNumber();

		//plan test job
		scheduler.addJob(job);

		Long newSize = scheduler.getPlannedJobNumber();

		Assert.assertTrue(newSize > oldSize);

	}

	@Test
	public void testDeleteJob() {
		Job job = new Job();
		job.setEnabled(true);
		job.setScheduling("00 00 * * * *");
		job.setJobData("");
		job.setName("test job");
		job.setJobRunnerName("fake_job_runner");

		//plan test job
		job = scheduler.addJob(job);

		Long oldSize = scheduler.getPlannedJobNumber();

		//delete test job
		scheduler.deleteJob(job);

		Long newSize = scheduler.getPlannedJobNumber();

		//assert deletion
		Assert.assertTrue(oldSize > newSize);
	}

	@Test
	public void testDisableJob() {
		Job job = new Job();
		job.setEnabled(true);
		job.setScheduling("00 00 * * * *");
		job.setJobData("");
		job.setName("test job");
		job.setJobRunnerName("fake_job_runner");

		//plan test job
		job = scheduler.addJob(job);

		Long oldSize = scheduler.getPlannedJobNumber();

		//disable test job
		Assert.assertTrue(scheduler.disableJob(job, true));

		Long newSize = scheduler.getPlannedJobNumber();

		//assert deletion
		Assert.assertTrue(oldSize > newSize);
		Assert.assertEquals(false, jobService.getById(job.getId()).isEnabled());
	}

	@Test
	public void testEnableJob() {
		Job job = new Job();
		job.setEnabled(false);
		job.setScheduling("00 00 * * * *");
		job.setJobData("");
		job.setName("test job");
		job.setJobRunnerName("fake_job_runner");

		//plan test job
		job = scheduler.addJob(job);

		Long oldSize = scheduler.getPlannedJobNumber();

		//disable test job
		Assert.assertTrue(scheduler.enableJob(job));

		Long newSize = scheduler.getPlannedJobNumber();

		//assert deletion
		Assert.assertTrue(oldSize < newSize);
		Assert.assertEquals(true, jobService.getById(job.getId()).isEnabled());
	}
}
