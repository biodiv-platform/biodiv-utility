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
import com.strandls.utility.pojo.GallerySlider;
import com.strandls.utility.pojo.MiniGallerySlider;
import com.strandls.utility.util.AbstractDAO;

/**
 * @author Mekala Rishitha Ravi
 *
 */
public class MiniGallerySliderDao extends AbstractDAO<MiniGallerySlider, Long> {

	private final Logger logger = LoggerFactory.getLogger(MiniGallerySliderDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected MiniGallerySliderDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public MiniGallerySlider findById(Long id) {
		MiniGallerySlider result = null;
		Session session = sessionFactory.openSession();
		try {
			result = session.get(MiniGallerySlider.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<MiniGallerySlider> getAllGallerySliderInfo(boolean isAdminList, Long galleryId) {
		List<MiniGallerySlider> result = null;
		String qry = isAdminList ?
				"from  MiniGallerySlider where galleryId = :galleryId order by display_order asc" :
				"from  MiniGallerySlider where galleryId = :galleryId and is_truncated is true order by display_order asc";
		Session session = sessionFactory.openSession();
		try {
			Query<MiniGallerySlider> query = session.createQuery(qry);
			query.setParameter("galleryId", galleryId);
			result = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<MiniGallerySlider> findBySliderId(Long sId) {
		String qry = "from MiniGallerySlider where sliderId = :sId";
		Session session = sessionFactory.openSession();
		List<MiniGallerySlider> result = null;
		try {
			Query<MiniGallerySlider> query = session.createQuery(qry);
			query.setParameter("sId", sId);
			result = query.getResultList();

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;

	}

}
