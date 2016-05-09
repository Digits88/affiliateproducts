package batch.jobs;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import models.Product;
import models.cj.CJProduct;

import org.apache.log4j.Logger;

import play.Play;
import play.db.jpa.JPA;
import play.libs.F.Promise;
import repositories.Repository;
import services.EmailService;
import services.FileService;
import services.cj.impl.CJFileService;
import services.impl.DefaultProductService;
import utils.log.Log;
import batch.jobs.product.synchroniser.ProductSynchroniser;
import batch.jobs.product.synchroniser.ProductSynchroniserFactory;
import batch.jobs.product.synchroniser.SKProductSynchroniser;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import models.cj.CJProduct;
import models.searskmart.SearsKmartProduct;

import org.apache.log4j.Logger;

import play.Play;
import play.libs.F.Promise;
import repositories.Repository;
import services.impl.DefaultProductService;
import services.sk.impl.SKFileService;
import utils.log.Log;
import batch.jobs.product.synchroniser.ProductSynchroniser;
import batch.jobs.product.synchroniser.ProductSynchroniserFactory;

import com.google.common.io.Files;

import constants.AffiliateConstants;
import constants.SKProductsConstants;

@Entity
@DiscriminatorValue("SYNC_PRODUCTS_DETAILS")
@play.jobs.On("cron.job.sync.sk.frequency")
public class SyncSKProductsDetailsTSV extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(SyncSKProductsDetailsTSV.class);

	@Transient
	public List<Promise> childJobs = null;

	@Transient
	public Boolean allChildJobsCompleted = false;

	@Inject
	protected static Repository repository;
	
	@Inject
	protected static EmailService emailService;

	@Override
	public void doJob() throws Exception {
		logger.info("========== READY TO START JOB ==========");
		if (tryLock(this.getClass())) {
			logger.info(">>>>> lock the Job <<<<<");
			runJob();
		}
		logger.info(">>>>> Unlock the Job <<<<<");
		logger.info("========== !!! GREAT JOB !!!  ==========");
	}

	private void runJob() {

		logger.info(Runtime.getRuntime().freeMemory());
		logger.info(Runtime.getRuntime().maxMemory());

		try {
			String host = Play.configuration.getProperty("affiliate.batch.server.hostname");
			String serverHost = InetAddress.getLocalHost().getHostName();
			logger.info("+++ Host Name : " + serverHost + " +++");

			if (!host.equals(serverHost)) {
				logger.info("#########################################################");
				logger.info("### This job can be executed only in the Batch Server ###");
				logger.info("################### See you next time ###################");
				logger.info("#########################################################");
			} else {
				logger.info("++++++++++++++++++++++++ HOORAY ++++++++++++++++++++++++");
				logger.info("++++++ THE PROGRAM IS RUNNING ON THE RIGHT SERVER ++++++");
				logger.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

				long startTime = System.currentTimeMillis();
				// Download SK Feed
				SKFileService fs = new SKFileService();
				fs.downloadSKFeeds();
				childJobs = new ArrayList<Promise>();

				File folder = new File(Play.configuration.getProperty("affiliate.cj.product.feed.input.location"));

				logger.info("File Path: " + folder.getAbsolutePath());

				if (!folder.exists()) {
					logger.error(
							Log.message("Exiting the process as no such folder exists : " + folder.getAbsolutePath()));
				} else {
					File[] listOfSubfolder = folder.listFiles();

					for (File subFolder : listOfSubfolder) {
						List<Set<String>> productSKUList = new ArrayList<Set<String>>();

						logger.info("Start working on ---> " + subFolder.getName());

						long advertiseID = getAdvertiserID(subFolder.getName());
						File[] listOfFiles = subFolder.listFiles();
						for (int i = 0; i < listOfFiles.length; i++) {
							File file = listOfFiles[i];
							logger.info("Sub File Name: " + file.getAbsolutePath());
							if (isValidFileToParse(file)) {
								Promise promise = invokeProductSynchroniser(advertiseID, file, productSKUList);
								childJobs.add(promise);
								logger.info("Child Jobs' Number is " + childJobs.size());
							} else {
								logger.error(Log.message("Skipping the file : " + file.getName()
										+ " as it is not a valid file to parse!!!"));
							}
						}

						while (!allChildJobsCompleted) {
							logger.info("Waiting for each child job (seller product synchronisation) to complete...");
							allChildJobsCompleted = true;
							Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
							for (Promise promise : childJobs) {
								allChildJobsCompleted = allChildJobsCompleted & promise.isDone();
							}
						}

						deleteOutOfSyncSKProducts(advertiseID, productSKUList);
						logger.info("Total productSKUList size: " + productSKUList.size());

						allChildJobsCompleted = false;
						childJobs.clear();
						logger.info("Product --> " + advertiseID + " <-- was finished !!");
					}
				}
				fs.cleanFolder(folder);
				logger.info("!!! Finish Job !!!");
				logger.info("Total Time Used: " + (System.currentTimeMillis() - startTime));
				
				// Send out notification
				emailService.sendEmail(SKProductsConstants.SUCCESS_EMAIL_SUBJECT, SKProductsConstants.SUCCESS_EMAIL_BODY);
			}
		} catch (Exception e) {
			// Send out notification
			emailService.sendEmail(SKProductsConstants.EXCEPTION_EMAIL_SUBJECT, SKProductsConstants.EXCEPTION_EMAIL_BODY);
			logger.error(Log.message("Exception occurred in SyncProductsDetailsTSV job : " + e.getMessage()));
		} finally {
			unlock(this.getClass());
		}
	}

	public void deleteOutOfSyncSKProducts(Long skAdvertiserID, List<Set<String>> productSKUList) {

		logger.info("SK Delect out of sync -- Start");

		if (skAdvertiserID != null) {
			List<String> existingProductSkusInDB = (List<String>) JPA.em()
					.createQuery("SELECT sku FROM Product where advertiserId=" + skAdvertiserID).getResultList();

			// How many SKU in input file
			Set<String> productSKUs = new HashSet<String>();
			for (Set<String> set : productSKUList) {
				productSKUs.addAll(set);
			}

			// Check how many sku in the input file.
			try {
				Thread.sleep(15000);
				logger.info("SKU in file:	" + productSKUs.size());
				logger.info("SKU in DB: 	" + existingProductSkusInDB.size());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (existingProductSkusInDB != null && existingProductSkusInDB.size() > 0) {
				Boolean deletionChildJobComplete = false;
				List<Promise> deletionChildJobs = new ArrayList<Promise>();

				// Split skus from DB into multiple small files
				List<List<String>> subListSKUsInDB = Lists.partition(existingProductSkusInDB, 1000);
				try {
					for (List<String> subList : subListSKUsInDB) {
						Promise promise = new SyncSKProductsDetailTSVChildDeletion(skAdvertiserID, subList, productSKUs)
								.now();
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

					logger.error(Log.message("Exception occurred commit transaction : " + e.getMessage()));
					e.printStackTrace();
				}

				logger.info("Deletion is finished ...");
			}
			logger.info("Successfully completed deleting the products that are out of sync for the seller : "
					+ skAdvertiserID);
		}
	}

	private Promise invokeProductSynchroniser(long advertiseID, File inputFile, List<Set<String>> list) {
		Promise promise = null;
		logger.info("Invoking the product synchroniser for the seller : " + advertiseID);
		SKProductSynchroniser productSynchroniser = new SKProductSynchroniser(advertiseID, inputFile, true, list);
		promise = productSynchroniser.now();
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
