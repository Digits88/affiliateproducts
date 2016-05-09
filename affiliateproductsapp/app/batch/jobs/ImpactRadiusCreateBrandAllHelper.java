package batch.jobs;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.Transient;

import models.Brand;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import repositories.Repository;
import utils.log.Log;

public class ImpactRadiusCreateBrandAllHelper extends AbstractBatchJob {

	private static Logger logger = Logger
			.getLogger(ImpactRadiusCreateBrandAllHelper.class);

	@Inject
	protected static Repository repository;

	@Transient
	private List<String> brands = null;

	public ImpactRadiusCreateBrandAllHelper(List<String> brands) {
		this.brands = brands;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		try {
			logger.info(Log.message("Creating new Brand start"));
			for (String brand : brands) {
				Brand b = Brand.find("byName", brand).first();
				if (b == null) {
					repository.create(new Brand(brand));
					logger.info(Log.message("Created the brand : " + brand));
				}
			}
			logger.info(Log.message("Creating new Brand end "));
		} catch (Exception e) {
			logger.error(Log
					.message("Exception occurred in ImpactRadiusCreateBrandAllHelper job : " + e.getMessage()));
		}
	}

}
