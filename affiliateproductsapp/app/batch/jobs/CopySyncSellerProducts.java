package batch.jobs;

import javax.inject.Inject;

import models.Seller;

import org.apache.log4j.Logger;

import repositories.Repository;
import services.ProductService;
import utils.log.Log;
import batch.jobs.product.synchroniser.BRProductSynchroniser;
import batch.jobs.product.synchroniser.DefaultProductSynchroniser;
import enums.SellerEnum;

/**
 * 
 * Class to synchronize the product details for all the brands from SYW to MAG
 * DB
 * 
 */
public class CopySyncSellerProducts extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(CopySyncSellerProducts.class);

	@Inject
	protected static Repository repository;

	@Inject
	protected static ProductService productService;

	private Long advertiserId;

	CopySyncSellerProducts(Long advertiserId) {
		this.advertiserId = advertiserId;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		Seller seller = Seller.find("byAdvertiserId", advertiserId).first();
		invokeProductCreator(seller);
	}
	
	private void invokeProductCreator(Seller seller) {
		logger.info(Log
				.message("Invoking the product creator for the seller : "
						+ seller.getName()));
		if (seller.getName().trim()
				.equals(SellerEnum.BANANAREPUBLIC.sellerName)) {
			new BRProductSynchroniser(seller.getAdvertiserId(), null, false, null).now();
			// 10 14
			// new BRProductSynchroniser(seller.getAdvertiserId(), null, false).now();
		} else {
			new DefaultProductSynchroniser(seller.getAdvertiserId(), null, false, null).now();
			// 10 14 -- Home Depot
			// new DefaultProductSynchroniser(seller.getAdvertiserId(), null, false).now();
		}
	}
}