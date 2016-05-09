package batch.jobs;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.Transient;

import models.Product;
import play.modules.guice.InjectSupport;

import org.apache.log4j.Logger;

import repositories.Repository;
import utils.log.Log;

@InjectSupport
public class SyncSKProductsDetailTSVChildDeletion extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(SyncSKProductsDetailTSVChildDeletion.class);

	@Inject
	protected static Repository repository;

	@Transient
	private Long skAdvertiserID;

	@Transient
	private List<String> skuList = null;

	@Transient
	private Set<String> productSKUs = null;

	public SyncSKProductsDetailTSVChildDeletion(Long skAdvertiserID, List<String> skuList, Set<String> productSKUs) {
		this.skAdvertiserID = skAdvertiserID;
		this.skuList = skuList;
		this.productSKUs = productSKUs;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		if (repository == null) {
			logger.info("Repository is Null Value, Please check!!");
		}
		String check = null;
		try {
			if (skuList != null && skuList.size() > 0) {
				for (String sku : skuList) {
					if (!productSKUs.contains(sku)) {
						check = sku;
						Product p = repository.find(Product.class, "from Product where advertiserId=? and sku=?",
								skAdvertiserID, sku);
						p.setInStock(false);
						try {
							p = repository.update(p);
						} catch (Exception e) {
							logger.error(Log.message("Issue is happening on sku : " + check));
							e.printStackTrace();
							logger.error(Log.message(e.getMessage()));
						}
						// logger.info("Updated Product : " +
						// p.getAdvertiserId() + p.getId() + " -- " +
						// p.getName());
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(
					Log.message("Exception occurred in SyncProductsDetailTSVChildDeletion job : " + e.getMessage()));
		}
	}

}
