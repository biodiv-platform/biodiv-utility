package com.strandls.utility.dao;


import java.util.List;


import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.google.inject.Inject;
import com.strandls.utility.pojo.GalleryConfig;
import com.strandls.utility.util.AbstractDAO;


public class GalleryConfigDao extends AbstractDAO<GalleryConfig, Long> {
   private final Logger logger = LoggerFactory.getLogger(GalleryConfig.class);


   @Inject
   protected GalleryConfigDao(SessionFactory sessionFactory) {
       super(sessionFactory);
   }


   @Override
   public GalleryConfig findById(Long id) {
       GalleryConfig result = null;
       Session session = sessionFactory.openSession();
       try {
           result = session.get(GalleryConfig.class, id);
       } catch (Exception e) {
           logger.error(e.getMessage());
       } finally {
           session.close();
       }
       return result;


   }
  
   @SuppressWarnings("unchecked")
   public List<GalleryConfig> getAllMiniSlider(boolean isAdminList) {
	   List<GalleryConfig> result = null;
       String qry = isAdminList ?
               "from  GalleryConfig order by id asc" :
               "from  GalleryConfig where is_active is true order by id asc";
       Session session = sessionFactory.openSession();
       try {
           Query<GalleryConfig> query = session.createQuery(qry);
           result = query.getResultList();
       } catch (Exception e) {
           logger.error(e.getMessage());
       } finally {
           session.close();
       }
       return result;
   }


}
