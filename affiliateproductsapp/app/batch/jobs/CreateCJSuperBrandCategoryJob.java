package batch.jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

import constants.AffiliateConstants;
import play.Play;
import play.libs.F.Promise;
import services.cj.impl.CJFileService;
import utils.log.Log;

@Entity
@DiscriminatorValue("SYNC_PRODUCTS_DETAILS")
public class CreateCJSuperBrandCategoryJob extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(CreateCJSuperBrandCategoryJob.class);

	@Override
	public void doJob() throws Exception {
		logger.info("========== READY TO START JOB ==========");
		runJob();
		logger.info("========== !!! GREAT JOB !!!  ==========");
	}

	private void runJob() {

		long startTime = System.currentTimeMillis();
		try {
			CJFileService cjFileService = new CJFileService();
			File folder = new File(Play.configuration.getProperty("affiliate.cj.product.feed.input.location"));
			logger.info("File Path: " + folder.getAbsolutePath());
			if (!folder.exists()) {
				logger.error(Log.message("Exiting the process as no such folder exists : " + folder.getName()));
			} else {
				File[] listOfFiles = folder.listFiles();
				Set<String> brands = cjFileService.getCJBrandFile(listOfFiles[0]);
				Set<String> categories = cjFileService.getCJCategoryFile(listOfFiles[0]);
				logger.info("===============================================================================");
				logger.info("Brands 	: " + brands.size());
				logger.info("Categories : " + categories.size());
				logger.info("===============================================================================");
				createCJBrands(brands);
				createCJCategories(categories);
			}
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
		} finally {
			logger.info("!!! Finish Job !!!");
			logger.info("Total Time Used: " + (System.currentTimeMillis() - startTime));
		}
	}
	
	private void createCJBrands(Set<String> brands) {
		if (brands == null) {
			logger.error(Log.message("Brands set is not available!!!"));
			return;
		}
		if (brands.size() == 0) {
			logger.info("Brands set is Empty!!!");
			return;
		}
		try {
			logger.info("====> Update New Brand is Started <====");
			List<Promise> jobs = new ArrayList<Promise>();
			boolean allJobsCompleted = false;
			List<String> brandList = new ArrayList<String>();
			brandList.addAll(brands);
			List<List<String>> brandSubLists = Lists.partition(brandList, 1000);
			for (List<String> bList : brandSubLists) {
				Promise promise = new CreateCJBrandHelper(bList).now();
				jobs.add(promise);
			}
			while (!allJobsCompleted) {
				logger.info("Waiting for each child job (seller product synchronisation) to complete...");
				allJobsCompleted = true;
				Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
				for (Promise promise : jobs) {
					allJobsCompleted = allJobsCompleted & promise.isDone();
				}
			}
			logger.info("====> Update New Brand is Finished <====");
		} catch (Exception e) {
			logger.error(Log.message("Issue in createCJBrands : " + e.getMessage()));
		}
	}
	
	private void createCJCategories(Set<String> categories) {
		if (categories == null) {
			logger.error(Log.message("Categories set is not available!!!"));
			return;
		}
		if (categories.size() == 0) {
			logger.info("Categories set is Empty!!!");
			return;
		}
		try {
			logger.info("====> Update New Category is Started <====");
			List<Promise> jobs = new ArrayList<Promise>();
			boolean allJobsCompleted = false;
			List<String> categoriesList = new ArrayList<String>();
			categoriesList.addAll(categories);
			List<List<String>> categoriesSubLists = Lists.partition(categoriesList, 1000);
			for (List<String> cList : categoriesSubLists) {
				Promise promise = new CreateCJCategoryHelper(cList).now();
				jobs.add(promise);
			}
			while (!allJobsCompleted) {
				logger.info("Waiting for each child job (seller product synchronisation) to complete...");
				allJobsCompleted = true;
				Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
				for (Promise promise : jobs) {
					allJobsCompleted = allJobsCompleted & promise.isDone();
				}
			}
			logger.info("====> Update New Category is Finished <====");
		} catch (Exception e) {
			logger.error(Log.message("Issue in createCJBrands : " + e.getMessage()));
		}
	}
}
