package batch.jobs.product.synchroniser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import models.cj.CJProduct;
import models.searskmart.SearsKmartProduct;

import org.apache.log4j.Logger;

import play.modules.guice.InjectSupport;
import repositories.Repository;
import services.SKProductService;
import services.cj.CJService;
import utils.AffiliateStringUtil;
import utils.log.Log;
import batch.jobs.CJProductMethodHandler;
import batch.jobs.CJProductObjCreatorFromTSV;

@InjectSupport
public class SKProductCreator {

	public Long advertiserId = null;
	public File inputFile = null;

	private static Logger logger = Logger.getLogger(SKProductCreator.class);

	@Inject
	protected static Repository repository;

	@Inject
	private static SKProductService skProductService;

	public SKProductCreator(Long advertiserId, File inputFile) {
		super();
		this.advertiserId = advertiserId;
		this.inputFile = inputFile;
	}

	public List<SearsKmartProduct> createSKProducts() {
		List<SearsKmartProduct> skProducts = new ArrayList<SearsKmartProduct>();
		skProducts = createSKProductsFromTSV();
		return skProducts;
	}

	public List<SearsKmartProduct> createSKProductsFromTSV() {
		BufferedReader reader = null;
		List<SearsKmartProduct> skProducts = new ArrayList<SearsKmartProduct>();
		String productEntry = null;
		try {
			reader = new BufferedReader(new FileReader(inputFile));
			logger.info(Log.message("Create SK Products From TSV file : "
					+ inputFile.getAbsolutePath()));

			SearsKmartProduct skProduct = null;
			while ((productEntry = reader.readLine()) != null
					&& !productEntry.isEmpty()) {
				skProduct = new SearsKmartProduct();
				String[] list = productEntry.split("\\|");

				skProduct.setPartnumber(list[0]);
				skProduct.setProductName(list[2]);
				skProduct.setShorDescription(list[3]);
				skProduct.setCategory(list[4]);
				skProduct.setInstallation(list[5]);
				skProduct.setProtectionPlan(list[6]);
				skProduct.setMaintenanceAgreement(list[7]);
				
				skProduct.setManufacturePartnumber(list[9]);
				skProduct.setImageName(list[10]);
				skProduct.setProductURL(list[11]);
				
				// Set advertiserName
				String advertiser = (this.advertiserId == 9999999) ? "Sears"
						: "Kmart";
				skProduct.setAdvertiserName(advertiser);
				
				// How to set brand name for Sears and Kmart Feeds
				String brandName = list[1];

				String manufacturerName = list[8];
				skProduct.setManufacturerName(manufacturerName);
				
				if(brandName.trim().length() > 0) {
					skProduct.setBrand(brandName);					
				} else if(manufacturerName.trim().length() > 0) {
					skProduct.setBrand(manufacturerName);
				} else {
					skProduct.setBrand(advertiser);
				}

				// SKProduct Price Setting
				BigDecimal bd_1 = null;
				BigDecimal bd_2 = null;
				DecimalFormat df = new DecimalFormat();
				df.setParseBigDecimal(true);
				if (list[12] != null && !list[12].trim().equals("")) {
					bd_1 = (BigDecimal) df.parse(list[12]);
					skProduct.setRegularPrice(bd_1);
				}
				if (list[13] != null && !list[13].trim().equals("")) {
					bd_2 = (BigDecimal) df.parse(list[13]);
					skProduct.setSellingPrice(bd_2);
				}

				skProduct.setMapPriceIndicator(list[14]);
				skProduct.setSaveStory(list[15]);
				skProduct.setUpc(list[16]);
				skProduct.setParentName(list[17]);
				skProduct.setOtherAttributes(list[18]);

				skProduct.setInStock(true);

				// Set advertiserID
				skProduct.setAdvertiserId(this.advertiserId);

				// skProductService.createOrUpdateSK(skProduct);

				skProducts.add(skProduct);
				skProduct = null;
			}
			reader.close();

		} catch (Exception e) {
			logger.error(Log
					.message("Exception occurred in parsing the file and creating the SKProduct objects : "
							+ inputFile.getAbsolutePath()
							+ " Exception message : " + e.getMessage()));
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
					reader = null;
				} catch (IOException e1) {
					logger.info(Log
							.message("Exception occurred while closing the file reader on exception..."
									+ inputFile.getAbsolutePath()));
					e1.printStackTrace();
				}
			}
		}
		logger.info(Log
				.message("Successfully completed creating the SK product objects for the seller : "
						+ inputFile.getAbsolutePath()));
		return skProducts;
	}
}
