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
package eu.ill.puma.persistence.service.jobscheduler;

import eu.ill.puma.persistence.domain.jobscheduler.Job;
import eu.ill.puma.persistence.repository.jobscheduler.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by letreguilly on 13/06/17.
 */
@Service
@Transactional
public class JobService {

	@Autowired
	private JobRepository jobRepository;

	public Job getById(Long id) {
		return this.jobRepository.getById(id);
	}

	public Job getByName(String name) {
		return this.jobRepository.getByName(name);
	}

	public List<Job> getByImporterId(Long ImporterId){
		return this.jobRepository.getByImporterId(ImporterId);
	}

	public List<Job> getAll() {
		return this.jobRepository.getAll();
	}

	public Job save(Job job) {
		if (job.getId() == null) {
			synchronized (this) {
				job = this.jobRepository.persist(job);
			}
		} else {
			job = this.jobRepository.merge(job);
		}
		return job;
	}

	public void delete(Job job){
		this.jobRepository.delete(job);
	}

	public void disableJob(Job job){
		job.setEnabled(false);
		this.save(job);
	}

	public void enableJob(Job job){
		job.setEnabled(true);
		this.save(job);
	}

}
