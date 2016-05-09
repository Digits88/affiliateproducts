package batch.jobs.product.synchroniser;

import java.io.File;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import models.Product;
import models.cj.CJProduct;
import models.searskmart.SearsKmartProduct;

import org.apache.log4j.Logger;

import batch.jobs.AbstractBatchJob;
import play.db.jpa.JPA;
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
public class ProductSynchroniser extends AbstractBatchJob {

	@Inject
	protected static ProductService productService;

	private static Logger logger = Logger.getLogger(ProductSynchroniser.class);

	@Inject
	protected static Repository repository;

	public Long advertiserId = null;
	public File inputFile = null;
	public Boolean tsvBasedProcess = false;
	public List<Set<String>> productSKUlist = null;

	public ProductSynchroniser(Long advertiserId, File inputFile,
			Boolean tsvBasedProcess, List<Set<String>> productSKUlist) {
		this.advertiserId = advertiserId;
		this.inputFile = inputFile;
		this.tsvBasedProcess = tsvBasedProcess;
		this.productSKUlist = productSKUlist;
	}

	// Setting the in_stock to false if the product does not exist in the feed
	// anymore
	public void deleteOutOfSyncProducts(List<CJProduct> cjProducts,
			Set<String> productSKUs) {

		logger.info(Log.message("Delect out of sync -- Start"));

		if (cjProducts != null && cjProducts.get(0) != null
				&& tsvBasedProcess == true) {
			List<String> existingProductSkus = (List<String>) JPA
					.em()
					.createQuery(
							"SELECT sku FROM Product where advertiserId="
									+ cjProducts.get(0).getAdvertiserId())
					.getResultList();

			if (existingProductSkus != null && existingProductSkus.size() > 0) {
				for (String sku : existingProductSkus) {
					if (!productSKUs.contains(sku)) {
						Product p = repository.find(Product.class,
								"from Product where advertiserId=? and sku=?",
								cjProducts.get(0).getAdvertiserId(), sku);
						p.setInStock(false);
						repository.update(p);
					}
				}
			}
			logger.info(Log
					.message("Successfully completed deleting the products that are out of sync for the seller : "
							+ inputFile + " : " + advertiserId));
		}
	}
}