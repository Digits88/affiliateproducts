package batch.jobs.product.synchroniser;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import models.cj.CJProduct;

import org.apache.log4j.Logger;

import repositories.Repository;
import services.ProductService;
import utils.log.Log;

public class UGGProductSynchroniser extends ProductSynchroniser {

	@Inject
	protected static ProductService productService;

	private static Logger logger = Logger.getLogger(UGGProductSynchroniser.class);

	@Inject
	protected static Repository repository;

	public UGGProductSynchroniser(Long advertiserId, File inputFile, Boolean tsvBasedProcess, List<Set<String>> productSKUlist) {
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
					String[] sku_line = cjProduct.getSku().split("_");
					String sku = (sku_line.length == 1) ? sku_line[0] : sku_line[0]+"-"+sku_line[1];
					cjProduct.setSku(sku);
					
					productService.createOrUpdate(cjProduct);
					productSKUs.add(cjProduct.getSku());
				}
				
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

