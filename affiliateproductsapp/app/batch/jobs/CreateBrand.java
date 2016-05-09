package batch.jobs;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import models.Brand;
import models.Seller;

import org.apache.log4j.Logger;

import play.db.jpa.JPA;
import repositories.Repository;
import utils.log.Log;

/**
 * 
 * Class to synchronize the product details for all the sellers from CJ to AFFILIATEPRODUCTS
 * DB
 * 
 */
@Entity
@DiscriminatorValue("SYNC_PRODUCTS_DETAILS")
public class CreateBrand extends AbstractBatchJob {

	@Transient
	public List<Long> sellerIds = null;

	private static Logger logger = Logger.getLogger(CreateBrand.class);

	@Inject
	protected static Repository repository;

	@Override
	public void doJob() throws Exception {
		if (tryLock(this.getClass())) {
			runJob();
		}
	}

	private void runJob() {
		try {
			// To get all the brands from product feed to Brand table
			List<String> brandNames = (List<String>) JPA
					.em()
					.createQuery(
							"SELECT distinct(manufacturerName) FROM Product")
					.getResultList();
			for (String brandName : brandNames) {
				if (brandName != null) {
					Brand brand = Brand.find("byName", brandName).first();
					if (brand == null) {
						repository.create(new Brand(brandName));
					}
				}
			}

			// Adding all the sellers as brands - Used as brand when the
			// manufacturer_name is empty in product feed
			List<Seller> sellerList = Seller.findAll();
			for (Seller seller : sellerList) {
				if (seller != null) {
					Brand brand = Brand.find("byName", seller.getName())
							.first();
					if (brand == null) {
						repository.create(new Brand(seller.getName()));
					}
				}
			}

			logger.info(Log
					.message("Successfully created all the missing brands..."));
		} catch (Exception e) {
			logger.error(Log.message("Exception occurred in CreateBrand job : "
					+ e.getMessage()));
		} finally {
			unlock(this.getClass());
		}
	}

}