package batch.jobs;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.Transient;

import models.cj.CJProduct;
import models.searskmart.SearsKmartProduct;

import org.apache.log4j.Logger;

import batch.jobs.product.synchroniser.CJProductsCreator;
import batch.jobs.product.synchroniser.SKProductCreator;
import play.libs.F.Promise;
import repositories.Repository;
import utils.log.Log;

public class SKCreateBrandCategoryAllChild extends AbstractBatchJob {

	private static Logger logger = Logger
			.getLogger(SKCreateBrandCategoryAllChild.class);

	@Inject
	protected static Repository repository;

	@Transient
	public List<Promise> childJobs = null;

	@Transient
	public Boolean allChildJobsCompleted = false;

	@Transient
	public File file;

	@Transient
	public Set<String> brands;

	@Transient
	public Set<String> categories;

	@Transient
	public long advertiseID;

	public SKCreateBrandCategoryAllChild(File file, Set<String> brands,
			Set<String> categories, long advertiseID) {
		super();
		this.file = file;
		this.brands = brands;
		this.categories = categories;
		this.advertiseID = advertiseID;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		try {
			SKProductCreator skProductCreator = new SKProductCreator(
					advertiseID, file);

			List<SearsKmartProduct> skProducts = skProductCreator
					.createSKProducts();

			if (skProducts != null && skProducts.size() > 0) {
				for (SearsKmartProduct skProduct : skProducts) {
					if (skProduct.getBrand() != null
							&& skProduct.getBrand().length() > 0) {
						brands.add(skProduct.getBrand());
					}

					if (skProduct.getCategory() != null
							& skProduct.getCategory().length() > 0) {
						categories.add(skProduct.getCategory());
					}
				}
			}
			logger.info(Log
					.message("Successfully created the brand and category List from the file : "
							+ file.getAbsolutePath()));
			logger.info(Log.message("Free Memory	: " + Runtime.getRuntime().freeMemory()/(1024*1024) + " Mb"));
		} catch (Exception e) {
			logger.error(Log
					.message("Exception occurred in SKCreateBrandCategoryTSVChild job: "
							+ e.getMessage()));
			e.printStackTrace();
		}
	}
}
