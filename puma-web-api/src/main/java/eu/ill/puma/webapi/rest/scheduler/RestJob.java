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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.ill.puma.persistence.domain.jobscheduler.Job;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * Created by letreguilly on 14/06/17.
 */
public class RestJob {

	@JsonIgnore
	private Job job;

	public RestJob() {
		this.job = new Job();
	}

	public RestJob(Job job) {
		this.job = job;
	}

	public Job getJob() {
		return job;
	}

	@JsonGetter
	@ApiModelProperty(example = "null", required = true, value = "")
	@NotNull
	public Long getId() {
		return this.job.getId();
	}

	public void setId(Long id) {
		this.job.setId(id);
	}

	@JsonGetter
	@ApiModelProperty(example = "null", required = true, value = "")
	@NotNull
	public String getName() {
		return this.job.getName();
	}

	public void setName(String name) {
		this.job.setName(name);
	}

	@JsonGetter
	@ApiModelProperty(example = "null", required = true, value = "")
	@NotNull
	public String getJobRunnerName() {
		return this.job.getJobRunnerName();
	}

	public void setJobRunnerName(String jobRunnerName) {
		this.job.setJobRunnerName(jobRunnerName);
	}

	@JsonGetter
	@ApiModelProperty(example = "null", required = true, value = "")
	@NotNull
	public String getScheduling() {
		return this.job.getScheduling();
	}

	public void setScheduling(String scheduling) {
		this.job.setScheduling(scheduling);
	}

	@JsonGetter
	@ApiModelProperty(example = "null", required = true, value = "")
	@NotNull
	public boolean isEnabled() {
		return this.job.isEnabled();
	}

	public void setEnabled(boolean enable) {
		this.job.setEnabled(enable);
	}

	@JsonGetter
	@ApiModelProperty(example = "null", required = true, value = "")
	@NotNull
	public String getJobData() {
		return this.job.getJobData();
	}

	public void setJobData(String jobData) {
		this.job.setJobData(jobData);
	}
}
