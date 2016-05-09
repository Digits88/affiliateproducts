package batch.jobs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.persistence.Transient;

import org.apache.log4j.Logger;

import com.google.common.io.Files;

import batch.jobs.product.synchroniser.DefaultCJSuperProductSynchroniser;
import batch.jobs.product.synchroniser.ProductSynchroniser;
import batch.jobs.product.synchroniser.ProductSynchroniserFactory;
import constants.AffiliateConstants;
import constants.CJProductsConstants;
import models.cj.CJProduct;
import play.libs.F.Promise;
import utils.log.Log;

public class SyncCJSuperProductFeedCreateUpdateJob extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(SyncCJSuperProductFeedJob.class);

	@Transient
	private File folder = null;

	@Transient
	private List<Set<String>> productSKUList = null;

	@Transient
	private List<Promise> childJobs = null;

	@Transient
	private Boolean allChildJobsCompleted = false;

	public SyncCJSuperProductFeedCreateUpdateJob(File folder, List<Set<String>> productSKUList) {
		super();
		this.folder = folder;
		this.productSKUList = productSKUList;
	}

	@Override
	public void doJob() throws Exception {
		logger.info("========== READY TO START CREATE OR UPDATE JOB ==========");
		runJob();
		logger.info("========== !!! FINISH CREATE OR UPDATE JOB !!!  ==========");
	}

	private void runJob() {
		try {
			File[] listOfSubfolder = folder.listFiles();

			for (File subFolder : listOfSubfolder) {

				logger.info("+++ Start working on ---> " + subFolder.getName());

				long cjAdvertiserID = CJProductsConstants.BARNES_NOBLE_ADVERTISERID;

				File[] listOfFiles = subFolder.listFiles();
				for (int i = 0; i < listOfFiles.length; i++) {
					File file = listOfFiles[i];
					logger.info("Sub File Name: " + file.getAbsolutePath());
					if (isValidFileToParse(file)) {
						Promise promise = invokeCJSuperProductSynchroniser(cjAdvertiserID, file, productSKUList, "Thread_" + i);
						childJobs.add(promise);
						logger.info("Child Jobs' Number is " + childJobs.size());
					} else {
						logger.error(Log.message(
								"Skipping the file : " + file.getName() + " as it is not a valid file to parse!!!"));
					}
				}
				while (!allChildJobsCompleted) {
					logger.info("Waiting for each child job to complete...");
					allChildJobsCompleted = true;
					Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
					for (Promise promise : childJobs) {
						allChildJobsCompleted = allChildJobsCompleted & promise.isDone();
					}
				}
				logger.info("----> Sync Super Feed For " + cjAdvertiserID + " Is Finished <----");
			}
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
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
		return cjProduct;
	}

	private Promise invokeCJSuperProductSynchroniser(long cjAdvertiserId, File inputFile,
			List<Set<String>> productSKUList, String thread) {
		Promise promise = null;
		logger.info(Log.message("Invoking the product synchroniser for the seller : " + cjAdvertiserId));

		promise = new DefaultCJSuperProductSynchroniser(cjAdvertiserId, inputFile, true, productSKUList, thread).now();
		return promise;
	}
}
