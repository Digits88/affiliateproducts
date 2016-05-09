package services.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.OptimisticLockException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.Brand;
import models.AdvertiserCategory;
import models.Product;
import models.Seller;
import models.cj.CJProduct;
import models.rakuten.RakutenProduct;
import models.searskmart.SearsKmartProduct;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.exception.LockAcquisitionException;

import play.db.jpa.JPA;
import product.filter.ProductFilter;
import product.filter.ProductFilterFactory;
import repositories.Repository;
import services.CacheService;
import services.ProductService;
import services.SKProductService;
import services.rakuten.RakutenProductService;
import utils.AffiliateProductUtil;
import utils.PriceUtils;
import utils.log.Log;

import com.google.inject.Inject;

import constants.AffiliateConstants;
import constants.RakutenConstants;
import enums.ProductFilterEntity;
import exceptions.ServiceException;

public class DefaultRakutenProductService implements RakutenProductService {
	private static Logger logger = Logger.getLogger(DefaultRakutenProductService.class);

	@Inject
	private Repository repository;

	@Inject
	private CacheService cacheService;

	@Override
	public void createOrUpdate(RakutenProduct rakutenProduct, Map<String, String> colorSKUMap) {

		Product p = null;
		Product prod = null;
		try {
			// logger.info(Log.message("createOrUpdate"));
			Seller seller = getSeller(rakutenProduct.getMerchantId());

			if (seller == null) {
				throw new ServiceException("Seller does not exist in the SELLER table for the advertiserId : "
						+ rakutenProduct.getMerchantId());
			}
			Brand brand = getBrand(rakutenProduct.getBrand(), rakutenProduct.getMerchantName());

			AdvertiserCategory category = null;
			if (rakutenProduct.getMerchantId() == RakutenConstants.PETSMART_ADVERTISERID) {
				category = getCategory(rakutenProduct.getSecondaryCategory());
			} else if (rakutenProduct.getMerchantId() == RakutenConstants.BLOOMINGDALES_ADVERTISERID
					|| rakutenProduct.getMerchantId() == RakutenConstants.KOHLS_ADVERTISERID
					|| rakutenProduct.getMerchantId() == RakutenConstants.JCPENNY_ADVERTISERID) {
				if (rakutenProduct.getSecondaryCategory() != null
						&& rakutenProduct.getSecondaryCategory().trim().length() > 0) {
					category = getCategory(
							rakutenProduct.getPrimaryCategory() + " > " + rakutenProduct.getSecondaryCategory());
				} else {
					category = getCategory(rakutenProduct.getPrimaryCategory());
				}
			} else if (rakutenProduct.getMerchantId() == RakutenConstants.WALMART_ADVERTISERID) {
				if (rakutenProduct.getPrimaryCategory() != null && rakutenProduct.getSecondaryCategory() != null) {
					category = getCategory(
							rakutenProduct.getSecondaryCategory().concat(RakutenConstants.CATEGORY_LINK_WITH_SPACE)
									.concat(rakutenProduct.getPrimaryCategory()));
				} else if (rakutenProduct.getSecondaryCategory() != null) {
					category = getCategory(rakutenProduct.getSecondaryCategory());
				} else {
					category = getCategory(rakutenProduct.getPrimaryCategory());
				}
			} else if (rakutenProduct.getMerchantId() == RakutenConstants.NIKE_ADVERTISERID) {
				if (rakutenProduct.getSecondaryCategory() != null
						&& rakutenProduct.getSecondaryCategory().trim().length() > 0) {
					category = getCategory(rakutenProduct.getPrimaryCategory()
							+ RakutenConstants.CATEGORY_LINK_WITH_SPACE + rakutenProduct.getSecondaryCategory()
									.replaceAll("~~", RakutenConstants.CATEGORY_LINK_WITH_SPACE));
					// logger.info("Nike Category : " + category.getId() + " --
					// " + category.getName());
				} else {
					category = getCategory(rakutenProduct.getPrimaryCategory());
				}
			} else {
				category = getCategory(rakutenProduct.getPrimaryCategory());
			}

			// need modify to new one <-- change it later
			prod = new Product(rakutenProduct, seller, brand, category, colorSKUMap);

			if (rakutenProduct.getMerchantId() == RakutenConstants.SHOESCOM_ADVERTISERID) {
				try {
					p = repository.find(Product.class, "from Product where advertiserId=? and sku=?",
							rakutenProduct.getMerchantId(), rakutenProduct.getName());
				} catch (Exception e) {
					logger.error(Log.message("ISSUE in Finding --> " + rakutenProduct.getMerchantId() + " -- "
							+ rakutenProduct.getName()));
					logger.error(Log.message(e.getMessage()));
				}
			} else if (rakutenProduct.getMerchantId() == RakutenConstants.NORDSTROM_ADVERTISERID) {
				String sku = rakutenProduct.getPrimaryCategory() + "-" + rakutenProduct.getManufacturerName() + "-"
						+ rakutenProduct.getPartNumber();
				try {
					p = repository.find(Product.class, "from Product where advertiserId=? and sku=?",
							rakutenProduct.getMerchantId(), sku);
				} catch (Exception e) {
					logger.error(Log.message("ISSUE in Finding --> " + rakutenProduct.getMerchantId() + " -- " + sku));
					logger.error(Log.message(e.getMessage()));
				}
			} else if (rakutenProduct.getMerchantId() == RakutenConstants.JCPENNY_ADVERTISERID) {
				String color = colorSKUMap.get(rakutenProduct.getPrimaryCategory() + "-" + rakutenProduct.getName()
						+ "-" + rakutenProduct.getSku());
				String sku = rakutenProduct.getPrimaryCategory() + "-" + rakutenProduct.getName() + "-" + color;
				try {
					p = repository.find(Product.class, "from Product where advertiserId=? and sku=?",
							rakutenProduct.getMerchantId(), sku);
				} catch (Exception e) {
					logger.error(Log.message("ISSUE in Finding --> " + rakutenProduct.getMerchantId() + " -- " + sku));
					logger.error(Log.message(e.getMessage()));
				}
			} else if (rakutenProduct.getMerchantId() == RakutenConstants.KOHLS_ADVERTISERID) {
				String name = rakutenProduct.getName().split(",")[0];
				String partName = "";
				if (rakutenProduct.getPartNumber() != null && rakutenProduct.getPartNumber().trim().length() > 0) {
					partName = "-" + rakutenProduct.getPartNumber();
				}
				String cate = rakutenProduct.getPrimaryCategory();
				String sku = cate + "-" + name + partName;
				// logger.info(Log.message("38605 SKU: " + sku));
				try {
					p = repository.find(Product.class, "from Product where advertiserId=? and sku=?",
							rakutenProduct.getMerchantId(), sku);
				} catch (Exception e) {
					logger.error(Log.message("ISSUE in Finding --> " + rakutenProduct.getMerchantId() + " -- " + sku));
					logger.error(Log.message(e.getMessage()));
				}
			} else if (rakutenProduct.getMerchantId() == RakutenConstants.NIKE_ADVERTISERID) {
				try {
					p = repository.find(Product.class, "from Product where advertiserId=? and sku=?",
							rakutenProduct.getMerchantId(), rakutenProduct.getPartNumber());
				} catch (Exception e) {
					logger.error(Log.message("ISSUE in Finding --> " + rakutenProduct.getMerchantId() + " -- "
							+ rakutenProduct.getPartNumber()));
					logger.error(Log.message(e.getMessage()));
				}
			} else {
				try {
					p = repository.find(Product.class, "from Product where advertiserId=? and sku=?",
							rakutenProduct.getMerchantId(), rakutenProduct.getSku());
				} catch (Exception e) {
					logger.error(Log.message("ISSUE in Finding --> " + rakutenProduct.getMerchantId() + " -- "
							+ rakutenProduct.getSku()));
					logger.error(Log.message(e.getMessage()));
				}
			}

			if (p == null) {
				try {
					repository.create(prod);
				} catch (Exception e) {
					logger.error(Log.message("ISSUE in creating --> " + prod.getName() + " -- " + prod.getSku()));
					logger.error(Log.message(e.getMessage()));
				}
			} else {
				if (AffiliateProductUtil.isUpdateNeededForRakutenProduct(p, rakutenProduct)) {
					p.setPrice((rakutenProduct.getRetailPrice() != null) ? rakutenProduct.getRetailPrice()
							: rakutenProduct.getSalePrice());
					p.setSalePrice(rakutenProduct.getSalePrice());
					BigDecimal finalPrice = (rakutenProduct.getSalePrice() != null
							&& rakutenProduct.getSalePrice().compareTo(BigDecimal.ZERO) == 1)
									? rakutenProduct.getSalePrice() : rakutenProduct.getRetailPrice();
					p.setFinalPrice(finalPrice);
					p.setSale(PriceUtils.getSale(p.getPrice(), finalPrice));
					p.setInStock(true);
					try {
						repository.update(p);
					} catch (Exception e) {
						logger.error("ISSUE in updating --> " + p.getName() + " -- " + p.getSku());
						logger.error(Log.message(e.getMessage()));
					}
				}
			}
		} catch (Exception e) {
			logger.error(Log.message("!!!!! createOrUpdate Function has issue !!!!!  " + e.getMessage()));
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

	public Map<String, Object> getSellers(Integer offset, Integer maxResults) {
		Map<String, Object> result = new HashMap<String, Object>();
		List<Seller> sellers = repository.createNamedQuery("JPQL_GET_SELLERS", Seller.class, offset, maxResults);
		result.put(AffiliateConstants.SELLERS, sellers);
		Long count = Seller.count();
		result.put(AffiliateConstants.COUNT, count);
		return result;
	}

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

	public Product createOrUpdate(CJProduct cjProduct) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This function is used for Rakuten Super Feed, like Walmart
	 */
	@Override
	public void createOrUpdate(RakutenProduct rakutenProduct, String threadName) {
		Product p = null;
		Product prod = null;
		try {
			// logger.info(threadName + " ---- Processing Prod -- " +
			// rakutenProduct.getSku());
			// logger.info(Log.message("createOrUpdate"));
			Seller seller = getSeller(rakutenProduct.getMerchantId());

			if (seller == null) {
				throw new ServiceException("Seller does not exist in the SELLER table for the advertiserId : "
						+ rakutenProduct.getMerchantId());
			}
			// logger.info(threadName + " ---- Get Brand");
			Brand brand = getBrand(rakutenProduct.getBrand(), rakutenProduct.getMerchantName());
			// logger.info(threadName + " ---- Get Brand -- " +
			// brand.getName());
			AdvertiserCategory category = null;
			// logger.info(threadName + " ---- Get category");
			if (rakutenProduct.getMerchantId() == RakutenConstants.WALMART_ADVERTISERID) {
				if (rakutenProduct.getPrimaryCategory() != null && rakutenProduct.getSecondaryCategory() != null) {
					category = getCategory(
							rakutenProduct.getSecondaryCategory().concat(RakutenConstants.CATEGORY_LINK_WITH_SPACE)
									.concat(rakutenProduct.getPrimaryCategory()));
				} else if (rakutenProduct.getSecondaryCategory() != null) {
					category = getCategory(rakutenProduct.getSecondaryCategory());
				} else {
					category = getCategory(rakutenProduct.getPrimaryCategory());
				}
			} else {
				category = getCategory(rakutenProduct.getPrimaryCategory());
			}
			// logger.info(threadName + " ---- Get category -- " +
			// category.getName());

			// need modify to new one <-- change it later
			prod = new Product(rakutenProduct, seller, brand, category);

			try {
				p = repository.find(Product.class, "from Product where advertiserId=? and sku=?",
						rakutenProduct.getMerchantId(), rakutenProduct.getSku());
			} catch (Exception e) {
				logger.error(Log
						.message("ISSUE in Finding --> " + rakutenProduct.getMerchantId() + " -- " + rakutenProduct.getSku()));
				logger.error(Log.message(e.getMessage()));
			}
			// logger.info(threadName + " ---- Start Create/Update ");

			if (p == null) {
				// logger.info(threadName + " -- " +
				// prod.getAdvertiserCategory() + " -- Creating Prod -- " +
				// prod.getSku());
				try {
					repository.create(prod);
				} catch (Exception e) {
					logger.error(Log.message("ISSUE in creating --> " + prod.getName() + " -- " + prod.getSku()));
					logger.error(Log.message(e.getMessage()));
				}
			} else {
				if (AffiliateProductUtil.isUpdateNeededForRakutenProduct(p, rakutenProduct)) {
					p.setPrice((rakutenProduct.getRetailPrice() != null) ? rakutenProduct.getRetailPrice()
							: rakutenProduct.getSalePrice());
					p.setSalePrice(rakutenProduct.getSalePrice());
					BigDecimal finalPrice = (rakutenProduct.getSalePrice() != null
							&& rakutenProduct.getSalePrice().compareTo(BigDecimal.ZERO) == 1)
									? rakutenProduct.getSalePrice() : rakutenProduct.getRetailPrice();
					p.setFinalPrice(finalPrice);
					p.setSale(PriceUtils.getSale(p.getPrice(), finalPrice));
					p.setInStock(true);
					// logger.info(threadName + " -- " +
					// p.getAdvertiserCategory() + " -- Updating P -- " +
					// p.getSku());
					try {
						repository.update(p);
					} catch (Exception e) {
						logger.error(Log.message("ISSUE in updating --> " + p.getName() + " -- " + p.getSku()));
						logger.error(Log.message(e.getMessage()));
					}
				}
				// logger.info(threadName + " -- No Change for P -- " +
				// p.getSku());
			}
		} catch (Exception e) {
			logger.error(Log.message("!!!!! createOrUpdate Function has issue !!!!!  " + e.getMessage()));
			e.printStackTrace();
		}
	}
}
