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
package eu.ill.puma.persistence.repository;

public abstract class PumaDocumentEntityRepository<T> extends PumaRepository<T> {

	protected String createGetAllQueryString() {
		String queryString = "select " + initial + " from " + className + " " + initial
			+ " where " + initial + ".obsolete = false"
			+ " order by " + initial + ".id asc";
		return queryString;
	}

	protected String createGetAllIdsQueryString() {
		String queryString = "select " + initial + ".id from " + className + " " + initial
			+ " where " + initial + ".obsolete = false"
			+ " order by " + initial + ".id asc";
		return queryString;
	}

	protected String createGetIteratorFromQueryString(Long startId) {
		String queryString = "select " + initial + " from " + className + " " + initial;
		queryString += " where " + initial + ".obsolete = false";
		if (startId != null) {
			queryString += " and " + initial + ".id > :id";
		}
		queryString += " order by " + initial + ".id asc";
		return queryString;
	}

	protected String createGetPageQueryString(int pageNumber, int pageSize) {
		String queryString = "select " + initial + " from " + className + " " + initial
			+ " where " + initial + ".obsolete = false"
			+ " order by " + initial + ".id asc";
		return queryString;
	}

	public String createGetPartialQueryString(long startId, int pageSize) {
		String queryString = "select " + initial + " from " + className + " " + initial
			+ " where " + initial + ".id > :id"
			+ " and "+ initial + ".obsolete = false"
			+ " order by " + initial + ".id asc";
		return queryString;
	}

	protected String createCountQueryString() {
		String queryString = "select count(" + initial + ") from " + className + " " + initial
			+ " where " + initial + ".obsolete = false";
		return queryString;
	}


	protected String createTypedQueryBaseQueryString() {
		String queryString = "select " + initial + " from " + className + " " + initial
			+ " where " + initial + ".obsolete = false"
			+ " and ";

		return queryString;
	}

}
