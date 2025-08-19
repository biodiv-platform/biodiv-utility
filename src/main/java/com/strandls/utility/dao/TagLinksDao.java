/** */
package com.strandls.utility.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.utility.pojo.TagLinks;
import com.strandls.utility.util.AbstractDAO;

import jakarta.inject.Inject;

/**
 * @author Abhishek Rudra
 */
public class TagLinksDao extends AbstractDAO<TagLinks, Long> {

	private final Logger logger = LoggerFactory.getLogger(TagLinksDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected TagLinksDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public TagLinks findById(Long id) {
		Session session = sessionFactory.openSession();
		TagLinks entity = null;
		try {
			entity = session.get(TagLinks.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public List<TagLinks> findObjectTags(String objectType, Long id) {
		List<TagLinks> result = new ArrayList<>();
		Session session = sessionFactory.openSession();
		String qry = "from TagLinks where type = :type and tagRefer = :id";
		try {
			Query<TagLinks> query = session.createQuery(qry);
			query.setParameter("type", objectType);
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
	public TagLinks checkIfTagsLinked(String objectType, Long objectId, Long tagId) {
		String qry = "from TagLinks where type = :type and tagRefer = :objectId and tagId = :tagId";
		Session session = sessionFactory.openSession();
		TagLinks result = null;
		try {
			Query<TagLinks> query = session.createQuery(qry);
			query.setParameter("type", objectType);
			query.setParameter("objectId", objectId);
			query.setParameter("tagId", tagId);
			result = query.getSingleResult();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<TagLinks> findResourceTags(List<String> objectTypes, List<Long> id, List<String> tagRefIds) {
		List<TagLinks> result = new ArrayList<>();
		Session session = sessionFactory.openSession();

		String qry = "FROM TagLinks WHERE tagId IN :id";

		if (objectTypes != null && !objectTypes.isEmpty() && !objectTypes.contains("all")) {
			qry += " AND type IN :types";
		}

		if (tagRefIds != null && !tagRefIds.isEmpty()) {
			qry += " AND tagRefer IN :tagRefIds";
		}

		try {
			Query<TagLinks> query = session.createQuery(qry);
			query.setParameter("id", id);

			if (objectTypes != null && !objectTypes.isEmpty() && !objectTypes.contains("all")) {
				query.setParameterList("types", objectTypes);
			}

			if (tagRefIds != null && !tagRefIds.isEmpty()) {
				query.setParameterList("tagRefIds", tagRefIds);
			}

			result = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return result;
	}
}
