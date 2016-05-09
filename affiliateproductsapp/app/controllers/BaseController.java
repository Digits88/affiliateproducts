package controllers;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import models.log.LogParam;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import play.Play;
import play.data.validation.Validation;
import play.db.jpa.NoTransaction;
import play.i18n.Messages;
import play.mvc.Catch;
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.Http;
import play.mvc.Util;
import repositories.Repository;
import services.CacheService;
import services.ProductService;
import utils.TL;
import utils.log.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import controllers.json.VersionJson;
import exceptions.BaseException;
import exceptions.ServiceError;
import exceptions.controller.RenderErrorJson;

public class BaseController extends Controller {

	private static Logger logger = Logger.getLogger(BaseController.class);

	public static final String ARG_CUSTOM_BODY = "customBody";
	public static final String ARG_ERROR_MSG = "errorMessage";
	public static final String ARG_EXCEPTION = "exception";
	public static final String ARG_TOKEN = "token";

	@Inject
	protected static CacheService cacheService;

	@Inject
	protected static Repository repository;

	@Inject
	protected static ProductService productService;

	@Catch(value = BaseException.class, priority = 0)
	public static void catchBaseException(BaseException exception) {
		logger.error(Log.message(
				exception.getClass().getName(),
				LogParam.get("exception",
						ExceptionUtils.getMessage((Throwable) exception)),
				exception));
		putExceptionToRequestArgs(exception);
		errorJSON(Http.StatusCode.OK, exception.getMessage(),
				exception.getParams());
	}

	@Catch(value = ServiceError.class, priority = 1)
	public static void catchServiceException(ServiceError exception) {
		logger.error(Log.message(
				exception.getClass().getName(),
				LogParam.get("serviceError",
						ExceptionUtils.getMessage((Throwable) exception)),
				exception));
		putExceptionToRequestArgs(exception);
		errorJSON(Http.StatusCode.INTERNAL_ERROR, exception.getMessage(),
				exception.getParams());
	}

	@Catch(value = Throwable.class, priority = 2)
	public static void catchAnyOtherException(Throwable throwable) {
		logger.error(Log.message(
				throwable.getClass().getName(),
				LogParam.get("throwable", ExceptionUtils.getMessage(throwable)),
				throwable));
		putExceptionToRequestArgs(throwable);
		errorJSON(Http.StatusCode.INTERNAL_ERROR, throwable.toString());
	}

	@Util
	public static void putExceptionToRequestArgs(Throwable throwable) {
		request.args.put(ARG_EXCEPTION, throwable);
	}

	@Finally
	public static void afterRequest(String token, Long userId) {
		logger.warn(Log.message("action: " + request.action));
		/**
		 * This should be in the end only as it is clearing the thread local
		 * object.
		 */
		TL.remove();
	}

	@NoTransaction
	public static void version() {
		renderJSON(new VersionJson()
				.appendBuildDate(Play.configuration.getProperty("build.date"))
				.appendBuildNumber(
						Play.configuration.getProperty("build.number"))
				.appendAppMode(Play.mode).appendAppId(Play.id));
	}

	/**
	 * @param msgKey
	 *            either a message key from messages.properties or any text
	 */
	@Util
	public static void errorJSON(String msgKey, Object... params) {
		errorJSON(Http.StatusCode.OK, msgKey, params);
	}

	@Util
	public static void errorJSON(int status, String msgKey, Object... params) {
		JsonObject errorInfoJson = getErrorJsonObject(msgKey, params);
		JsonObject errorJson = new JsonObject();
		errorJson.add("error", errorInfoJson);
		request.args.put(ARG_ERROR_MSG, getMessage(msgKey, params));
		throw new RenderErrorJson(errorJson, status);
	}

	private static JsonObject getErrorJsonObject(String msgKey,
			Object... params) {
		String message = getMessage(msgKey, params);
		logger.error(Log.message(message));

		JsonObject errorInfoJson = new JsonObject();
		errorInfoJson.addProperty("code", msgKey);
		errorInfoJson.addProperty("message", message);
		return errorInfoJson;
	}

	private static JsonObject getErrorJsonObject(String msgKey, String message) {
		JsonObject errorInfoJson = new JsonObject();
		errorInfoJson.addProperty("code", msgKey);
		errorInfoJson.addProperty("message", message);
		return errorInfoJson;
	}

	private static String getMessage(String msgKey, Object... params) {
		String message;
		try {
			message = Messages.get(msgKey, resolveParams(params));
		} catch (Exception e) {
			message = msgKey;
		}
		return message;
	}

	private static Object[] resolveParams(Object... params) {
		if (params == null) {
			return null;
		}
		Object[] resolved = new Object[params.length];
		for (int i = 0; i < params.length; i++) {
			resolved[i] = Messages.get(params[i]);
		}
		return resolved;
	}

	@Util
	public static JsonElement getRequestBodyAsJson() throws IOException {
		String body = IOUtils.toString(request.body);
		JsonParser jsonParser = new JsonParser();
		JsonElement jsonBody = jsonParser.parse(body);
		return jsonBody;
	}

	@Util
	public static void validateRequestParameters() {
		if (Validation.hasErrors()) {
			JsonArray errors = new JsonArray();
			for (play.data.validation.Error error : Validation.errors()) {
				JsonObject errorInfoJson = getErrorJsonObject(error.getKey(),
						error.message());
				errors.add(errorInfoJson);
			}
			JsonObject errorJson = new JsonObject();
			errorJson.add("errors", errors);
			throw new RenderErrorJson(errorJson, Http.StatusCode.OK);
			// errorJSON(Http.StatusCode.OK,
			// ObjectUtils.toString(Validation.errors()));
		}
	}

	@Util
	public static String getRequestParamsAsString() {
		StringBuilder result = new StringBuilder();
		for (Map.Entry<String, String[]> param : request.params.all()
				.entrySet()) {
			String paramKey = param.getKey();
			String[] paramValues = param.getValue();
			if ("body".equals(paramKey)) {
				continue;
			}
			if (paramValues == null) {
				result.append(paramKey).append("=;\n");
				continue;
			}
			for (String paramValue : paramValues) {
				if ("token".equals(paramKey)
						|| StringUtils.containsIgnoreCase(paramKey, "image")) {
					result.append(paramKey).append("=<skipped>;\n");
					continue;
				} else {
					result.append(paramKey).append('=').append(paramValue)
							.append(";\n");
				}
			}
		}
		Object customBody = request.args.get(ARG_CUSTOM_BODY);
		if (customBody != null) {
			result.append(ARG_CUSTOM_BODY).append('=').append(customBody);
		}
		return result.toString();
	}
}
