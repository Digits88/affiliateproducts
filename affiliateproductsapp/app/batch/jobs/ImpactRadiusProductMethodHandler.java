package batch.jobs;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;



import models.impactradius.ImpactRadiusProduct;

import org.apache.log4j.Logger;

import utils.log.Log;

public class ImpactRadiusProductMethodHandler {

	// id title link price description condition image_link availability brand
	// gtin mpn google_product_category product_type additional_image_link
	// item_group_id color size material pattern gender age_group
	// shipping_weight sale_price sale_price_effective_date tax shipping
	// expiration_date online_only excluded_destination product_review_average
	// product_review_count adwords_publish adwords_grouping adwords_labels
	// adwords_queryparam adwords_queryparam2 adwords_queryparam3
	// adwords_redirect promotion_id custom_label_0 custom_label_1
	// custom_label_2 custom_label_3 custom_label_4
	
	public static HashMap<Integer, String> POSITION_PARAM = new HashMap();

	public static HashMap<String, Method> METHOD_HANDLERS = new HashMap();
	
	static {
		try {
			/*METHOD_HANDLERS.put("PROGRAMNAME", ImpaceRadiusProductObjCreatorFromTXT.class
					.getMethod("setAdvertiserNameAndId", CJProduct.class,
							String.class));*/
			METHOD_HANDLERS.put("title", ImpactRadiusProductObjCreatorFromTXT.class
					.getMethod("setTitle", ImpactRadiusProduct.class, String.class));
			
			METHOD_HANDLERS
					.put("description", ImpactRadiusProductObjCreatorFromTXT.class
							.getMethod("setDescription", ImpactRadiusProduct.class,
									String.class));
			
			// sku -- id column in feed
			METHOD_HANDLERS.put("id", ImpactRadiusProductObjCreatorFromTXT.class
					.getMethod("setId", ImpactRadiusProduct.class, String.class));
			
			METHOD_HANDLERS.put("brand",
					ImpactRadiusProductObjCreatorFromTXT.class.getMethod(
							"setBrand", ImpactRadiusProduct.class,
							String.class));
			
			METHOD_HANDLERS.put("price", ImpactRadiusProductObjCreatorFromTXT.class
					.getMethod("setPrice", ImpactRadiusProduct.class, String.class));
			
			METHOD_HANDLERS.put("sale_price", ImpactRadiusProductObjCreatorFromTXT.class
					.getMethod("setSalePrice", ImpactRadiusProduct.class, String.class));
			
			METHOD_HANDLERS.put("link", ImpactRadiusProductObjCreatorFromTXT.class
					.getMethod("setLink", ImpactRadiusProduct.class, String.class));
			METHOD_HANDLERS.put("image_link", ImpactRadiusProductObjCreatorFromTXT.class
					.getMethod("setImageLink", ImpactRadiusProduct.class, String.class));
			METHOD_HANDLERS.put("google_product_category",
					ImpactRadiusProductObjCreatorFromTXT.class.getMethod(
							"setGoogleProductCategory", ImpactRadiusProduct.class,
							String.class));
			METHOD_HANDLERS.put("availability", ImpactRadiusProductObjCreatorFromTXT.class
					.getMethod("setAvailability", ImpactRadiusProduct.class, String.class));
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	private static Logger logger = Logger
			.getLogger(ImpactRadiusProductMethodHandler.class);

	// Invoking the corresponding method based on parameter
	public static Boolean configurePositionForParams(String firstLine) {
		try {
			if (firstLine != null) {
				List<String> params = Arrays.asList(firstLine.split("\t"));
				if (params.contains("google_product_category")) {
					for (int i = 0; i < params.size(); i++) {
						POSITION_PARAM.put(i, params.get(i));
					}
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error(Log
					.message("Exception occurred while performing configurePositionForParams"));
			e.printStackTrace();
		}
		return false;
	}

}
