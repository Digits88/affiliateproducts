package batch.jobs.product.synchroniser;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import models.cj.CJProduct;
import models.impactradius.ImpactRadiusProduct;

import org.apache.log4j.Logger;

import batch.jobs.AbstractBatchJob;
import play.modules.guice.InjectSupport;
import repositories.Repository;
import services.ProductService;
import services.impactradius.ImpactRadiusProductService;
import utils.log.Log;

@InjectSupport
public class ImpactRadiusProductSynchroniser extends AbstractBatchJob {

	@Inject
	protected static ImpactRadiusProductService impactRadiusProductService;

	private static Logger logger = Logger.getLogger(ImpactRadiusProductSynchroniser.class);

	@Inject
	protected static Repository repository;

	private Long advertiserId = null;
	private File inputFile = null;
	private List<Set<String>> productSKUlist = null;

	public ImpactRadiusProductSynchroniser(Long advertiserId, File inputFile, List<Set<String>> productSKUlist) {
		super();
		this.advertiserId = advertiserId;
		this.inputFile = inputFile;
		this.productSKUlist = productSKUlist;
	}

	@Override
	public void doJob() throws Exception {
		ImpactRadiusProductCreator impactRadiusProductsCreator = new ImpactRadiusProductCreator(advertiserId, inputFile);
		List<ImpactRadiusProduct> impactRadiusProducts = impactRadiusProductsCreator.createImpactRadiusProducts();
		syncImpactRadiusProducts(impactRadiusProducts);
	}

	private void syncImpactRadiusProducts(List<ImpactRadiusProduct> impactRadiusProducts) {

		Set<String> productSKUs = new HashSet<String>();
		try {
			if (impactRadiusProducts.size() > 0) {
				for (ImpactRadiusProduct impactRadiusProduct : impactRadiusProducts) {
					impactRadiusProductService.createOrUpdate(impactRadiusProduct);
					productSKUs.add(impactRadiusProduct.getUniqueMerchantSKU());
				}

				productSKUlist.add(productSKUs);
				
				logger.info("+++++++++++++++++++++++++++++++++++++++ productSKUlist size : " + productSKUlist.size() + " +++++++++++++++++++++++++++++++++++++++++++++++++++++");
				logger.info("Successfully completed creating the products for seller : " + inputFile + " <---- Update is done!");
				logger.info("Free Memory : " + Runtime.getRuntime().freeMemory() / (1024 * 1024) + " Mb");
				logger.info("Max Memory  : " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " Mb");
				long usedMem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
				logger.info("Used Memory : " + usedMem + " Mb	<----");
			}
		} catch (Exception e) {
			logger.error(Log.message("Exception occurred while creating/deleting the products for the seller : "
					+ inputFile + " : " + advertiserId));
			e.printStackTrace();
		}
	}
}
