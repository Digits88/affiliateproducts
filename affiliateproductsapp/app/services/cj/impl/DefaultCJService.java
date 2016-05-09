package services.cj.impl;

import groovy.util.Node;
import groovy.util.NodeList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.cj.CJProduct;

import org.apache.log4j.Logger;

import play.Play;
import play.libs.WS;
import play.libs.WS.WSRequest;
import services.cj.CJService;
import utils.AffiliateStringUtil;
import utils.log.Log;
import ws.WSClient;

public class DefaultCJService implements CJService {

	private static Logger logger = Logger.getLogger(DefaultCJService.class);

	private static HashMap<String, Method> METHOD_HANDLERS = new HashMap();

	static {
		try {
			METHOD_HANDLERS.put("advertiser-id", CJProduct.class.getMethod(
					"setAdvertiserIdReflection", String.class));
			METHOD_HANDLERS.put("advertiser-category", CJProduct.class
					.getMethod("setAdvertiserCategory", String.class));
			METHOD_HANDLERS.put("buy-url",
					CJProduct.class.getMethod("setBuyURL", String.class));
			METHOD_HANDLERS.put("description",
					CJProduct.class.getMethod("setDescription", String.class));
			METHOD_HANDLERS.put("image-url",
					CJProduct.class.getMethod("setImageURL", String.class));
			METHOD_HANDLERS.put("in-stock", CJProduct.class.getMethod(
					"setInStockReflection", String.class));
			METHOD_HANDLERS.put("manufacturer-name", CJProduct.class.getMethod(
					"setManufacturerName", String.class));
			METHOD_HANDLERS.put("name",
					CJProduct.class.getMethod("setName", String.class));
			METHOD_HANDLERS.put("price", CJProduct.class.getMethod(
					"setPriceReflection", String.class));
			METHOD_HANDLERS.put("retail-price", CJProduct.class.getMethod(
					"setRetailPriceReflection", String.class));
			METHOD_HANDLERS.put("sale-price", CJProduct.class.getMethod(
					"setSalePriceReflection", String.class));
			METHOD_HANDLERS.put("sku",
					CJProduct.class.getMethod("setSku", String.class));
			METHOD_HANDLERS.put("currency",
					CJProduct.class.getMethod("setCurrency", String.class));
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<CJProduct> getProductsByAdvertiserId(String advertiserId) {
		List<CJProduct> cjProductsList = new ArrayList<CJProduct>();
		int recordsPerPage = Integer.parseInt(Play.configuration
				.getProperty("affiliate.cj.number.of.records.per.page"));
		int maxPageCount = Integer.parseInt(Play.configuration
				.getProperty("affiliate.cj.max.number.of.pages"));
		for (int pageNumber = 1; pageNumber <= maxPageCount; pageNumber++) {
			List<CJProduct> cjProducts = getProductsByAdvertiserId(
					advertiserId, recordsPerPage, pageNumber);
			if (cjProducts != null) {
				cjProductsList.addAll(cjProducts);
			} else {
				break;
			}
		}
		return cjProductsList;
	}

	private List<CJProduct> getProductsByAdvertiserId(String advertiserId,
			int recordsPerPage, int pageNumber) {
		List<CJProduct> cjProducts = null;
		String url = Play.configuration
				.getProperty("affiliate.cj.products.search.by.advertiser.id");
		String websiteId = Play.configuration
				.getProperty("affiliate.cj.shopyourway.website.id");
		WSRequest request = WS.url(url, websiteId, advertiserId,
				Integer.toString(recordsPerPage), Integer.toString(pageNumber));
		request.setHeader("Authorization",
				Play.configuration.getProperty("affiliate.cj.authorization.key"));
		Node parentNode = WSClient.GETXmlNode(request);
		if (parentNode != null) {
			Node products = (Node) ((NodeList) parentNode.get("products"))
					.get(0);
			List<Node> productList = products.children();
			if (productList != null && productList.size() > 0) {
				cjProducts = new ArrayList<CJProduct>();
				for (Node product : productList) {
					CJProduct cjProduct = createCJProduct(product);

					if (cjProduct != null && isValidToAdd(cjProduct)) {
						if (cjProduct.getName() != null
								&& cjProduct.getName().trim().length() > 0) {
							cjProduct.setName(AffiliateStringUtil
									.afterUnEscapeHtmlXml(cjProduct.getName()));
						}
						if (cjProduct.getDescription() != null
								&& cjProduct.getDescription().length() > 0) {
						cjProduct.setDescription(AffiliateStringUtil
								.afterUnEscapeHtmlXml(cjProduct
										.getDescription()));
					}
						if (cjProduct.getAdvertiserCategory() != null
								&& cjProduct.getAdvertiserCategory().length() > 0) {
							cjProduct.setAdvertiserCategory(AffiliateStringUtil
									.afterUnEscapeHtmlXml(cjProduct
											.getAdvertiserCategory()));
					}
					cjProducts.add(cjProduct);
				}
			}
		}
		}
		return cjProducts;
	}

	private CJProduct createCJProduct(Node product) {
		try {
			CJProduct cjProduct = new CJProduct();
			List<Node> params = product.children();
			for (Node param : params) {
				String paramName = (String) param.name();
				if (((NodeList) param.value()).size() > 0) {
					String paramValue = (String) ((NodeList) param.value())
							.get(0);
					// logger.info(Log.message(paramName + " : " + paramValue));
					Method method = METHOD_HANDLERS.get(paramName);
					if (method != null) {
						method.invoke(cjProduct, paramValue);
					}
					// if (cjProduct.getManufacturerName() == null) {
					// cjProduct.setManufacturerName(cjProduct
					// .getAdvertiserName());
					// }
				}
			}
			return cjProduct;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// Validating the product before adding into DB
		private Boolean isValidToAdd(CJProduct cjProduct) {
			try {
				if (cjProduct != null) {
					if (cjProduct.getCurrency().equals("USD")) {
						return true;
					}
				}
			} catch (Exception e) {
				logger.error(Log
						.message("Exception occurred while validating the product"));
				e.printStackTrace();
			}
			return false;
		}
}
