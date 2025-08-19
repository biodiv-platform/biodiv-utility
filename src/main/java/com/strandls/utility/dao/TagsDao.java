/** */
package com.strandls.utility.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.utility.pojo.Tags;
import com.strandls.utility.util.AbstractDAO;

import jakarta.inject.Inject;

/**
 * @author Abhishek Rudra
 */
public class TagsDao extends AbstractDAO<Tags, Long> {

	private final Logger logger = LoggerFactory.getLogger(TagsDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected TagsDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public Tags findById(Long id) {
		Session session = sessionFactory.openSession();
		Tags entity = null;
		try {
			entity = session.get(Tags.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public Tags fetchByName(String phrase) {
		Session session = sessionFactory.openSession();
		Tags tags = null;
		Object[] result = null;
		String qry = "SELECT id,strip_tags(name) FROM public.tags where strip_tags(name) ='" + phrase + "'";
		try {
			Query<Object[]> query = session.createNativeQuery(qry);
			result = query.getSingleResult();
			tags = new Tags(Long.parseLong(result[0].toString()), result[1].toString());
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return tags;
	}

	@SuppressWarnings("unchecked")
	public List<Tags> fetchNameByLike(String phrase) {
		Session session = sessionFactory.openSession();
		List<Tags> tagsList = new ArrayList<>();
		List<Object[]> result = null;

		String qry = "SELECT id, strip_tags(name) FROM public.tags where name "
				+ "like phrase order by char_length(name) asc limit 10";

		try {
			qry = qry.replace("phrase", "'" + phrase + "%'");
			Query<Object[]> query = session.createNativeQuery(qry);
			result = query.getResultList();

			for (Object[] obj : result) {
				tagsList.add(new Tags(Long.parseLong(obj[0].toString()), obj[1].toString()));
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return tagsList;
	}

	@SuppressWarnings("unchecked")
	public List<Tags> fetchTag(List<String> phrase) {
		Session session = sessionFactory.openSession();
		List<Tags> tagsList = new ArrayList<>();

		String qry = "SELECT id, name FROM public.tags where name IN :phrase";

		try {
			Query<Object[]> query = session.createNativeQuery(qry);
			query.setParameter("phrase", phrase);
			List<Object[]> results = query.getResultList();

			for (Object[] obj : results) {
				long id = Long.parseLong(obj[0].toString());
				String name = obj[1].toString();
				Tags tags = new Tags(id, name);
				tagsList.add(tags);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return tagsList;
	}
}
