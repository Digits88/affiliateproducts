package batch.jobs;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.common.io.Files;

import batch.jobs.product.synchroniser.DefaultCJSuperProductSynchroniser;
import constants.AffiliateConstants;
import constants.CJProductsConstants;
import play.Play;
import play.libs.F.Promise;
import utils.log.Log;

@Entity
@DiscriminatorValue("SYNC_PRODUCTS_DETAILS")
public class SyncCJSuperProductFeedJob extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(SyncCJSuperProductFeedJob.class);

	@Transient
	public List<Promise> childJobs = null;

	@Transient
	public Boolean allChildJobsCompleted = false;

	@Override
	public void doJob() throws Exception {
		logger.info("========== READY TO START JOB ==========");
		if (tryLock(this.getClass())) {
			logger.info(Log.message(">>>>> lock the Job <<<<<"));
			runJob();
		}
		logger.info("========== !!! GREAT JOB !!!  ==========");
	}

	private void runJob() {
		logger.info("Free Memory : " + Runtime.getRuntime().freeMemory() / (1024 * 1024) + " Mb");
		logger.info("Max Memory  : " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " Mb");
		long usedMem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
		logger.info("Used Memory : " + usedMem + " Mb	<----");
		long startTime = System.currentTimeMillis();
		File folder = new File(Play.configuration.getProperty("affiliate.cj.product.feed.input.location"));

		try {

			String host = Play.configuration.getProperty("affiliate.batch.server.hostname");
			String serverHost = InetAddress.getLocalHost().getHostName();
			logger.info(Log.message("+++ Host Name : " + serverHost + " +++"));

			if (!host.equals(serverHost)) {
				logger.info("#########################################################");
				logger.info("### This job can be executed only in the Batch Server ###");
				logger.info("################### See you next time ###################");
				logger.info("#########################################################");
			} else {
				logger.info("++++++++++++++++++++++++ HOORAY ++++++++++++++++++++++++");
				logger.info("++++++ THE PROGRAM IS RUNNING ON THE RIGHT SERVER ++++++");
				logger.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

				childJobs = new ArrayList<Promise>();
				// Run Download job
				Promise downloadJob = new SyncCJSuperProductFeedDownloadJob().now();
				childJobs.add(downloadJob);

				while (!allChildJobsCompleted) {
					logger.info("##### Waiting for Downloading CJ Super Feed Job to complete... #####");
					allChildJobsCompleted = true;
					Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
					for (Promise promise : childJobs) {
						allChildJobsCompleted = allChildJobsCompleted & promise.isDone();
					}
				}
				logger.info("========== !!! Download Job Is Done !!!  ==========");
				logger.info("|||||||||||||||||||||||||||||||||||||||||||||||||||");
				logger.info("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV");

				// clear all promise in job list
				childJobs.clear();
				
				// Run Update Product Job
				if (!folder.exists()) {
					logger.error(
							Log.message("Exiting the process as no such folder exists : " + folder.getAbsolutePath()));
				} else {
					
					File[] listOfSubfolder = folder.listFiles();

					for (File subFolder : listOfSubfolder) {

						logger.info("+++ Start working on ---> " + subFolder.getName());

						long cjAdvertiserID = CJProductsConstants.BARNES_NOBLE_ADVERTISERID;

						File[] listOfFiles = subFolder.listFiles();
						List<Set<String>> productSKUList = new ArrayList<Set<String>>();
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
					
						syncOutOfStockProducts(productSKUList, cjAdvertiserID);
					}
				}
				
			}
			// Empty the folder
			logger.info("Total size of the Folder " + folder.length() / (1024) + " Mb");
			// cleanFolder(folder);
			logger.info("!!! Finish Job !!!");
			logger.info("Total Time Used: " + (System.currentTimeMillis() - startTime));
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
		} finally {
			unlock(this.getClass());
			logger.info(Log.message(">>>>> Unlock the Job <<<<<"));
		}
	}
	
	public void syncOutOfStockProducts( List<Set<String>> productSKUList, Long cjAdvertiserID) {
		try {
			logger.info("========== !!! Create Or Update Job Is Done !!!  ==========");
			logger.info("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
			logger.info("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV");
			
			Promise updateOOSJob = new SyncCJSuperProductUpdateOutOfStockJob(productSKUList, cjAdvertiserID).now();

			while (!updateOOSJob.isDone()) {
				Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
				logger.info("Waiting for each child job (Update Out Of Stock Products) to complete...");
			}
			
			logger.info("========== !!! Update Out Of Stock Job Is Done !!!  ==========");
			logger.info("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
			logger.info("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV");
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
		}
	}
	
	private Promise invokeCJSuperProductSynchroniser(long cjAdvertiserId, File inputFile,
			List<Set<String>> productSKUList, String thread) {
		Promise promise = null;
		logger.info(Log.message("Invoking the product synchroniser for the seller : " + cjAdvertiserId));

		promise = new DefaultCJSuperProductSynchroniser(cjAdvertiserId, inputFile, true, productSKUList, thread).now();
		return promise;
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

	private void cleanFolder(File folder) {
		try {
			if (folder.exists()) {
				logger.info(Log.message("Start empty the folder : " + folder.getAbsolutePath()));
				FileUtils.cleanDirectory(folder);
			}
			logger.info(Log.message("Finish empty the folder : " + folder.getAbsolutePath()));
		} catch (IOException e) {
			logger.error(Log.message("Issues in cleanFolder :  " + e.getMessage()));
			e.printStackTrace();
		}
	}
}
