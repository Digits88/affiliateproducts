/**
 * 
 */
package controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.apache.log4j.Logger;

import play.Play;
import play.mvc.Http;
import play.mvc.Util;
import utils.log.Log;
import batch.jobs.CleanFeedFolderJob;
import batch.jobs.CreateBrand;
import batch.jobs.CreateBrandCategoryAllTSV;
import batch.jobs.CreateCJSuperBrandCategoryJob;
import batch.jobs.CreateCategory;
import batch.jobs.CreateProductFeedAsTSV;
import batch.jobs.ImpactRadiusCreateBrandCategoryAll;
import batch.jobs.RakutenCreateBrandCategoryAll;
import batch.jobs.SKCreateBrandCategoryAllTSV;
import batch.jobs.SaveBrandImageJob;
import batch.jobs.SaveSellerImageJob;
import batch.jobs.SaveUserInstagramProfileImageJob;
import batch.jobs.SyncCJSuperProductFeedJob;
import batch.jobs.SyncImpactRadiusProductsDetails;
import batch.jobs.SyncProductsDetailTSV;
import batch.jobs.SyncProductsDetails;
import batch.jobs.SyncRakutenProductsDetails;
import batch.jobs.SyncRakutenSuperFeedJob;
import batch.jobs.SyncSKProductsDetailsTSV;
import batch.jobs.TmpUpdateFinalPriceAndSaleParent;
import batch.jobs.UpdateBrandCategoryAll;
import batch.jobs.UpdateCategoryMappingTSVFile;
import batch.jobs.UpdateGoogleCategory;
import batch.jobs.UpdateMissingBrandJob;
import batch.jobs.UpdateMissingCategoryJob;
import batch.jobs.UpdateSimilarityProductsForSK;
import batch.jobs.ZTestJobZ;

public class JobsController extends SecuredController {

	private static Logger logger = Logger.getLogger(JobsController.class);
	private static String jobkey;

	static {
		jobkey = Play.configuration.getProperty("affiliate.job.token");
	}

	public static void syncProductDetails(String key) throws Exception {
		isJobKeyValid(key);
		new SyncProductsDetails().now();
		logger.info(Log.message("Kicked off the sync products process..."));
		renderText("Kicked off the sync products process...");
	}

	public static void syncProductDetailsTSV(String key) throws Exception {
		isJobKeyValid(key);
		new SyncProductsDetailTSV().now();
		logger.info(Log.message("Kicked off the SyncProductsDetailsTSV process..."));
		renderText("Kicked off the SyncProductsDetailsTSV process...");
	}

	public static void syncProductDetailsFor(String key, List<Long> ids) throws Exception {
		isJobKeyValid(key);
		SyncProductsDetails syncProductsDetails = new SyncProductsDetails();
		syncProductsDetails.sellerIds = ids;
		syncProductsDetails.now();
		logger.info(Log.message("Kicked off the sync products process for the seller Ids : " + ids + "...."));
		renderText("Kicked off the sync products process for the seller Ids : " + ids + "....");
	}

	public static void createBrandCategoryAllTSV(String key) throws Exception {
		isJobKeyValid(key);
		new CreateBrandCategoryAllTSV().now();
		logger.info(Log.message("Kicked off the brand and category create process from the tsv files..."));
		renderText("Kicked off the brand and category create process from the tsv files...");
	}

	public static void createCategory(String key) throws Exception {
		isJobKeyValid(key);
		new CreateCategory().now();
		logger.info(Log.message("Kicked off the category create process..."));
		renderText("Kicked off the category create process...");
	}

	public static void createBrand(String key) throws Exception {
		isJobKeyValid(key);
		new CreateBrand().now();
		logger.info(Log.message("Kicked off the brand create process..."));
		renderText("Kicked off the brand create process...");
	}

	public static void createProductFeedAsTSV(String key) throws Exception {
		isJobKeyValid(key);
		new CreateProductFeedAsTSV().now();
		logger.info(Log.message("Kicked off the tab separated product feed generation process..."));
		renderText("Kicked off the tab separated product feed generation process...");
	}

	public static void updateBrandAndCategory(String key) throws Exception {
		isJobKeyValid(key);
		new UpdateBrandCategoryAll().now();
		logger.info(Log.message("Kicked off the UpdateBrandCategory process..."));
		renderText("Kicked off the UpdateBrandCategory process...");
	}

	public static void updateCategoryMappingTSV(String key) throws Exception {
		isJobKeyValid(key);
		new UpdateCategoryMappingTSVFile().now();
		logger.info(Log.message("Kicked off the UpdateCategoryMappingTSV process..."));
		renderText("Kicked off the UpdateCategoryMappingTSV process...");
	}

	public static void updateFinalPriceAndSale(String key, List<Long> ids) throws Exception {
		isJobKeyValid(key);
		new TmpUpdateFinalPriceAndSaleParent(ids).now();
		logger.info(Log.message("Kicked off the UpdateCategoryMappingTSV process..."));
		renderText("Kicked off the UpdateCategoryMappingTSV process...");
	}

	@Util
	private static void isJobKeyValid(String key) {
		if (!key.equals(jobkey)) {
			errorJSON(Http.StatusCode.UNAUTHORIZED, "err.msg.unauthorized.request");
		}
	}

	// Add this for Sears/Kmart Feeds
	public static void syncSKProductDetailsTSV(String key) throws Exception {
		isJobKeyValid(key);
		new SyncSKProductsDetailsTSV().now();
		logger.info(Log.message("Kicked off the SyncSKProductsDetailsTSV process..."));
		renderText("Kicked off the SyncSKProductsDetailsTSV process...");
	}

	// Add this for create Brand and Category for Sears and Kmart Feeds
	public static void createSKBrandCategoryAllTSV(String key) throws Exception {
		isJobKeyValid(key);
		new SKCreateBrandCategoryAllTSV().now();
		logger.info(Log
				.message("Kicked off the Sears and Kmart's brand and category create process from the tsv files..."));
		renderText("Kicked off the Sears and Kmart's brand and category create process from the tsv files...");
	}

	// Add this for Rakuten Feeds
	public static void syncRakutenProductDetails(String key) throws Exception {
		isJobKeyValid(key);
		new SyncRakutenProductsDetails().now();
		logger.info(Log.message("Kicked off the SyncRakutenProductsDetails process..."));
		renderText("Kicked off the SyncRakutenProductsDetails process...");
	}

	// Add this for create Brand and Category for Rakuten Feeds
	public static void createRakutenBrandCategoryAll(String key) throws Exception {
		isJobKeyValid(key);
		new RakutenCreateBrandCategoryAll().now();
		logger.info(Log.message("Kicked off the Rakuten's brand and category create process from the xml files..."));
		renderText("Kicked off the Rakuten's brand and category create process from the tsv files...");
	}

	// Add this for Impact Radius' Feeds
	public static void syncImpactRadiusProductDetails(String key) throws Exception {
		isJobKeyValid(key);
		new SyncImpactRadiusProductsDetails().now();
		logger.info(Log.message("Kicked off the SyncImpactRadiusProductsDetails process..."));
		renderText("Kicked off the SyncImpactRadiusProductsDetails process...");
	}

	// Add this for create Brand and Category for Impact Radius' Feeds
	public static void createImpactRadiusBrandCategoryAll(String key) throws Exception {
		isJobKeyValid(key);
		new ImpactRadiusCreateBrandCategoryAll().now();
		logger.info(
				Log.message("Kicked off the Impact Radius' brand and category create process from the txt files..."));
		renderText("Kicked off the Impact Radius' brand and category create process from the txt files...");
	}

	// Add this for Rakuten Super Feeds
	public static void syncRakutenSuperFeedJob(String key) throws Exception {
		isJobKeyValid(key);
		new SyncRakutenSuperFeedJob().now();
		logger.info(Log.message("Kicked off the SyncRakutenSuperFeedJob process..."));
		renderText("Kicked off the SyncRakutenSuperFeedJob process...");
	}

	// Add this for Test
	public static void testCodeJob(String key) throws Exception {
		isJobKeyValid(key);
		new ZTestJobZ().now();
		logger.info(Log.message("Kicked off the Code Test process..."));
		renderText("Kicked off the Code Test process...");
	}

	// Add this for Cleaning Feed Folder
	public static void cleanFeedFolder(String key) throws Exception {
		isJobKeyValid(key);
		new CleanFeedFolderJob().now();
		logger.info(Log.message("Kicked off the Clean Feed Folder process..."));
		renderText("Kicked off the Clean Feed Folder process...");
	}

	// Save Seller Image Job
	public static void saveSellerImageJob(String key) throws Exception {
		isJobKeyValid(key);
		new SaveSellerImageJob().now();
		logger.info(Log.message("Kicked off the Save Seller Image process..."));
		renderText("Kicked off the Save Seller Image process...");
	}

	// Save Brand Image Job
	public static void saveBrandImageJob(String key) throws Exception {
		isJobKeyValid(key);
		new SaveBrandImageJob().now();
		logger.info(Log.message("Kicked off the Save Brand Image process..."));
		renderText("Kicked off the Save Brand Image process...");
	}

	// Save User Instagram Profile Image Job
	public static void saveUserInstagramProfileImageJob(String key) throws Exception {
		isJobKeyValid(key);
		new SaveUserInstagramProfileImageJob().now();
		logger.info(Log.message("Kicked off the Save User Instangram Profile Image process..."));
		renderText("Kicked off the Save User Instagram Profile Image process...");
	}

	/**
	 * Update taxonomy job
	 * 
	 * @param key
	 * @throws Exception
	 */
	public static void updateCategoriesJob(String key) throws Exception {
		isJobKeyValid(key);
		new UpdateGoogleCategory().now();
		logger.info(
				Log.message("Kicked off the job to start loading taxonomies from google file to category table..."));
		renderText("Kicked off the job to start loading taxonomies from google file to category table...");
	}
	
	public static void syncCJSuperProductDetailsTSV(String key) throws Exception {
		isJobKeyValid(key);
		new SyncCJSuperProductFeedJob().now();
		logger.info(Log.message("Kicked off the Sync CJ Super Products Feed process..."));
		renderText("Kicked off the Sync CJ Super Products Feed process...");
	}
	
	public static void createCJSuperFeedBrandCategory(String key) throws Exception {
		isJobKeyValid(key);
		new CreateCJSuperBrandCategoryJob().now();
		logger.info(Log.message("Kicked off the Create CJ Super Products Feed Brand Category process..."));
		renderText("Kicked off the Create CJ Super Products Feed Brand Category process...");
	}

	/**
	 * Calculate the similarity products.
	 */
	public static void updateSKSimilarityProduct(Long skid, String skcid, Long oid, String ocid) {
		try {
		logger.info("SK Category Before Decode: " + skcid);
		skcid = URLDecoder.decode(skcid, "UTF-8");
		logger.info("SK Category After Decode : " + skcid);
		
		logger.info("Other Category Before Decode: " + ocid);
		ocid = URLDecoder.decode(ocid, "UTF-8");
		logger.info("Other Category After Decode : " + ocid);
		
		new UpdateSimilarityProductsForSK(skid, skcid, oid, ocid).now();
		} catch (UnsupportedEncodingException e) {
			logger.error(Log.message(e.getMessage()));
			e.printStackTrace();
		}
		logger.info("Kicked off the job to update similar product for Sears and Kmart...");
		renderText("Kicked off the job to update similar product for Sears and Kmart...");
	}
	
	public static void updateMissingCategoryJob(String key) throws Exception {
		isJobKeyValid(key);
		new UpdateMissingCategoryJob().now();
		logger.info(Log.message("Kicked off the Update Missing Category process..."));
		renderText("Kicked off Update Missing Category process...");
	}
	
	public static void updateMissingBrandJob(String key) throws Exception {
		isJobKeyValid(key);
		new UpdateMissingBrandJob().now();
		logger.info(Log.message("Kicked off the Update Missing Brand process..."));
		renderText("Kicked off Update Missing Brand process...");
	}
}
