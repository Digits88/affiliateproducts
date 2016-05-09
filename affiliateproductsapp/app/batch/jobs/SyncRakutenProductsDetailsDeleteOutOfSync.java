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
public class SyncRakutenProductsDetailsDeleteOutOfSync extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(SyncRakutenProductsDetailsDeleteOutOfSync.class);

	@Inject
	protected static Repository repository;

	@Transient
	private Long rakutenAdvertiserID;

	@Transient
	private List<String> skuList = null;

	@Transient
	private Set<String> productSKUs = null;

	@Transient
	private String threadName;

	public SyncRakutenProductsDetailsDeleteOutOfSync(Long rakutenAdvertiserID, List<String> skuList,
			Set<String> productSKUs, String threadName) {
		super();
		this.rakutenAdvertiserID = rakutenAdvertiserID;
		this.skuList = skuList;
		this.productSKUs = productSKUs;
		this.threadName = threadName;
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
						Product p = repository.find(Product.class, "from Product where advertiserId=? and sku=?",
								rakutenAdvertiserID, sku);
						p.setInStock(false);
						try {
							p = repository.update(p);
						} catch (Exception e) {
							logger.error(Log.message("Issue is happening on sku : " + check));
							e.printStackTrace();
							logger.error(Log.message(e.getMessage()));
						}
						// logger.info(threadName + " -- Update Out Of Stock :
						// Advertiser " + rakutenAdvertiserID + " --> SKU : " +
						// sku);
					}
				}
			}
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
		}
	}
}
