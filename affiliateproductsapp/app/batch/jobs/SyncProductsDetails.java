package batch.jobs;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import models.Seller;

import org.apache.log4j.Logger;

import play.libs.F.Promise;
import repositories.Repository;
import utils.log.Log;
import batch.jobs.product.synchroniser.ProductSynchroniser;
import batch.jobs.product.synchroniser.ProductSynchroniserFactory;
import constants.AffiliateConstants;

/**
 * 
 * Class to synchronize the product details for all the sellers from CJ to
 * affiliateproducts DB
 * 
 */
@Entity
@DiscriminatorValue("SYNC_PRODUCTS_DETAILS")
// @play.jobs.On("cron.job.sync.products.frequency")
public class SyncProductsDetails extends AbstractBatchJob {

	@Transient
	public List<Long> sellerIds = null;

	@Transient
	public List<Promise> childJobs = null;

	@Transient
	public Boolean allChildJobsCompleted = false;

	private static Logger logger = Logger.getLogger(SyncProductsDetails.class);

	@Inject
	protected static Repository repository;

	@Override
	public void doJob() throws Exception {
		if (tryLock(this.getClass())) {
			runJob();
		}
	}

	private void runJob() {
		childJobs = new ArrayList<Promise>();
		try {
			if (sellerIds == null) {
				List<Seller> sellers = Seller.findAll();
				for (Seller seller : sellers) {
					Promise promise = invokeProductSynchroniser(seller);
					childJobs.add(promise);
				}
			} else {
				for (Long sellerId : sellerIds) {
					Seller seller = Seller.findById(sellerId);
					Promise promise = invokeProductSynchroniser(seller);
					childJobs.add(promise);
				}
			}

			while (!allChildJobsCompleted) {
				logger.info(Log
						.message("Waiting for each child job (seller product synchronisation) to complete..."));
				allChildJobsCompleted = true;
				Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
				for (Promise promise : childJobs) {
					allChildJobsCompleted = allChildJobsCompleted
							& promise.isDone();
				}
			}

			logger.info(Log
					.message("All the child jobs (seller product synchronisation) are completed..."));

		} catch (Exception e) {
			logger.error(Log
					.message("Exception occurred in SyncProductsDetails job : "
							+ e.getMessage()));
			e.printStackTrace();
		} finally {
			unlock(this.getClass());
		}
	}

	private Promise invokeProductSynchroniser(Seller seller) {
		Promise promise = null;
		logger.info(Log
				.message("Invoking the product synchroniser for the seller : "
						+ seller.getName()));
		ProductSynchroniser productSynchroniser = ProductSynchroniserFactory
				.getProductSyncrhoniser(seller.getName(),
						seller.getAdvertiserId(), null, false, null);
		
		// 10 14 -- Home Depot
		/*ProductSynchroniser productSynchroniser = ProductSynchroniserFactory
				.getProductSyncrhoniser(seller.getName(),
						seller.getAdvertiserId(), null, false);*/
		
		promise = productSynchroniser.now();
		return promise;
	}

}