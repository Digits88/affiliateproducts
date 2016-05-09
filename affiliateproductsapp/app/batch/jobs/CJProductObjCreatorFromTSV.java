package batch.jobs;

import java.math.BigDecimal;

import javax.inject.Inject;

import models.Seller;
import models.cj.CJProduct;

import org.apache.commons.lang.StringUtils;

import play.modules.guice.InjectSupport;
import services.CacheService;

@InjectSupport
public class CJProductObjCreatorFromTSV {

	@Inject
	protected static CacheService cacheService;

	public void setAdvertiserNameAndId(CJProduct cjProduct, String sellerName) {
		cjProduct.setAdvertiserName(sellerName);
		Seller seller = getSeller(sellerName);
		if (seller != null) {
			cjProduct.setAdvertiserId(seller.getAdvertiserId());
		}
	}

	public void setAdvertiserCategory(CJProduct cjProduct,
			String advertiserCategory) {
		cjProduct.setAdvertiserCategory(advertiserCategory);
	}

	public void setBuyURL(CJProduct cjProduct, String buyURL) {
		cjProduct.setBuyURL(buyURL);
	}

	public void setDescription(CJProduct cjProduct, String description) {
		cjProduct.setDescription(description);
	}

	public void setImageURL(CJProduct cjProduct, String imageURL) {
		cjProduct.setImageURL(imageURL);
	}

	public void setInStock(CJProduct cjProduct, String inStock) {
		if (inStock != null && inStock.length() > 0) {
			if (inStock.trim().equalsIgnoreCase("yes")) {
				cjProduct.setInStock(new Boolean(true));
			} else {
				cjProduct.setInStock(new Boolean(false));
			}
		}
	}

	public void setManufacturerName(CJProduct cjProduct, String manufacturerName) {
		cjProduct.setManufacturerName(manufacturerName);
	}

	public void setName(CJProduct cjProduct, String name) {
		cjProduct.setName(name);
	}

	public void setPrice(CJProduct cjProduct, String price) {
		if (price != null && price.length() > 0) {
			cjProduct.setPrice(new BigDecimal(price));
		}
	}

	public void setRetailPrice(CJProduct cjProduct, String retailPrice) {
		if (retailPrice != null && retailPrice.length() > 0) {
			cjProduct.setRetailPrice(new BigDecimal(retailPrice));
		}
	}

	public void setSalePrice(CJProduct cjProduct, String salePrice) {
		if (salePrice != null && salePrice.length() > 0) {
			cjProduct.setSalePrice(new BigDecimal(salePrice));
		}
	}

	public void setSku(CJProduct cjProduct, String sku) {
		cjProduct.setSku(sku);
	}
	
	public void setCurrency(CJProduct cjProduct, String currency) {
		cjProduct.setCurrency(currency);
	}

	public void setKeywords(CJProduct cjProduct, String keywords) {
		cjProduct.setKeywords(keywords);
	}

	private Seller getSeller(String sellerName) {
		String cacheKey = Seller.CACHE_PREFIX
				+ StringUtils.deleteWhitespace(sellerName);
		Seller seller = cacheService.get(cacheKey, Seller.class);
		if (seller == null) {
			seller = Seller.find("byName", sellerName).first();
			cacheService.addToLongCache(cacheKey, seller);
		}
		return seller;
	}

}
