package batch.jobs;

import java.util.Date;

import javax.inject.Inject;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.log4j.Logger;

import play.db.jpa.JPA;
import play.jobs.Job;
import repositories.Repository;

@Entity
@Table(name = "AFFILIATE_BATCH_JOBS")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "job_name", length = 64)
public abstract class AbstractBatchJob extends Job {

	private static Logger logger = Logger.getLogger(AbstractBatchJob.class);

	@Inject
	protected static Repository repository;

	@Id
	private Long id;

	@Column(name = "is_enabled", nullable = false)
	private boolean isEnabled;

	@Column(name = "is_running", nullable = false)
	private boolean isRunning;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_run")
	private Date lastRun;

	@Column(name = "last_elapsed_time")
	private Long lastElapsedTime;

	@Column(name = "job_name", insertable = false, updatable = false)
	private String jobName;

	void begin(EntityTransaction tx) {
		if (!tx.isActive())
			tx.begin();
	}

	void commit(EntityTransaction tx) {
		if (tx.isActive())
			tx.commit();
	}

	void rollback(EntityTransaction tx) {
		if (tx.isActive())
			tx.rollback();
	}

	public Long getId() {
		return id;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public Date getLastRun() {
		return lastRun;
	}

	public Long getLastElapsedTime() {
		return lastElapsedTime;
	}

	public void setRunning(boolean running) {
		isRunning = running;
	}

	public void setLastRun(Date lastRun) {
		this.lastRun = lastRun;
	}

	public void setLastElapsedTime(Long lastElapsedTime) {
		this.lastElapsedTime = lastElapsedTime;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	/**
	 * "Lock" means to set isRunning flag to true which will prevent all other
	 * active threads from executing this job.
	 */
	protected boolean tryLock(Class<? extends AbstractBatchJob> jobClass) {
		EntityManager em = JPA.newEntityManager();
		EntityTransaction tx = em.getTransaction();
		Query query = em.createQuery("update " + jobClass.getSimpleName()
				+ " set isRunning = true, lastRun = current_timestamp"
				+ " where isEnabled = true and isRunning = false");
		int updatedRows = 0;
		try {
			begin(tx);
			updatedRows = query.executeUpdate();
			commit(tx);
		} catch (Exception e) {
			logger.error("JobsManager.tryLock", e);
			rollback(tx);
		} finally {
			em.close();
		}
		return updatedRows > 0;
	}

	/**
	 * "Unlock" means to set isRunning flag to false which will allow all other
	 * active threads to execute this job.
	 */
	protected void unlock(Class<? extends AbstractBatchJob> jobClass) {
		EntityManager em = JPA.newEntityManager();
		EntityTransaction tx = em.getTransaction();
		Query query = em
				.createQuery("update "
						+ jobClass.getSimpleName()
						+ " set isRunning = false, lastElapsedTime = CURRENT_TIMESTAMP - lastRun"
						+ " where isEnabled = true and isRunning = true");
		try {
			begin(tx);
			query.executeUpdate();
			commit(tx);
		} catch (Exception e) {
			logger.error("JobsManager.unlock", e);
			rollback(tx);
		} finally {
			em.close();
		}
	}

	protected AbstractBatchJob getJob() {
		String batchJobName = "'"
				+ this.getClass().getAnnotation(DiscriminatorValue.class)
						.value() + "'";
		String query = "from " + this.getClass().getSimpleName()
				+ " where jobName= " + batchJobName;
		AbstractBatchJob job = (AbstractBatchJob) repository.select(query);
		return job;
	}
}
