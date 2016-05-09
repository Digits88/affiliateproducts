package batch.jobs.product.synchroniser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import models.cj.CJProduct;

import org.apache.log4j.Logger;

import play.modules.guice.InjectSupport;
import repositories.Repository;
import services.ProductService;
import services.cj.CJService;
import utils.AffiliateStringUtil;
import utils.log.Log;
import batch.jobs.AbstractBatchJob;
import batch.jobs.CJProductMethodHandler;
import batch.jobs.CJProductObjCreatorFromTSV;

/**
 * 
 * Class to synchronize the product details for all the sellers from CJ to
 * affiliateproducts DB
 * 
 */
@InjectSupport
public class CJProductsCreator {

	public Long advertiserId = null;
	public File inputFile = null;
	public Boolean tsvBasedProcess = false;

	private static Logger logger = Logger.getLogger(CJProductsCreator.class);

	@Inject
	protected static Repository repository;

	@Inject
	private static CJService cjService;

	// 10 14 -- Home Depot
	@Inject
	protected static ProductService productService;

	public CJProductsCreator(Long advertiserId, File inputFile, Boolean tsvBasedProcess) {
		super();
		this.advertiserId = advertiserId;
		this.inputFile = inputFile;
		this.tsvBasedProcess = tsvBasedProcess;
	}

	public List<CJProduct> createCJProducts() {
		List<CJProduct> cjProducts = new ArrayList<CJProduct>();
		if (tsvBasedProcess == true) {
			cjProducts = createCJProductsFromTSV();
		} else {
			cjProducts = createCJProductsFromAPI();
		}
		return cjProducts;
	}

	public List<CJProduct> createCJProductsFromTSV() {
		BufferedReader reader = null;
		List<CJProduct> cjProducts = new ArrayList<CJProduct>();
		String productEntry = null;
		try {
			reader = new BufferedReader(new FileReader(inputFile));
			logger.info(Log
					.message("Invoking the configurePositionForParams for the file : " + inputFile.getAbsolutePath()));
			if (CJProductMethodHandler.configurePositionForParams(reader.readLine())) {
				CJProductObjCreatorFromTSV cjProductCreator = new CJProductObjCreatorFromTSV();

				while ((productEntry = reader.readLine()) != null) {
					CJProduct cjProduct = createCJProduct(cjProductCreator, productEntry);

					if (cjProduct != null && isValidToAdd(cjProduct)) {
						if (cjProduct.getName() != null && cjProduct.getName().trim().length() > 0) {
							cjProduct.setName(AffiliateStringUtil.afterUnEscapeHtmlXml(cjProduct.getName()));
						}
						if (cjProduct.getDescription() != null && cjProduct.getDescription().length() > 0) {
							cjProduct.setDescription(
									AffiliateStringUtil.afterUnEscapeHtmlXml(cjProduct.getDescription()));
						}
						if (cjProduct.getAdvertiserCategory() != null
								&& cjProduct.getAdvertiserCategory().length() > 0) {
							cjProduct.setAdvertiserCategory(
									AffiliateStringUtil.afterUnEscapeHtmlXml(cjProduct.getAdvertiserCategory()));
						}

						cjProducts.add(cjProduct);

					}
				}
				reader.close();
			}

		} catch (Exception e) {
			logger.error(Log.message("Exception occurred in parsing the file and creating the cjProduct objects : "
					+ inputFile.getAbsolutePath() + " Exception message : " + e.getMessage()));
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
					logger.info(Log.message("Exception occurred while closing the file reader on exception..."
							+ inputFile.getAbsolutePath()));
					e1.printStackTrace();
				}
			}
		}
		logger.info(Log.message("Successfully completed creating the CJ product objects for the seller : "
				+ inputFile.getAbsolutePath()));
		return cjProducts;
	}

	public List<CJProduct> createCJProductsFromAPI() {
		List<CJProduct> cjProducts = new ArrayList<CJProduct>();
		try {
			cjProducts = cjService.getProductsByAdvertiserId(Long.toString(advertiserId));

		} catch (Exception e) {
			logger.error(Log.message("Exception occurred in calling the API and creating the cjProduct objects : "
					+ advertiserId + " Exception message : " + e.getMessage()));
			e.printStackTrace();
		}
		return cjProducts;
	}

	private CJProduct createCJProduct(CJProductObjCreatorFromTSV cjProductCreator, String productEntry) {
		CJProduct cjProduct = null;
		Method method = null;

		List<String> params = Arrays.asList(productEntry.split("\t"));
		// logger.info(Log.message(params.get(7)));

		try {
			if (params != null && params.size() > 0) {
				cjProduct = new CJProduct();
				for (int i = 0; i < params.size(); i++) {
					method = CJProductMethodHandler.METHOD_HANDLERS.get(CJProductMethodHandler.POSITION_PARAM.get(i));
					if (method != null) {
						method.invoke(cjProductCreator, cjProduct, params.get(i));
					}
				}
			}
		} catch (Exception e) {
			logger.info(Log.message("Exception occurred while parsing the entry from the file : " + params.get(7)));
			return null;
		}
		return cjProduct;

	}

	// Validating the product before adding into DB
	private Boolean isValidToAdd(CJProduct cjProduct) {
		try {
			if (cjProduct != null) {
				if (cjProduct.getCurrency().equals("USD")) {
					return true;
				}
			}
		} catch (Exception e) {
			logger.error(Log.message("Exception occurred while validating the product"));
			e.printStackTrace();
		}
		return false;
	}

}