package batch.jobs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import models.Brand;
import models.AdvertiserCategory;
import models.Product;

import org.apache.log4j.Logger;

import constants.AffiliateConstants;
import repositories.Repository;
import utils.log.Log;

/**
 * 
 * Class to update the brand and category for all the products
 * 
 */

public class UpdateBrandCategoryChild extends AbstractBatchJob {
	private static Logger logger = Logger
			.getLogger(UpdateBrandCategoryChild.class);

	@Inject
	protected static Repository repository;

	@Transient
	private List<Long> pList;
	@Transient
	private Map<String, Brand> brandMap;;
	@Transient
	private Map<String, AdvertiserCategory> categoryMap;
	@Transient
	private Long sellerId;

	public UpdateBrandCategoryChild(Map<String, Brand> brandMap,
			Map<String, AdvertiserCategory> categoryMap, List<Long> pList, Long sellerId) {
		super();
		this.pList = pList;
		this.brandMap = brandMap;
		this.categoryMap = categoryMap;
		this.sellerId = sellerId;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		
		// logger.info(Log.message("--> Start SubList Update <-- Seller: " + sellerId));
		for (Long productId : pList) {
			try {
				Boolean brandUpdateNeeded = false;
				Boolean categoryUpdateNeeded = false;
				Product product = repository.find(Product.class, "from Product where id=?", productId);
				
				if (product.getBrand() == null && product.getManufacturerName() != null
						&& brandMap.get(product.getManufacturerName()) != null) {
					product.setBrand(brandMap.get(product.getManufacturerName()));
					brandUpdateNeeded = true;
					/*if(productId == 2493855) {
						logger.info(Log.message("Product ID: 		" + productId));
						logger.info(Log.message("Product Name: 		" + product.getName()));
						logger.info(Log.message("Product Manu: 		" + product.getManufacturerName()));
						logger.info(Log.message("Product Brand: 	" + product.getBrand().getName()));
						logger.info(Log.message("Product Advertiser:" + product.getAdvertiserId()));
						Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
					}*/
				}
				if (product.getCategory() == null && product.getAdvertiserCategory() != null
						&& categoryMap.get(product.getAdvertiserCategory()) != null) {
					product.setCategory(categoryMap.get(product.getAdvertiserCategory()));
					categoryUpdateNeeded = true;
				}
				if (brandUpdateNeeded || categoryUpdateNeeded) {
					repository.update(product);
				}
			} catch (Exception e) {
				logger.error(Log
						.message("Exception occurred in UpdateBrandAndCategoryChild job for the product : "
								+ productId + " - " + e.getMessage()));
			}
		}
		logger.info(Log.message("Free Memory	: " + Runtime.getRuntime().freeMemory()/(1024*1024) + " Mb"));
	}
}
