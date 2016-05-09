package controllers.json;

import models.Brand;

public class BrandJson extends BaseJson {

	String name;

	public BrandJson(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public BrandJson(Brand brand) {
		super();
		if (brand != null) {
			this.id = brand.getId();
			this.name = brand.getName();
		}
	}
}
