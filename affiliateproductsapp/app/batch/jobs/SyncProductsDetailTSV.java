package batch.jobs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Transient;

import models.Product;
import models.cj.CJProduct;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import constants.AffiliateConstants;
import constants.CJProductsConstants;
import play.Play;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.jobs.Job;
import play.libs.F.Promise;
import repositories.Repository;
import services.EmailService;
import services.cj.impl.CJFileService;
import services.impactradius.ImpactRadiusFileService;
import services.impl.DefaultEmailService;
import utils.log.Log;
import batch.jobs.AbstractBatchJob;
import batch.jobs.product.synchroniser.ProductSynchroniser;
import batch.jobs.product.synchroniser.ProductSynchroniserFactory;

@Entity
@DiscriminatorValue("SYNC_PRODUCTS_DETAILS")
@play.jobs.On("cron.job.sync.productsall.frequency")
public class SyncProductsDetailTSV extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(SyncProductsDetailTSV.class);

	@Transient
	public List<Promise> childJobs = null;

	@Transient
	public Boolean allChildJobsCompleted = false;

	@Inject
	protected static Repository repository;
	
	@Inject
	protected static EmailService emailService;

	@Transient
	private CJFileService cjFileService;

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
		logger.info("| | | | | START | | | | |");
		logger.info("V V V V V V V V V V V V V");
		logger.info(Runtime.getRuntime().freeMemory());
		logger.info(Runtime.getRuntime().maxMemory());
		childJobs = new ArrayList<Promise>();
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
				childJobs = new ArrayList<Promise>();
				File folder = new File(Play.configuration.getProperty("affiliate.cj.product.feed.input.location"));

				logger.info("+++ File Path: " + folder.getAbsolutePath());

				cjFileService = new CJFileService();
				// clean the folder first
				cjFileService.cleanFiles(folder);
				// download the files
				cjFileService.downloadCJFeeds();

				if (!folder.exists()) {
					logger.error(Log
							.message("### Exiting the process as no such folder exists : " + folder.getAbsolutePath()));
				} else {

					File[] listOfSubfolder = folder.listFiles();

					for (File subFolder : listOfSubfolder) {
						List<Set<String>> productSKUList = new ArrayList<Set<String>>();

						logger.info("+++ Start working on ---> " + subFolder.getName());

						long cjAdvertiserID = 0;

						File[] listOfFiles = subFolder.listFiles();
						for (int i = 0; i < listOfFiles.length; i++) {
							File file = listOfFiles[i];
							logger.info("Sub File Name: " + file.getAbsolutePath());

							if (isValidFileToParse(file)) {
								CJProduct cjProduct = getSeller(file);
								logger.info("cjProduct is: " + cjProduct);
								if (i == 0) {
									cjAdvertiserID = cjProduct.getAdvertiserId();
								}

								logger.info("This CJ Advertiser ID:  " + cjAdvertiserID);
								if (cjProduct != null && cjProduct.getName() != null) {
									Promise promise = invokeProductSynchroniser(cjProduct, file, productSKUList);
									childJobs.add(promise);
									logger.info("Child Jobs' Number is " + childJobs.size());
								}
							} else {
								logger.error(Log.message("Skipping the file : " + file.getName()
										+ " as it is not a valid file to parse!!!"));
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
						logger.info("----> Sync Products For " + cjAdvertiserID + " Is Finished <----");

						/**
						 * Delete
						 */
						deleteOutOfSyncProducts(cjAdvertiserID, productSKUList);
						logger.info(Log
								.message(cjAdvertiserID + " Has Total productSKUList size: " + productSKUList.size()));

						allChildJobsCompleted = false;
						childJobs.clear();
						logger.info("Sync CJ Feed --> " + cjAdvertiserID + " <-- was finished !!");
					}
				}

				logger.info("+++ Cleaning --> " + folder.getAbsolutePath());
				// clean the folder
				cjFileService.cleanFiles(folder);

				logger.info("!!! Finish Job !!!");
				logger.info("Total Time Used: " + (System.currentTimeMillis() - startTime));
				
				// Send out notification
				emailService.sendEmail(CJProductsConstants.SUCCESS_EMAIL_SUBJECT, CJProductsConstants.SUCCESS_EMAIL_BODY);
			}
		} catch (Exception e) {
			// Send out notification
			emailService.sendEmail(CJProductsConstants.EXCEPTION_EMAIL_SUBJECT, CJProductsConstants.EXCEPTION_EMAIL_BODY);
			logger.error(
					Log.message("Exception occurred in SyncProductsDetailsTSV Child job (SyncProductsDetailTSVChild) : "
							+ e.getMessage()));
			e.printStackTrace();
		} finally {
			unlock(this.getClass());
			logger.info(Log.message(">>>>> Unlock the Job <<<<<"));
		}
	}

	public void deleteOutOfSyncProducts(Long cjAdvertiserID, List<Set<String>> productSKUList) {

		logger.info("+++++ Update out of Stock function -- Start +++++");

		if (cjAdvertiserID != null) {
			List<String> existingProductSkusInDB = (List<String>) JPA.em()
					.createQuery("SELECT sku FROM Product where advertiserId=" + cjAdvertiserID).getResultList();

			if (existingProductSkusInDB != null && existingProductSkusInDB.size() > 0) {

				// How many SKU in input file
				Set<String> productSKUs = new HashSet<String>();
				for (Set<String> set : productSKUList) {
					productSKUs.addAll(set);
				}

				// Check how many sku in the input file.
				try {
					Thread.sleep(5000);
					logger.info(Log.message("SKU in file:	" + productSKUs.size()));
					logger.info(Log.message("SKU in DB: 	" + existingProductSkusInDB.size()));
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
							Promise promise = new SyncProductsDetailTSVChildDeletion(cjAdvertiserID, subList,
									productSKUs).now();

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

						logger.error(Log
								.message("Exception occurred during update out of stock function : " + e.getMessage()));

						e.printStackTrace();
					}

					logger.info(Log.message("+++++ Update out of Stock function -- Finish +++++"));
				}
				logger.info(Log
						.message("Successfully completed deleting the products that are out of sync for the seller : "
								+ cjAdvertiserID));
			}
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

	private Promise invokeProductSynchroniser(CJProduct cjProduct, File inputFile, List<Set<String>> productSKUList) {
		Promise promise = null;
		logger.info(Log.message("Invoking the product synchroniser for the seller : " + cjProduct.getAdvertiserName()));
		ProductSynchroniser productSynchroniser = ProductSynchroniserFactory.getProductSyncrhoniser(
				cjProduct.getAdvertiserName(), cjProduct.getAdvertiserId(), inputFile, true, productSKUList);
		promise = productSynchroniser.now();
		return promise;
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
}
