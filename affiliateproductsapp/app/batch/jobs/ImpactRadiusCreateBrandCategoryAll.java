package batch.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import services.impactradius.ImpactRadiusFileService;
import utils.log.Log;

@Entity
@DiscriminatorValue("SYNC_PRODUCTS_DETAILS")
public class ImpactRadiusCreateBrandCategoryAll extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(ImpactRadiusCreateBrandCategoryAll.class);

	@Inject
	protected static Repository repository;

	@Transient
	public List<Promise> childJobs = null;

	@Transient
	public Boolean allChildJobsCompleted = false;

	@Transient
	public Map<String, String> impactRadiusProductMap = null;

	@Transient
	private ImpactRadiusFileService impactRadiusFileService;

	@Override
	public void doJob() throws Exception {
		if (tryLock(this.getClass())) {
			runJob();
		}
	}

	private void runJob() {

		System.gc();
		logger.info(Log.message("Free	Memory	: " + Runtime.getRuntime().freeMemory() / (1024 * 1024) + " Mb"));
		logger.info(Log.message("total	Memory	: " + Runtime.getRuntime().totalMemory() / (1024 * 1024) + " Mb"));
		logger.info(Log.message("Max	Memory	: " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " Mb"));
		long startTime = System.currentTimeMillis();
		try {
			childJobs = new ArrayList<Promise>();
			File folder = new File(Play.configuration.getProperty("affiliate.cj.product.feed.input.location"));

			logger.info(Log.message("File Path: " + folder.getAbsolutePath()));

			// Get the impactRadiusProductMap
			String propertiesPath = Play.configuration
					.getProperty("cron.job.sync.impactradius.products.ftp.properties");
			impactRadiusProductMap = getImpactRadiusProductsMap(propertiesPath);
			logger.info(Log.message("Get Impact Radius Product Map From : " + propertiesPath));

			// Download Impact Radius Feed -- Will be added Soon
			impactRadiusFileService = new ImpactRadiusFileService();
			impactRadiusFileService.cleanFiles(folder);
			impactRadiusFileService.fetchImpactRadiusFeeds(impactRadiusProductMap);
			impactRadiusFileService = null;

			childJobs = new ArrayList<Promise>();

			if (!folder.exists()) {
				logger.error(Log.message("Exiting the process as no such folder exists : " + folder.getAbsolutePath()));
			} else {

				File[] listOfSubfolder = folder.listFiles();

				for (File subFolder : listOfSubfolder) {
					File[] listOfFiles = subFolder.listFiles();

					Set<String> brands = new HashSet<String>();
					Set<String> categories = new HashSet<String>();

					// Set<String> checkDuplicates = new HashSet<String>();

					for (int i = 0; i < listOfFiles.length; i++) {
						File file = listOfFiles[i];
						if (isValidFileToParse(file)) {
							Promise promise = invokeImpactRadiusProductSynchroniser(file, brands, categories,
									impactRadiusProductMap);
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
						logger.info(Log.message("childJobs 	: " + childJobs.size()));
						allChildJobsCompleted = true;
						Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
						for (Promise promise : childJobs) {
							allChildJobsCompleted = allChildJobsCompleted & promise.isDone();
						}
					}
					logger.info(Log.message(
							"==============================================================================="));
					logger.info(Log.message("Brands 	: " + brands.size()));
					logger.info(Log.message("Categories : " + categories.size()));
					logger.info(Log.message(
							"==============================================================================="));
					updateImpactRadiusBrands(brands);
					updateImpactRadiusCategories(categories);

					logger.info(Log
							.message("All the jobs (to create the brand and category) are completed sequentially..."));
				}
			}

		} catch (Exception e) {
			logger.error(Log.message("Issue in RakutenCreateBrandCategoryAll runJob : " + e.getMessage()));
		} finally {
			unlock(this.getClass());
			logger.info(Log.message("!!! Finish Job !!!"));
			logger.info(Log.message("Total Time Used: " + (System.currentTimeMillis() - startTime)));
			System.gc();
		}

	}

	private void updateImpactRadiusBrands(Set<String> brands) {
		if (brands == null) {
			logger.error(Log.message("Brands set is not available!!!"));
			return;
		}
		if (brands.size() == 0) {
			logger.info(Log.message("Brands set is Empty!!!"));
			return;
		}
		try {
			logger.info(Log.message("====> Update New Brand is Started <===="));
			List<Promise> jobs = new ArrayList<Promise>();
			boolean allJobsCompleted = false;
			List<String> brandList = new ArrayList<String>();
			brandList.addAll(brands);
			List<List<String>> brandSubLists = Lists.partition(brandList, 1000);
			for (List<String> bList : brandSubLists) {
				Promise promise = new ImpactRadiusCreateBrandAllHelper(bList).now();
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
			logger.info(Log.message("====> Update New Brand is Finished <===="));
		} catch (Exception e) {
			logger.error(Log.message("Issue in updateImpactRadiusBrands : " + e.getMessage()));
		}
	}

	private void updateImpactRadiusCategories(Set<String> categories) {
		if (categories == null) {
			logger.error(Log.message("Categories set is not available!!!"));
			return;
		}
		if (categories.size() == 0) {
			logger.info(Log.message("Categories set is Empty!!!"));
			return;
		}
		try {
			logger.info(Log.message("====> Update New Category is Started <===="));
			List<Promise> jobs = new ArrayList<Promise>();
			boolean allJobsCompleted = false;
			List<String> categoryList = new ArrayList<String>();
			categoryList.addAll(categories);
			List<List<String>> categorySubList = Lists.partition(categoryList, 1000);
			for (List<String> cList : categorySubList) {
				Promise promise = new ImpactRadiusCreateCategoryAllHelper(cList).now();
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
			logger.info(Log.message("====> Update New Category is Finished <===="));
		} catch (Exception e) {
			logger.error(Log.message("Issue in updateImpactRadiusCategories : " + e.getMessage()));
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

	private Promise invokeImpactRadiusProductSynchroniser(File file, Set<String> brands, Set<String> categories,
			Map<String, String> impactRadiusProductMap) {
		Promise promise = null;
		logger.info(Log.message("Invoking the product Impact Radius synchroniser for the seller --"));
		ImpactRadiusCreateBrandCategoryAllChild impactRadiusCreateBrandCategoryAllChild = new ImpactRadiusCreateBrandCategoryAllChild(
				file, brands, categories, impactRadiusProductMap);
		promise = impactRadiusCreateBrandCategoryAllChild.now();
		return promise;
	}

	/**
	 * Use this function to get all products which needs to Download Use this
	 * map to get the advertiserId for each seller from Impact Radius
	 */
	private Map<String, String> getImpactRadiusProductsMap(String propertiesPath) {
		logger.info(Log.message("--> Start getting Impact Radius Product File Map  <--"));
		if (propertiesPath == null || propertiesPath.length() == 0) {
			logger.error(Log.message("Properties Path is invalid : " + propertiesPath));
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(propertiesPath);
			prop.load(input);
			for (String key : prop.stringPropertyNames()) {
				String advertiserID = prop.getProperty(key);
				if (map.get(key) == null) {
					map.put(key, advertiserID);
				}
			}
		} catch (IOException e) {
			logger.error(Log.message("Issues in getImpactRadiusProductsMap : " + e.getMessage()));
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
					input = null;
				} catch (IOException e) {
					logger.error(Log.message("Reading Properties file has issue" + e.getMessage()));
					e.printStackTrace();
				}
			}
		}
		logger.info(Log.message("--> Finish getting Impact Radius Product File Map  <--"));
		return map;
	}

}
