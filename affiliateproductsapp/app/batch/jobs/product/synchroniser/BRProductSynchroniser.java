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
public class BRProductSynchroniser extends ProductSynchroniser {

	@Inject
	protected static ProductService productService;

	private static Logger logger = Logger.getLogger(BRProductSynchroniser.class);

	@Inject
	protected static Repository repository;

	public BRProductSynchroniser(Long advertiserId, File inputFile, Boolean tsvBasedProcess, List<Set<String>> productSKUlist) {
		super(advertiserId, inputFile, tsvBasedProcess, productSKUlist);
	}

	@Override
	public void doJob() throws Exception {
		CJProductsCreator cjProductsCreator = new CJProductsCreator(advertiserId, inputFile, tsvBasedProcess);
		List<CJProduct> cjProducts = cjProductsCreator.createCJProducts();
		syncProducts(cjProducts);
	}

	private void syncProducts(List<CJProduct> cjProducts) {

		try {
			Set<String> productSKUs = new HashSet<String>();

			if (cjProducts.size() > 0) {

				for (CJProduct cjProduct : cjProducts) {
					cjProduct.setSku(cjProduct.getSku().split("_")[0]);
					cjProduct.setName(cjProduct.getName().split("Size")[0]);
					productService.createOrUpdate(cjProduct);
					productSKUs.add(cjProduct.getSku());
				}
				
				// 10 14 -- Home Depot
				productSKUlist.add(productSKUs);
				
				logger.info(Log
						.message("Successfully completed creating the products for : "
								+ inputFile + " : " + advertiserId));

				// Delete out of sync products logic
				// deleteOutOfSyncProducts(cjProducts, productSKUs);
			}
		} catch (Exception e) {
			logger.error(Log
					.message("Exception occurred while creating/deleting the products for : "
							+ inputFile + " : " + advertiserId));
			e.printStackTrace();
		}
	}

}