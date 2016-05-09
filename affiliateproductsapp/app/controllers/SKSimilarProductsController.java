package controllers;

import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.Min;

import org.apache.log4j.Logger;

import controllers.response.AffiliateResponse;
import models.Product;
import play.data.validation.Required;
import play.db.jpa.Transactional;
import services.SKSimiliarProductService;
import utils.JsonUtils;
import utils.TransformUtils;

public class SKSimilarProductsController extends SecuredController {
	
	@Inject
	private static SKSimiliarProductService skSimiliarProductService;
	
	private static final int maxResults = 5;
	
	@Transactional
	public static void getSameProducts(@Required @Min(1) Long id,  @Required @Min(1) Integer pageNumber) {
		validateRequestParameters();
		List<Product> sameProducts = skSimiliarProductService.getSameProducts(id, pageNumber, maxResults);
		String json = JsonUtils.toJson(new AffiliateResponse().add(
				AffiliateResponse.Key.SK_SAME_PRODUCT,
				TransformUtils.toJsonList(sameProducts)));
		renderJSON(json);
	}
	
	@Transactional
	public static void getSimilarProducts(@Required @Min(1) Long id,  @Required @Min(1) Integer pageNumber) {
		validateRequestParameters();
		List<Product> similarProducts = skSimiliarProductService.getSimilarProducts(id, pageNumber, maxResults);
		String json = JsonUtils.toJson(new AffiliateResponse().add(
				AffiliateResponse.Key.SK_SIMILAR_PRODUCT,
				TransformUtils.toJsonList(similarProducts)));
		renderJSON(json);
	}
}
