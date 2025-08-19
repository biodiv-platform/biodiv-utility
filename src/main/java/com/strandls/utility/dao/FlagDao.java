/** */
package com.strandls.utility.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.utility.pojo.Flag;
import com.strandls.utility.util.AbstractDAO;

import jakarta.inject.Inject;

/**
 * @author Abhishek Rudra
 */
public class FlagDao extends AbstractDAO<Flag, Long> {

	private static final Logger logger = LoggerFactory.getLogger(FlagDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected FlagDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public Flag findById(Long id) {
		Session session = sessionFactory.openSession();
		Flag entity = null;
		try {
			entity = session.get(Flag.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public List<Flag> findByObjectId(String objType, Long id) {
		String qry = "from Flag f where f.objectType = :objType and f.objectId = :id";
		Session session = sessionFactory.openSession();
		List<Flag> flag = null;
		try {
			Query<Flag> query = session.createQuery(qry);
			query.setParameter("objType", objType);
			query.setParameter("id", id);

			flag = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return flag;
	}

	@SuppressWarnings("unchecked")
	public List<Flag> findByUserId(Long id) {

		String qry = "from Flag f where f.authorId = :id";
		Session session = sessionFactory.openSession();
		List<Flag> result = null;
		try {
			Query<Flag> query = session.createQuery(qry);
			query.setParameter("id", id);

			result = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public Flag findByObjectIdUserId(Long objectId, Long userId, String objectType) {
		Session session = sessionFactory.openSession();
		Flag result = null;
		String qry = "from Flag where objectType = :objType and objectId = :id and authorId = :userId";
		try {
			Query<Flag> query = session.createQuery(qry);
			query.setParameter("objType", objectType);
			query.setParameter("id", objectId);
			query.setParameter("userId", userId);

			result = query.getSingleResult();

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}
}
