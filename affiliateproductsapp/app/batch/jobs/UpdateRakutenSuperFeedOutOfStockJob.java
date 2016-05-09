package batch.jobs;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import constants.RakutenConstants;
import models.Product;
import repositories.Repository;

public class UpdateRakutenSuperFeedOutOfStockJob extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(UpdateRakutenSuperFeedOutOfStockJob.class);

	@Inject
	protected static Repository repository;

	@Transient
	private Long rakutenAdvertiserID;

	@Transient
	private List<String> skuList = null;

	public UpdateRakutenSuperFeedOutOfStockJob(Long rakutenAdvertiserID, List<String> skuList) {
		super();
		this.rakutenAdvertiserID = rakutenAdvertiserID;
		this.skuList = skuList;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		try {
			if (skuList != null && skuList.size() > 0) {
				for (String sku : skuList) {
					Product p = repository.find(Product.class, "from Product where advertiserId=? and sku=?",
							rakutenAdvertiserID, sku);
					p.setInStock(RakutenConstants.PRODUCT_NOT_IN_FEED);
					repository.update(p);
				}
			}
		} catch (Exception e) {
			logger.error("Issues in UpdateRakutenSuperFeedOutOfStockJob runJob : " + e.getMessage());
		}
	}
}
