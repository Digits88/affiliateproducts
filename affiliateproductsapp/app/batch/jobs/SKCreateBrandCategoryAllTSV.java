package batch.jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import constants.AffiliateConstants;
import play.Play;
import play.libs.F.Promise;
import repositories.Repository;
import utils.log.Log;

@Entity
@DiscriminatorValue("SYNC_PRODUCTS_DETAILS")
public class SKCreateBrandCategoryAllTSV extends AbstractBatchJob {

	private static Logger logger = Logger
			.getLogger(SKCreateBrandCategoryAllTSV.class);

	@Inject
	protected static Repository repository;

	@Transient
	public List<Promise> childJobs = null;

	@Transient
	public Boolean allChildJobsCompleted = false;

	@Override
	public void doJob() throws Exception {
		if (tryLock(this.getClass())) {
			runJob();
		}
	}

	private void runJob() {

		logger.info(Log.message("Free Memory	: " + Runtime.getRuntime().freeMemory()/(1024*1024) + " Mb"));
		logger.info(Log.message("Max Memory		: " + Runtime.getRuntime().maxMemory()/(1024*1024) + " Mb"));

		long startTime = System.currentTimeMillis();

		childJobs = new ArrayList<Promise>();

		try {
			File folder = new File(
					Play.configuration
							.getProperty("affiliate.cj.product.feed.input.location"));

			logger.info(Log.message("File Path: " + folder.getAbsolutePath()));

			if (!folder.exists()) {
				logger.error(Log
						.message("Exiting the process as no such folder exists : "
								+ folder.getAbsolutePath()));
			} else {

				File[] listOfSubfolder = folder.listFiles();

				for (File subFolder : listOfSubfolder) {
					Set<String> brands = new HashSet<String>();
					Set<String> categories = new HashSet<String>();

					logger.info(Log.message("Sub Folder: "
							+ subFolder.getName()));
					SKCreateBrandCategoryAllParent skCreateBrandCategoryAllParent = new SKCreateBrandCategoryAllParent(
							subFolder, brands, categories);
					Promise promise = skCreateBrandCategoryAllParent.now();
					childJobs.add(promise);
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

			}
		} catch (Exception e) {
			logger.error(Log
					.message("Exception occurred in SKCreateBrandCategoryAllTSV job : "
							+ e.getMessage()));
		} finally {
			unlock(this.getClass());
			logger.info(Log.message("!!! Finish Job !!!"));

			logger.info(Log.message("Total Time Used: "
					+ (System.currentTimeMillis() - startTime)));
		}

	}

}
