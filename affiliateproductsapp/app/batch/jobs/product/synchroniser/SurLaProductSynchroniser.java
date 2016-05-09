package batch.jobs.product.synchroniser;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
public class SurLaProductSynchroniser extends ProductSynchroniser {

	@Inject
	protected static ProductService productService;

	private static Logger logger = Logger
			.getLogger(SurLaProductSynchroniser.class);

	@Inject
	protected static Repository repository;
	
	static Map<String, String> toReplace = new HashMap<String, String>();
	
	static {
		toReplace.put("Ã¢Â€Â™", "'");
		toReplace.put("Ã¢Â„Â¢", "™");
		toReplace.put("â¢", "™");
		toReplace.put("Ã‚Â®", "®");
		toReplace.put("Â®", "®");
		toReplace.put("Ã‚Â", "");
		toReplace.put("Â", "");
		toReplace.put("â", "'");
	}
	


	public SurLaProductSynchroniser(Long advertiserId, File inputFile,
			Boolean tsvBasedProcess, List<Set<String>> productSKUlist) {
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
					String modifiedName = getModifiedName(cjProduct.getName());
					cjProduct.setName(modifiedName);
					// 10 14 -- Home Depot
					productService.createOrUpdate(cjProduct);
					productSKUs.add(cjProduct.getSku());
				}
				
				// 10 14 -- Home Depot
				productSKUlist.add(productSKUs);
				
				logger.info(Log
						.message("Successfully completed creating the products for : "
								+ inputFile + " : " + advertiserId + " <---- Update is done!"));
				
				logger.info(Log.message("Free Memory	: " + Runtime.getRuntime().freeMemory()/(1024*1024) + " Mb"));

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

	private String getModifiedName(String name) {
		if(name!=null){
			for (Map.Entry<String, String> entry : toReplace.entrySet()) {
			    name = name.replaceAll(entry.getKey(), entry.getValue());
			}			
		}
		return name;
	}

	public static void main(String args[]) {
		String name = "BodumÃ‚Â® PeboÃ¢Â„Â¢ Vacuum Coffee Maker";
		System.out.println(new SurLaProductSynchroniser(null, null, null, null).getModifiedName(name));	// 10 14
	}

}