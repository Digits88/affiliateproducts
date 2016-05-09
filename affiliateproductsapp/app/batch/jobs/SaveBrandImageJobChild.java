package batch.jobs;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import models.Brand;
import models.Seller;
import repositories.Repository;
import services.image.ImageService;
import utils.log.Log;

public class SaveBrandImageJobChild extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(SaveBrandImageJobChild.class);

	@Inject
	protected static Repository repository;

	@Transient
	private List<Brand> brandList;

	@Transient
	private ImageService imageService = new ImageService();

	public SaveBrandImageJobChild(List<Brand> brandList) {
		super();
		this.brandList = brandList;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		try {
			for (Brand brand : brandList) {
				String localImageUrl = imageService.saveBrandImage(brand);
				if(localImageUrl != null) {
					Brand b = repository.find(Brand.class, "from Brand where id=?", brand.getId());
					b.setLocalImageUrl(localImageUrl);
					repository.update(b);
					logger.info("Finish Brand Image: " + b.getName());
				} else {
					logger.error(Log.message("Brand : " + brand.getName() + " Cannot have right Local Image URL, Please Check Original URL First! "));
				}
				logger.info("----------------------------------------------------------------------");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}