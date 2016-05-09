package batch.jobs.product.synchroniser;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import models.cj.CJProduct;

import org.apache.log4j.Logger;

import play.modules.guice.InjectSupport;
import repositories.Repository;
import services.ProductService;
import utils.log.Log;

/**
 * 
 * Class to synchronize the product details for all the sellers from CJ to
 * affiliateproducts DB
 * 
 */
@InjectSupport
public class DefaultProductSynchroniser extends ProductSynchroniser {

	@Inject
	protected static ProductService productService;

	private static Logger logger = Logger.getLogger(DefaultProductSynchroniser.class);

	@Inject
	protected static Repository repository;

	public DefaultProductSynchroniser(Long advertiserId, File inputFile, Boolean tsvBasedProcess,
			List<Set<String>> productSKUlist) {
		super(advertiserId, inputFile, tsvBasedProcess, productSKUlist);
	}

	@Override
	public void doJob() throws Exception {
		CJProductsCreator cjProductsCreator = new CJProductsCreator(advertiserId, inputFile, tsvBasedProcess);
		List<CJProduct> cjProducts = cjProductsCreator.createCJProducts();
		syncProducts(cjProducts);
	}

	public void syncProducts(List<CJProduct> cjProducts) {
		try {
			Set<String> productSKUs = new HashSet<String>();

			if (cjProducts.size() > 0) {
				for (CJProduct cjProduct : cjProducts) {
					productService.createOrUpdate(cjProduct, inputFile);
					productSKUs.add(cjProduct.getSku());
				}
				productSKUlist.add(productSKUs);

				logger.info("Successfully completed creating the products for seller : " + inputFile + " : "
						+ advertiserId + " <---- Sync Product is done!");
				logger.info("Free Memory	: " + Runtime.getRuntime().freeMemory() / (1024 * 1024) + " Mb");

				// Delete out of sync products logic
				// deleteOutOfSyncProducts(cjProducts, productSKUs);
			}
		} catch (Exception e) {
			logger.error(Log.message("Exception occurred while creating/deleting the products for the seller : "
					+ inputFile + " : " + advertiserId));
			e.printStackTrace();
		}
	}

}