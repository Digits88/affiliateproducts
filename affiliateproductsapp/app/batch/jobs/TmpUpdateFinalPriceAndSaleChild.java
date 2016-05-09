package batch.jobs;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import models.Product;
import models.Seller;

import org.apache.log4j.Logger;

import repositories.Repository;
import utils.PriceUtils;
import utils.log.Log;

@Entity
@DiscriminatorValue("SYNC_PRODUCTS_DETAILS")
public class TmpUpdateFinalPriceAndSaleChild extends AbstractBatchJob {

	private static Logger logger = Logger
			.getLogger(TmpUpdateFinalPriceAndSaleChild.class);

	@Inject
	protected static Repository repository;

	Long sellerId = null;

	public TmpUpdateFinalPriceAndSaleChild(Long id) {
		sellerId = id;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		try {
			Seller seller = Seller.findById(sellerId);
			List<Product> products = Product.find("bySeller", seller).fetch();
			for (Product p : products) {
				System.out.println("Updating the product : " + p.getId());
				BigDecimal finalPrice = p.getSalePrice() != null ? p
						.getSalePrice() : p.getPrice();
				p.setFinalPrice(finalPrice);
				p.setSale(PriceUtils.getSale(p.getPrice(), finalPrice));
				repository.update(p);
			}

		} catch (Exception e) {
			logger.error(Log
					.message("Exception occurred in TmpUpdateFinalPriceAndSaleChild job : "
							+ e.getMessage()));
		} finally {
		}
	}

}