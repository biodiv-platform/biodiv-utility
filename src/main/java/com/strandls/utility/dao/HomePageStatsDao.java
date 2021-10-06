package com.strandls.utility.dao;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.utility.pojo.HomePageStats;

public class HomePageStatsDao {

	private final Logger logger = LoggerFactory.getLogger(HomePageStatsDao.class);

	@Inject
	private SessionFactory sessionFactory;

	@SuppressWarnings("unchecked")
	public HomePageStats fetchPortalStats() {
		HomePageStats stats = new HomePageStats();
		Session session = sessionFactory.openSession();

		String obvQry = "select count(*) from observation  where is_deleted = false";
		String docQry = "select count(*) from document";
		String speciesQry = "SELECT count(*) FROM public.species where is_deleted = false";
		String discussionQry = "select count(*) from discussion where is_deleted= false";
		String actUserQry = "select count(*) from suser where account_expired = false and is_deleted = false and  account_locked = false and enabled = true";
		try {
			Query<Object> obvquery = session.createNativeQuery(obvQry);
			Query<Object> docQuery = session.createNativeQuery(docQry);
			Query<Object> speciesQuery = session.createNativeQuery(speciesQry);
			Query<Object> disQuery = session.createNativeQuery(discussionQry);
			Query<Object> actUserQuery = session.createNativeQuery(actUserQry);

			stats.setObservation(Long.parseLong(obvquery.getSingleResult().toString()));
			stats.setDocuments(Long.parseLong(docQuery.getSingleResult().toString()));
			stats.setSpecies(Long.parseLong(speciesQuery.getSingleResult().toString()));
			stats.setDiscussions(Long.parseLong(disQuery.getSingleResult().toString()));
			stats.setActiveUser(Long.parseLong(actUserQuery.getSingleResult().toString()));
			stats.setMaps(203L);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return stats;
	}

}
