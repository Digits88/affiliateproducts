package batch.jobs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
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
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;

import batch.jobs.product.synchroniser.RakutenProductSynchroniser;
import batch.jobs.product.synchroniser.RakutenSuperFeedSynchroniser;
import constants.AffiliateConstants;
import constants.RakutenConstants;
import models.AdvertiserCategory;
import models.Product;
import models.cj.CJProduct;
import play.Play;
import play.db.jpa.JPA;
import play.libs.F.Promise;
import repositories.Repository;
import services.EmailService;
import services.rakuten.RakutenFileService;
import utils.log.Log;

@Entity
@DiscriminatorValue("SYNC_PRODUCTS_DETAILS")
@play.jobs.On("cron.job.sync.rakutensuper.frequency")
public class SyncRakutenSuperFeedJob extends AbstractBatchJob {

	@Transient
	public List<Promise> childJobs = null;

	@Transient
	public Boolean allChildJobsCompleted = false;

	private static Logger logger = Logger.getLogger(SyncRakutenSuperFeedJob.class);

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
		logger.info("Free Memory : " + Runtime.getRuntime().freeMemory() / (1024 * 1024) + " Mb");
		logger.info("Max Memory  : " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " Mb");
		long usedMem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
		logger.info("Used Memory : " + usedMem + " Mb	<----");

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

				// we don't need to this list since we don't need to check color
				// Map<String, String> colorSKUMap = new Hashtable<String,
				// String>();

				/**
				 * Download the feed
				 */
				// Empty the folder
				cleanFolder(folder);
				RakutenFileService rakutenFileService = RakutenFileService.getInstance();
				rakutenFileService.fetchSuperProductFeed();

				/**
				 * Sync Process is started
				 */
				logger.info("File Path: " + folder.getAbsolutePath());

				if (!folder.exists()) {
					logger.error(
							Log.message("Exiting the process as no such folder exists : " + folder.getAbsolutePath()));
				} else {
					logger.info("Total size of the Folder " + folder.length() / (1024) + " Mb");
					File[] sellers = folder.listFiles();
					for (File sellerFolder : sellers) {
						List<Set<String>> productSKUList = new ArrayList<Set<String>>();
						long advertiseID = Long.parseLong(sellerFolder.getName());
						logger.info("Start importing seller " + advertiseID);

						rakutenFileService.splitSuperFeedBasedOnCategoryNew(sellerFolder);

						File[] listOfFiles = sellerFolder.listFiles();

						for (int i = 0; i < listOfFiles.length; i++) {
							File file = listOfFiles[i];
							logger.info("Sub File Name: " + file.getAbsolutePath());

							if (isValidFileToParse(file)) {
								Promise promise = invokeRakutenProductSynchroniser(advertiseID, file, productSKUList,
										"Thread_" + i);
								childJobs.add(promise);
								logger.info(Log.message("Child Jobs' Number is " + childJobs.size()));
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

						logger.info("        ++++++++++++++++++++++++++++");
						logger.info("        Finish Sync Products Process");
						logger.info("        ||||||||||||||||||||||||||||");

						/**
						 * Run post process if in_latest_feed = 0, then update
						 * it as out-of-stock
						 */
						syncOutOfStockProducts(advertiseID, productSKUList);

						allChildJobsCompleted = false;
						// childJobs.clear();

						logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
						logger.info("Product --> " + sellerFolder.getName() + " <-- was finished");
						logger.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
					}
				}
				// Empty the folder
				logger.info("Total size of the Folder " + folder.length() / (1024) + " Mb");
				cleanFolder(folder);
				logger.info("!!! Finish Job !!!");
				logger.info("Total Time Used: " + (System.currentTimeMillis() - startTime));

				// Send out notification
				emailService.sendEmail(RakutenConstants.SUCCESS_EMAIL_SUBJECT_SUPER, RakutenConstants.SUCCESS_EMAIL_BODY_SUPER);
			}
		} catch (Exception e) {
			// Send out notification
			emailService.sendEmail(RakutenConstants.EXCEPTION_EMAIL_SUBJECT_SUPER, RakutenConstants.EXCEPTION_EMAIL_BODY_SUPER);
			logger.error(Log.message(e.getMessage()));
		} finally {
			unlock(this.getClass());
			logger.info(Log.message(">>>>> Unlock the Job <<<<<"));
		}
	}

	public void syncOutOfStockProducts(Long rakutenAdvertiserID, List<Set<String>> productSKUList) {
		try {
			Promise promise = null;
			logger.info(Log.message(
					"SyncRakutenSuperFeedJob invokeRakutenProductSynchroniser : Invoking the product Rakuten synchroniser for the seller -- "
							+ rakutenAdvertiserID));
			SyncRakutenSuperFeedUpdateOOSJob syncRakutenSuperFeedUpdateOOSJob = new SyncRakutenSuperFeedUpdateOOSJob(
					rakutenAdvertiserID, productSKUList);
			promise = syncRakutenSuperFeedUpdateOOSJob.now();

			while (!promise.isDone()) {
				Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
				logger.info("Waiting for each child job (Update Out Of Stock Products) to complete...");
			}
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
		}
	}

	private void deleteOutOfSyncRakutenProducts(Long rakutenAdvertiserID, List<Set<String>> productSKUList) {

		logger.info("VVVVVVVVVVVVVVVVVVVVVVVVVVVV");
		logger.info("Update Out-Of-Stock Products");
		logger.info("++++++++++++++++++++++++++++");

		if (rakutenAdvertiserID != null) {
			List<String> existingProductSkusInDB = (List<String>) JPA.em()
					.createQuery("SELECT sku FROM Product WHERE advertiserId=" + rakutenAdvertiserID).getResultList();

			// List<String> existingProductSkusInDB =
			// repository.createNamedQuery2("JPQL_GET_SKUS",
			// rakutenAdvertiserID);

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
			String threadName) {
		Promise promise = null;
		logger.info(Log.message(
				"SyncRakutenSuperFeedJob invokeRakutenProductSynchroniser : Invoking the product Rakuten synchroniser for the seller -- "
						+ advertiseID));
		RakutenSuperFeedSynchroniser productSynchroniser = new RakutenSuperFeedSynchroniser(advertiseID, inputFile,
				productSKUList, threadName);
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
	private long getRakutenSuperAdvertiserID(String name) {
		if (name == null || name.length() <= 0) {
			return 0;
		}
		return Long.parseLong(name);
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

	/**
	 * use to get category:
	 * 
	 * @param file
	 * @param File
	 */
	private String getCategory(File file) {
		String res = null;
		try {
			if (file == null || file.length() == 0) {
				logger.error(Log.message("Invalid Input File " + file.getName()));
				return res;
			}
			BufferedReader reader = null;
			reader = new BufferedReader(new FileReader(file));
			String firstProduct = reader.readLine();
			firstProduct = reader.readLine();
			reader.close();

			InputSource is = new InputSource(new StringReader(firstProduct));
			DOMParser dp = new DOMParser();
			dp.parse(is);
			Document doc = dp.getDocument();
			NodeList n1 = doc.getElementsByTagName("primary");
			NodeList n2 = doc.getElementsByTagName("secondary");
			String primary = StringEscapeUtils.unescapeXml(n1.item(0).getTextContent());
			String secondary = StringEscapeUtils.unescapeXml(n2.item(0).getTextContent());

			String categoryName = secondary.concat(RakutenConstants.CATEGORY_LINK_WITH_SPACE).concat(primary);
			logger.info("Working on Categoty : " + categoryName);

			AdvertiserCategory c = AdvertiserCategory.find("byName", categoryName).first();
			if (c == null) {
				logger.info("Category " + categoryName + " is not Existed ! ");
				repository.create(new AdvertiserCategory(categoryName));
				logger.info("Successfully Created " + categoryName + " in Category table !! ");
			}
			res = categoryName;
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
			e.printStackTrace();
		}
		return res;
	}

	private String getPattern(String fileName, Map<String, String> patternTable) {
		String category = fileName.split(RakutenConstants.CATEGORY_NAME_UNDERLINE)[2];
		if (patternTable.get(category) != null) {
			logger.info("---> " + fileName + " --- will be splited by Product Name");
			return RakutenConstants.PRODUCT_PATTERN_FOR_NAME;
		} else {
			logger.info("---> " + fileName + " --- will be splited by Image URL");
			return RakutenConstants.IMAGEURL_PATTERN;
		}
	}
}
