package services.impl;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;

import constants.ImpactRadiusConstants;
import exceptions.ServiceException;
import models.Brand;
import models.AdvertiserCategory;
import models.Product;
import models.Seller;
import models.impactradius.ImpactRadiusProduct;
import repositories.Repository;
import services.CacheService;
import services.impactradius.ImpactRadiusProductService;
import utils.AffiliateProductUtil;
import utils.PriceUtils;
import utils.log.Log;

public class DefaultImpactRadiusProductService implements ImpactRadiusProductService {

	private static Logger logger = Logger.getLogger(DefaultImpactRadiusProductService.class);

	@Inject
	private Repository repository;

	@Inject
	private CacheService cacheService;

	@Override
	public void createOrUpdate(ImpactRadiusProduct impactRadiusProduct) {

		Product p = null;
		Product prod = null;
		try {
			// logger.info(Log.message("createOrUpdate"));
			Seller seller = getSeller(impactRadiusProduct.getAdvertiserId());

			if (seller == null) {
				throw new ServiceException("Seller does not exist in the SELLER table for the advertiserId : "
						+ impactRadiusProduct.getAdvertiserId());
			}
			Brand brand = getBrand(impactRadiusProduct.getManufacturer(), seller.getName());

			AdvertiserCategory category = getCategory(impactRadiusProduct.getCategory());

			prod = new Product(impactRadiusProduct, seller, brand, category);

			try {
				p = repository.find(Product.class, "from Product where advertiserId=? and sku=?",
						impactRadiusProduct.getAdvertiserId(), impactRadiusProduct.getUniqueMerchantSKU());
			} catch (Exception e) {
				logger.error(Log.message("ISSUE in Finding --> " + impactRadiusProduct.getAdvertiserId() + " -- "
						+ impactRadiusProduct.getUniqueMerchantSKU()));
				logger.error(Log.message(e.getMessage()));
			}
			if (p == null) {
				try {
					repository.create(prod);
				} catch (Exception e) {
					logger.error(Log.message("ISSUE in creating --> " + prod.getName() + " -- " + prod.getSku()));
					logger.error(Log.message(e.getMessage()));
				}
			} else {
				boolean ultaUpdate = false;
				if (impactRadiusProduct.getAdvertiserId().toString().equals(ImpactRadiusConstants.ULTA_ADVERTISERID)
						|| impactRadiusProduct.getAdvertiserId().toString()
								.equals(ImpactRadiusConstants.DIAPERSCOM_ADVERTISERID)
						|| impactRadiusProduct.getAdvertiserId().toString()
								.equals(ImpactRadiusConstants.SOAPCOM_ADVERTISERID)) {
					if (AffiliateProductUtil.isUpdateNeededForImpactRadiusProductCategory(p, impactRadiusProduct)
							|| AffiliateProductUtil.isUpdateNeededForImpactRadiusProductLink(p, impactRadiusProduct)) {
						p.setAdvertiserCategory(impactRadiusProduct.getCategory());
						p.setBuyURL(impactRadiusProduct.getProductURL());
						p.setDescription(impactRadiusProduct.getProductDescription());
						p.setImageURL(impactRadiusProduct.getImageURL());

						if (impactRadiusProduct.isStockAvailability()) {
							p.setInStock(true);
						} else {
							p.setInStock(false);
						}

						p.setManufacturerName(impactRadiusProduct.getManufacturer());
						p.setName(impactRadiusProduct.getProductName());

						p.setCategory(category);
						p.setBrand(brand);
						p.setSeller(seller);

						ultaUpdate = true;
					}
				}
				// This check is very important for performance
				if (AffiliateProductUtil.isUpdateNeededForImpactRadiusProductPrice(p, impactRadiusProduct)) {

					p.setPrice(impactRadiusProduct.getOriginalPrice());
					p.setSalePrice((impactRadiusProduct.getCurrentPrice() != null
							&& impactRadiusProduct.getCurrentPrice().compareTo(BigDecimal.ZERO) == 1)
									? impactRadiusProduct.getCurrentPrice() : impactRadiusProduct.getOriginalPrice());

					BigDecimal finalPrice = (impactRadiusProduct.getCurrentPrice() != null
							&& impactRadiusProduct.getCurrentPrice().compareTo(BigDecimal.ZERO) == 1)
									? impactRadiusProduct.getCurrentPrice() : impactRadiusProduct.getOriginalPrice();

					p.setFinalPrice(finalPrice);
					p.setSale(PriceUtils.getSale(p.getPrice(), finalPrice));
					p.setInStock(true);
					try {
						repository.update(p);
					} catch (Exception e) {
						logger.error(Log.message("ISSUE in updating --> " + p.getName() + " -- " + p.getSku()));
						logger.error(Log.message(e.getMessage()));
					}
				} else {
					if (ultaUpdate) {
						try {
							repository.update(p);
						} catch (Exception e) {
							logger.error(Log.message("ISSUE in updating --> " + p.getName() + " -- " + p.getSku()));
							logger.error(Log.message(e.getMessage()));
						}
					}
				}
			}
		} catch (Exception e) {
			if (prod != null) {
				logger.error(Log.message("!!!!! createOrUpdate Function has issue with Prod --> " + prod.getSku()
						+ " ---> " + e.getMessage()));
			}
			if (p != null) {
				logger.error(Log.message("!!!!! createOrUpdate Function has issue with P --> " + p.getSku() + " ---> "
						+ e.getMessage()));
			}
			logger.error(Log.message("!!!!! createOrUpdate Function has issue !!!!! " + e.getMessage()));
			e.printStackTrace();
		}

	}

	private Seller getSeller(Long advertiserId) {
		String cacheKey = Seller.CACHE_PREFIX + advertiserId;
		Seller seller = cacheService.get(cacheKey, Seller.class);
		if (seller == null) {
			seller = Seller.find("byAdvertiserId", advertiserId).first();
			cacheService.addToLongCache(cacheKey, seller);
		}
		return seller;
	}

	private Brand getBrand(String brandName, String sellerName) {
		// If the brand_name is null, then treat the seller name as
		// manufacturer name
		Brand brand = null;
		try {
			if (brandName == null || brandName.trim().length() < 1) {
				brandName = sellerName;
			}
			String brandNameCacheKey = brandName;
			brandNameCacheKey = StringUtils.deleteWhitespace(brandName);
			if (brandNameCacheKey != null) {
				String cacheKey = Brand.CACHE_PREFIX + brandNameCacheKey;
				brand = cacheService.get(cacheKey, Brand.class);
				if (brand == null) {
					brand = Brand.find("byName", brandName).first();
					cacheService.addToLongCache(cacheKey, brand);
				}
			}
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
		}
		return brand;
	}

	private AdvertiserCategory getCategory(String categoryName) {
		AdvertiserCategory category = null;
		try {
			String categoryNameCacheKey = categoryName;
			categoryNameCacheKey = StringUtils.deleteWhitespace(categoryName);
			if (categoryNameCacheKey != null) {
				String cacheKey = AdvertiserCategory.CACHE_PREFIX + categoryNameCacheKey;
				category = cacheService.get(cacheKey, AdvertiserCategory.class);
				if (category == null) {
					category = AdvertiserCategory.find("byName", categoryName).first();
					cacheService.addToLongCache(cacheKey, category);
				}
			}
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
		}
		return category;
	}
}
