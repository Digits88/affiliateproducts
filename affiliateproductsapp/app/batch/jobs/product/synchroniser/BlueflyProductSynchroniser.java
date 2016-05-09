package batch.jobs.product.synchroniser;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import models.cj.CJProduct;

import org.apache.log4j.Logger;

import play.Play;
import play.modules.guice.InjectSupport;
import repositories.Repository;
import services.ProductService;
import utils.log.Log;

/**
 * 
 * Class to synchronize the product details for all the sellers from CJ to
 * affiliateproducts DB
 * 
 */
@InjectSupport
public class BlueflyProductSynchroniser extends ProductSynchroniser {

	@Inject
	protected static ProductService productService;

	private static Logger logger = Logger
			.getLogger(BlueflyProductSynchroniser.class);

	@Inject
	protected static Repository repository;

	public BlueflyProductSynchroniser(Long advertiserId, File inputFile,
			Boolean tsvBasedProcess, List<Set<String>> productSKUlist) {
		super(advertiserId, inputFile, tsvBasedProcess, productSKUlist);
	}

	@Override
	public void doJob() throws Exception {
		CJProductsCreator cjProductsCreator = new CJProductsCreator(advertiserId, inputFile, tsvBasedProcess);
		List<CJProduct> cjProducts = cjProductsCreator.createCJProducts();
		syncProducts(cjProducts);
	}

	private void syncProducts(List<CJProduct> cjProducts) {

		try {
			Set<String> productSKUs = new HashSet<String>();

			if (cjProducts.size() > 0) {

				for (CJProduct cjProduct : cjProducts) {
					String modifiedUrl = getModifiedImageUrl(cjProduct
							.getImageURL());
					if (modifiedUrl != null) {
						cjProduct.setImageURL(modifiedUrl);
					}
					// 10 14 -- Home Depot
					productService.createOrUpdate(cjProduct);
					productSKUs.add(cjProduct.getSku());
				}
				
				// 10 14 -- Home Depot
				productSKUlist.add(productSKUs);
				
				logger.info(Log
						.message("Successfully completed creating the products for : "
								+ inputFile + " : " + advertiserId));

				// Delete out of sync products logic
				// deleteOutOfSyncProducts(cjProducts, productSKUs);
			}
		} catch (Exception e) {
			logger.error(Log
					.message("Exception occurred while creating/deleting the products for : "
							+ inputFile + " : " + advertiserId));
			e.printStackTrace();
		}
	}

	private String getModifiedImageUrl(String url) {
		String modifiedUrl = null;
		int widthValue = 0;
		int heightValue = 0;
		String[] splittedUrl = url.split("\\?");
		if (splittedUrl != null && splittedUrl.length > 1) {
			String justUrl = splittedUrl[0];
			String queryParams = splittedUrl[1];
			if (queryParams.contains("width=")
					&& queryParams.contains("height=")) {
				String[] individualParams = queryParams.split("&");
				for (int i = 0; i < individualParams.length; i++) {
					if (individualParams[i].contains("width")) {
						widthValue = Integer.parseInt(individualParams[i]
								.split("=")[1]);
					} else if (individualParams[i].contains("height")) {
						heightValue = Integer.parseInt(individualParams[i]
								.split("=")[1]);
					}
				}
				if (widthValue != 0 && heightValue != 0) {
					int adjustedWidth = getAdjustedWidth(widthValue,
							heightValue);
					int adjustedHeight = getAdjustedHeight(widthValue,
							heightValue);
					modifiedUrl = justUrl + "?";
					for (int i = 0; i < individualParams.length; i++) {

						if (individualParams[i].contains("width=")) {
							modifiedUrl = modifiedUrl + "width="
									+ adjustedWidth;
						} else if (individualParams[i].contains("height=")) {
							modifiedUrl = modifiedUrl + "height="
									+ adjustedHeight;
						} else {
							modifiedUrl = modifiedUrl + individualParams[i];
						}
						if (i < individualParams.length - 1) {
							modifiedUrl = modifiedUrl + "&";
						}
					}

				}
			}

		}
		return modifiedUrl;
	}

	private int getAdjustedWidth(int widthValue, int heightValue) {
		int maxSize = Integer.parseInt(Play.configuration.getProperty("affiliate.bluefly.product.image.max.size"));
		if(widthValue > heightValue){
			return maxSize;
		}else{
			int proportionalWidth = (widthValue * maxSize) /  heightValue;
			return proportionalWidth;
		}
	}

	private int getAdjustedHeight(int widthValue, int heightValue) {
		int maxSize = Integer.parseInt(Play.configuration.getProperty("affiliate.bluefly.product.image.max.size"));
		if(heightValue > widthValue){
			return maxSize;
		}else{
			int proportionalHeight = (heightValue * maxSize) /  widthValue;
			return proportionalHeight;
		}
	}
}