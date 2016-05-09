package batch.jobs;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.Transient;

import models.Brand;
import models.AdvertiserCategory;
import models.Product;

import org.apache.log4j.Logger;

import repositories.Repository;
import utils.log.Log;

public class RakutenCreateCategoryAllHelper extends AbstractBatchJob {

	private static Logger logger = Logger
			.getLogger(RakutenCreateCategoryAllHelper.class);

	@Inject
	protected static Repository repository;

	@Transient
	private List<String> categories = null;

	public RakutenCreateCategoryAllHelper(List<String> categories) {
		this.categories = categories;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		try {
			logger.info(Log.message("Creating new Category start"));
			for (String category : categories) {
				AdvertiserCategory c = AdvertiserCategory.find("byName", category).first();
				if (c == null) {
					logger.info(Log.message("Creating the category : " + category));
					repository.create(new AdvertiserCategory(category));
				}
			}
			logger.info(Log.message("Creating new Category end "));
		} catch (Exception e) {
			logger.error(Log
					.message("Exception occurred in RakutenCreateCategoryAllHelper job : "
							+ e.getMessage()));
		}
	}

}
