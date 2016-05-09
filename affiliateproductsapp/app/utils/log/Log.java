package utils.log;

import java.util.Date;
import java.util.Map;

import models.log.LogParam;
import models.log.LogRequest;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import play.mvc.Http.Request;
import services.LogService;
import services.impl.DatabaseLogService;
import utils.TL;

import com.google.common.base.Objects;

import constants.AffiliateConstants;

public class Log {
	
	public static final String ARG_CUSTOM_BODY = "customBody";
	public static final String ARG_ERROR_MSG = "errorMessage";
	public static final String ARG_EXCEPTION = "exception";
	
	private static LogService logService = new DatabaseLogService();
	
	public static String message(String message, Object ...args) {
		StringBuilder messageBuilder = new StringBuilder();
		messageBuilder.append(getStaticRequestParam());
		String tempMessage = getMessage(message);
		messageBuilder.append(tempMessage);
		String tempParams = getDynamicParams(args);
		messageBuilder.append(tempParams);
		createLogRequest(tempMessage, tempParams);
		return messageBuilder.toString();
	}
	

	public static String message(String message) {
		StringBuilder messageBuilder = new StringBuilder();
		messageBuilder.append(getStaticRequestParam());
		messageBuilder.append(getMessage(message));
		String tempMessage = getMessage(message);
		createLogRequest(tempMessage, null);
		return messageBuilder.toString();
	}
	
	public static String message(Object ...args) {
		StringBuilder messageBuilder = new StringBuilder();
		messageBuilder.append(getStaticRequestParam());
		messageBuilder.append(getDynamicParams(args));
		String tempParams = getDynamicParams(args);
		createLogRequest(null, tempParams);
		return messageBuilder.toString();
	}
	
	public static LogParam toLogParam(String key, Object value) {
		return new LogParam(key, value);
	}
	
	public static String getRequestParamsAsString() {
		Request request = Request.current();
		StringBuilder result = new StringBuilder();
		for (Map.Entry<String, String[]> param : request.params.all().entrySet()) {
			String paramKey = param.getKey();
			String[] paramValues = param.getValue();
			if (LogRequest.BODY_PARAM_NAME.equals(paramKey)
					|| LogRequest.SOURCE_PARAM_NAME.equals(paramKey)
					|| LogRequest.CHANNEL_PARAM_NAME.equals(paramKey)) {
				continue;
			}
			if (paramValues == null) {
				result.append(paramKey).append("=;\n");
				continue;
			}
			for (String paramValue : paramValues) {
				if ("token".equals(paramKey) || StringUtils.containsIgnoreCase(paramKey, "image")) {
					result.append(paramKey).append("=<skipped>;\n");
					continue;
				} else {
					result.append(paramKey).append('=').append(paramValue).append(";\n");
				}
			}
		}
		Object customBody = request.args.get(ARG_CUSTOM_BODY);
		if (customBody != null) {
			result.append(ARG_CUSTOM_BODY).append('=').append(customBody);
		}
		return result.toString();
	}
	
	private static String getMessage(String message) {
		StringBuilder builder = new StringBuilder();
		if (message != null && !message.isEmpty()) {
			builder.append(message);
			builder.append(AffiliateConstants.SPACE);
		}
		return builder.toString();
	}


	private static String getStaticRequestParam() {
		
		StringBuilder builder = new StringBuilder();
		String requestId = TL.getRequestId();
		Long tokenUserId = TL.getUserId();
		
		if (requestId != null) {
			builder.append(requestId);
		}
		
		if (requestId != null && tokenUserId != null) {
			builder.append(" - ");
		}
		
		if (tokenUserId != null) {
			builder.append(tokenUserId);
		}
		
		builder.append(AffiliateConstants.SPACE);
		
		return builder.toString();
	}
	
	private static String getDynamicParams(Object... args) {
		StringBuilder builder = new StringBuilder();
		if (args != null && args.length > 0) {
			for (Object object : args) {
				builder.append(object.toString());
				builder.append(AffiliateConstants.SPACE);
				builder.append(AffiliateConstants.STRING_SEPERATOR);
			}
		}
		return builder.toString();
	}
	private static void createLogRequest(String message, String params) {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		String className = stack[3].getClassName().toString();
		String methodName = stack[3].getMethodName().toString();
		LogRequest logRequest = new LogRequest();
		Request request = Request.current();
		setRequestParams(logRequest, request);
		logRequest.setClassName(className);
		logRequest.setMethodName(methodName);
		logRequest.setMessage(message);
		logRequest.setParams(params);
		logService.logRequest(logRequest);
	}


	private static void setRequestParams(LogRequest logRequest, Request request) {
		Date now = new Date();
		logRequest.setBegin(now);
		logRequest.setEnd(now);
		if (request != null) {
			logRequest.setServerHost(request.host);
			logRequest.setIpAdress(request.remoteAddress);
			logRequest.setElapsedTime((logRequest.getEnd().getTime() - request.date.getTime()));
			logRequest.setHttpMethod(request.method);
			logRequest.setPath(request.path);
			logRequest.setParams(getRequestParamsAsString());
			Object excp = request.args.get(ARG_EXCEPTION);
			if (excp instanceof Throwable) {
				logRequest.setException(ExceptionUtils.getStackTrace((Throwable) excp));
			} else {
				logRequest.setException(ObjectUtils.toString(request.args.get(ARG_ERROR_MSG), null));
			}
		}
	}
	
	public static class Param {

		private String key;
		private Object value;

		public Param(String key, Object value) {
			this.key = key;
			this.value = value;
		}

		public static Param get(String key, Object value) {
			return new Param(key, value);
		}

		public String toString() {
			return Objects.toStringHelper(this).add(key, value).toString();
		}
	}
}
