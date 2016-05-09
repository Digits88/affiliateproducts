package services.impl;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.AdvertiserCategory;
import models.Brand;
import models.Product;
import models.Seller;
import models.cj.CJProduct;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import play.db.jpa.JPA;
import product.filter.ProductFilter;
import product.filter.ProductFilterFactory;
import repositories.Repository;
import services.CacheService;
import services.ProductService;
import utils.AffiliateProductUtil;
import utils.AffiliateStringUtil;
import utils.PriceUtils;
import utils.log.Log;

import com.google.inject.Inject;

import constants.AffiliateConstants;
import constants.CJProductsConstants;
import enums.ProductFilterEntity;
import exceptions.ServiceException;

public class DefaultProductService implements ProductService {

	private static Logger logger = Logger.getLogger(DefaultProductService.class);

	@Inject
	private Repository repository;

	@Inject
	private CacheService cacheService;

	@Override
	public void createOrUpdate(CJProduct cjProduct, File inputFile) {

		Seller seller = getSeller(cjProduct.getAdvertiserId());
		if (seller == null) {
			throw new ServiceException(
					"Seller does not exist in the SELLER table for the advertiserId : " + cjProduct.getAdvertiserId());
		}
		Brand brand = getBrand(cjProduct.getManufacturerName(), seller.getName());
		// logger.info("Get Brand : " + brand);

		if (cjProduct.getAdvertiserId() == CJProductsConstants.STAPLES_ADVERTISERID
				&& cjProduct.getAdvertiserCategory().contains(CJProductsConstants.SPALES_LONG_CATE_PATTERN)) {
			logger.info("===> Long Category : " + cjProduct.getAdvertiserCategory());
			cjProduct
					.setAdvertiserCategory(cjProduct.getAdvertiserCategory().split("-")[0].replaceAll("\"", "").trim());
			logger.info("===> After Replace : " + cjProduct.getAdvertiserCategory());
		}

		AdvertiserCategory category = getCategory(cjProduct.getAdvertiserCategory());
		// logger.info("Get AdvertiserCategory : " + category);

		Product prod = new Product(cjProduct, seller, brand, category);
		
		Product p = null;

		try {
			p = repository.find(Product.class, "from Product where advertiserId=? and sku=?",
					cjProduct.getAdvertiserId(), cjProduct.getSku());
			// Below is used for BrookStone
			// p.setBrand(brand);
			// repository.update(p);
		} catch (Exception e) {
			logger.error(Log.message("ISSUE in Finding --> " + cjProduct.getAdvertiserId() + " -- " + cjProduct.getSku()));
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
			
			
			/*if (cjProduct.getAdvertiserId() == 1488218) {
				Brand brand1 = getBrand(cjProduct.getManufacturerName(), seller.getName());
				p.setBrand(brand1);
				logger.info("Brand name is " + brand.getName());
			}

			*/// This check is very important for performance
			if (AffiliateProductUtil.isUpdateNeeded(p, cjProduct)) {
				// As per Nora, update only the availability and sale price
				p.setInStock(cjProduct.getInStock());
				p.setPrice(cjProduct.getPrice());
				p.setSalePrice(cjProduct.getSalePrice());
				BigDecimal finalPrice = cjProduct.getSalePrice() != null ? cjProduct.getSalePrice()
						: cjProduct.getPrice();
				p.setFinalPrice(finalPrice);
				p.setSale(PriceUtils.getSale(cjProduct.getPrice(), finalPrice));
				p.setBuyURL(cjProduct.getBuyURL());
				p.setImageURL(cjProduct.getImageURL());
				
				p.setManufacturerName(cjProduct.getManufacturerName());
				// p.setBrand(brand);
				// logger.info("Set Brand : " + brand);
				// p.setCategory(category);
				// brand = getBrand(cjProduct.getManufacturerName(), seller.getName());
				// p.setBrand(brand);
				// p.setInStock(true);
				try {
					p = repository.update(p);
					// logger.info(">>>>> Updated : " + p.getId() + " -- " +p.getName() + " With Brand : " + p.getBrand().getName());
				} catch (Exception e) {
					logger.error(Log.message("ISSUE in updating --> " + p.getName() + " -- " + p.getSku()));
					logger.error(Log.message("Product : " + p));
					logger.error(Log.message(e.getMessage()));
					e.printStackTrace();
				}
			}
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

	@Override
	public Map<String, Object> getBySeller(Long sellerId, Integer offset, Integer maxResults) {
		Map<String, Object> result = new HashMap<String, Object>();
		Seller seller = Seller.findById(sellerId);
		List<Product> products = repository.createNamedQuery("JPQL_GET_PRODUCTS_OF_SELLER", Product.class, offset,
				maxResults, seller);
		result.put(AffiliateConstants.PRODUCTS, products);
		Long count = Product.count("seller", seller);
		result.put(AffiliateConstants.COUNT, count);
		return result;
	}

	@Override
	public Map<String, Object> getSellers(Integer offset, Integer maxResults) {
		Map<String, Object> result = new HashMap<String, Object>();
		List<Seller> sellers = repository.createNamedQuery("JPQL_GET_SELLERS", Seller.class, offset, maxResults);
		result.put(AffiliateConstants.SELLERS, sellers);
		Long count = Seller.count();
		result.put(AffiliateConstants.COUNT, count);
		return result;
	}

	@Override
	public Map<String, Object> getFilteredProducts(List<Long> sellerIds, List<Long> brandIds, List<Long> categoryIds,
			BigDecimal minPrice, BigDecimal maxPrice, Integer sale, String keyword, Integer start, Integer limit) {
		Map<String, Object> result = new HashMap<String, Object>();
		CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
		CriteriaQuery cq = cb.createQuery();
		Root<Product> root = cq.from(Product.class);
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		for (ProductFilterEntity entity : ProductFilterEntity.values()) {
			ProductFilter productFilter = ProductFilterFactory.getFilter(entity, cb, cq, root);
			List<Predicate> predicates = productFilter.filter(sellerIds, brandIds, categoryIds, minPrice, maxPrice,
					sale, keyword);
			predicatesList.addAll(predicates);
		}

		cq.select(cb.count(root));
		cq.where(predicatesList.toArray(new Predicate[] {}));
		Long totalRecordsCount = (Long) JPA.em().createQuery(cq).getSingleResult();

		cq.select(root);
		cq.orderBy(cb.asc(root.get("id")));
		cq.where(predicatesList.toArray(new Predicate[] {}));
		List<Product> products = repository.list(Product.class, cq, start, limit);

		result.put(AffiliateConstants.PRODUCTS, products);
		result.put(AffiliateConstants.TOTAL_PRODUCTS_COUNT, totalRecordsCount);
		logger.info(Log.message("Exiting", result));
		return result;
	}

	@Override
	public Map<String, Object> searchProducts(String keyword, Integer start, Integer limit) {

		/**
		 * select distinct(p) from Product p where name like '%gold color
		 * bracelet%' or name like '%color gold bracelet%' or name like '%gold
		 * bracelet color%' or name like '%bracelet gold color%' or name like
		 * '%color bracelet gold%' or name like '%bracelet color gold%' or name
		 * like '%gold%color%bracelet%' or name like '%color%gold%bracelet%' or
		 * name like '%gold%bracelet%color%' or name like
		 * '%bracelet%gold%color%' or name like '%color%bracelet%gold%' or name
		 * like '%bracelet%color%gold%' or name like '%gold%' or name like
		 * '%color%' or name like '%bracelet%' ORDER BY CASE WHEN name like
		 * '%gold color bracelet%' THEN 0 WHEN name like '%color gold bracelet%'
		 * THEN 1 WHEN name like '%gold bracelet color%' THEN 2 WHEN name like
		 * '%bracelet gold color%' THEN 3 WHEN name like '%color bracelet gold%'
		 * THEN 4 WHEN name like '%bracelet color gold%' THEN 5 WHEN name like
		 * '%gold%color%bracelet%' THEN 6 WHEN name like '%color%gold%bracelet%'
		 * THEN 7 WHEN name like '%gold%bracelet %color%' THEN 8 WHEN name like
		 * '%bracelet%gold%color%' THEN 9 WHEN name like '%color%bracelet%gold%'
		 * THEN 10 WHEN name like '%bracelet%color%gold%' THEN 11 WHEN name like
		 * '%gold%' THEN 12 WHEN name like '%color%' THEN 13 WHEN name like
		 * '%bracelet%' THEN 14 ELSE 15 END, name #
		 */

		List<Product> products = new ArrayList<Product>();
		// Long totalRecordsCount = 0L;
		Map<String, Object> result = new HashMap<String, Object>();
		String nameLike = " or name like ";
		String keywordsLike = " or keywords like ";
		String orderBy = " ORDER BY CASE ";
		String whenNameLike = " WHEN name like ";
		String whenKeywordsLike = " WHEN keywords like ";
		String then = " THEN ";
		StringBuilder query = new StringBuilder("select distinct(p) from Product p where name like ");

		try {
			if (keyword != null && keyword.trim().length() > 0) {
				// Constructing the various input string combination and the
				// expressions based on that
				List<String> queryInputCombinations = AffiliateProductUtil.getInputCombinations(keyword.trim());

				// Appending LIKE clause to filter the search results
				for (int i = 0; i < queryInputCombinations.size(); i++) {
					String expression = null;
					if (i == 0) {
						query.append("'" + queryInputCombinations.get(i) + "'");
						expression = keywordsLike + "'" + queryInputCombinations.get(i) + "'";
						query.append(expression);
						continue;
					}
					if (queryInputCombinations.size() >= i + 1) {
						expression = nameLike + "'" + queryInputCombinations.get(i) + "'";
						query.append(expression);
						expression = keywordsLike + "'" + queryInputCombinations.get(i) + "'";
						query.append(expression);
					}
				}

				// Appending order by clause to get the results in order
				int count = 0;
				query.append(orderBy);
				for (int i = 0; i < queryInputCombinations.size(); i++) {
					String expression = null;
					if (i == 0) {
						query.append(whenNameLike + "'" + queryInputCombinations.get(i) + "'" + then + " " + count);
						query.append(
								whenKeywordsLike + "'" + queryInputCombinations.get(i) + "'" + then + " " + (++count));
						continue;
					}
					if (queryInputCombinations.size() >= i + 1) {
						expression = whenNameLike + "'" + queryInputCombinations.get(i) + "'" + then + " " + (++count);
						query.append(expression);
						expression = whenKeywordsLike + "'" + queryInputCombinations.get(i) + "'" + then + " "
								+ (++count);
						query.append(expression);
					}
				}
				query.append(" ELSE " + (++count) + " END");
				logger.info(Log.message("Query - ", query.toString()));
				products = repository.list(Product.class, query.toString(), start, limit);
			}
		} catch (Exception e) {
			logger.info(Log.message("Excepton occurred in keyword based product search process"));
			e.printStackTrace();
		}
		result.put(AffiliateConstants.PRODUCTS, products);
		logger.info(Log.message("Exiting", result));
		return result;
	}

	@Override
	public void createOrUpdate(CJProduct cjProduct, File inputFile, String thread) {

		try {
			Seller seller = getSeller(cjProduct.getAdvertiserId());
			if (seller == null) {
				throw new ServiceException("Seller does not exist in the SELLER table for the advertiserId : "
						+ cjProduct.getAdvertiserId());
			}
			Brand brand = getBrand(cjProduct.getManufacturerName(), seller.getName());

			AdvertiserCategory category = getCategory(cjProduct.getAdvertiserCategory());

			Product prod = new Product(cjProduct, seller, brand, category);
			Product p = repository.find(Product.class, "from Product where advertiserId=? and sku=?",
					cjProduct.getAdvertiserId(), cjProduct.getSku());
			if (p == null) {
				prod = repository.create(prod);
				// logger.info(thread + " -- Created -- " + prod.getId() + " | "
				// + prod.getName());
			} else {

				// This check is very important for performance
				if (AffiliateProductUtil.isUpdateNeeded(p, cjProduct)) {
					// As per Nora, update only the availability and sale price
					p.setInStock(cjProduct.getInStock());
					p.setPrice(cjProduct.getPrice());
					p.setSalePrice(cjProduct.getSalePrice());
					BigDecimal finalPrice = cjProduct.getSalePrice() != null ? cjProduct.getSalePrice()
							: cjProduct.getPrice();
					p.setFinalPrice(finalPrice);
					p.setSale(PriceUtils.getSale(cjProduct.getPrice(), finalPrice));
					p.setInStock(true);
					p = repository.update(p);
					// logger.info(thread + " -- Updated -- " + p.getId() + " |
					// " + p.getName());
				}
			}
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
		}
	}

	@Override
	public Product createOrUpdate(CJProduct cjProduct) {
		// TODO Auto-generated method stub
		return null;
	}

	// @Override
	// public Map<String, Object> searchProducts(String keyword, Integer start,
	// Integer limit) {
	//
	// /**
	// * select distinct(p) from Product p where name like '%gold color
	// * bracelet%' or name like '%color gold bracelet%' or name like '%gold
	// * bracelet color%' or name like '%bracelet gold color%' or name like
	// * '%color bracelet gold%' or name like '%bracelet color gold%' or name
	// * like '%gold%color%bracelet%' or name like '%color%gold%bracelet%' or
	// * name like '%gold%bracelet%color%' or name like
	// * '%bracelet%gold%color%' or name like '%color%bracelet%gold%' or name
	// * like '%bracelet%color%gold%' or name like '%gold%' or name like
	// * '%color%' or name like '%bracelet%' ORDER BY CASE WHEN name like
	// * '%gold color bracelet%' THEN 0 WHEN name like '%color gold bracelet%'
	// * THEN 1 WHEN name like '%gold bracelet color%' THEN 2 WHEN name like
	// * '%bracelet gold color%' THEN 3 WHEN name like '%color bracelet gold%'
	// * THEN 4 WHEN name like '%bracelet color gold%' THEN 5 WHEN name like
	// * '%gold%color%bracelet%' THEN 6 WHEN name like '%color%gold%bracelet%'
	// * THEN 7 WHEN name like '%gold%bracelet %color%' THEN 8 WHEN name like
	// * '%bracelet%gold%color%' THEN 9 WHEN name like '%color%bracelet%gold%'
	// * THEN 10 WHEN name like '%bracelet%color%gold%' THEN 11 WHEN name like
	// * '%gold%' THEN 12 WHEN name like '%color%' THEN 13 WHEN name like
	// * '%bracelet%' THEN 14 ELSE 15 END, name #
	// */
	//
	// List<Product> products = new ArrayList<Product>();
	// // Long totalRecordsCount = 0L;
	// Map<String, Object> result = new HashMap<String, Object>();
	// String nameLike = " or name like ";
	// String orderBy = " ORDER BY CASE ";
	// String when = " WHEN name like ";
	// String then = " THEN ";
	// StringBuilder query = new StringBuilder(
	// "select distinct(p) from Product p where name like ");
	//
	// try {
	// if (keyword != null && keyword.trim().length() > 0) {
	// // Constructing the various input string combination and the
	// // expressions based on that
	// List<String> queryInputCombinations =
	// AffiliateProductUtil.getInputCombinations(keyword.trim());
	//
	// // Appending LIKE clause to filter the search results
	// for (int i = 0; i < queryInputCombinations.size(); i++) {
	// String expression = null;
	// if (i == 0) {
	// query.append("'" + queryInputCombinations.get(i) + "'");
	// continue;
	// }
	// if (queryInputCombinations.size() >= i + 1) {
	// expression = nameLike + "'"
	// + queryInputCombinations.get(i) + "'";
	// query.append(expression);
	// }
	// }
	//
	// // Appending order by clause to get the results in order
	// query.append(orderBy);
	// for (int i = 0; i < queryInputCombinations.size(); i++) {
	// String expression = null;
	// if (i == 0) {
	// query.append(when + "'" + queryInputCombinations.get(i)
	// + "'" + then + " " + i);
	// continue;
	// }
	// if (queryInputCombinations.size() >= i + 1) {
	// expression = when + "'" + queryInputCombinations.get(i)
	// + "'" + then + " " + i;
	// query.append(expression);
	// }
	// }
	// query.append(" ELSE " + queryInputCombinations.size()
	// + " END");
	// logger.info(Log.message("Query - ", query.toString()));
	// products = repository.list(Product.class, query.toString(),
	// start, limit);
	// }
	// } catch (Exception e) {
	// logger.info(Log
	// .message("Excepton occurred in keyword based product search process"));
	// e.printStackTrace();
	// }
	// result.put(AffiliateConstants.PRODUCTS, products);
	// logger.info(Log.message("Exiting", result));
	// return result;
	// }

}
