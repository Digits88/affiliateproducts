package batch.jobs;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import models.Product;
import models.Seller;
import play.libs.F.Promise;
import repositories.Repository;
import services.image.ImageService;
import utils.log.Log;

@Entity
@DiscriminatorValue("SYNC_PRODUCTS_DETAILS")
public class SaveSellerImageJobChild extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(SaveSellerImageJobChild.class);

	@Inject
	protected static Repository repository;

	@Transient
	private Seller seller;

	@Transient
	private ImageService imageService = new ImageService();

	public SaveSellerImageJobChild(Seller seller) {
		super();
		this.seller = seller;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		try {
			String localImageUrl = imageService.saveSellerImage(seller);
			if(localImageUrl != null) {
				// seller.setLocalImageUrl(localImageUrl);
				Seller s = repository.find(Seller.class, "from Seller where id=?", seller.getId());
				s.setLocalImageUrl(localImageUrl);
				repository.update(s);
				logger.info("Finish Save Image: " + seller.getName());
			} else {
				logger.error(Log.message(seller.getName() + " Cannot have right Local Image URL, Please Check Original URL First! "));
			}
			logger.info("----------------------------------------------------------------------");
		} catch (Exception e) {
			logger.error(Log.message("Exception occurred on seller : " + seller.getName() + " | "+ e.getMessage()));
		}
	}

}
