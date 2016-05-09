package controllers.json;

import java.util.Stack;

import models.AdvertiserCategory;

public class CategoryJson extends BaseJson {

	String name;
	Long sywTagId;
	CategoryV1Json hierarchy;

	public CategoryJson(AdvertiserCategory category) {
		super();
		if (category != null) {
			this.id = category.getId();
			this.name = category.getName();
			this.sywTagId = category.getSywTagId();
		}
	}

	public CategoryJson(AdvertiserCategory category, Boolean withChildJson) {
		this(category);
		if (withChildJson) {
			CategoryV1Json categoryV1Json = new CategoryV1Json(
					category.getCategoryV1(), new Stack(), false);
			if (categoryV1Json != null) {
				this.hierarchy = categoryV1Json;
			}
		}
	}

}
