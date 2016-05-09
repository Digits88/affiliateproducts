package batch.jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import models.Brand;
import models.AdvertiserCategory;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import constants.AffiliateConstants;
import play.Play;
import play.libs.F.Promise;
import repositories.Repository;
import services.rakuten.RakutenFileService;
import utils.log.Log;

@Entity
@DiscriminatorValue("SYNC_PRODUCTS_DETAILS")
public class RakutenCreateBrandCategoryAll extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(RakutenCreateBrandCategoryAll.class);

	@Inject
	protected static Repository repository;

	@Transient
	public List<Promise> childJobs = null;

	@Transient
	public Boolean allChildJobsCompleted = false;

	@Override
	public void doJob() throws Exception {
		logger.info("========== READY TO START JOB ==========");
		runJob();
		logger.info("========== !!! GREAT JOB !!!  ==========");
	}

	private void runJob() {

		logger.info(Log.message("Free Memory	: " + Runtime.getRuntime().freeMemory() / (1024 * 1024) + " Mb"));
		logger.info(Log.message(Runtime.getRuntime().maxMemory()));

		long startTime = System.currentTimeMillis();

		childJobs = new ArrayList<Promise>();
		
		// Create a map for parse the color attributes for JCPenny (788)
		Map<String, String> colorSKUMap = new Hashtable<String, String>();
		RakutenFileService rfs = new RakutenFileService(colorSKUMap);
		File folder = new File(Play.configuration.getProperty("affiliate.cj.product.feed.input.location"));

		try {

			logger.info(Log.message("File Path: " + folder.getAbsolutePath()));


			rfs.cleanFiles(folder);
			rfs.downloadRakutenFeed();

			if (!folder.exists()) {
				logger.error(Log.message("Exiting the process as no such folder exists : " + folder.getAbsolutePath()));
			} else {

				File[] listOfFolders = folder.listFiles();
				
				for (File subFolder : listOfFolders) {

					File[] listOfFiles = subFolder.listFiles();
					
					Set<String> brands = new HashSet<String>();
					Set<String> categories = new HashSet<String>();

					for (int i = 0; i < listOfFiles.length; i++) {
						File file = listOfFiles[i];
						if (isValidFileToParse(file)) {
							Promise promise = invokeRakutenProductSynchroniser(file, brands, categories);
							childJobs.add(promise);
							logger.info(Log.message("Child Jobs' Number is " + childJobs.size()));
						} else {
							logger.error(Log.message("Skipping the file : " + file.getName()
									+ " as it is not a valid file to parse!!!"));
						}
					}
					while (!allChildJobsCompleted) {
						logger.info(Log
								.message("Waiting for each child job (seller product synchronisation) to complete..."));
						allChildJobsCompleted = true;
						Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
						for (Promise promise : childJobs) {
							allChildJobsCompleted = allChildJobsCompleted & promise.isDone();
						}
					}

					updateRakutenBrands(brands);
					updateRakutenCategories(categories);

					logger.info(Log
							.message("All the jobs (to create the brand and category) are completed sequentially..."));
				}
			}

		} catch (Exception e) {
			logger.error(Log.message("Issue in RakutenCreateBrandCategoryAll runJob : " + e.getMessage()));
		} finally {
			logger.info(Log.message("!!! Finish Job !!!"));
			logger.info(Log.message("Total Time Used: " + (System.currentTimeMillis() - startTime)));
			rfs.cleanFiles(folder);
		}

	}

	private void updateRakutenBrands(Set<String> brands) {
		if (brands == null) {
			logger.error(Log.message("Brands set is not available!!!"));
			return;
		}
		if (brands.size() == 0) {
			logger.info(Log.message("Brands set is Empty!!!"));
			return;
		}
		try {
			List<Promise> jobs = new ArrayList<Promise>();
			boolean allJobsCompleted = false;
			List<String> brandList = new ArrayList<String>();
			brandList.addAll(brands);
			List<List<String>> brandSubLists = Lists.partition(brandList, 1000);
			for (List<String> bList : brandSubLists) {
				Promise promise = new RakutenCreateBrandAllHelper(bList).now();
				jobs.add(promise);
			}
			while (!allJobsCompleted) {
				logger.info(Log.message("Waiting for each child job (seller product synchronisation) to complete..."));
				allJobsCompleted = true;
				Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
				for (Promise promise : jobs) {
					allJobsCompleted = allJobsCompleted & promise.isDone();
				}
			}
		} catch (Exception e) {
			logger.error(Log.message("Issue in updateRakutenBrands : " + e.getMessage()));
		}
	}

	private void updateRakutenCategories(Set<String> categories) {
		if (categories == null) {
			logger.error(Log.message("Categories set is not available!!!"));
			return;
		}
		if (categories.size() == 0) {
			logger.info(Log.message("Categories set is Empty!!!"));
			return;
		}
		try {
			List<Promise> jobs = new ArrayList<Promise>();
			boolean allJobsCompleted = false;
			List<String> categoryList = new ArrayList<String>();
			categoryList.addAll(categories);
			List<List<String>> categorySubList = Lists.partition(categoryList, 1000);
			for (List<String> cList : categorySubList) {
				Promise promise = new RakutenCreateCategoryAllHelper(cList).now();
				jobs.add(promise);
			}
			while (!allJobsCompleted) {
				logger.info(Log.message("Waiting for each child job (seller product synchronisation) to complete..."));
				allJobsCompleted = true;
				Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
				for (Promise promise : jobs) {
					allJobsCompleted = allJobsCompleted & promise.isDone();
				}
			}
		} catch (Exception e) {
			logger.error(Log.message("Issue in updateRakutenCategories : " + e.getMessage()));
		}
	}

	private Boolean isValidFileToParse(File inputFile) {
		String fileExtension = "xml";
		if (inputFile.isFile()) {
			if (Files.getFileExtension(inputFile.getAbsolutePath().toString()).equals(fileExtension)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private Promise invokeRakutenProductSynchroniser(File file, Set<String> brands, Set<String> categories) {
		Promise promise = null;
		logger.info(Log.message(
				"RakutenCreateBrandCategoryAll invokeRakutenProductSynchroniser : Invoking the product Rakuten synchroniser for the seller --"));
		RakutenCreateBrandCategoryAllChild rakutenCreateBrandCategoryAllChild = new RakutenCreateBrandCategoryAllChild(
				file, brands, categories);
		promise = rakutenCreateBrandCategoryAllChild.now();
		return promise;
	}
}
