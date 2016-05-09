package batch.jobs;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;

import org.apache.log4j.Logger;

import batch.jobs.product.synchroniser.RakutenProductCreator;
import utils.log.Log;
import models.impactradius.ImpactRadiusProduct;

public class ImpactRadiusProductObjCreatorFromTXT {

	private static Logger logger = Logger
			.getLogger(ImpactRadiusProductObjCreatorFromTXT.class);
	
/*	// id -- sku
	public void setId(ImpactRadiusProduct impactRadiusProduct, String id) {
		impactRadiusProduct.setId(id);
	}

	// title -- name 
	public void setTitle(ImpactRadiusProduct impactRadiusProduct, String title) {
		impactRadiusProduct.setTitle(title);
	}

	// description -- description
	public void setDescription(ImpactRadiusProduct impactRadiusProduct,
			String description) {
		impactRadiusProduct.setDescription(description);
	}

	// brand -- brand/Manufacturer
	public void setBrand(ImpactRadiusProduct impactRadiusProduct, String brand) {
		impactRadiusProduct.setBrand(brand);
	}

	// price -- price
	public void setPrice(ImpactRadiusProduct impactRadiusProduct, String price) {
		try {

			DecimalFormat df = new DecimalFormat();
			df.setParseBigDecimal(true);
			BigDecimal p;
			p = (BigDecimal) df.parse(price);
			impactRadiusProduct.setPrice(p);
		} catch (ParseException e) {
			logger.error(Log
					.message("Exception occurred in setPrice function : " + e.getMessage()));
			e.printStackTrace();
		}
	}
	
	// sale_price -- sale price
	public void setSalePrice(ImpactRadiusProduct impactRadiusProduct, String salePrice) {
		try {

			DecimalFormat df = new DecimalFormat();
			df.setParseBigDecimal(true);
			BigDecimal p;
			p = (BigDecimal) df.parse(salePrice);
			impactRadiusProduct.setPrice(p);
		} catch (ParseException e) {
			logger.error(Log
					.message("Exception occurred in setSalePrice function : " + e.getMessage()));
			e.printStackTrace();
			e.printStackTrace();
		}
	}
	
	// link -- buy_url
	public void setLink(ImpactRadiusProduct impactRadiusProduct, String link) {
		impactRadiusProduct.setLink(link);
	}
	
	// image_link -- image_url
	public void setImageLink(ImpactRadiusProduct impactRadiusProduct, String imageLink) {
		impactRadiusProduct.setImageLink(imageLink);
	}
	
	// google_product_category -- advertiser Category
	public void setGoogleProductCategory(ImpactRadiusProduct impactRadiusProduct, String googleProductCategory) {
		impactRadiusProduct.setGoogleProductCategory(googleProductCategory);
	}
	
	// availability -- in_stock
	public void setAvailability(ImpactRadiusProduct impactRadiusProduct, String availability) {
		impactRadiusProduct.setAvailability(availability);
	}*/
}
