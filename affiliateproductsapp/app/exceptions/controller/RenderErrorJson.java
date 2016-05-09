package exceptions.controller;

import play.db.jpa.JPA;
import play.mvc.Http;
import play.mvc.results.RenderJson;
import utils.JsonUtils;

import com.google.gson.JsonObject;

public class RenderErrorJson extends RenderJson {

	private final int status;

	public RenderErrorJson(JsonObject json, int status) {
		super(JsonUtils.toJson(json));
		this.status = status;
	}

	@Override
	public void apply(Http.Request request, Http.Response response) {
		super.apply(request, response);
		response.status = status;
		if (JPA.isEnabled() && JPA.isInsideTransaction()) {
			JPA.setRollbackOnly();
		}
	}
}
