package batch.jobs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Transient;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

import constants.AffiliateConstants;
import play.db.jpa.JPA;
import play.libs.F.Promise;
import utils.log.Log;

public class SyncCJSuperProductUpdateOutOfStockJob extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(SyncCJSuperProductUpdateOutOfStockJob.class);

	@Transient
	private List<Set<String>> productSKUList = null;

	@Transient
	private Long cjAdvertiserID = null;

	public SyncCJSuperProductUpdateOutOfStockJob(List<Set<String>> productSKUList, Long cjAdvertiserID) {
		super();
		this.productSKUList = productSKUList;
		this.cjAdvertiserID = cjAdvertiserID;
	}

	@Override
	public void doJob() throws Exception {
		logger.info("========== READY TO START UPDATE OUT OF STOCK JOB ==========");
		runJob();
		logger.info("========== !!! FINISH UPDATE OUT OF STOCK JOB !!!  ==========");
	}

	private void runJob() {
		logger.info("+++++ Update out of Stock function -- Start +++++");

		if (cjAdvertiserID != null) {
			List<String> existingProductSkusInDB = (List<String>) JPA.em()
					.createQuery("SELECT sku FROM Product where advertiserId=" + cjAdvertiserID).getResultList();

			if (existingProductSkusInDB != null && existingProductSkusInDB.size() > 0) {

				// How many SKU in input file
				Set<String> productSKUs = new HashSet<String>();
				for (Set<String> set : productSKUList) {
					productSKUs.addAll(set);
				}

				// Check how many sku in the input file.
				try {
					Thread.sleep(5000);
					logger.info("SKU in file:	" + productSKUs.size());
					logger.info("SKU in DB: 	" + existingProductSkusInDB.size());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (existingProductSkusInDB != null && existingProductSkusInDB.size() > 0) {

					Boolean deletionChildJobComplete = false;
					List<Promise> deletionChildJobs = new ArrayList<Promise>();

					// Split skus from DB into multiple small files
					List<List<String>> subListSKUsInDB = Lists.partition(existingProductSkusInDB, 10000);
					try {
						for (List<String> subList : subListSKUsInDB) {
							Promise promise = new SyncProductsDetailTSVChildDeletion(cjAdvertiserID, subList,
									productSKUs).now();

							deletionChildJobs.add(promise);
						}

						while (!deletionChildJobComplete) {
							logger.info(
									"Waiting for each Deletion Child Job (seller product synchronisation) to complete...");

							deletionChildJobComplete = true;
							Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
							for (Promise promise : deletionChildJobs) {
								deletionChildJobComplete = deletionChildJobComplete & promise.isDone();
							}
						}
					} catch (Exception e) {
						logger.error(Log
								.message("Exception occurred during update out of stock function : " + e.getMessage()));
						e.printStackTrace();
					}
					logger.info("+++++ Update out of Stock function -- Finish +++++");
				}
				logger.info("Successfully completed deleting the products that are out of sync for the seller : " + cjAdvertiserID);
			}
		} else {
			logger.error(Log.message("Invalid CJ Advertiser ID"));
		}
	}
}
