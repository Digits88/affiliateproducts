package batch.jobs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import batch.jobs.product.synchroniser.RakutenProductSynchroniser;
import batch.jobs.product.synchroniser.SKProductSynchroniser;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import constants.AffiliateConstants;
import constants.RakutenConstants;
import play.Play;
import play.db.jpa.JPA;
import play.libs.F.Promise;
import repositories.Repository;
import services.EmailService;
import services.rakuten.RakutenFileService;
import utils.log.Log;

@Entity
@DiscriminatorValue("SYNC_PRODUCTS_DETAILS")
@play.jobs.On("cron.job.sync.rakuten.frequency")
public class SyncRakutenProductsDetails extends AbstractBatchJob {

	@Transient
	public List<Promise> childJobs = null;

	@Transient
	public Boolean allChildJobsCompleted = false;

	private static Logger logger = Logger.getLogger(SyncRakutenProductsDetails.class);

	@Inject
	protected static Repository repository;
	
	@Inject
	protected static EmailService emailService;

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

				// Empty the folder
				cleanFolder(folder);

				// Create a map for parse the color attributes for JCPenny (788)
				Map<String, String> colorSKUMap = new Hashtable<String, String>();

				RakutenFileService rfs = new RakutenFileService(colorSKUMap);
				rfs.downloadRakutenFeed();

				logger.info("File Path: " + folder.getAbsolutePath());

				if (!folder.exists()) {
					logger.error(
							Log.message("Exiting the process as no such folder exists : " + folder.getAbsolutePath()));
				} else {
					File[] listOfFolders = folder.listFiles();
					for (File subFolder : listOfFolders) {
						List<Set<String>> productSKUList = new ArrayList<Set<String>>();
						File[] listOfFiles = subFolder.listFiles();
						long advertiseID = getRakutenAdvertiserID(listOfFiles[0].getName());
						for (int i = 0; i < listOfFiles.length; i++) {
							File file = listOfFiles[i];
							logger.info("Sub File Name: " + file.getAbsolutePath());

							if (isValidFileToParse(file)) {
								Promise promise = invokeRakutenProductSynchroniser(advertiseID, file, productSKUList,
										colorSKUMap);
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

						deleteOutOfSyncRakutenProducts(advertiseID, productSKUList);
						logger.info("Total productSKUList size: " + productSKUList.size());

						allChildJobsCompleted = false;
						childJobs.clear();
						logger.info("Product --> " + subFolder.getName() + " <-- was finished !!");
					}
				}

				// Empty the folder
				cleanFolder(folder);
				logger.info("!!! Finish Job !!!");
				logger.info("Total Time Used: " + (System.currentTimeMillis() - startTime));
				
				// Send out notification
				emailService.sendEmail(RakutenConstants.SUCCESS_EMAIL_SUBJECT, RakutenConstants.SUCCESS_EMAIL_BODY);
			}
		} catch (Exception e) {
			// Send out notification
			emailService.sendEmail(RakutenConstants.EXCEPTION_EMAIL_SUBJECT, RakutenConstants.EXCEPTION_EMAIL_BODY);
			logger.error(Log.message("Exception occurred in SyncRakutenProductsDetail job : " + e.getMessage()));
		} finally {
			unlock(this.getClass());
			logger.info(Log.message(">>>>> Unlock the Job <<<<<"));
		}
	}

	public void deleteOutOfSyncRakutenProducts(Long rakutenAdvertiserID, List<Set<String>> productSKUList) {

		logger.info(Log.message("SyncRakutenProductsDetail deleteOutOfSyncSKProducts is Started .."));

		if (rakutenAdvertiserID != null) {
			List<String> existingProductSkusInDB = (List<String>) JPA.em()
					.createQuery("SELECT sku FROM Product WHERE advertiserId=" + rakutenAdvertiserID).getResultList();

			// How many SKU in input file
			Set<String> productSKUs = new HashSet<String>();
			for (Set<String> set : productSKUList) {
				productSKUs.addAll(set);
			}

			// Check how many sku in the input file.
			try {
				logger.info(Log.message("SKU in file:	" + productSKUs.size()));
				logger.info(Log.message("SKU in DB: 	" + existingProductSkusInDB.size()));

				if (existingProductSkusInDB != null && existingProductSkusInDB.size() > 0) {
					Boolean deletionChildJobComplete = false;
					List<Promise> deletionChildJobs = new ArrayList<Promise>();

					// Split skus from DB into multiple small files
					List<List<String>> subListSKUsInDB = Lists.partition(existingProductSkusInDB, 1000);

					for (int i = 0; i < subListSKUsInDB.size(); i++) {
						List<String> subList = subListSKUsInDB.get(i);
						Promise promise = new SyncRakutenProductsDetailsDeleteOutOfSync(rakutenAdvertiserID, subList,
								productSKUs, "Thread_" + i).now();
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
				}
			} catch (Exception e) {
				logger.error(Log.message(
						"Issues in SyncRakutenProductsDetails deleteOutOfSyncRakutenProducts : " + e.getMessage()));
				e.printStackTrace();
			}
			logger.info(Log.message("SyncRakutenProductsDetails deleteOutOfSyncRakutenProducts is finished !!"));
		}
		logger.info(Log.message(
				"SyncRakutenProductsDetails : Successfully completed deleting the products that are out of sync for the seller -- "
						+ rakutenAdvertiserID));
	}

	private Promise invokeRakutenProductSynchroniser(long advertiseID, File inputFile, List<Set<String>> productSKUList,
			Map<String, String> colorSKUMap) {
		Promise promise = null;
		logger.info(Log.message(
				"SyncRakutenProductsDetails invokeRakutenProductSynchroniser : Invoking the product Rakuten synchroniser for the seller -- "
						+ advertiseID));
		RakutenProductSynchroniser productSynchroniser = new RakutenProductSynchroniser(advertiseID, inputFile, true,
				productSKUList, colorSKUMap);
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

	// get Rakuten advertiser ID
	private long getRakutenAdvertiserID(String name) {
		if (name == null || name.length() <= 0) {
			return 0;
		}
		String[] list = name.split("_");
		return Long.parseLong(list[0]);
	}

	private String getRakutenCategoryName(String name, Map<String, String> map) {
		if (name == null || name.length() <= 0 || map == null || map.size() <= 0) {
			logger.error(
					Log.message("Issues in SyncRakutenProductsDetails getRakutenCategoryName : Invalid Parameters -- "
							+ name + " -- " + map.size()));
		}
		String[] list = name.split("_");
		return map.get(list[2]);
	}

	// function to get category map for current Rakuten Advertiser
	private Map<String, String> getCategoryMap(File file) {
		if (!file.exists()) {
			logger.error(Log.message("Issues in SyncRakutenProductsDetails getCategoryMap : " + file.getAbsolutePath()
					+ " is not existing!!"));
			return null;
		}
		Map<String, String> categoryMap = new HashMap<String, String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));

			String line = null;
			while ((line = reader.readLine()) != null && !line.trim().equals("")) {
				String[] list = line.split("\\|");
				if (!categoryMap.containsKey(list[0])) {
					categoryMap.put(list[0], list[1]);
					logger.info(Log.message("Map : " + list[0] + " -- " + list[1]));
				}
			}
		} catch (IOException e) {
			logger.error(Log.message("Issues in SyncRakutenProductsDetails getCategoryMap :  " + e.getMessage()));
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
					reader = null;
				}
				if (file.exists()) {
					file.delete();
				}
				logger.info(Log.message(
						"SyncRakutenProductsDetails getCategoryMap : Get Category Map -- size: " + categoryMap.size()));
			} catch (IOException e) {
				logger.error(Log.message("Issues in SyncRakutenProductsDetails getCategoryMap : " + e.getMessage()));
				e.printStackTrace();
			}
		}
		return categoryMap;
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
