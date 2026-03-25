package com.strandls.utility.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.strandls.utility.pojo.HomePageData;
import com.strandls.utility.util.AbstractDAO;

public class HomePageDao extends AbstractDAO<HomePageData, Long> {
	private final Logger logger = LoggerFactory.getLogger(HomePageDao.class);

	@Inject
	protected HomePageDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public HomePageData findById(Long id) {
		HomePageData result = null;
		Session session = sessionFactory.openSession();
		try {
			result = session.get(HomePageData.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;

	}

	@SuppressWarnings("unchecked")
	public HomePageData findByLanguageId(Long languageId) {

		String qry = "from HomePageData where languageId = :languageId";
		Session session = sessionFactory.openSession();
		HomePageData result = null;

		try {
			Query<HomePageData> query = session.createQuery(qry);
			query.setParameter("languageId", languageId);
			result = query.getSingleResult();

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return result;
	}

}
