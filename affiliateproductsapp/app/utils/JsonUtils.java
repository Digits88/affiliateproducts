package utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public final class JsonUtils {

	private final static ThreadLocal<Gson> gsonTL = new ThreadLocal<Gson>();

	public static Gson getGson() {
		Gson gson = gsonTL.get();
		if (gson == null) {
			gsonTL.set(gson = new Gson());
		}
		return gson;
	}

	public static boolean isErrorJson(JsonElement json) {
		return ! json.isJsonNull() && json.isJsonObject() && json.getAsJsonObject().get("error") != null;
	}

	public static <T> T fromJson(JsonElement json, Class<T> type) {
		Gson gson = getGson();
		return gson.fromJson(json, type);
	}

	public static <T> T fromJson(String json, Class<T> type) {
		Gson gson = getGson();
		return gson.fromJson(json, type);
	}

	public static String toJson(Object obj) {
		Gson gson = getGson();
		return gson.toJson(obj);
	}

	public static String toJson(JsonElement json) {
		Gson gson = getGson();
		return gson.toJson(json);
	}
}
