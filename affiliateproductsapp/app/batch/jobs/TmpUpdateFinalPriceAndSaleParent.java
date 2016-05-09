package batch.jobs;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import play.libs.F.Promise;
import repositories.Repository;
import utils.log.Log;
import constants.AffiliateConstants;

@Entity
@DiscriminatorValue("SYNC_PRODUCTS_DETAILS")
public class TmpUpdateFinalPriceAndSaleParent extends AbstractBatchJob {

	private static Logger logger = Logger
			.getLogger(TmpUpdateFinalPriceAndSaleParent.class);

	@Inject
	protected static Repository repository;

	@Transient
	public Boolean allChildJobsCompleted = false;

	@Transient
	public List<Promise> childJobs = null;

	@Transient
	List<Long> sellerIds = null;

	public TmpUpdateFinalPriceAndSaleParent(List<Long> ids) {
		sellerIds = ids;
	}

	@Override
	public void doJob() throws Exception {
		if (tryLock(this.getClass())) {
			runJob();
		}
	}

	private void runJob() {
		childJobs = new ArrayList<Promise>();
		try {
			for (Long sellerId : sellerIds) {
				Promise promise = new TmpUpdateFinalPriceAndSaleChild(sellerId)
						.now();
				childJobs.add(promise);
			}
			while (!allChildJobsCompleted) {
				logger.info(Log
						.message("Waiting for each child job (price update) to complete..."));
				allChildJobsCompleted = true;
				Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
				for (Promise promise : childJobs) {
					allChildJobsCompleted = allChildJobsCompleted
							& promise.isDone();
				}
			}
			logger.info(Log
					.message("Successfully completed the job (price update)..."));
			
		} catch (Exception e) {
			logger.error(Log
					.message("Exception occurred in TmpUpdateFinalPriceAndSaleParent job : "
							+ e.getMessage()));
		} finally {
			unlock(this.getClass());
		}
	}

}