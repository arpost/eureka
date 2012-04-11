package edu.emory.cci.aiw.cvrg.eureka.etl.dao;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import edu.emory.cci.aiw.cvrg.eureka.common.entity.Configuration;

/**
 * Implementation of the {@link ConfDao} interface, based on JPA.
 * 
 * @author hrathod
 * 
 */
public class JpaConfDao implements ConfDao {

	/**
	 * The class level logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(JpaConfDao.class);
	/**
	 * The entity manager used to interact with the data store.
	 */
	private final EntityManager entityManager;

	/**
	 * Creates an object using the given entity manager.
	 * 
	 * @param manager The entity manager to use for interaction with the data
	 *            store.
	 */
	@Inject
	public JpaConfDao(EntityManager manager) {
		this.entityManager = manager;
	}

	@Override
	@Transactional
	public void save(Configuration conf) {
		this.entityManager.persist(conf);
	}

	@Override
	public Configuration get(Long userId) {
		Configuration configuration;
		try {
			Query query = this.entityManager.createQuery(
					"select c from Configuration c where c.userId = ?1",
					Configuration.class).setParameter(1, userId);
			configuration = (Configuration) query.getSingleResult();
		} catch (NoResultException nre) {
			LOGGER.error(nre.getMessage(), nre);
			configuration = null;
		} catch (NonUniqueResultException nure) {
			LOGGER.error(nure.getMessage(), nure);
			configuration = null;
		}
		return configuration;
	}
}
