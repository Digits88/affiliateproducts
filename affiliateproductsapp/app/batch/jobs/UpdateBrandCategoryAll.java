package batch.jobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import models.Brand;
import models.AdvertiserCategory;
import models.Seller;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

import constants.AffiliateConstants;
import play.db.jpa.JPA;
import play.libs.F.Promise;
import repositories.Repository;
import utils.log.Log;

/**
 * 
 * Class to update the brand and category for all the products
 * 
 */
@Entity
@DiscriminatorValue("SYNC_PRODUCTS_DETAILS")
public class UpdateBrandCategoryAll extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(UpdateBrandCategoryAll.class);

	@Transient
	public List<Promise> childJobs = null;

	@Transient
	public Boolean allChildJobsCompleted = false;

	@Inject
	protected static Repository repository;

	@Override
	public void doJob() throws Exception {
		if (tryLock(this.getClass())) {
			runJob();
		}
	}

	private void runJob() {
		childJobs = new ArrayList<Promise>();
		try {
			HashMap<String, Brand> brandMap = new HashMap<String, Brand>();
			HashMap<String, AdvertiserCategory> categoryMap = new HashMap<String, AdvertiserCategory>();
			// To get all the brands from product table
			List<Brand> brands = Brand.findAll();
			logger.info("Brands size : " + brands.size());
			for (Brand brand : brands) {
				brandMap.put(brand.getName(), brand);
			}
			logger.info("BrandMap size : " + brandMap.size());

			// To get all the categories from product table
			List<AdvertiserCategory> categories = AdvertiserCategory.findAll();
			logger.info("Categories size :" + categories.size());
			for (AdvertiserCategory category : categories) {
				categoryMap.put(category.getName(), category);
			}
			logger.info("Category Map Size : " + categoryMap.size());
			
			List<Seller> sellers = Seller.findAll();
			logger.info("Sellers Size : " + sellers.size());
			
			for (Seller seller : sellers) {
				logger.info("Begin updating the brand and category for the products of seller - " + seller.getName());

				List<Long> productIds = (List<Long>) JPA.em().createQuery(
						"SELECT id FROM Product where (brand is null or category is null) and seller=" + seller.getId())
						.getResultList();

				logger.info("Seller " + seller.getId() + " has " + productIds.size() + " products need update!!");

				List<List<Long>> pIDList = Lists.partition(productIds, 1000);
				logger.info("Seller " + seller.getId() + " is splited into " + pIDList.size() + " subList!!");

				for (int i = 0; i < pIDList.size(); i++) {
					List<Long> pList = pIDList.get(i);
					logger.info(seller.getId() + "----> Start UpdateBrandCategoryChild Job ----> " + i);
					Promise promise = new UpdateBrandCategoryChild(brandMap, categoryMap, pList, seller.getId()).now();
					childJobs.add(promise);
					logger.info(seller.getId() + " <---- End   UpdateBrandCategoryChild Job ----> " + i);
				}

				while (!allChildJobsCompleted) {
					logger.info("Waiting for each child job (Update category and brand) to complete...");
					allChildJobsCompleted = true;
					Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
					for (Promise promise : childJobs) {
						allChildJobsCompleted = allChildJobsCompleted & promise.isDone();
					}
				}

				logger.info("--> Finish Update <-- Seller: " + seller.getName());
				
				allChildJobsCompleted = false;
				childJobs.clear();
				logger.info("----> Cleaned Child Jobs List <----");
			}
			
			logger.info(Log.message("--> Finish Job <-- "));
		} catch (Exception e) {
			logger.error(Log.message("Exception occurred in UpdateBrandCategoryAll job : " + e.getMessage()));
		} finally {
			unlock(this.getClass());
		}
	}

}