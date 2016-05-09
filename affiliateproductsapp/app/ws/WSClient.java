package ws;

import java.util.concurrent.TimeUnit;

import models.shopyourway.WSError;

import org.apache.log4j.Logger;

import play.Play;
import play.libs.Time;
import play.libs.WS;
import utils.JsonUtils;
import utils.log.Log;

import com.google.gson.JsonElement;

import exceptions.WSException;
import groovy.util.Node;
import groovy.util.XmlParser;

public class WSClient {

	private static Logger logger = Logger.getLogger(WSClient.class);

	private static final long TIMEOUT = Time.parseDuration(Play.configuration
			.getProperty("affiliate.ws.timeout"));
	private static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;
	private static String NULL_JSON_KEY = "err.msg.json.null";

	public static JsonElement GET(WS.WSRequest newRequest, String errorMessage,
			Object... errMsgParams) {
		WS.HttpResponse newResponse = null;
		try {
			logger.info(Log.message("Request URL - " + newRequest.url));
			newResponse = newRequest.getAsync().get(TIMEOUT, TIMEOUT_UNIT);
		} catch (Exception e) {
			logger.error(Log.message("Exception - " + e.getMessage()));
		}
		assertResponse(newResponse, errorMessage, errMsgParams);
		JsonElement json = newResponse.getJson();
		assertJson(json);
		return json;
	}

	public static JsonElement POST(WS.WSRequest newRequest,
			String errorMessage, Object... errMsgParams) {
		WS.HttpResponse newResponse = null;
		try {
			logger.info(Log.message("Request URL - " + newRequest.url));
			newResponse = newRequest.postAsync().get(TIMEOUT, TIMEOUT_UNIT);
		} catch (Exception e) {
			logger.error(Log.message("Exception - " + e.getMessage()));
		}
		assertResponse(newResponse, errorMessage, errMsgParams);
		JsonElement json = newResponse.getJson();
		assertJson(json);
		return json;
	}

	private static void assertResponse(WS.HttpResponse response, String errMsg,
			Object... errMsgParams) {
		if (response == null) {
			logger.error(Log.message("Response is null"));
			throw new WSException(errMsg, errMsgParams);
		} else if (!response.success()) {
			logger.error(Log.message(response.getString()));
			throw new WSException(errMsg, errMsgParams);
		}
	}

	private static void assertJson(JsonElement json) {
		if (json == null || json.isJsonNull()) {
			throw new WSException(NULL_JSON_KEY);
		} else if (JsonUtils.isErrorJson(json)) {
			throw new WSException(JsonUtils.fromJson(json, WSError.class));
		}
	}

	public static Node GETXmlNode(WS.WSRequest newRequest) {
		WS.HttpResponse newResponse = null;
		Node node = null;
		try {
			logger.info(Log.message("Request URL - " + newRequest.url));
			newResponse = newRequest.getAsync().get(TIMEOUT, TIMEOUT_UNIT);
			if(newResponse!=null){
				node = new XmlParser().parseText(newResponse.getString());
			}else{
				logger.error(Log.message("Response is null for the request - " + newRequest.url));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return node;
	}
}
