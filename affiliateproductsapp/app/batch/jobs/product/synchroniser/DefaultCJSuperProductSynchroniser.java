package batch.jobs.product.synchroniser;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import batch.jobs.AbstractBatchJob;
import models.cj.CJProduct;
import services.ProductService;
import utils.log.Log;

public class DefaultCJSuperProductSynchroniser extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(DefaultCJSuperProductSynchroniser.class);

	@Inject
	protected static ProductService productService;

	private Long advertiserId = null;
	private File inputFile = null;
	private Boolean tsvBasedProcess = false;
	private List<Set<String>> productSKUlist = null;
	private String thread;

	public DefaultCJSuperProductSynchroniser(Long advertiserId, File inputFile, Boolean tsvBasedProcess,
			List<Set<String>> productSKUlist, String thread) {
		super();
		this.advertiserId = advertiserId;
		this.inputFile = inputFile;
		this.tsvBasedProcess = tsvBasedProcess;
		this.productSKUlist = productSKUlist;
		this.thread = thread;
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
					productService.createOrUpdate(cjProduct, inputFile, thread);
					productSKUs.add(cjProduct.getSku());
				}
				productSKUlist.add(productSKUs);

				logger.info("Successfully completed creating the products for seller : " + inputFile + " : "
						+ advertiserId + " <---- Sync Product is done!");
				logger.info("==============>>> " + thread + " is synced already !!! <<<============");
				logger.info("Free Memory	: " + Runtime.getRuntime().freeMemory() / (1024 * 1024) + " Mb");
			}
		} catch (Exception e) {
			logger.error(Log.message("Exception occurred while creating/deleting the products for the seller : "
					+ inputFile + " : " + advertiserId));
			e.printStackTrace();
		}
	}

}
