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

import eu.ill.puma.persistence.domain.ScrollableResultIterator;
import eu.ill.puma.persistence.repository.utils.HibernateIterableQuery;
import eu.ill.puma.persistence.repository.utils.IterableQuery;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;

public abstract class PumaRepository<T> {

	private static final Logger log = LoggerFactory.getLogger(PumaRepository.class);

	protected Class classType;
	protected String className;
	protected String initial;

	@PersistenceContext
	protected EntityManager entityManager;

	@Autowired
	protected EntityManagerFactory factory;

	@SuppressWarnings("unchecked")
	public PumaRepository() {
		this.classType  = (Class) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		this.className = classType.getSimpleName();
		this.initial = className.toLowerCase().substring(0, 1);
	}

	/**
	 * Returns all entities of the generic type
	 * @return All entities
	 */
	public List<T> getAll() {
		String queryString = this.createGetAllQueryString();
		log.debug(queryString);

		TypedQuery<T> query = entityManager.createQuery(queryString, classType);
		return query.getResultList();
	}

	protected String createGetAllQueryString() {
		String queryString = "select " + initial + " from " + className + " " + initial
			+ " order by " + initial + ".id asc";
		return queryString;
	}

	public List<Long> getAllIds() {
		String queryString = this.createGetAllIdsQueryString();
		log.debug(queryString);

		TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
		return query.getResultList();
	}

	protected String createGetAllIdsQueryString() {
		String queryString = "select " + initial + ".id from " + className + " " + initial
			+ " order by " + initial + ".id asc";
		return queryString;
	}

	/**
	 * Returns an iterator of entities of the generic type
	 * @return An iterator of entities
	 */
	public ScrollableResultIterator<T> getIterator() {
		return this.getIteratorFrom(null);
	}

	/**
	 * Returns an iterator of entities of the generic type
	 * @return An iterator of entities
	 */
	public ScrollableResultIterator<T> getIteratorFrom(Long startId) {
		String queryString = this.createGetIteratorFromQueryString(startId);
		log.debug(queryString);

		SessionFactory sessionFactory = factory.unwrap(SessionFactory.class);

		IterableQuery<T> query = new HibernateIterableQuery(sessionFactory.openStatelessSession(), queryString, classType);
		query.setParameter("id", startId);

		return query.getResultIterator();
	}

	protected String createGetIteratorFromQueryString(Long startId) {
		String queryString = "select " + initial + " from " + className + " " + initial;
		if (startId != null) {
			queryString += " where " + initial + ".id > :id";
		}
		queryString += " order by " + initial + ".id asc";
		return queryString;
	}

	/**
	 * Returns page of entities of the generic type
	 * @return A page of entities
	 */
	public List<T> getPage(int pageNumber, int pageSize) {
		String queryString = this.createGetPageQueryString(pageNumber, pageSize);
		log.debug(queryString);

		TypedQuery<T> query = entityManager.createQuery(queryString, classType);

		query.setFirstResult(pageNumber * pageSize);
		query.setMaxResults(pageSize);

		return query.getResultList();
	}

	protected String createGetPageQueryString(int pageNumber, int pageSize) {
		String queryString = "select " + initial + " from " + className + " " + initial
			+ " order by " + initial + ".id asc";
		return queryString;
	}

	/**
	 * Returns set of entities of the generic type starting from a specific index
	 * @return A set of entities
	 */
	public List<T> getPartial(long startId, int pageSize) {
		String queryString = this.createGetPartialQueryString(startId, pageSize);
		log.debug(queryString);

		TypedQuery<T> query = entityManager.createQuery(queryString, classType);
		query.setParameter("id", startId);

		query.setMaxResults(pageSize);

		return query.getResultList();
	}

	protected String createGetPartialQueryString(long startId, int pageSize) {
		String queryString = "select " + initial + " from " + className + " " + initial
			+ " where " + initial + ".id > :id"
			+ " order by " + initial + ".id asc";
		return queryString;
	}

	/**
	 * Returns a count of all entities of the generic type
	 * @return
	 */
	public long count() {
		String queryString = this.createCountQueryString();
		log.debug(queryString);

		// Generate the query
		TypedQuery<Long> query = this.entityManager.createQuery(queryString, Long.class);

		return query.getSingleResult();
	}

	protected String createCountQueryString() {
		String queryString = "select count(" + initial + ") from " + className + " " + initial;
		return queryString;
	}

	/**
	 * Returns the entity with the specified Id
	 * @param id the entity id
	 * @return The entity
	 */
	public T getById(Long id) {
		return this.getFirstEntity("id", id);
	}

	/**
	 * Persists a new entity
	 * @param entity the Entity to persist
	 * @return The persisted entity
	 */
	public T persist(T entity) {
		this.entityManager.persist(entity);
		return entity;
	}

	/**
	 * Merges an entity
	 * @param entity The entity to merge
	 * @return The merged entity
	 */
	public T merge(T entity) {
		T merged = this.entityManager.merge(entity);
		return merged;
	}

	/**
	 * Deletes an entity
	 * @param t The entity to delete
	 */
	public void delete(T t) {
		// First merge the entity to ensure it is not detached
		T merged = this.merge(t);

		// Remove/delete the entity
		this.entityManager.remove(merged);
	}

	/**
	 * Returns the first entity matching the given search parameter
	 * @param parameterName The search parameter name
	 * @param parameterValue The search parameter value
	 * @return The retrieved entity
	 */
	protected <T> T getFirstEntity(String parameterName, Object parameterValue) {
		return this.getFirstEntity(Arrays.asList(parameterName), parameterValue);
	}

	/**
	 * Returns the first entity matching the given search parameters
	 * @param parameterNames The search parameter names
	 * @param parameters The search parameter values
	 * @return The retrieved entity
	 */
	protected <T> T getFirstEntity(List<String> parameterNames, Object ... parameters) {
		try {
			TypedQuery<T> query = this.getTypedQuery(parameterNames, parameters);

			// Single result
			query.setMaxResults(1);

			return query.getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	/**
	 * Returns all entities matching the given search parameter
	 * @param parameterName The search parameter name
	 * @param parameterValue The search parameter value
	 * @return The list of retrieved entities
	 */
	protected <T> List<T> getEntities(String parameterName, Object parameterValue) {
		return this.getEntities(Arrays.asList(parameterName), parameterValue);
	}

	/**
	 * Returns all entities matching the given search parameters
	 * @param parameterNames The search parameter names
	 * @param parameters The search parameter values
	 * @return The list of retrieved entities
	 */
	protected <T> List<T> getEntities(List<String> parameterNames, Object ... parameters) {
		try {
			TypedQuery<T> query = this.getTypedQuery(parameterNames, parameters);

			return query.getResultList();

		} catch (NoResultException e) {
			return null;
		}
	}

	/**
	 * Generic function to obtain a TypedQuery from a given list of parameters
	 * @param parameterNames The parameter names
	 * @param parameters The parameter values
	 * @return The TypedQuery
	 */
	private <T> TypedQuery<T> getTypedQuery(List<String> parameterNames, Object ... parameters) {
		String className = classType.getSimpleName();
		String initial = className.toLowerCase().substring(0, 1);

		// Build up query string from parameters
		String queryString = this.createTypedQueryBaseQueryString();
		for (int i = 0; i < parameterNames.size(); i++) {
			String name = parameterNames.get(i);
			Object value = parameters[i];

			queryString += " (" + initial + "." + name +
					(value == null ? " is null)" : (" = :" + name + ")")) +
					((i < parameterNames.size() - 1) ? " and" : "");
		}
		queryString += " order by " + initial + ".id asc";

		log.debug(queryString);

		// Generate the query
		TypedQuery<T> query = entityManager.createQuery(queryString, classType);

		// Set the query parameters
		for (int i = 0; i < parameterNames.size(); i++) {
			String name = parameterNames.get(i);
			Object value = parameters[i];

			if (value != null) {
				query.setParameter(name, value);
			}
		}

		return query;
	}

	protected String createTypedQueryBaseQueryString() {
		String queryString = "select " + initial + " from " + className + " " + initial
			+ " where ";

		return queryString;
	}
}
