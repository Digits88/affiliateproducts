package controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import models.Product;
import models.Seller;

import org.apache.log4j.Logger;

import play.Play;
import play.data.validation.Required;
import utils.JsonUtils;
import utils.PriceUtils;
import utils.TransformUtils;
import utils.log.Log;
import constants.AffiliateConstants;
import controllers.json.ProductJson;
import controllers.json.SellerJson;
import controllers.response.AffiliateResponse;
import exceptions.controller.ControllerException;

public class ProductController extends SecuredController {

	private static Logger logger = Logger.getLogger(ProductController.class);

	/**
	 * Method to retrieve n number sellers
	 * 
	 * @param pageNumber
	 * @param maxResults
	 */
	public static void getSellers(@Required Integer pageNumber,
			Integer maxResults) {

		logger.info(Log.message("Entering",
				Log.Param.get("pageNumber", pageNumber),
				Log.Param.get("maxResults", maxResults)));
		validation.min(pageNumber, 1);
		validateRequestParameters();

		int limit = Integer.parseInt(Play.configuration
				.getProperty("affiliate.product.max.results.get.sellers"));
		if (maxResults != null) {
			if (maxResults > limit) {
				maxResults = limit;
			}
		} else {
			maxResults = limit;
		}

		int offset = (pageNumber - 1) * maxResults;
		Map<String, Object> result = productService.getSellers(offset,
				maxResults);

		List<SellerJson> sellers = TransformUtils
				.toJsonList((List<Seller>) result.get(AffiliateConstants.SELLERS));

		Long count = (Long) result.get(AffiliateConstants.COUNT);

		AffiliateResponse affiliateResponse = new AffiliateResponse();
		affiliateResponse.add(AffiliateResponse.Key.SELLERS, sellers);
		affiliateResponse.add(AffiliateResponse.Key.COUNT, count);
		String json = JsonUtils.toJson(affiliateResponse);
		// logger.info(Log.message("Exiting", Log.Param.get("json", json)));
		logger.info(Log.message("Exiting"));
		renderJSON(json);
	}

	/**
	 * Method to retrieve n number of products based on the seller id
	 * 
	 * @param id
	 * @param pageNumber
	 * @param maxResults
	 */
	public static void getBySeller(@Required Long id,
			@Required Integer pageNumber, Integer maxResults) {

		logger.info(Log.message("Entering", Log.Param.get("SellerId", id),
				Log.Param.get("pageNumber", pageNumber),
				Log.Param.get("maxResults", maxResults)));
		validation.min(pageNumber, 1);
		validateRequestParameters();

		int limit = Integer.parseInt(Play.configuration
				.getProperty("affiliate.product.max.results.get.by.seller"));
		if (maxResults != null) {
			if (maxResults > limit) {
				maxResults = limit;
			}
		} else {
			maxResults = limit;
		}

		int offset = (pageNumber - 1) * maxResults;
		Map<String, Object> result = productService.getBySeller(id, offset,
				maxResults);

		List<ProductJson> products = TransformUtils
				.toJsonList((List<Product>) result.get(AffiliateConstants.PRODUCTS));

		Long count = (Long) result.get(AffiliateConstants.COUNT);

		AffiliateResponse affiliateResponse = new AffiliateResponse();
		affiliateResponse.add(AffiliateResponse.Key.PRODUCTS, products);
		affiliateResponse.add(AffiliateResponse.Key.COUNT, count);
		String json = JsonUtils.toJson(affiliateResponse);
		// logger.info(Log.message("Exiting", Log.Param.get("json", json)));
		logger.info(Log.message("Exiting"));
		renderJSON(json);
	}

	// public static void findProducts(@Required String searchText,
	// @Required @Min(1) Integer pageNumber,
	// @Required @Min(1) @Max(100) Integer pageSize) {
	// logger.info(Log.message("Entering", Log.Param.get("searchText",
	// searchText),
	// Log.Param.get("pageNumber", pageNumber),
	// Log.Param.get("pageSize", pageSize)));
	// validateRequestParameters();
	//
	// /**
	// * tags is used only in the case of HOME department right now - Only one
	// tags should be passed.
	// * If multiple tags are provided - only products that are associated to
	// all provided tags are included.
	// */
	// String tags = "";
	// if(MagazineTL.getDepartment().getName().equalsIgnoreCase(enums.Department.HOME.name())){
	// Brand brand = brandService.getRandomBrandWithProducts();
	// tags = Long.toString(brand.getTagId());
	// }
	//
	// String sellers =
	// metadataService.getSellers(MagazineTL.getDepartment().getId());
	//
	// int limit = pageSize + 2; // +2 because we need to know if there is more
	// data to load
	// Iterable<SywProduct> products =
	// shopYourWayService.findProducts(MagazineTL.getToken(), searchText,
	// pageNumber, limit, SywProductOrderBy.relevancy,
	// SywProductAvailability.AVAILABLE_UNKNOWN, sellers, tags);
	//
	// products =
	// Lists.newArrayList(ProductValidatorUtil.filterProductsBasedOnSourceAndAvailability(products,
	// MagazineTL.getDepartment().getId()));
	//
	// Boolean hasMoreData = Iterables.size(products) > pageSize;
	// // Limit to pageSize
	// products = Iterables.limit(products, pageSize);
	//
	// List<SywProduct> sywProducts = Lists.newArrayList(products);
	//
	// String json = JsonUtils.toJson(new
	// MagResponse().add(MagResponse.Key.PRODUCTS,
	// TransformUtils.toProductJson(sywProducts)).add(
	// MagResponse.Key.HASMOREDATA, hasMoreData));
	//
	// logger.info(Log.message("Exiting" , Log.Param.get("json", json)));
	// renderJSON(json);
	//
	// }

	

	/**
	 * Method to retrieve n number of products based on the keyword
	 * 
	 * @param keyword
	 * @param pageNumber
	 */
	public static void searchProducts(String keyword,
			@Required Integer pageNumber) {

		logger.info(Log.message("Entering", Log.Param.get("keyword", keyword),
				Log.Param.get("pageNumber", pageNumber)));
		validation.min(pageNumber, 1);
		validateRequestParameters();

		int limit = Integer.parseInt(Play.configuration
				.getProperty("affiliate.max.product.search.results"));
		int start = (pageNumber - 1) * limit;

		Map<String, Object> result = productService.searchProducts(keyword,
				start, limit);

		List<ProductJson> products = TransformUtils
				.toJsonList((List<Product>) result.get(AffiliateConstants.PRODUCTS));

		AffiliateResponse affiliateResponse = new AffiliateResponse();
		affiliateResponse.add(AffiliateResponse.Key.PRODUCTS, products);
		String json = JsonUtils.toJson(affiliateResponse);
		logger.info(Log.message("Exiting", Log.Param.get("json", json)));
		renderJSON(affiliateResponse);
	}

}
