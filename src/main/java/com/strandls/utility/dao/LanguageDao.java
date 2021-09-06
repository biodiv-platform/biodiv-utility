/**
 * 
 */
package com.strandls.utility.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.NoResultException;

import com.strandls.utility.pojo.Language;
import com.strandls.utility.util.AbstractDAO;

/**
 * @author Abhishek Rudra
 *
 */
public class LanguageDao extends AbstractDAO<Language, Long> {

	private final Logger logger = LoggerFactory.getLogger(LanguageDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected LanguageDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public Language findById(Long id) {

		Session session = sessionFactory.openSession();
		Language entity = null;
		try {
			entity = session.get(Language.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return entity;
	}

	@SuppressWarnings("unchecked")
	public List<Language> findAll(Boolean isDirty) {
		String qry = "from Language where isDirty = :isDirty";
		Session session = sessionFactory.openSession();
		List<Language> resultList = new ArrayList<>();
		try {
			Query<Language> query = session.createQuery(qry);
			query.setParameter("isDirty", isDirty);
			resultList = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return resultList;
	}

	@SuppressWarnings("unchecked")
	public Language getLanguageByProperty(String property, String value, String condition) {

		String queryStr = "from Language where property condition :value";
		queryStr = queryStr.replace("property", property);
		queryStr = queryStr.replace("condition", condition);

		Session session = sessionFactory.openSession();

		Query<Language> query = session.createQuery(queryStr);
		query.setParameter("value", value);

		Language entity = null;
		try {
			entity = (Language) query.getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage());
			throw e;
		}
		session.close();
		return entity;

	}

}
