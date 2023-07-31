package com.strandls.utility.dao;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.utility.pojo.HomePageStats;
import com.strandls.naksha.controller.LayerServiceApi;

public class HomePageStatsDao {

	private final Logger logger = LoggerFactory.getLogger(HomePageStatsDao.class);

	@Inject
	private SessionFactory sessionFactory;

	@Inject
	private LayerServiceApi layerServiceApi;

	@SuppressWarnings("unchecked")
	public HomePageStats fetchPortalStats() {
		HomePageStats stats = new HomePageStats();
		Session session = sessionFactory.openSession();

		String obvQry = "select count(*) from observation  where is_deleted = false";
		String docQry = "select count(*) from document";
		String speciesQry = "SELECT count(*) FROM public.species where is_deleted = false";

		String actUserQry = "select count(*) from suser where account_expired = false and is_deleted = false and  enabled = true";

		Long layerCount = 0L;
		try {
			// This is written in seprate try block so if somewhere naksha not setup it
			// should still work
			// and not break other code
			layerCount = Long.parseLong(layerServiceApi.getLayerCount().toString());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		try {
			Query<Object> obvquery = session.createNativeQuery(obvQry);
			Query<Object> docQuery = session.createNativeQuery(docQry);
			Query<Object> speciesQuery = session.createNativeQuery(speciesQry);
			Query<Object> actUserQuery = session.createNativeQuery(actUserQry);

			stats.setObservation(Long.parseLong(obvquery.getSingleResult().toString()));
			stats.setDocuments(Long.parseLong(docQuery.getSingleResult().toString()));
			stats.setSpecies(Long.parseLong(speciesQuery.getSingleResult().toString()));
			stats.setActiveUser(Long.parseLong(actUserQuery.getSingleResult().toString()));
			stats.setMaps(layerCount);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return stats;
	}

}
