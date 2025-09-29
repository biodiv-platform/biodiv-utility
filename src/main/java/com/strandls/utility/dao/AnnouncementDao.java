/**
 * 
 */
package com.strandls.utility.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.strandls.utility.pojo.Announcement;
import com.strandls.utility.util.AbstractDAO;

/**
 * @author Mekala Rishitha Ravi
 *
 */
public class AnnouncementDao extends AbstractDAO<Announcement, Long> {

	private final Logger logger = LoggerFactory.getLogger(MiniGallerySliderDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected AnnouncementDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public Announcement findById(Long id) {
		Announcement result = null;
		Session session = sessionFactory.openSession();
		try {
			result = session.get(Announcement.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public Announcement getActiveAnnouncemntInfo() {
		Announcement result = null;
		String qry = "from  Announcement where enabled is true";
		Session session = sessionFactory.openSession();
		try {
			Query<Announcement> query = session.createQuery(qry);
			result = query.getResultList().get(0);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<Announcement> findByAnnouncemntId(Long aId) {
		String qry = "from Announcement where announcementId = :aId";
		Session session = sessionFactory.openSession();
		List<Announcement> result = null;
		try {
			Query<Announcement> query = session.createQuery(qry);
			query.setParameter("aId", aId);
			result = query.getResultList();

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;

	}

}