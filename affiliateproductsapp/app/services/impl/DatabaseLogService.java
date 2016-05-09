package services.impl;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.EntityManager;

import models.log.LogRequest;
import play.Play;
import play.db.jpa.JPA;
import play.jobs.Job;
import services.LogService;

import com.google.common.collect.Lists;

/**
 * This service stores log messages in the database.
 */
public class DatabaseLogService implements LogService {

	private final int logBatchSize = Integer.valueOf(Play.configuration.getProperty("affiliate.log.service.batch.size"));
	private final Boolean logEnable = Boolean.valueOf(Play.configuration.getProperty("affiliate.log.db.enable"));

	private volatile List<LogRequest> logRequests = Lists.newArrayListWithCapacity(logBatchSize);
	private final Lock logRequestsLock = new ReentrantLock();

	public DatabaseLogService() {
		new Job() {
			@Override
			public void doJob() {
				if(logEnable) {
					logRequestsLock.lock();
					try {
						if (logRequests.size() > 0) {
							new LogRequestJob().now();
						}
					} finally {
						logRequestsLock.unlock();
					}
				}
			}
		}.every(Play.configuration.getProperty("affiliate.log.service.job.delay"));
	}

	@Override
	public void logRequest(LogRequest logRequest) {
		if(logEnable) {
			logRequestsLock.lock();
			try {
				logRequests.add(logRequest);
				if (logRequests.size() >= logBatchSize) {
					new LogRequestJob().now();
				}
			} finally {
				logRequestsLock.unlock();
			}
		}
	}

	private class LogRequestJob extends Job {
		private final Iterable<LogRequest> logRequestsRef;
		private LogRequestJob() {
			logRequestsRef = logRequests;
			logRequests = Lists.newArrayListWithCapacity(logBatchSize);
		}
		@Override
		public void doJob() {
			EntityManager em = JPA.em();
			for (LogRequest logRequest : logRequestsRef) {
				em.persist(logRequest);
			}
			em.flush();
			em.clear();
		}
	}
}
