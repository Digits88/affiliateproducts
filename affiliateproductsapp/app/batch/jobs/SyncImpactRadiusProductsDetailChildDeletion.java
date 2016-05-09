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
public class SyncImpactRadiusProductsDetailChildDeletion extends
		AbstractBatchJob {

	private static Logger logger = Logger
			.getLogger(SyncImpactRadiusProductsDetailChildDeletion.class);

	@Inject
	protected static Repository repository;

	@Transient
	private Long impactRadiusAdvertiserID;

	@Transient
	private List<String> skuList = null;

	@Transient
	private Set<String> productSKUs = null;

	public SyncImpactRadiusProductsDetailChildDeletion(
			Long impactRadiusAdvertiserID, List<String> skuList,
			Set<String> productSKUs) {
		this.impactRadiusAdvertiserID = impactRadiusAdvertiserID;
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
			if (skuList != null && skuList.size() > 0) {
				for (String sku : skuList) {
					if (!productSKUs.contains(sku)) {
						check = sku;
						Product p = repository.find(Product.class,
								"from Product where advertiserId=? and sku=?",
								impactRadiusAdvertiserID, sku);
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
		} catch (Exception e) {
			logger.error(Log
					.message("### Issues in SyncImpactRadiusProductsDetailChildDeletion runJob : "
							+ e.getMessage()));
		}
	}
}
