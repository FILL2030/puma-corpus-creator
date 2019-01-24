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
package eu.ill.puma.persistence.repository.utils;

import eu.ill.puma.persistence.domain.ScrollableResultIterator;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.SharedSessionContract;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class HibernateIterableQuery<T> implements IterableQuery<T> {

	private final SharedSessionContract session;
	private final String sql;
	private final Class<T> type;
	private final Map<String, Object> parameters = new HashMap<>();
	private Integer fetchSize;

	public HibernateIterableQuery(SharedSessionContract session, String sql, Class<T> type) {
		this.session = session;
		this.sql = sql;
		this.type = type;
	}

	@Override
	public IterableQuery<T> setParameter(String name, Object value) {
		parameters.put(name, value);
		return this;
	}

	@Override
	public IterableQuery<T> setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
		return this;
	}

	public ScrollableResultIterator<T> getResultIterator() {
		Query query = session.createQuery(sql);
		if (fetchSize != null) {
			query.setFetchSize(fetchSize);
		}
		query.setReadOnly(true);
		for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
			query.setParameter(parameter.getKey(), parameter.getValue());
		}
		ScrollableResults scroll = query.scroll(ScrollMode.FORWARD_ONLY);

		return new HibernateScrollableResultIterator<>(scroll, type);
	}

	private static class HibernateScrollableResultIterator<T> extends ScrollableResultIterator<T> {

		private final ScrollableResults results;
		private final Class<T> type;

		private Object[] nextRow;

		HibernateScrollableResultIterator(ScrollableResults results, Class<T> type) {
			this.results = results;
			this.type = type;
		}

		@Override
		public boolean hasNext() {
			if (!hasNextRow()) {
				return goToNextRow();
			}

			return hasNextRow();
		}

		@Override
		public T next() {
			if (!hasNext()) {
				throw new NoSuchElementException("No more results");
			}

			try {
				return type.cast(nextRow[0]);
			} finally {
				goToNextRow();
			}
		}

		@Override
		public void close() {
			if (this.results != null) {
				this.results.close();
			}
		}

		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			this.close();
		}

		private boolean goToNextRow() {
			results.next();
			nextRow = results.get();

			return hasNextRow();
		}

		private boolean hasNextRow() {
			return nextRow != null;
		}
	}
}
