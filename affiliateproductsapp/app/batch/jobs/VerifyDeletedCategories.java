package batch.jobs;

import java.util.List;

import models.Category;

import org.apache.log4j.Logger;

import utils.log.Log;

public class VerifyDeletedCategories extends AbstractBatchJob {

	private static Logger logger = Logger
			.getLogger(VerifyDeletedCategories.class);

	List<Long> googleTaxoIds;

	public VerifyDeletedCategories(List<Long> googleTaxoIds) {
		this.googleTaxoIds = googleTaxoIds;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		logger.info(Log.message("Started VerifyDeletedCategories Job"));

		List<Long> existingGoogleTaxoIds = Category.find(
				"select c.googleTaxonomy from CategoryV1 c").fetch();
		
		for(Long taxoIdFromDB:existingGoogleTaxoIds){
			if(!googleTaxoIds.contains(taxoIdFromDB)){
				logger.error(Log.message("(X)  Google taxonomy id: '"+taxoIdFromDB+"' is not available in the new File."));
			}
		}

		logger.info(Log.message("Successfully completed verification of taxonomies from file and db"));
	}
}
