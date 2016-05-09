package batch.jobs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import play.Play;
import play.libs.F.Promise;
import repositories.Repository;
import services.cj.impl.CJFileService;
import utils.log.Log;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import constants.AffiliateConstants;
import models.cj.CJProduct;

/**
 * 
 * Class to synchronize the product details for all the sellers from CJ to
 * AFFILIATEPRODUCTS DB
 * 
 */
@Entity
@DiscriminatorValue("SYNC_PRODUCTS_DETAILS")
public class CreateBrandCategoryAllTSV extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(CreateBrandCategoryAllTSV.class);

	@Inject
	protected static Repository repository;

	@Transient
	public List<Promise> childJobs = null;

	@Transient
	public Boolean allChildJobsCompleted = false;

	@Transient
	private CJFileService cjFileService;

	@Override
	public void doJob() throws Exception {
		logger.info("========== READY TO START JOB ==========");
		runJob();
		logger.info("========== !!! GREAT JOB !!!  ==========");
	}

	private void runJob() {
		File folder = new File(Play.configuration.getProperty("affiliate.cj.product.feed.input.location"));

		logger.info("Free	Memory	: " + Runtime.getRuntime().freeMemory() / (1024 * 1024) + " Mb");
		logger.info("total	Memory	: " + Runtime.getRuntime().totalMemory() / (1024 * 1024) + " Mb");
		logger.info("Max	Memory	: " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " Mb");
		long startTime = System.currentTimeMillis();
		try {
			childJobs = new ArrayList<Promise>();
			logger.info("+++ File Path: " + folder.getAbsolutePath());

			cjFileService = new CJFileService();
			// clean the folder first
			cjFileService.cleanFiles(folder);
			// download the files
			cjFileService.downloadCJFeeds();

			if (!folder.exists()) {
				logger.error(Log.message("Exiting the process as no such folder exists : " + folder.getName()));
			} else {
				

				File[] listOfSubfolder = folder.listFiles();

				for (File subFolder : listOfSubfolder) {
					
					File[] listOfFiles = subFolder.listFiles();
					
					long cjAdvertiserID = 0;

					if (listOfFiles != null && listOfFiles.length >= 0 && isValidFileToParse(listOfFiles[0])) {
						logger.info("1");
						CJProduct cjProduct = getSeller(listOfFiles[0]);
						cjAdvertiserID = cjProduct.getAdvertiserId();
						logger.info("2" + cjProduct.getAdvertiserId());
						logger.info("This CJ Advertiser ID:  " + cjAdvertiserID);
					}
					logger.info("3");

					Set<String> brands = new HashSet<String>();
					Set<String> categories = new HashSet<String>();
					logger.info("Start create category and brands for " + folder.getAbsolutePath());
					for (int i = 0; i < listOfFiles.length; i++) {
						File file = listOfFiles[i];
						if (isValidFileToParse(file)) {
							Promise promise = invokeCreateBrandCategory(cjAdvertiserID, file, brands, categories);
							childJobs.add(promise);
							logger.info("Child Jobs' Number is " + childJobs.size());
						} else {
							logger.error(Log.message("Skipping the file : " + file.getName()
									+ " as it is not a valid file to parse!!!"));
						}
					}
					while (!allChildJobsCompleted) {
						logger.info("Waiting for each child job (seller product synchronisation) to complete...");
						logger.info("childJobs 	: " + childJobs.size());
						allChildJobsCompleted = true;
						Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
						for (Promise promise : childJobs) {
							allChildJobsCompleted = allChildJobsCompleted & promise.isDone();
						}
					}

					logger.info("===============================================================================");
					logger.info("Brands 	: " + brands.size());
					logger.info("Categories : " + categories.size());
					logger.info("===============================================================================");
					createCJBrands(brands);
					createCJCategories(categories);

					logger.info("All the jobs (to create the brand and category) are completed sequentially...");
				}
			}
		} catch (Exception e) {
			logger.error(Log.message("Exception occurred in CreateBrandCategoryAllTSV job : " + e.getMessage()));
		} finally {
			logger.info("!!! Finish Job !!!");
			logger.info("Total Time Used: " + (System.currentTimeMillis() - startTime));
			// clean the folder
			cjFileService.cleanFiles(folder);
		}
	}

	private Boolean isValidFileToParse(File inputFile) {
		String fileExtension = "txt";
		if (inputFile.isFile()) {
			// System.out.println(Files.getFileExtension(inputFile.getAbsolutePath().toString()));
			if (Files.getFileExtension(inputFile.getAbsolutePath().toString()).equals(fileExtension)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private CJProduct getSeller(File inputFile) {
		logger.info("Start getSeller : " + inputFile.getAbsolutePath());
		CJProduct cjProduct = null;
		BufferedReader reader = null;
		try {
			String productEntry = null;
			reader = new BufferedReader(new FileReader(inputFile));
			String firstLine = reader.readLine();
			String secondLine = reader.readLine();
			reader.close();

			if (CJProductMethodHandler.configurePositionForParams(firstLine)) {
				CJProductObjCreatorFromTSV cjProductCreator = new CJProductObjCreatorFromTSV();

				if ((productEntry = secondLine) != null) {
					List<String> params = Arrays.asList(productEntry.split("\t"));
					if (params != null && params.size() > 0) {
						cjProduct = new CJProduct();
						for (int i = 0; i < params.size(); i++) {
							Method method = CJProductMethodHandler.METHOD_HANDLERS
									.get(CJProductMethodHandler.POSITION_PARAM.get(i));
							if (method != null) {
								method.invoke(cjProductCreator, cjProduct, params.get(i));
							}
						}
					}
				}
			}

		} catch (Exception e) {
			logger.error(Log.message("Exception occurred in finding the seller from the file : "
					+ inputFile.getAbsolutePath() + " Exception message : " + e.getMessage()));
			e.printStackTrace();
		}
		logger.info("Finish getSeller : " + inputFile.getAbsolutePath());
		return cjProduct;
	}

	private Promise invokeCreateBrandCategory(long cjAdvertiserID, File file, Set<String> brands,
			Set<String> categories) {
		Promise promise = null;
		logger.info("Invoking the brand and category create job for the file : " + file.getAbsolutePath());
		CreateBrandCategoryTSV createBrandCategory = new CreateBrandCategoryTSV(cjAdvertiserID, file, brands,
				categories);
		promise = createBrandCategory.now();
		return promise;
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