package controllers.json;

import models.Seller;

public class SellerJson extends BaseJson {

	Long advertiserId;
	String name;
	
	public SellerJson(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public SellerJson(Seller seller) {
		super();
		if (seller != null) {
			this.id = seller.getId();
			this.name = seller.getName();
		}
	}
}
