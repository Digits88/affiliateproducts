package batch.jobs;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import constants.RakutenConstants;
import models.Product;
import repositories.Repository;
import utils.log.Log;

public class UpdateRakutenInLatestFeedToFalse extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(UpdateRakutenSuperFeedOutOfStockJob.class);

	@Inject
	protected static Repository repository;

	@Transient
	private Long rakutenAdvertiserID;

	public UpdateRakutenInLatestFeedToFalse(Long rakutenAdvertiserID) {
		super();
		this.rakutenAdvertiserID = rakutenAdvertiserID;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		try {
			String query = "UPDATE Product SET in_latest_feed=?1 WHERE advertiser_id=?2";
			int updatedRow = repository.update(query, RakutenConstants.PRODUCT_NOT_IN_FEED, rakutenAdvertiserID);
			if (updatedRow > 0) {
				logger.info("-----> Update \'in_latest_feed\' ( " + updatedRow + " products) to 0 for " + rakutenAdvertiserID + " is done!!!");
			} else {
				logger.error(Log.message("!!! Update \'in_latest_feed\' to 0 for " + rakutenAdvertiserID + " is Having Issue !!!"));
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
