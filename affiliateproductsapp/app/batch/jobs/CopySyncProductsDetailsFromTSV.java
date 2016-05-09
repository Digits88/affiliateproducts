package batch.jobs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import models.cj.CJProduct;

import org.apache.log4j.Logger;

import repositories.Repository;
import services.ProductService;
import utils.log.Log;
import batch.jobs.product.synchroniser.BRProductSynchroniser;
import batch.jobs.product.synchroniser.DefaultProductSynchroniser;
import enums.SellerEnum;

/**
 * 
 * Class to synchronize the product details for all the sellers from CJ to
 * affiliateproducts DB
 * 
 */
public class CopySyncProductsDetailsFromTSV extends AbstractBatchJob {

	@Inject
	protected static ProductService productService;

	public File inputFile = null;

	private static Logger logger = Logger
			.getLogger(CopySyncProductsDetailsFromTSV.class);

	@Inject
	protected static Repository repository;

	public CopySyncProductsDetailsFromTSV(File inputFile) {
		super();
		this.inputFile = inputFile;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		BufferedReader reader = null;
		try {
			String productEntry = null;
			reader = new BufferedReader(new FileReader(inputFile));
			String firstLine = reader.readLine();
			String secondLine = reader.readLine();
			reader.close();

			if (CJProductMethodHandler.configurePositionForParams(firstLine)) {
				CJProductObjCreatorFromTSV cjProductCreator = new CJProductObjCreatorFromTSV();

				if ((productEntry = secondLine) != null) {
					CJProduct cjProduct = null;
					List<String> params = Arrays.asList(productEntry
							.split("\t"));
					if (params != null && params.size() > 0) {
						cjProduct = new CJProduct();
						for (int i = 0; i < params.size(); i++) {
							Method method = CJProductMethodHandler.METHOD_HANDLERS
									.get(CJProductMethodHandler.POSITION_PARAM
											.get(i));
							if (method != null) {
								method.invoke(cjProductCreator, cjProduct,
										params.get(i));
							}
						}
					}
					if (cjProduct != null) {
						invokeProductCreator(cjProduct);
					}
				}
			}

		} catch (Exception e) {
			logger.error(Log
					.message("Exception occurred in invoking the product creator for the file : "
							+ inputFile.getAbsolutePath()
							+ " Exception message : " + e.getMessage()));
			e.printStackTrace();
		}

	}

	private void invokeProductCreator(CJProduct cjProduct) {
		logger.info(Log
				.message("Invoking the product creator for the seller : "
						+ cjProduct.getAdvertiserName()));
		if (cjProduct.getAdvertiserName().trim()
				.equals(SellerEnum.BANANAREPUBLIC.sellerName)) {
			new BRProductSynchroniser(null, inputFile, true, null).now();
			// 10 14
			// new BRProductSynchroniser(null, inputFile, true).now();
		} else {
			new DefaultProductSynchroniser(null, inputFile, true, null).now();
			// 10 14 -- Home Depot
			//new DefaultProductSynchroniser(null, inputFile, true).now();
			
		}
	}

}