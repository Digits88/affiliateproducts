package batch.jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.Transient;

import models.Brand;
import models.AdvertiserCategory;

import org.apache.log4j.Logger;

import com.google.common.io.Files;

import constants.AffiliateConstants;
import play.libs.F.Promise;
import repositories.Repository;
import utils.log.Log;

public class SKCreateBrandCategoryAllParent extends AbstractBatchJob {

	private static Logger logger = Logger
			.getLogger(SKCreateBrandCategoryAllParent.class);

	@Transient
	public List<Promise> childJobs = null;

	@Transient
	public Boolean allChildJobsCompleted = false;

	@Inject
	protected static Repository repository;

	@Transient
	public File folder;

	@Transient
	public Set<String> brands;

	@Transient
	public Set<String> categories;

	public SKCreateBrandCategoryAllParent(File folder, Set<String> brands,
			Set<String> categories) {
		super();
		this.folder = folder;
		this.brands = brands;
		this.categories = categories;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		childJobs = new ArrayList<Promise>(); 
		try {
			// get advertiser ID for this seller
			long advertiserID = getAdvertiserID(folder.getName());

			File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
				File file = listOfFiles[i];
				logger.info(Log.message("Sub File Name: "
						+ file.getAbsolutePath()));
				if (isValidFileToParse(file)) {
					Promise promise = invokeSKProductSynchroniser(file, brands,
							categories, advertiserID);
					childJobs.add(promise);
				} else {
					logger.error(Log.message("Skipping the file : "
							+ file.getName()
							+ " as it is not a valid file to parse!!!"));
				}
			}

			while (!allChildJobsCompleted) {
				logger.info(Log
						.message("Waiting for each child job to complete..."));
				allChildJobsCompleted = true;
				Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
				for (Promise promise : childJobs) {
					allChildJobsCompleted = allChildJobsCompleted
							& promise.isDone();
				}
			}

			for (String brand : brands) {
				Brand b = Brand.find("byName", brand).first();
				if (b == null) {
					logger.info(Log.message("Creating the brand : " + brand
							+ " from the file : " + folder.getAbsolutePath()));
					repository.create(new Brand(brand));
				}
			}

			for (String category : categories) {
				AdvertiserCategory c = AdvertiserCategory.find("byName", category).first();
				if (c == null) {
					logger.info(Log.message("Creating the category : "
							+ category + " from the file : "
							+ folder.getAbsolutePath()));
					repository.create(new AdvertiserCategory(category));
				}
			}
			logger.info(Log
					.message("Successfully created the brand and category from the file : "
							+ folder.getAbsolutePath()));
		} catch (Exception e) {
			logger.error(Log
					.message("Exception occurred in SKCreateBrandCategoryAllTSVParent job : "
							+ e.getMessage()));
		}
	}

	private Boolean isValidFileToParse(File inputFile) {
		String fileExtension = "txt";
		if (inputFile.isFile()) {
			if (Files.getFileExtension(inputFile.getAbsolutePath().toString())
					.equals(fileExtension)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private Promise invokeSKProductSynchroniser(File file, Set<String> brands,
			Set<String> categories, long advertiserID) {
		Promise promise = null;
		logger.info(Log
				.message("Invoking the SK product synchroniser for the seller"));
		SKCreateBrandCategoryAllChild skCreateBrandCategoryAllChild = new SKCreateBrandCategoryAllChild(
				file, brands, categories, advertiserID);
		promise = skCreateBrandCategoryAllChild.now();
		return promise;
	}

	/**
	 * Use this function to get Feeds' advertiseID
	 */
	private long getAdvertiserID(String name) {
		if (name == null || name.length() <= 0) {
			return 0;
		}
		if (name.equals("Sears")) {
			return 9999999;
		}
		if (name.equals("Kmart")) {
			return 8888888;
		}
		return 0;
	}
}
