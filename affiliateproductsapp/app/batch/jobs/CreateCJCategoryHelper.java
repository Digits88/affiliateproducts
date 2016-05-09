package batch.jobs;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import constants.CJProductsConstants;
import models.AdvertiserCategory;
import repositories.Repository;
import utils.log.Log;

public class CreateCJCategoryHelper extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(CreateCJCategoryHelper.class);

	@Inject
	protected static Repository repository;
	
	@Transient
	private List<String> categories;

	public CreateCJCategoryHelper(List<String> categories) {
		super();
		this.categories = categories;
	}
	
	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		AdvertiserCategory c;
		try {
			logger.info(Log.message("Creating new Category start"));
			for (String category : categories) {
				if (category.length() > 255 && category.contains(CJProductsConstants.SPALES_LONG_CATE_PATTERN)) {
					logger.info("===> Long Category : " + category);
					category = category.split("-")[0].replaceAll("\"", "").trim();
					logger.info("===> After Replace : " + category);
				}
				c = AdvertiserCategory.find("byName", category).first();
				if (c == null) {
					repository.create(new AdvertiserCategory(category));
					logger.info("Created the Category : " + category);
				}
			}
		} catch (Exception e) {
			logger.error(Log.message("Exception occurred in CreateCJCategoryHelper job : " + e.getMessage()));
		}
	}
}
