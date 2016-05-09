package batch.jobs;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.Transient;

import models.AdvertiserCategory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import repositories.Repository;
import utils.log.Log;

public class ImpactRadiusCreateCategoryAllHelper  extends AbstractBatchJob {

	private static Logger logger = Logger
			.getLogger(ImpactRadiusCreateCategoryAllHelper.class);

	@Inject
	protected static Repository repository;

	@Transient
	private List<String> categories = null;

	public ImpactRadiusCreateCategoryAllHelper(List<String> categories) {
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
				// logger.info(Log.message("Working on : " + category));
				AdvertiserCategory c = AdvertiserCategory.find("byName", category).first();
				if (c == null) {
					repository.create(new AdvertiserCategory(category));
					logger.info("Created the category : " + category);
				}
			}
			logger.info(Log.message("Creating new Category end "));
		} catch (Exception e) {
			logger.error(Log
					.message("Exception occurred with : " + e.getMessage()));
		}
	}

}
