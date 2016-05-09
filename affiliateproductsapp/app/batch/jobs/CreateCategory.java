package batch.jobs;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import models.AdvertiserCategory;

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
public class CreateCategory extends AbstractBatchJob {

	@Transient
	public List<Long> sellerIds = null;

	private static Logger logger = Logger.getLogger(CreateCategory.class);

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
			List<String> categoryNames = (List<String>) JPA
					.em()
					.createQuery(
							"SELECT distinct(advertiserCategory) FROM Product")
					.getResultList();
			for (String categoryName : categoryNames) {
				if (categoryName != null && categoryName.length() > 0) {
					AdvertiserCategory category = AdvertiserCategory.find("byName", categoryName)
							.first();
					if (category == null) {
						repository.create(new AdvertiserCategory(categoryName));
					}
				}
			}
			logger.info(Log
					.message("Successfully created all the missing categories..."));
		} catch (Exception e) {
			logger.error(Log
					.message("Exception occurred in CreateCategory job : "
							+ e.getMessage()));
		} finally {
			unlock(this.getClass());
		}
	}

}