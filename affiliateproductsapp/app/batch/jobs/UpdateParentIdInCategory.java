package batch.jobs;

import java.util.List;

import javax.inject.Inject;

import models.Category;

import org.apache.log4j.Logger;

import repositories.Repository;
import utils.log.Log;

public class UpdateParentIdInCategory extends AbstractBatchJob {

	private static Logger logger = Logger
			.getLogger(UpdateParentIdInCategory.class);

	private static final String CATEGORY_DELIMITER = " > ";

	List<Long> googleTaxoIds;
	
	public UpdateParentIdInCategory(List<Long> googleTaxoIds){
		this.googleTaxoIds=googleTaxoIds;
	}
	
	@Inject
	protected static Repository repository;

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		logger.info(Log.message("Started UpdateParentIdInCategory Job"));

		List<Category> categories = Category.find("parent is null").fetch();

		while (categories != null && !categories.isEmpty()) {
			Category category = categories.get(0);
			String fullParentTaxonomy = category.getParentName();
			String parentCategoryName = null;
			if (fullParentTaxonomy != null) {
				String[] parentCategories = fullParentTaxonomy
						.split(CATEGORY_DELIMITER);
				parentCategoryName = parentCategories[parentCategories.length - 1];

				logger.info("parentCategory: "+fullParentTaxonomy+", parentCategoryName: "+parentCategoryName);
				Category parentCat = Category.find("byName",
						parentCategoryName).first();
				if (parentCat != null) {
					logger.info("Found parentCategory record: "+parentCat.toString());
					int updatedCount = repository
							.update("update CategoryV1 set parent = ?1 where parentName = ?2",
									parentCat.getId(), fullParentTaxonomy);
					
					logger.info(Log.message("Updated " + updatedCount
							+ " records and set parentID=" + parentCat.getId()
							+ ", with parentName= " + fullParentTaxonomy));
					
					categories = Category.find("parent is null").fetch();
					
					logger.info(Log.message("Found "+categories.size()+" categories that are still without parent category ID"));
				} else {
					logger.error(Log.message("Parent Category for the record: "
							+ category.getId() + ", not found"));
					break;
				}

			}
		}
		logger.info(Log.message("Successfully completed updating parent ID in the category table. will trigger verification in 2 seconds"));
		new VerifyDeletedCategories(googleTaxoIds).in(2);
	}
}
