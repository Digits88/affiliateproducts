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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import models.Product;
import models.cj.CJProduct;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import play.Play;
import play.db.jpa.JPA;
import play.libs.F.Promise;
import repositories.Repository;
import services.EmailService;
import services.FileService;
import services.cj.impl.CJFileService;
import services.impactradius.ImpactRadiusFileService;
import services.impl.DefaultProductService;
import utils.log.Log;
import batch.jobs.product.synchroniser.ImpactRadiusProductSynchroniser;
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
import java.net.UnknownHostException;
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
import constants.ImpactRadiusConstants;

@Entity
@DiscriminatorValue("SYNC_PRODUCTS_DETAILS")
@play.jobs.On("cron.job.sync.ir.frequency")
public class SyncImpactRadiusProductsDetails extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(SyncImpactRadiusProductsDetails.class);

	@Transient
	private List<Promise> childJobs = null;

	@Transient
	private Boolean allChildJobsCompleted = false;

	@Transient
	private Map<String, String> impactRadiusProductMap = null;

	@Inject
	protected static Repository repository;
	
	@Inject
	protected static EmailService emailService;

	@Transient
	private ImpactRadiusFileService impactRadiusFileService = null;

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

		logger.info(Log.message(Runtime.getRuntime().freeMemory()));
		logger.info(Log.message(Runtime.getRuntime().maxMemory()));

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

				long startTime = System.currentTimeMillis();
				childJobs = new ArrayList<Promise>();
				File folder = new File(Play.configuration.getProperty("affiliate.cj.product.feed.input.location"));

				logger.info("+++ File Path: " + folder.getAbsolutePath());

				// Get the impactRadiusProductMap
				String propertiesPath = Play.configuration
						.getProperty("cron.job.sync.impactradius.products.ftp.properties");
				impactRadiusProductMap = getImpactRadiusProductsMap(propertiesPath);
				logger.info(Log.message("+++ Get Impact Radius Product Map From : " + propertiesPath));

				// Download Impact Radius Feed -- Will be added Soon
				impactRadiusFileService = new ImpactRadiusFileService();
				impactRadiusFileService.cleanFiles(folder);
				impactRadiusFileService.fetchImpactRadiusFeeds(impactRadiusProductMap);
				impactRadiusFileService = null;

				if (!folder.exists()) {
					logger.error(Log
							.message("### Exiting the process as no such folder exists : " + folder.getAbsolutePath()));
				} else {
					File[] listOfSubfolder = folder.listFiles();

					for (File subFolder : listOfSubfolder) {
						List<Set<String>> productSKUList = new ArrayList<Set<String>>();
						logger.info("+++ Start working on ---> " + subFolder.getName());
						// Modify getAdvertiserID for Impact Radius Feeds
						logger.info("+++ SubFolder Name ---> " + subFolder.getName());
						long advertiseID = getAdvertiserID(subFolder.getName(), impactRadiusProductMap);
						logger.info("+++ advertiser ID ---> " + advertiseID);
						File[] listOfFiles = subFolder.listFiles();

						for (int i = 0; i < listOfFiles.length; i++) {
							File file = listOfFiles[i];
							logger.info("+++ Sub File Name: " + file.getAbsolutePath());
							if (isValidFileToParse(file)) {
								Promise promise = invokeProductSynchroniser(advertiseID, file, productSKUList);
								childJobs.add(promise);
								logger.info("+++ Child Jobs' Number is " + childJobs.size());
							} else {
								logger.error(Log.message("### Skipping the file : " + file.getName()
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

						logger.info("++++++++++++++++++++++++++++");
						logger.info("Finish Sync Products Process");
						logger.info("++++++++++++++++++++++++++++");
						
						deleteOutOfSyncImpactRadiusProducts(advertiseID, productSKUList);
						logger.info("+++ Total productSKUList size: " + productSKUList.size());

						allChildJobsCompleted = false;
						childJobs.clear();
						logger.info("++++++++++++++++++++++++++++++++++++++++++++++++++++");
						logger.info("Product --> " + advertiseID + " <-- was finished");
						logger.info("++++++++++++++++++++++++++++++++++++++++++++++++++++");
					}
				}

				logger.info("+++ Cleaning --> " + folder.getAbsolutePath());
				cleanFolder(folder);
				logger.info(Log.message("!!! Finish Job !!!"));
				logger.info(Log.message("Total Time Used: " + (System.currentTimeMillis() - startTime)));
				
				// Send out notification
				emailService.sendEmail(ImpactRadiusConstants.SUCCESS_EMAIL_SUBJECT, ImpactRadiusConstants.SUCCESS_EMAIL_BODY);
			}
		} catch (Exception e) {
			// Send out notification
			emailService.sendEmail(ImpactRadiusConstants.EXCEPTION_EMAIL_SUBJECT, ImpactRadiusConstants.EXCEPTION_EMAIL_BODY);
			logger.error(Log.message("Exception occurred in SyncImpactRadiusProductsDetails job : " + e.getMessage()));
		} finally {
			unlock(this.getClass());
			logger.info(Log.message(">>>>> Unlock the Job <<<<<"));
		}
	}

	public void deleteOutOfSyncImpactRadiusProducts(Long impactRadiusAdvertiserID, List<Set<String>> productSKUList) {

		logger.info(Log.message(impactRadiusAdvertiserID + " Impact Radius Delect out of Sync function -- Start"));

		if (impactRadiusAdvertiserID != null) {
			List<String> existingProductSkusInDB = (List<String>) JPA.em()
					.createQuery("SELECT sku FROM Product where advertiserId=" + impactRadiusAdvertiserID)
					.getResultList();

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
						Promise promise = new SyncImpactRadiusProductsDetailChildDeletion(impactRadiusAdvertiserID,
								subList, productSKUs).now();
						deletionChildJobs.add(promise);
					}

					while (!deletionChildJobComplete) {
						logger.info(Log.message(
								"Waiting for each Deletion Child Job (seller product synchronisation) to complete..."));
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

				logger.info(Log.message("Deletion is finished ..."));
			}
			logger.info(
					Log.message("Successfully completed deleting the products that are out of sync for the seller : "
							+ impactRadiusAdvertiserID));
		}
	}

	private Promise invokeProductSynchroniser(long advertiseID, File inputFile, List<Set<String>> list) {
		Promise promise = null;
		logger.info("Invoking the product synchroniser for the seller : " + advertiseID + " *** " + inputFile.getName()
				+ " ***");
		ImpactRadiusProductSynchroniser productSynchroniser = new ImpactRadiusProductSynchroniser(advertiseID,
				inputFile, list);
		promise = productSynchroniser.now();
		return promise;
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

	/**
	 * Use this function to get Feeds' advertiseID
	 */
	private long getAdvertiserID(String folderName, Map<String, String> map) {
		if (map == null || map.size() == 0) {
			logger.error(Log.message("Prodcut Map is invalid ! "));
			return 0;
		}
		if (folderName == null || folderName.length() <= 0) {
			logger.error(Log.message("Folder is invalid ! " + folderName));
			return 0;
		}
		long res = 0;
		try {
			res = Long.valueOf(map.get(folderName)).longValue();
		} catch (Exception e) {
			logger.error(Log.message("" + e.getMessage()));
		}
		return res;
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
