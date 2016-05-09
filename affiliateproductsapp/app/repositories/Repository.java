package repositories;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;

import models.BaseEntity;

/**
 * Basic CRUD operations.
 */
public interface Repository {

	/**
	 * Save the passed entity to a database. <b>The passed entity should not exist in the database.</b>
	 */
	<T extends BaseEntity> T create(T entity);

	/**
	 * Save the passed entity to a database. <b>The passed entity should exist in the database.</b>
	 */
	<T extends BaseEntity> T update(T entity);

	int update(String query, Object... params);
	
    int update(EntityManager em, String jpaQuery, Object... params);

	<T extends BaseEntity> void delete(T entity);

	<T extends BaseEntity> boolean exists(Class<T> entityClass, Long id);
	
	<T extends BaseEntity> boolean exists(Class<T> returnClass, String query, Object... params);

	<T extends BaseEntity> T find(Class<T> entityClass, Long id);

	<T extends Object> T find(Class<T> returnClass, String query, Object... params);
	

	<T extends Object> List<T> findList(Class<T> returnClass, String query, Object... params);

	<T extends Object> List<T> findList(Class<T> returnClass, String query, int limit, Object... params);

	<T extends Object> List<T> list(Class<T> returnClass, String query, int offset, int limit, Object... params);

	<T extends BaseEntity> void detach(T entity);
	
	<T extends BaseEntity> Long max(Class<T> entityClass);
	
	
	<T> List<T> createNamedQuery(String query, Class<T> returnClass);
	<T> List<T> createNamedQuery(String query, Class<T> returnClass, int offset, int limit);
	<T> List<T> createNamedQuery(String query, Class<T> returnClass, int offset, int limit, Object... params);
	List createNamedQuery2(String query, Object... params);
	<T> List<T> createNamedQuery2(String query, Class<T> returnClass, Object... params);
	List createNamedQuery3(String query, int offset, int limit, Object... params);
	
	int update(String query);

	/**
	 * Read the entity data from a database. <b>All changes made to the passed entity will be discarded.</b>
	 */
	<T extends BaseEntity> void refresh(T entity);

	boolean isInsideTransaction();

	Object select(String query);
	
	<T extends Object> List<T> list(Class<T> returnClass, CriteriaQuery<T> cq, int offset,
			int limit);
	
}
