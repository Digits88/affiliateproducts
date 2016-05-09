package batch.jobs.product.synchroniser;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import batch.jobs.AbstractBatchJob;
import models.rakuten.RakutenProduct;
import repositories.Repository;
import services.rakuten.RakutenProductService;
import utils.log.Log;

public class RakutenSuperFeedSynchroniser extends AbstractBatchJob {

	@Inject
	protected static RakutenProductService rakutenProductService;

	private static Logger logger = Logger.getLogger(RakutenSuperFeedSynchroniser.class);

	@Inject
	protected static Repository repository;

	private Long advertiserId;
	private File inputFile;
	private List<Set<String>> productSKUList;
	private String threadName;

	public RakutenSuperFeedSynchroniser(Long advertiserId, File inputFile, List<Set<String>> productSKUList, String threadName) {
		super();
		this.advertiserId = advertiserId;
		this.inputFile = inputFile;
		this.productSKUList = productSKUList;
		this.threadName = threadName;
	}

	public void doJob() throws Exception {
		RakutenProductCreator rakutenProductCreator = new RakutenProductCreator(advertiserId, inputFile);
		List<RakutenProduct> rakutenProducts = rakutenProductCreator.createRakutenProducts();
		syncRakutenProducts(rakutenProducts);
	}

	private void syncRakutenProducts(List<RakutenProduct> rakutenProducts) {
		Set<String> productSKUs = new HashSet<String>();
		try {
			if (rakutenProducts.size() > 0) {
				for (RakutenProduct rakutenProduct : rakutenProducts) {
					rakutenProductService.createOrUpdate(rakutenProduct, threadName);
					productSKUs.add(rakutenProduct.getSku());
				}
				productSKUList.add(productSKUs);
				
				logger.info(inputFile.getName() + " has products # : " + rakutenProducts.size());
				logger.info("Successfully completed creating the products for seller : " + inputFile
						+ " <---- Update is done!");
				logger.info("Free Memory	: " + Runtime.getRuntime().freeMemory() / (1024 * 1024) + " Mb");
			}
		} catch (Exception e) {
			logger.error(Log.message("Exception occurred in RakutenProductSynchroniser syncRakutenProducts : "
					+ inputFile + " : " + advertiserId + e.getMessage()));
			e.printStackTrace();
		}
	}

}
