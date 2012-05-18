package edu.emory.cci.aiw.cvrg.eureka.etl.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.emory.cci.aiw.cvrg.eureka.common.comm.JobFilter;
import edu.emory.cci.aiw.cvrg.eureka.common.dao.GenericDao;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.Job;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.JobEvent_;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.Job_;

/**
 * Implements the {@link JobDao} interface, with the use of JPA entity managers.
 *
 * @author gmilton
 * @author hrathod
 *
 */
public class JpaJobDao extends GenericDao<Job, Long> implements JobDao {

	/**
	 * The class level logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(JpaJobDao.class);

	/**
	 * Construct instance with the given EntityManager provider.
	 *
	 * @param inEMProvider The entity manager provider.
	 */
	@Inject
	public JpaJobDao(final Provider<EntityManager> inEMProvider) {
		super(Job.class, inEMProvider);
	}

	@Override
	public List<Job> get(final JobFilter jobFilter) {
		EntityManager entityManager = this.getEntityManager();
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Job> query = builder.createQuery(Job.class);
		Root<Job> root = query.from(Job.class);
		List<Predicate> predicates = new ArrayList<Predicate>();

		LOGGER.debug("Checking for job ID.");
		if (jobFilter.getJobId() != null) {
			LOGGER.debug("Found job ID: {}", jobFilter.getJobId());
			predicates.add(builder.equal(root.get(Job_.id),
					jobFilter.getJobId()));
		}
		LOGGER.debug("Checking for user ID.");
		if (jobFilter.getUserId() != null) {
			LOGGER.debug("Found user ID: {}", jobFilter.getUserId());
			predicates.add(builder.equal(root.get(Job_.userId),
					jobFilter.getUserId()));
		}
		LOGGER.debug("Checking for start time.");
		if (jobFilter.getFrom() != null) {
			LOGGER.debug("Found start time: {}", jobFilter.getFrom());
			predicates.add(builder.greaterThanOrEqualTo(
					root.<Date>get(Job_.timestamp), jobFilter.getFrom()));
		}
		LOGGER.debug("Checking for end time.");
		if (jobFilter.getTo() != null) {
			LOGGER.debug("Found end time: {}", jobFilter.getTo());
			predicates.add(builder.lessThanOrEqualTo(
					root.<Date>get(Job_.timestamp), jobFilter.getTo()));
		}
		LOGGER.debug("Checking for state.");
		if (jobFilter.getState() != null) {
			LOGGER.debug("Found state: {}", jobFilter.getState());
			predicates.add(builder.equal(
					root.join(Job_.jobEvents).get(JobEvent_.state),
					jobFilter.getState()));
		}

		LOGGER.debug("{} predicates found from filter", predicates.size());
		Predicate[] predicatesArray = new Predicate[predicates.size()];
		predicates.toArray(predicatesArray);
		query.where(predicatesArray);
		LOGGER.debug("Creating typed query.");
		TypedQuery<Job> typedQuery = entityManager.createQuery(query);
		LOGGER.debug("Returning results.");
		return typedQuery.getResultList();
	}
}
