package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.validation.constraints.Min;

import models.AdvertiserCategory;
import models.Category;

import org.apache.log4j.Logger;

import play.Play;
import play.data.validation.Required;
import services.CategoryService;
import utils.JsonUtils;
import utils.log.Log;
import controllers.json.CategoryJson;
import controllers.json.CategoryV1Json;
import controllers.response.AffiliateResponse;

public class CategoryController extends SecuredController {

	@Inject
	protected static CategoryService categoryService;

	private static Logger logger = Logger.getLogger(CategoryController.class);

	public static void getSellerCategories(@Required @Min(1) Long sellerId,
			@Required Boolean isCreate, @Required @Min(1) Integer pageNumber) {
		validateRequestParameters();

		int maxResults = Integer.parseInt(Play.configuration
				.getProperty("category.max.results"));

		List<AdvertiserCategory> advertiserCategories = categoryService
				.getSellerCategories(sellerId, pageNumber, maxResults);

		List<CategoryJson> categories = new ArrayList<CategoryJson>();

		for (AdvertiserCategory category : advertiserCategories) {
			categories.add(new CategoryJson(category, isCreate));
		}

		AffiliateResponse affiliateResponse = new AffiliateResponse();
		affiliateResponse.add(AffiliateResponse.Key.CATEGORY, categories);
		String json = JsonUtils.toJson(affiliateResponse);
		logger.info(Log.message("Exiting"));
		renderJSON(json);

	}

	public static void getChildCategories(@Required @Min(1) Long categoryId) {
		validateRequestParameters();

		List<Category> categories = categoryService
				.getChildCategories(categoryId);

		List<CategoryV1Json> categoryJsons = new ArrayList<CategoryV1Json>();

		for (Category category : categories) {
			categoryJsons.add(new CategoryV1Json(category));
		}

		AffiliateResponse affiliateResponse = new AffiliateResponse();
		affiliateResponse.add(AffiliateResponse.Key.CATEGORY, categories);
		String json = JsonUtils.toJson(affiliateResponse);
		logger.info(Log.message("Exiting"));
		renderJSON(json);
	}

	public static void mapCategory(@Required Map<Long, Long> categoryMap) {
		validateRequestParameters();
		categoryService.mapCategory(categoryMap);
	}
}
