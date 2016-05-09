package batch.jobs;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.Transient;

import models.Brand;
import models.Product;

import org.apache.log4j.Logger;

import repositories.Repository;
import utils.log.Log;

public class RakutenCreateBrandAllHelper extends AbstractBatchJob {

	private static Logger logger = Logger
			.getLogger(RakutenCreateBrandAllHelper.class);

	@Inject
	protected static Repository repository;

	@Transient
	private List<String> brands = null;

	public RakutenCreateBrandAllHelper(List<String> brands) {
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
					logger.info(Log.message("Creating the brand : " + brand));
					repository.create(new Brand(brand));
				}
			}
			logger.info(Log.message("Creating new Brand end "));
		} catch (Exception e) {
			logger.error(Log
					.message("Exception occurred in RakutenCreateBrandAllHelper job : "
							+ e.getMessage()));
		}
	}

}
