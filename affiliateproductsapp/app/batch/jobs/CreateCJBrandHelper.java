package batch.jobs;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import models.Brand;
import repositories.Repository;
import utils.log.Log;

public class CreateCJBrandHelper extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(CreateCJBrandHelper.class);

	@Inject
	protected static Repository repository;
	
	@Transient
	private List<String> brands;

	public CreateCJBrandHelper(List<String> brands) {
		super();
		this.brands = brands;
	}
	
	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		Brand b;
		try {
			logger.info(Log.message("Creating new Brand start"));
			for (String brand : brands) {
				b = Brand.find("byName", brand).first();
				if (b == null) {
					logger.info("Creating the brand : " + brand);
					repository.create(new Brand(brand));
				}
			}
		} catch (Exception e) {
			logger.error(Log.message("Exception occurred in CJCreateBrandHelper job : " + e.getMessage()));
		}
	}
}


