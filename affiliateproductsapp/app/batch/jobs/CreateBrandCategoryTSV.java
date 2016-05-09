package batch.jobs;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.Transient;

import models.Brand;
import models.AdvertiserCategory;
import models.cj.CJProduct;
import play.libs.F.Promise;

import org.apache.log4j.Logger;

import repositories.Repository;
import utils.log.Log;
import batch.jobs.product.synchroniser.CJProductsCreator;

/**
 * 
 * Class to synchronize the product details for all the sellers from CJ to
 * AFFILIATEPRODUCTS DB
 * 
 */
public class CreateBrandCategoryTSV extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(CreateBrandCategoryTSV.class);

	@Inject
	protected static Repository repository;
	
	@Transient
	public List<Promise> childJobs = null;

	@Transient
	public Boolean allChildJobsCompleted = false;
	
	@Transient
	private Set<String> brands;
	
	@Transient
	private long cjAdvertiserID;

	@Transient
	private Set<String> categories;

	File file = null;

	public CreateBrandCategoryTSV(long cjAdvertiserID, File file, Set<String> brands, Set<String> categories) {
		super();
		this.cjAdvertiserID = cjAdvertiserID;
		this.file = file;
		this.brands = brands;
		this.categories = categories;
	}

	@Override
	public void doJob() throws Exception {
		createBrandAndCategory();
	}

	private void createBrandAndCategory() {
		CJProductsCreator cjProductsCreator = new CJProductsCreator(cjAdvertiserID, file, true);
		try {
			List<CJProduct> cjProducts = cjProductsCreator.createCJProducts();
			if (cjProducts != null && cjProducts.size() > 0) {
				for (CJProduct cjProduct : cjProducts) {
					if (cjProduct.getManufacturerName() != null && cjProduct.getManufacturerName().length() > 0) {
						brands.add(cjProduct.getManufacturerName());
					}
					if (cjProduct.getAdvertiserCategory() != null & cjProduct.getAdvertiserCategory().length() > 0) {
						categories.add(cjProduct.getAdvertiserCategory());
					}
				}

				logger.info("Successfully created the brand and category from the file : " + file.getAbsolutePath());
			}
		} catch (Exception e) {
			logger.error(Log.message("Exception occurred in CreateBrandCategoryTSV job : " + e.getMessage()));
			e.printStackTrace();
		}
	}
}