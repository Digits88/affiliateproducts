package batch.jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Transient;

import models.Product;
import models.cj.CJProduct;

import org.apache.log4j.Logger;

import batch.jobs.product.synchroniser.SKProductSynchroniser;

import com.google.common.io.Files;

import play.db.jpa.JPA;
import play.libs.F.Promise;
import repositories.Repository;
import utils.log.Log;
import constants.AffiliateConstants;

public class SyncSKProductDetailsTSVChild extends AbstractBatchJob {

	private static Logger logger = Logger
			.getLogger(SyncSKProductDetailsTSVChild.class);

	@Transient
	public List<Promise> childJobs = null;

	@Transient
	public Boolean allChildJobsCompleted = false;

	@Inject
	protected static Repository repository;

	@Transient
	public File file;

	@Transient
	List<Set<String>> productSKUList;

	public SyncSKProductDetailsTSVChild(File file,
			List<Set<String>> productSKUList) {
		this.file = file;
		this.productSKUList = productSKUList;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		childJobs = new ArrayList<Promise>();
		try {
			long advertiseID = getAdvertiserID(file.getName());
			File[] listOfFiles = file.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
				File file = listOfFiles[i];
				logger.info(Log.message("Sub File Name: "
						+ file.getAbsolutePath()));
				if (isValidFileToParse(file)) {
					Promise promise = invokeProductSynchroniser(advertiseID,
							file, productSKUList);
					childJobs.add(promise);
					logger.info(Log.message("Child Jobs' Number is "
							+ childJobs.size()));
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

			deleteOutOfSyncSKProducts(advertiseID, productSKUList);
			logger.info(Log.message("Total productSKUList size: "
					+ productSKUList.size()));

			allChildJobsCompleted = false;
			childJobs.clear();
			logger.info(Log.message("Product --> " + advertiseID
					+ " <-- was finished !!"));
		} catch (Exception e) {
			logger.error(Log
					.message("Exception occurred in SyncSKProductsDetailsTSV Child job (SyncSKProductsDetailTSVChild) : "
							+ e.getMessage()));
			e.printStackTrace();
		}
	}

	public void deleteOutOfSyncSKProducts(Long skAdvertiserID,
			List<Set<String>> productSKUList) {

		logger.info(Log.message("SK Delect out of sync -- Start"));

		if (skAdvertiserID != null) {
			List<String> existingProductSkusInDB = (List<String>) JPA
					.em()
					.createQuery(
							"SELECT sku FROM Product where advertiserId="
									+ skAdvertiserID).getResultList();

			// How many SKU in input file
			Set<String> productSKUs = new HashSet<String>();
			for (Set<String> set : productSKUList) {
				productSKUs.addAll(set);
			}

			// Check how many sku in the input file.
			try {
				Thread.sleep(15000);
				logger.info(Log.message("SKU in file:	" + productSKUs.size()));
				logger.info(Log.message("SKU in DB: 	"
						+ existingProductSkusInDB.size()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (existingProductSkusInDB != null
					&& existingProductSkusInDB.size() > 0) {

				// Add function for batch update job
				int count = 0;
				EntityManager em = JPA.newEntityManager();
				EntityTransaction transaction = em.getTransaction();

				try {
					transaction.begin();
					logger.info(Log.message("Transaction is started..."));
					for (String sku : existingProductSkusInDB) {
						if (!productSKUs.contains(sku)) {
							Product p = repository
									.find(Product.class,
											"from Product where advertiserId=? and sku=?",
											skAdvertiserID, sku);
							p.setInStock(false);
							/*
							 * logger.info(Log.message("Delete -- Update --> " +
							 * p.getSku())); repository.update(p);
							 */

							em.merge(p);
							count++;
							if (count == 10000) {
								logger.info(Log
										.message("Commit the current transaction ...1"
												+ count));
								transaction.commit();
								logger.info(Log
										.message("Commit the current transaction ...1 ... FINISHED"));
								em.clear();
								transaction.begin();
								count = 0;
							}
						}
					}
					if (count != 0) {
						logger.info(Log
								.message("Commit the current transaction ...2")
								+ count);
						transaction.commit();
						logger.info(Log
								.message("Commit the current transaction ...2 ... FINISHED"));
						em.close();
					}
				} catch (Exception e) {
					transaction.rollback();
					em.close();
					logger.error(Log
							.message("Exception occurred commit transaction : "
									+ e.getMessage()));
					e.printStackTrace();
				} finally {
					if(transaction != null) {
						transaction = null;
					}
					if(em != null) {
						em = null;
					}
				}
				logger.info(Log.message("Transaction is finished ..."));
			}
			logger.info(Log
					.message("Successfully completed deleting the products that are out of sync for the seller : "
							+ skAdvertiserID));
		}
	}

	private Boolean isValidFileToParse(File inputFile) {
		String fileExtension = "txt";
		if (inputFile.isFile()) {
			// System.out.println(Files.getFileExtension(inputFile.getAbsolutePath().toString()));
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

	private Promise invokeProductSynchroniser(long advertiseID, File inputFile,
			List<Set<String>> list) {
		Promise promise = null;
		logger.info(Log
				.message("Invoking the product synchroniser for the seller : "
						+ advertiseID));
		SKProductSynchroniser productSynchroniser = new SKProductSynchroniser(
				advertiseID, inputFile, true, list);
		promise = productSynchroniser.now();
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
