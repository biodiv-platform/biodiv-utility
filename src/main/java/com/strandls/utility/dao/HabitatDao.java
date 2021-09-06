/**
 * 
 */
package com.strandls.utility.dao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.utility.pojo.Habitat;
import com.strandls.utility.util.AbstractDAO;

/**
 * @author Abhishek Rudra
 *
 */
public class HabitatDao extends AbstractDAO<Habitat, Long> {

	private final Logger logger = LoggerFactory.getLogger(HabitatDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected HabitatDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public Habitat findById(Long id) {
		Habitat result = null;
		Session session = sessionFactory.openSession();
		try {
			result = session.get(Habitat.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Habitat> findAllHabitat() {
		String qry = "from Habitat order by habitatOrder";
		List<Habitat> result = new ArrayList<>();
		Session session = sessionFactory.openSession();
		try {
			Query<Habitat> query = session.createQuery(qry);
			result = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;

	}

}
