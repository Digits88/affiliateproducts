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
import java.util.Set;

import javax.inject.Inject;

import models.cj.CJProduct;
import models.impactradius.ImpactRadiusProduct;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import constants.ImpactRadiusConstants;
import batch.jobs.ImpactRadiusProductObjCreatorFromTXT;
import batch.jobs.ImpactRadiusProductMethodHandler;
import play.modules.guice.InjectSupport;
import repositories.Repository;
import services.cj.CJService;
import services.impactradius.ImpactRadiusProductService;
import utils.AffiliateStringUtil;
import utils.impactradius.ImpactRadiusXMLHandler;
import utils.impactradius.classfromxsd.Products;
import utils.impactradius.classfromxsd.Products.Product;
import utils.log.Log;

@InjectSupport
public class ImpactRadiusProductCreator {

	private Long advertiserId = null;
	private File inputFile = null;

	private static Logger logger = Logger.getLogger(ImpactRadiusProductCreator.class);

	public ImpactRadiusProductCreator(Long advertiserId, File inputFile) {
		super();
		this.advertiserId = advertiserId;
		this.inputFile = inputFile;

	}

	public List<ImpactRadiusProduct> createImpactRadiusProducts() {
		/*
		 * logger.info(Log .message("--> Advertiser ID : " + advertiserId +
		 * " <-- "));
		 */
		List<ImpactRadiusProduct> impactRadiusProducts = new ArrayList<ImpactRadiusProduct>();
		impactRadiusProducts = createimpactRadiusProductFromXML(inputFile);
		return impactRadiusProducts;
	}

	// Add function to get Impact Radius Products List from xml
	public List<ImpactRadiusProduct> createimpactRadiusProductFromXML(File inputFile) {
		List<ImpactRadiusProduct> impactRadiusProducts = new ArrayList<ImpactRadiusProduct>();
		Products products = null;
		ImpactRadiusXMLHandler xmlHandler = null;

		ImpactRadiusProduct impactRadiusProduct = null;

		BigDecimal bd1 = null;
		BigDecimal bd2 = null;

		try {
			xmlHandler = new ImpactRadiusXMLHandler();
			products = xmlHandler.getProductListFromSingleXML(inputFile);

			List<Product> productsFromXML = products.getProduct();
			for (Product pXML : productsFromXML) {
				if (pXML == null) {
					logger.error(
							Log.message("Exception occurred in ImpactRadiusProductCreator createSKProductsFromXML : "
									+ inputFile.getAbsolutePath() + " Exception message : Empty content in Product"));
				}

				DecimalFormat df = new DecimalFormat();
				df.setParseBigDecimal(true);

				if (pXML.getOriginalPrice() != null && pXML.getOriginalPrice().trim().length() > 0
						&& pXML.getCurrentPrice() != null && pXML.getCurrentPrice().trim().length() > 0) {
					bd1 = (BigDecimal) df.parse(pXML.getOriginalPrice());
					bd2 = (BigDecimal) df.parse(pXML.getCurrentPrice());
					if (bd2.compareTo(bd1) == 1) {
						continue;
					}
				}

				impactRadiusProduct = new ImpactRadiusProduct();
				impactRadiusProduct.setAdvertiserId(advertiserId);
				impactRadiusProduct.setUniqueMerchantSKU(pXML.getUniqueMerchantSKU());
				impactRadiusProduct.setProductName(pXML.getProductName());
				impactRadiusProduct.setProductType(pXML.getProductType());

				// set category
				if (pXML.getCategory() == null && pXML.getGender() != null) {
					impactRadiusProduct.setCategory(StringEscapeUtils.unescapeXml(pXML.getGender()));
				} else {
					impactRadiusProduct.setCategory(StringEscapeUtils.unescapeXml(pXML.getCategory()));
				}

				impactRadiusProduct.setProductURL(pXML.getProductURL());
				impactRadiusProduct.setImageURL(pXML.getImageURL());
				impactRadiusProduct.setProductDescription(pXML.getProductDescription());
				impactRadiusProduct.setManufacturer(StringEscapeUtils.unescapeXml(pXML.getManufacturer()));

				// Set Original Price
				if (pXML.getOriginalPrice() != null && pXML.getOriginalPrice().trim().length() > 0) {
					impactRadiusProduct.setOriginalPrice((BigDecimal) df.parse(pXML.getOriginalPrice()));
				} else {
					impactRadiusProduct.setOriginalPrice((BigDecimal) df.parse(pXML.getCurrentPrice()));
				}

				// Set current Price
				if (pXML.getCurrentPrice() != null && pXML.getCurrentPrice().trim().length() > 0) {
					impactRadiusProduct.setCurrentPrice((BigDecimal) df.parse(pXML.getCurrentPrice()));
				} else {
					impactRadiusProduct.setCurrentPrice((BigDecimal) df.parse(pXML.getOriginalPrice()));
				}

				if (pXML.getStockAvailability() != null && pXML.getStockAvailability().trim().equalsIgnoreCase("N")) {
					impactRadiusProduct.setStockAvailability(false);
				} else {
					impactRadiusProduct.setStockAvailability(true);
				}

				impactRadiusProduct.setColor(pXML.getColor());
				/*
				 * impactRadiusProduct.setCondition(pXML.getCondition());
				 * impactRadiusProduct.setEan(pXML.getEAN());
				 * impactRadiusProduct.setUpc(pXML.getUPC());
				 * impactRadiusProduct.setIsbn(pXML.getISBN());
				 * impactRadiusProduct.setMpn(pXML.getMPN());
				 * impactRadiusProduct.setShippingRate(pXML.getShippingRate());
				 * impactRadiusProduct.setCategoryID(pXML.getCategoryID());
				 */

				impactRadiusProducts.add(impactRadiusProduct);
				impactRadiusProduct = null;
			}
			logger.info(Log.message("Finish create list from file : " + inputFile.getName()));
		} catch (Exception e) {
			logger.error(Log.message("Exception occurred in ImpactRadiusProductCreator createSKProductsFromXML : "
					+ inputFile.getAbsolutePath()));
			logger.error(Log.message(" Exception message : " + e.getMessage()));
			if (impactRadiusProduct != null) {
				logger.error(Log.message(impactRadiusProduct.getProductName()));
			}
			e.printStackTrace();
		}
		products = null;
		return impactRadiusProducts;
	}
}
