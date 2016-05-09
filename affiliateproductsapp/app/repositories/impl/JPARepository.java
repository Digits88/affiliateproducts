package repositories.impl;

import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

import models.BaseEntity;

import org.apache.commons.lang.ArrayUtils;

import play.db.jpa.JPA;
import repositories.Repository;

public class JPARepository implements Repository {
	
	public static Repository repository = null;
	public static Repository getInstance() {
		if (repository == null) {
			repository = new JPARepository();
		}
		return repository;
	}

	@Override
	public <T extends BaseEntity> T create(T entity) {
		JPA.em().persist(entity);
		return entity;
	}

	@Override
	public <T extends BaseEntity> T update(T entity) {
		return entity.save();
	}

	@Override
	public int update(String jpaQuery, Object... params) {
		return update(JPA.em(), jpaQuery, params);
	}
	
	@Override
	public int update(EntityManager em, String jpaQuery, Object... params) {
		Query jpaQueryObj = em.createQuery(jpaQuery);
		setParameters(jpaQueryObj, params);
		return jpaQueryObj.executeUpdate();
	}

	@Override
	public <T extends BaseEntity> void delete(T entity) {
		JPA.em().remove(entity);
	}

	@Override
	public <T extends BaseEntity> boolean exists(Class<T> entityClass, Long id) {
		String query = "select id from " + entityClass.getName() + " where id = ?";
		return find(Number.class, query, id) != null;
	}
	
	@Override
	public <T extends BaseEntity> boolean exists(Class<T> returnClass,
			String query, Object... params) {
		return find(returnClass, query, params) != null;
	}
	

	@Override
	public <T extends BaseEntity> T find(Class<T> entityClass, Long id) {
		return JPA.em().find(entityClass, id);
	}

	@Override
	@Nullable
	public <T extends Object> T find(Class<T> returnClass, String jpaQuery, Object... params) {
		TypedQuery<T> typedQuery = JPA.em().createQuery(jpaQuery, returnClass);
		setParameters(typedQuery, params);
		try {
			return typedQuery.getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public <T extends Object> List<T> findList(Class<T> returnClass, String jpaQuery, Object... params) {
		TypedQuery<T> typedQuery = JPA.em().createQuery(jpaQuery, returnClass);
		setParameters(typedQuery, params);
		return typedQuery.getResultList();
	}

	@Override
	public <T extends Object> List<T> findList(Class<T> returnClass, String jpaQuery, int limit, Object... params) {
		TypedQuery<T> typedQuery = JPA.em().createQuery(jpaQuery, returnClass);
		setParameters(typedQuery, params);
		typedQuery.setMaxResults(limit);
		return typedQuery.getResultList();
	}

	@Override
	public <T extends Object> List<T> list(Class<T> returnClass, String jpaQuery, int offset, int limit, Object... params) {
		TypedQuery<T> typedQuery = JPA.em().createQuery(jpaQuery, returnClass);
		setParameters(typedQuery, params);
		typedQuery.setFirstResult(offset);
		typedQuery.setMaxResults(limit);
		return typedQuery.getResultList();
	}

	@Override
	public <T extends Object> List<T> list(Class<T> returnClass, CriteriaQuery<T> cq, int offset, int limit) {
		TypedQuery<T> typedQuery = JPA.em().createQuery(cq);
		typedQuery.setFirstResult(offset);
		typedQuery.setMaxResults(limit);
		return typedQuery.getResultList();
	}

	@Override
	public <T extends BaseEntity> void detach(T entity) {
		JPA.em().detach(entity);
	}

	@Override
	public <T extends BaseEntity> void refresh(T entity) {
		JPA.em().refresh(entity);
	}

	@Override
	public boolean isInsideTransaction() {
		return JPA.isEnabled() && JPA.isInsideTransaction();
	}

	private void setParameters(Query jpaQuery, Object... params) {
		if (ArrayUtils.isNotEmpty(params)) {
			for (int i = 0; i < params.length; i++) {
				jpaQuery.setParameter(i + 1, params[i]);
			}
		}
	}

	@Override
	public <T extends BaseEntity> Long max(Class<T> entityClass) {
		String query = "select coalesce(max(id), '0') from " + entityClass.getName();
		TypedQuery<Long> typedQuery = JPA.em().createQuery(query, Long.class);
		return typedQuery.getSingleResult();
	}
	
	@Override
	public Object select(String query) {
		return JPA.em().createQuery(query).getSingleResult();
	}
	
	@Override
	public <T> List<T> createNamedQuery(String query, Class<T> returnClass) {
		return createNamedQuery(JPA.em(), query, returnClass);
	}
	
	@Override
	public <T> List<T> createNamedQuery(String query, Class<T> returnClass, int offset, int limit) {
		return createNamedQuery(JPA.em(), query, returnClass, offset, limit);
	}
	
	@Override
	public <T> List<T> createNamedQuery(String query, Class<T> returnClass, int offset, int limit, Object... params) {
		return createNamedQuery(JPA.em(), query, returnClass, offset, limit, params);
	}
	
	<T> List<T> createNamedQuery(EntityManager em, String query, Class<T> returnClass) {
		return em.createNamedQuery(query, returnClass).getResultList();
	}
	
	<T>List<T> createNamedQuery(EntityManager em, String query, Class<T> returnClass,  int offset, int limit) {
		return em.createNamedQuery(query, returnClass).setFirstResult(offset)
				.setMaxResults(limit).getResultList();
	}
	
	<T> List<T> createNamedQuery(EntityManager em, String query, Class<T> returnClass,  int offset, int limit, Object... params) {
		TypedQuery<T> typedQuery = JPA.em().createNamedQuery(query, returnClass);
		setParameters(typedQuery, params);
		typedQuery.setFirstResult(offset);
		typedQuery.setMaxResults(limit);
		return typedQuery.getResultList();
	}
	
	@Override
	public List createNamedQuery2(String query, Object... params) {
		Query jpaQuery = JPA.em().createNamedQuery(query);
		setParameters(jpaQuery, params);
		return jpaQuery.getResultList();
	}
	
	@Override
	public <T> List<T> createNamedQuery2(String query, Class<T> returnClass,
			Object... params) {
		return createNamedQuery2(JPA.em(), query, returnClass, params);
	}

	<T> List<T> createNamedQuery2(EntityManager em, String query,
			Class<T> returnClass, Object... params) {
		TypedQuery<T> typedQuery = em.createNamedQuery(query, returnClass);
		setParameters(typedQuery, params);
		return typedQuery.getResultList();
	}
	
	@Override
	public int update(String query) {
		return JPA.em().createQuery(query).executeUpdate();
	}

	/**
	 * This method is used for fetching the Object[] array, which contains more than one class object.
	 */
	@Override
	public List createNamedQuery3(String query, int offset, int limit,
			Object... params) {
		Query jpaQuery = JPA.em().createNamedQuery(query);
		setParameters(jpaQuery, params);
		jpaQuery.setFirstResult(offset);
		jpaQuery.setMaxResults(limit);
		return jpaQuery.getResultList();
	}
}
