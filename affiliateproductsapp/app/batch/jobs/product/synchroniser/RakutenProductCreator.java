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
import models.rakuten.RakutenProduct;
import models.searskmart.SearsKmartProduct;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import play.modules.guice.InjectSupport;
import repositories.Repository;
import services.SKProductService;
import services.cj.CJService;
import services.rakuten.RakutenProductService;
import utils.AffiliateStringUtil;
import utils.log.Log;
import utils.rakuten.RakutenXMLHandler;
import utils.rakuten.classfromxsd.Category;
import utils.rakuten.classfromxsd.Description;
import utils.rakuten.classfromxsd.Discount;
import utils.rakuten.classfromxsd.Merchandiser;
import utils.rakuten.classfromxsd.Price;
import utils.rakuten.classfromxsd.Product;
import utils.rakuten.classfromxsd.Shipping;
import utils.rakuten.classfromxsd.URL;
import batch.jobs.CJProductMethodHandler;
import batch.jobs.CJProductObjCreatorFromTSV;

@InjectSupport
public class RakutenProductCreator {

	public Long advertiserId = null;
	public File inputFile = null;

	private static Logger logger = Logger
			.getLogger(RakutenProductCreator.class);

	@Inject
	protected static Repository repository;

	public RakutenProductCreator(Long advertiserId, File inputFile) {
		super();
		this.advertiserId = advertiserId;
		this.inputFile = inputFile;
	}

	public List<RakutenProduct> createRakutenProducts() {
		List<RakutenProduct> rakutenProducts = new ArrayList<RakutenProduct>();
		rakutenProducts = createRakutenProductsFromXML(inputFile);
		return rakutenProducts;
	}

	public List<RakutenProduct> createRakutenProductsFromXML(File inputFile) {
		List<RakutenProduct> rakutenProducts = new ArrayList<RakutenProduct>();

		RakutenXMLHandler xmlHandler = null;
		try {
			xmlHandler = new RakutenXMLHandler();
			Merchandiser merchandiser = xmlHandler
					.getProductListFromSingleXML(inputFile);
			Long merchantId = Long.parseLong(merchandiser.getHeader()
					.getMerchantId());
			String merchantName = merchandiser.getHeader().getMerchantName();
			String createOn = merchandiser.getHeader().getCreatedOn();
			List<Product> productsFromXML = merchandiser.getProduct();
			RakutenProduct rakutenProduct = null;
			for (Product pXML : productsFromXML) {
				if (pXML == null) {
					logger.error(Log
							.message("Exception occurred in RakutenProductCreator createSKProductsFromXML : "
									+ inputFile.getAbsolutePath()
									+ " Exception message : Empty content in Product"));
				}
				
				// create a new rakutenProduct
				/*if(pXML.getShipping().getAvailability().equalsIgnoreCase("in-stock")) {
					rakutenProduct = new RakutenProduct();
				} else {
					continue;
				}*/

				rakutenProduct = new RakutenProduct();
				
				// set availability
				rakutenProduct.setAvailability(true);

				// set merchantId, merchantName, createOn
				rakutenProduct.setMerchantId(merchantId);
				rakutenProduct.setMerchantName(StringEscapeUtils.unescapeXml(merchantName));
				rakutenProduct.setCreatedOn(createOn);

				// set product ID
				rakutenProduct.setProduct_id(pXML.getProductId());
				
				// set product name
				rakutenProduct.setName(StringEscapeUtils.unescapeXml(pXML.getName()));
				
				// set sku
				rakutenProduct.setSku(pXML.getSkuNumber()); 
				
				// set Manufacturer Name -- UnescapeXML
				rakutenProduct.setManufacturerName(StringEscapeUtils.unescapeXml(pXML.getManufacturerName()));
				
				// set part number
				rakutenProduct.setPartNumber(pXML.getPartNumber()); 

				// Set Category -- UnescapeXML
				rakutenProduct.setPrimaryCategory(StringEscapeUtils.unescapeXml(pXML.getCategory().getPrimary()));
				rakutenProduct.setSecondaryCategory(StringEscapeUtils.unescapeXml(pXML.getCategory().getSecondary()));
				
				// set URL
				rakutenProduct.setProductURL(pXML.getURL().getProduct());
				rakutenProduct.setImageURL(pXML.getURL().getProductImage());
				
				// set Description
				rakutenProduct.setShortDescription(pXML.getDescription().getShort());
				rakutenProduct.setLongDescription(pXML.getDescription().getLong());
				
				// set currency
				rakutenProduct.setCurrency(pXML.getDiscount().getCurrency());
				rakutenProduct.setType(pXML.getDiscount().getType());
				
				// set Price	
				rakutenProduct.setCurrency2(pXML.getPrice().getCurrency());
				DecimalFormat df = new DecimalFormat();
				df.setParseBigDecimal(true);
				if(pXML.getPrice().getSale() != null && !pXML.getPrice().getSale().trim().equals("")) {
					rakutenProduct.setSalePrice((BigDecimal) df.parse(pXML.getPrice().getSale()));					
				} else {
					rakutenProduct.setSalePrice((BigDecimal) df.parse(pXML.getPrice().getRetail()));
				}
				rakutenProduct.setRetailPrice((BigDecimal) df.parse(pXML.getPrice().getRetail()));				
				
				// set brand -- UnescapeXML
				rakutenProduct.setBrand(StringEscapeUtils.unescapeXml(pXML.getBrand()));
				
				// set upc
				rakutenProduct.setUpc(pXML.getUpc());
				
				// set pixel
				rakutenProduct.setPixel(pXML.getPixel());
				
				// set keyword
				rakutenProduct.setKeywords(pXML.getKeywords());
								
				// add rakutenProduct into return list
				rakutenProducts.add(rakutenProduct);
				
				rakutenProduct = null;
			}
		} catch (Exception e) {
			logger.error(Log
					.message("Exception occurred in RakutenProductCreator createSKProductsFromXML : "
							+ inputFile.getAbsolutePath()
							+ " Exception message : " + e.getMessage()));
			e.printStackTrace();
		}

		return rakutenProducts;
	}
}
