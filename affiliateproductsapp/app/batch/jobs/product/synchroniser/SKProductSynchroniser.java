package batch.jobs.product.synchroniser;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import models.cj.CJProduct;
import models.searskmart.SearsKmartProduct;

import org.apache.log4j.Logger;

import play.modules.guice.InjectSupport;
import repositories.Repository;
import services.ProductService;
import services.SKProductService;
import services.impl.DefaultSKProductService;
import utils.log.Log;

@InjectSupport
public class SKProductSynchroniser extends ProductSynchroniser {

	@Inject
	protected static SKProductService skProductService;

	private static Logger logger = Logger
			.getLogger(SKProductSynchroniser.class);

	@Inject
	protected static Repository repository;

	public SKProductSynchroniser(Long advertiserId, File inputFile,
			Boolean tsvBasedProcess, List<Set<String>> productSKUlist) {
		super(advertiserId, inputFile, tsvBasedProcess, productSKUlist);
	}

	@Override
	public void doJob() throws Exception {
		SKProductCreator skProductsCreator = new SKProductCreator(advertiserId,
				inputFile);
		List<SearsKmartProduct> skProducts = skProductsCreator
				.createSKProducts();
		syncSKProducts(skProducts);
	}

	private void syncSKProducts(List<SearsKmartProduct> skProducts) {

		Set<String> productSKUs = new HashSet<String>();
		try {
			if (skProducts.size() > 0) {
				for (SearsKmartProduct skProduct : skProducts) {
					skProductService.createOrUpdateSK(skProduct);
					productSKUs.add(skProduct.getParentName());
					// productSKUs.add(skProduct.getPartnumber());
				}

				productSKUlist.add(productSKUs);

				logger.info(Log.message("productSKUlist size : "
						+ productSKUlist.size()));
				logger.info(Log
						.message("Successfully completed creating the products for seller : "
								+ inputFile + " <---- Update is done!"));

				logger.info(Log.message("Free Memory	: " + Runtime.getRuntime().freeMemory()/(1024*1024) + " Mb"));
				// Delete out of sync products logic
				// deleteOutOfSyncSKProducts(skProducts, productSKUs);
			}
		} catch (Exception e) {
			logger.error(Log
					.message("Exception occurred while creating/deleting the products for the seller : "
							+ inputFile + " : " + advertiserId));
			e.printStackTrace();
		}
	}
}
