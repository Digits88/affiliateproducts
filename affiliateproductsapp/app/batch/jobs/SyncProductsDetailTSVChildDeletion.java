package batch.jobs;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.Transient;

import models.Product;
import play.modules.guice.InjectSupport;

import org.apache.log4j.Logger;

import repositories.Repository;
import services.image.ImageService;
import utils.log.Log;

@InjectSupport
public class SyncProductsDetailTSVChildDeletion extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(SyncProductsDetailTSVChildDeletion.class);

	@Inject
	protected static Repository repository;

	@Transient
	private Long cjAdvertiserID;

	@Transient
	private List<String> skuList = null;

	@Transient
	private Set<String> productSKUs = null;

	@Transient
	private ImageService imageService = new ImageService();

	public SyncProductsDetailTSVChildDeletion(Long cjAdvertiserID, List<String> skuList, Set<String> productSKUs) {
		this.cjAdvertiserID = cjAdvertiserID;
		this.skuList = skuList;
		this.productSKUs = productSKUs;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		String check = null;
		try {
			// logger.info(Log.message("Start DELETE--UPDATE " + cjAdvertiserID));
			if (skuList != null && skuList.size() > 0) {
				for (String sku : skuList) {
					if (!productSKUs.contains(sku)) {
						check = sku;
						Product p = repository.find(Product.class, "from Product where advertiserId=? and sku=?",
								cjAdvertiserID, sku);
						p.setInStock(false);
						try {
							p = repository.update(p);
						} catch (Exception e) {
							logger.error(Log.message("Issue is happening on sku : " + check));
							e.printStackTrace();
							logger.error(Log.message(e.getMessage()));
						}
					}
				}

			}
			// logger.info(Log.message("Finish DELETE--UPDATE " + cjAdvertiserID));
		} catch (Exception e) {
			logger.error(
					Log.message("Exception occurred in SyncProductsDetailTSVChildDeletion job : " + e.getMessage()));
		}
	}
}
