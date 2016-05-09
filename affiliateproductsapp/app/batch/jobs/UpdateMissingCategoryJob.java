package batch.jobs;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

import org.apache.log4j.Logger;

import constants.AffiliateConstants;
import models.Seller;
import play.db.jpa.JPA;
import play.libs.F.Promise;
import utils.log.Log;

public class UpdateMissingCategoryJob extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(UpdateMissingCategoryJob.class);

	@Transient
	public List<Promise> childJobs = new ArrayList<Promise>();

	@Transient
	public Boolean allChildJobsCompleted = false;

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		try {
			long startTime = System.currentTimeMillis();
			
			List<Seller> sellers = Seller.findAll();
			logger.info("Seller size : " + sellers.size());

			for (int i = 0; i < sellers.size(); i++) {
				Seller seller = sellers.get(i);
				/*
				 * List<Long> brandIDs = (List<Long>) JPA.em() .createQuery(
				 * "SELECT DISTINCT(p.brand.id) FROM Product p " +
				 * "WHERE p.seller.id=" + seller.getId() +
				 * " AND p.brand.id NOT IN " +
				 * "(SELECT b.id FROM Brand b WHERE b.id IN(SELECT DISTINCT(p1.brand.id) FROM Product p1 WHERE p1.seller.id="
				 * + seller.getId() + " AND p1.brand IS NOT null))")
				 * .getResultList();
				 */

				Promise promise = new UpdateMissingCategoryChild(seller, "Thread_" + i).now();
				childJobs.add(promise);
				logger.info("childJobs size : " + childJobs.size());

			}

			while (!allChildJobsCompleted) {
				logger.info("Waiting for each child job (Update category and brand) to complete...");
				allChildJobsCompleted = true;
				Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
				for (Promise p : childJobs) {
					allChildJobsCompleted = allChildJobsCompleted & p.isDone();
				}
			}

			logger.info("!!! Finish Job !!!");
			logger.info("Total Time Used: " + (System.currentTimeMillis() - startTime));
		} catch (Exception e) {
			logger.error(Log.message("Exception occurred in UpdateBrandCategoryAll job : " + e.getMessage()));
		}
	}
}
