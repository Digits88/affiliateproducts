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
import models.rakuten.RakutenProduct;
import models.searskmart.SearsKmartProduct;

import org.apache.log4j.Logger;

import batch.jobs.product.synchroniser.CJProductsCreator;
import batch.jobs.product.synchroniser.RakutenProductCreator;
import batch.jobs.product.synchroniser.SKProductCreator;
import constants.RakutenConstants;
import play.libs.F.Promise;
import repositories.Repository;
import utils.log.Log;

public class RakutenCreateBrandCategoryAllChild extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(RakutenCreateBrandCategoryAllChild.class);

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

	public RakutenCreateBrandCategoryAllChild(File file, Set<String> brands, Set<String> categories) {
		super();
		this.file = file;
		this.brands = brands;
		this.categories = categories;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		try {
			long advertiserID = getRakutenAdvertiserID(file.getName());
			RakutenProductCreator rakutenProductCreator = new RakutenProductCreator(advertiserID, file);

			List<RakutenProduct> rakutenProducts = rakutenProductCreator.createRakutenProducts();

			if (rakutenProducts != null && rakutenProducts.size() > 0) {
				logger.info(Log.message(file.getName() + " has " + rakutenProducts.size() + " RakutenProduct!"));
				for (RakutenProduct rakutenProduct : rakutenProducts) {
					if (rakutenProduct.getMerchantId() == RakutenConstants.PETSMART_ADVERTISERID) {
						if (rakutenProduct.getSecondaryCategory() != null
								&& rakutenProduct.getSecondaryCategory().length() > 0) {
							categories.add(rakutenProduct.getSecondaryCategory());
						}
					} else if (rakutenProduct.getMerchantId() == RakutenConstants.BLOOMINGDALES_ADVERTISERID
							|| rakutenProduct.getMerchantId() == RakutenConstants.KOHLS_ADVERTISERID
							|| rakutenProduct.getMerchantId() == RakutenConstants.JCPENNY_ADVERTISERID) {
						if (rakutenProduct.getSecondaryCategory() != null
								&& rakutenProduct.getSecondaryCategory().trim().length() > 0) {
							categories
									.add(rakutenProduct.getPrimaryCategory() + RakutenConstants.CATEGORY_LINK_WITH_SPACE
											+ rakutenProduct.getSecondaryCategory());
						} else {
							categories.add(rakutenProduct.getPrimaryCategory());
						}
					} else if (rakutenProduct.getMerchantId() == RakutenConstants.WALMART_ADVERTISERID) {
						if (rakutenProduct.getPrimaryCategory() != null
								&& rakutenProduct.getSecondaryCategory() != null) {
							categories.add(rakutenProduct.getSecondaryCategory()
									.concat(RakutenConstants.CATEGORY_LINK_WITH_SPACE)
									.concat(rakutenProduct.getPrimaryCategory()));
						} else if (rakutenProduct.getSecondaryCategory() != null) {
							categories.add(rakutenProduct.getSecondaryCategory());
						} else {
							categories.add(rakutenProduct.getPrimaryCategory());
						}
					} else if (rakutenProduct.getMerchantId() == RakutenConstants.NIKE_ADVERTISERID) {
						if (rakutenProduct.getSecondaryCategory() != null
								&& rakutenProduct.getSecondaryCategory().trim().length() > 0) {
							categories.add(rakutenProduct.getPrimaryCategory() + RakutenConstants.CATEGORY_LINK_WITH_SPACE
									+ rakutenProduct.getSecondaryCategory().replaceAll("~~", RakutenConstants.CATEGORY_LINK_WITH_SPACE));
						} else {
							categories.add(rakutenProduct.getPrimaryCategory());
						}
					} else {
						if (rakutenProduct.getPrimaryCategory() != null
								&& rakutenProduct.getPrimaryCategory().length() > 0) {
							categories.add(rakutenProduct.getPrimaryCategory());
						}
					}

					if (rakutenProduct.getBrand() != null && rakutenProduct.getBrand().length() > 0) {
						brands.add(rakutenProduct.getBrand());
					}
				}
			}

			logger.info(Log.message(
					"Successfully Add the brand and category into Set from the file : " + file.getAbsolutePath()));
			logger.info(Log.message("Free Memory	: " + Runtime.getRuntime().freeMemory() / (1024 * 1024) + " Mb"));
		} catch (Exception e) {
			logger.error(Log.message("Issues in RakutenCreateBrandCategoryTSVChild job: " + e.getMessage()));
			e.printStackTrace();
		}
	}

	private long getRakutenAdvertiserID(String name) {
		if (name == null || name.length() <= 0) {
			return 0;
		}
		String[] list = name.split("_");
		return Long.parseLong(list[0]);
	}
}
