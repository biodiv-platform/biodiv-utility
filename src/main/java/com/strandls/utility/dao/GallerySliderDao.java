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
import com.strandls.utility.util.AbstractDAO;

/**
 * @author Abhishek Rudra
 *
 */
public class GallerySliderDao extends AbstractDAO<GallerySlider, Long> {

	private final Logger logger = LoggerFactory.getLogger(GallerySliderDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected GallerySliderDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public GallerySlider findById(Long id) {
		GallerySlider result = null;
		Session session = sessionFactory.openSession();
		try {
			result = session.get(GallerySlider.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<GallerySlider> getAllGallerySliderInfo() {
		List<GallerySlider> result = null;
		String qry = "from  GallerySlider where ugId is NULL order by id asc";
		Session session = sessionFactory.openSession();
		try {
			Query<GallerySlider> query = session.createQuery(qry);
			result = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

}
