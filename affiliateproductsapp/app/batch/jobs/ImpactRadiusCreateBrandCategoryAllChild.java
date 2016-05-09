package batch.jobs;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.Transient;

import models.Brand;
import models.AdvertiserCategory;
import models.cj.CJProduct;
import models.impactradius.ImpactRadiusProduct;
import models.rakuten.RakutenProduct;
import models.searskmart.SearsKmartProduct;

import org.apache.log4j.Logger;

import batch.jobs.product.synchroniser.CJProductsCreator;
import batch.jobs.product.synchroniser.ImpactRadiusProductCreator;
import batch.jobs.product.synchroniser.RakutenProductCreator;
import batch.jobs.product.synchroniser.SKProductCreator;
import play.libs.F.Promise;
import repositories.Repository;
import utils.log.Log;

public class ImpactRadiusCreateBrandCategoryAllChild extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(ImpactRadiusCreateBrandCategoryAllChild.class);

	@Inject
	protected static Repository repository;

	@Transient
	private List<Promise> childJobs = null;

	@Transient
	private Boolean allChildJobsCompleted = false;

	@Transient
	private File file;

	@Transient
	private Set<String> brands;

	@Transient
	private Set<String> categories;

	@Transient
	private Map<String, String> impactRadiusProductMap;

	public ImpactRadiusCreateBrandCategoryAllChild(File file, Set<String> brands, Set<String> categories,
			Map<String, String> impactRadiusProductMap) {
		super();
		this.file = file;
		this.brands = brands;
		this.categories = categories;
		this.impactRadiusProductMap = impactRadiusProductMap;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		try {
			long advertiserId = getImpactRadiusAdvertiserID(file.getName());

			ImpactRadiusProductCreator impactRadiusProductsCreator = new ImpactRadiusProductCreator(advertiserId, file);
			List<ImpactRadiusProduct> impactRadiusProducts = impactRadiusProductsCreator.createImpactRadiusProducts();

			if (impactRadiusProducts != null && impactRadiusProducts.size() > 0) {
				logger.info(
						Log.message(file.getName() + " has " + impactRadiusProducts.size() + " ImpactRadiusProduct!"));
				for (ImpactRadiusProduct impactRadiusProduct : impactRadiusProducts) {
					if (impactRadiusProduct.getCategory() != null && impactRadiusProduct.getCategory().length() > 0) {
						categories.add(impactRadiusProduct.getCategory());

					}
					if (impactRadiusProduct.getManufacturer() != null
							&& impactRadiusProduct.getManufacturer().length() > 0) {
						brands.add(impactRadiusProduct.getManufacturer());

					}
				}
			}

			logger.info(Log.message("Successfully Add the brand and category into Set from the file : " + file.getAbsolutePath()));
			logger.info(Log.message("Brands size	: " + brands.size()));
			logger.info(Log.message("Categories size: " + categories.size()));
			logger.info(Log.message(" Free	Memory	: " + Runtime.getRuntime().freeMemory() / (1024 * 1024) + " Mb"));
			logger.info(Log.message("total	Memory	: " + Runtime.getRuntime().totalMemory() / (1024 * 1024) + " Mb"));
			long usedMem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
			logger.info(Log.message("Used	Memory	: " + usedMem + " Mb"));
		} catch (Exception e) {
			logger.error(Log.message("Issues in ImpactRadiusCreateBrandCategoryTSVChild job: " + e.getMessage()));
			e.printStackTrace();
		}
	}

	/**
	 * Use this function to get Feeds' advertiseID
	 */
	private long getImpactRadiusAdvertiserID(String fileName) {
		if (fileName == null || fileName.length() <= 0) {
			logger.error(Log.message("Folder is invalid ! " + fileName));
			return 0;
		}
		long res = 0;
		try {
			String advertiser = fileName.split("-")[0];
			res = Long.valueOf(impactRadiusProductMap.get(advertiser)).longValue();
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
		}
		return res;
	}
}
