package com.strandls.utility.util;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public abstract class AbstractDAO<T, K extends Serializable> {

	protected final SessionFactory sessionFactory;
	protected final Class<T> daoType;

	@SuppressWarnings("unchecked")
	protected AbstractDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
		this.daoType = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	public T save(T entity) {
		try (Session session = sessionFactory.openSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				session.save(entity);
				tx.commit();
			} catch (Exception e) {
				if (tx != null)
					tx.rollback();
				throw e;
			}
		}
		return entity;
	}

	public T update(T entity) {
		try (Session session = sessionFactory.openSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				session.update(entity);
				tx.commit();
			} catch (Exception e) {
				if (tx != null)
					tx.rollback();
				throw e;
			}
		}
		return entity;
	}

	public T delete(T entity) {
		try (Session session = sessionFactory.openSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				session.delete(entity);
				tx.commit();
			} catch (Exception e) {
				if (tx != null)
					tx.rollback();
				throw e;
			}
		}
		return entity;
	}

	public abstract T findById(K id);

	public List<T> findAll() {
		try (Session session = sessionFactory.openSession()) {
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<T> cq = cb.createQuery(daoType);
			Root<T> root = cq.from(daoType);
			cq.select(root).distinct(true);
			return session.createQuery(cq).getResultList();
		} catch (Exception e) {
			throw new RuntimeException("Error in findAll", e);
		}
	}

	public List<T> findAll(int limit, int offset) {
		try (Session session = sessionFactory.openSession()) {
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<T> cq = cb.createQuery(daoType);
			Root<T> root = cq.from(daoType);
			cq.select(root).distinct(true);
			return session.createQuery(cq).setFirstResult(offset).setMaxResults(limit).getResultList();
		} catch (Exception e) {
			throw new RuntimeException("Error in findAll (paginated)", e);
		}
	}
}
