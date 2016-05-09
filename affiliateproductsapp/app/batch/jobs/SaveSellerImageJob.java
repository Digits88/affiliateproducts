package batch.jobs;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import constants.AffiliateConstants;
import models.Seller;
import play.Play;
import play.libs.F.Promise;
import repositories.Repository;

@Entity
@DiscriminatorValue("SYNC_PRODUCTS_DETAILS")
public class SaveSellerImageJob extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(SaveSellerImageJob.class);

	@Transient
	private List<Promise> childJobs = null;

	@Transient
	private Boolean allChildJobsCompleted = false;

	@Override
	public void doJob() throws Exception {
		if (tryLock(this.getClass())) {
			runJob();
		}
	}

	private void runJob() {
		logger.info("| | | | | START | | | | |");
		logger.info("V V V V V V V V V V V V V");
		logger.info(Runtime.getRuntime().freeMemory());
		logger.info(Runtime.getRuntime().maxMemory());
		childJobs = new ArrayList<Promise>();

		try {
			String host = Play.configuration.getProperty("affiliate.batch.server.hostname");
			String serverHost = InetAddress.getLocalHost().getHostName();
			logger.info("+++ Host Name : " + serverHost + " +++");

			if (!host.equals(serverHost)) {
				logger.info("#########################################################");
				logger.info("### This job can be executed only in the Batch Server ###");
				logger.info("################### See you next time ###################");
				logger.info("#########################################################");
			} else {
				logger.info("++++++++++++++++++++++++ HOORAY ++++++++++++++++++++++++");
				logger.info("++++++ THE PROGRAM IS RUNNING ON THE RIGHT SERVER ++++++");
				logger.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

				long startTime = System.currentTimeMillis();

				List<Seller> sellers = Seller.findAll();
				for (Seller seller : sellers) {
					Promise promise = new SaveSellerImageJobChild(seller).now();
					childJobs.add(promise);
					logger.info("Child Jobs' Number is " + childJobs.size() + " ---> " + seller.getName());
				}

				while (!allChildJobsCompleted) {
					logger.info("Waiting for each child job to complete...");
					allChildJobsCompleted = true;
					Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
					for (Promise promise : childJobs) {
						allChildJobsCompleted = allChildJobsCompleted & promise.isDone();
					}
				}
				logger.info("!!! Finish Job !!!");
				logger.info("Total Time Used: " + (System.currentTimeMillis() - startTime));
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			unlock(this.getClass());
			System.gc();
		}
	}
}
