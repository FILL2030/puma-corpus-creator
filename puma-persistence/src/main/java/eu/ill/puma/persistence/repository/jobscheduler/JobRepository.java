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
package eu.ill.puma.persistence.repository.jobscheduler;

import eu.ill.puma.persistence.domain.jobscheduler.Job;
import eu.ill.puma.persistence.repository.PumaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by letreguilly on 13/06/17.
 */
@Repository
public class JobRepository extends PumaRepository<Job> {
	public Job getByName(String name) {
		return this.getFirstEntity("name", name);
	}

	public List<Job> getByImporterId(Long importerId) {
		String queryString = "select j from  Job j where j.jobData like '%\"importerId\":" + importerId + "%'";

		TypedQuery<Job> query = entityManager.createQuery(queryString, Job.class);

		return query.getResultList();
	}
}
