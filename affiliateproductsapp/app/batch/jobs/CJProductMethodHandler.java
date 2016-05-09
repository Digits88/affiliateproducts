package batch.jobs;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import models.cj.CJProduct;

import org.apache.log4j.Logger;

import utils.log.Log;

public class CJProductMethodHandler {

	// PROGRAMNAME PROGRAMURL CATALOGNAME LASTUPDATED NAME KEYWORDS DESCRIPTION
	// SKU MANUFACTURER
	// MANUFACTURERID UPC ISBN CURRENCY SALEPRICE PRICE RETAILPRICE FROMPRICE
	// BUYURL IMPRESSIONURL
	// IMAGEURL ADVERTISERCATEGORY THIRDPARTYID THIRDPARTYCATEGORY AUTHOR ARTIST
	// TITLE PUBLISHER LABEL
	// FORMAT SPECIAL GIFT PROMOTIONALTEXT STARTDATE ENDDATE OFFLINE ONLINE
	// INSTOCK CONDITION WARRANTY STANDARDSHIPPINGCOST

	public static HashMap<Integer, String> POSITION_PARAM = new HashMap();

	public static HashMap<String, Method> METHOD_HANDLERS = new HashMap();
	static {
		try {
			METHOD_HANDLERS.put("PROGRAMNAME", CJProductObjCreatorFromTSV.class
					.getMethod("setAdvertiserNameAndId", CJProduct.class,
							String.class));
			METHOD_HANDLERS.put("NAME", CJProductObjCreatorFromTSV.class
					.getMethod("setName", CJProduct.class, String.class));
			METHOD_HANDLERS
					.put("DESCRIPTION", CJProductObjCreatorFromTSV.class
							.getMethod("setDescription", CJProduct.class,
									String.class));
			METHOD_HANDLERS.put("SKU", CJProductObjCreatorFromTSV.class
					.getMethod("setSku", CJProduct.class, String.class));
			METHOD_HANDLERS.put("MANUFACTURER",
					CJProductObjCreatorFromTSV.class.getMethod(
							"setManufacturerName", CJProduct.class,
							String.class));
			METHOD_HANDLERS.put("SALEPRICE", CJProductObjCreatorFromTSV.class
					.getMethod("setSalePrice", CJProduct.class, String.class));
			METHOD_HANDLERS.put("PRICE", CJProductObjCreatorFromTSV.class
					.getMethod("setPrice", CJProduct.class, String.class));
			METHOD_HANDLERS
					.put("RETAILPRICE", CJProductObjCreatorFromTSV.class
							.getMethod("setRetailPrice", CJProduct.class,
									String.class));
			METHOD_HANDLERS.put("BUYURL", CJProductObjCreatorFromTSV.class
					.getMethod("setBuyURL", CJProduct.class, String.class));
			METHOD_HANDLERS.put("IMAGEURL", CJProductObjCreatorFromTSV.class
					.getMethod("setImageURL", CJProduct.class, String.class));
			METHOD_HANDLERS.put("ADVERTISERCATEGORY",
					CJProductObjCreatorFromTSV.class.getMethod(
							"setAdvertiserCategory", CJProduct.class,
							String.class));
			METHOD_HANDLERS.put("INSTOCK", CJProductObjCreatorFromTSV.class
					.getMethod("setInStock", CJProduct.class, String.class));
			METHOD_HANDLERS.put("CURRENCY", CJProductObjCreatorFromTSV.class
					.getMethod("setCurrency", CJProduct.class, String.class));
			METHOD_HANDLERS.put("KEYWORDS", CJProductObjCreatorFromTSV.class
					.getMethod("setKeywords", CJProduct.class, String.class));
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	private static Logger logger = Logger
			.getLogger(CJProductMethodHandler.class);

	// Invoking the corresponding method based on parameter
	public static Boolean configurePositionForParams(String firstLine) {
		try {
			if (firstLine != null) {
				List<String> params = Arrays.asList(firstLine.split("\t"));
				if (params.contains("PROGRAMNAME")) {
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