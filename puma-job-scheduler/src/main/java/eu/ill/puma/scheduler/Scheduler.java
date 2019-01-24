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
import eu.ill.puma.scheduler.jobrunner.JobRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by letreguilly on 13/06/17.
 */
@Service
@Configuration
@EnableScheduling
@EnableAsync
public class Scheduler implements ApplicationListener<ApplicationReadyEvent> {

	private static final Logger log = LoggerFactory.getLogger(Scheduler.class);

	@Autowired
	private JobService jobService;

	private ThreadPoolTaskScheduler poolTaskScheduler;

	private Map<String, JobRunner> jobRunnerMap = new ConcurrentHashMap();

	private Map<Long, ScheduledFuture> plannedJobMap = new ConcurrentHashMap();

	@PostConstruct
	public void runScheduler() {
		//create scheduler pool
		poolTaskScheduler = new ThreadPoolTaskScheduler();
		poolTaskScheduler.setPoolSize(16);
		poolTaskScheduler.initialize();
	}

	/**
	 * plan job after application startup to give to JobRunner the time to be register
	 *
	 * @param applicationReadyEvent
	 */
	@Override
	public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
		//add jobs to the scheduler
		for (Job job : jobService.getAll()) {
			if (job.isEnabled()) this.planJob(job);
		}
	}

	/**
	 * add a job to the DB and run it
	 *
	 * @param job
	 * @return
	 */
	public Job addJob(Job job) {
		job = jobService.save(job);

		if (job.isEnabled()){
			if (!this.planJob(job)) {
				jobService.delete(job);
				return null;
			}
		}

		log.info("add job " + job.getName());
		return job;
	}

	public Job updateJob(Job job) {
		Job oldJob = this.jobService.getById(job.getId());

		job = jobService.save(job);
		boolean isEnabled = job.isEnabled();

		if (oldJob != null && oldJob.isEnabled()){
			this.disableJob(oldJob, true);
		}

		if (isEnabled){
			if (!this.enableJob(job)) {
				jobService.delete(job);
				return null;
			}
		}

		log.info("updated job " + job.getName());
		return job;
	}

	/**
	 * delete a job
	 *
	 * @param job the job to delete
	 * @return
	 */
	public void deleteJob(Job job) {
		ScheduledFuture runningJob = plannedJobMap.get(job.getId());
		if (runningJob != null) {
			runningJob.cancel(true);
		}
		plannedJobMap.remove(job.getId());
		jobService.delete(job);
		log.info("delete job " + job.getName());
	}


	/**
	 * disable a job
	 *
	 * @param job                the job to disable
	 * @param interruptIfRunning set to true to interrupt the job if the task is running
	 * @return
	 */
	public boolean disableJob(Job job, boolean interruptIfRunning) {
		jobService.disableJob(job);
		ScheduledFuture runningJob = plannedJobMap.get(job.getId());
		plannedJobMap.remove(job.getId());
		if (runningJob != null) {
			log.info("disable job " + job.getName());
			return runningJob.cancel(interruptIfRunning);
		} else {
			return false;
		}
	}

	/**
	 * disable a job
	 *
	 * @param job the job to enabled
	 * @return
	 */
	public boolean enableJob(Job job) {
		jobService.enableJob(job);
		return this.planJob(job);
	}

	/**
	 * register a job runner
	 *
	 * @param jobRunner the job runner to register
	 */
	public void registerJobRunner(JobRunner jobRunner) {
		jobRunnerMap.put(jobRunner.getName(), jobRunner);
		log.info("register job runner " + jobRunner.getName());
	}

	/**
	 * run the job
	 *
	 * @param job run the job
	 */
	private boolean planJob(Job job) {
		try {
			//get job runner
			JobRunner jobRunner = jobRunnerMap.get(job.getJobRunnerName());

			if (jobRunner != null) {
				//create task
				Runnable runnable = () -> {
					//run job
					jobRunner.run(job);

					//set last run data
					Date currentDate = new Date(System.currentTimeMillis());
					job.setLastRunDate(currentDate);

					//save job
					jobService.save(job);
				};

				//plan it
				ScheduledFuture runningJob = poolTaskScheduler.schedule(runnable, new CronTrigger(job.getScheduling()));
				plannedJobMap.put(job.getId(), runningJob);

				log.info("plan job : " + job.getName());
				return true;
			} else {
				log.error("job runner : " + job.getJobRunnerName() + ", for job : " + job.getName() + ", not found");
			}
		} catch (Exception e) {
			log.error("can not plan job : " + job + ", invalid job ", e);
		}
		return false;
	}

	public Long getJobRunnerNumber(){
		return this.jobRunnerMap.size() + 0L;
	}

	public Long getPlannedJobNumber(){
		return this.plannedJobMap.size() + 0L;
	}

	public Set<String> getJobRunnerNames() {
		return this.jobRunnerMap.keySet();
	}
}
