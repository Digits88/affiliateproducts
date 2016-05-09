package batch.jobs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

import constants.AffiliateConstants;
import play.db.jpa.JPA;
import play.libs.F.Promise;
import repositories.Repository;
import utils.log.Log;

public class SyncRakutenSuperFeedUpdateOOSJob extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(SyncRakutenSuperFeedUpdateOOSJob.class);

	@Inject
	protected static Repository repository;

	@Transient
	private Long rakutenAdvertiserID;

	@Transient
	private List<Set<String>> productSKUList;

	public SyncRakutenSuperFeedUpdateOOSJob(Long rakutenAdvertiserID, List<Set<String>> productSKUList) {
		super();
		this.rakutenAdvertiserID = rakutenAdvertiserID;
		this.productSKUList = productSKUList;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {

		logger.info("++++++++++++++++++++++++++++");
		logger.info("Update Out-Of-Stock Products");
		logger.info("++++++++++++++++++++++++++++");

		if (rakutenAdvertiserID != null) {
			List<String> existingProductSkusInDB = (List<String>) JPA.em()
					.createQuery("SELECT sku FROM Product WHERE advertiserId=" + rakutenAdvertiserID).getResultList();

			// List<String> existingProductSkusInDB =
			// repository.createNamedQuery2("JPQL_GET_SKUS",
			// rakutenAdvertiserID);

			// How many SKU in input file
			Set<String> productSKUs = new HashSet<String>();
			for (Set<String> set : productSKUList) {
				productSKUs.addAll(set);
			}

			// Check how many sku in the input file.
			try {
				logger.info("SKU in file:	" + productSKUs.size());
				logger.info("SKU in DB: 	" + existingProductSkusInDB.size());

				if (existingProductSkusInDB != null && existingProductSkusInDB.size() > 0) {
					Boolean deletionChildJobComplete = false;
					List<Promise> deletionChildJobs = new ArrayList<Promise>();

					// Split skus from DB into multiple small files
					List<List<String>> subListSKUsInDB = Lists.partition(existingProductSkusInDB, 1000);

					for (int i = 0; i < subListSKUsInDB.size(); i++) {
						List<String> subList = subListSKUsInDB.get(i);
						Promise promise = new SyncRakutenProductsDetailsDeleteOutOfSync(rakutenAdvertiserID, subList,
								productSKUs, "Thread_" + i).now();
						deletionChildJobs.add(promise);
					}

					while (!deletionChildJobComplete) {
						logger.info(Log.message(
								"Waiting for each Deletion Child Job (seller product synchronisation) to complete..."));
						deletionChildJobComplete = true;
						Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
						for (Promise promise : deletionChildJobs) {
							deletionChildJobComplete = deletionChildJobComplete & promise.isDone();
						}
					}
				}
			} catch (Exception e) {
				logger.error(Log.message(e.getMessage()));
				e.printStackTrace();
			}
			logger.info("Sync Out Of Stock Products is finished !!");
		}
		logger.info(Log.message(
				"SyncRakutenProductsDetails : Successfully completed Sync Out Of Stock Products for the seller -- "
						+ rakutenAdvertiserID));
	}
}
