package controllers.json;

import java.math.BigDecimal;

import models.Product;
import utils.TransformUtils;

public class ProductJson extends BaseJson {

	//Long advertiserId;
	//String advertiserCategory;
	String buyURL;
	String description;
	String imageURL;
	Boolean inStock;
	String manufacturerName;
	String name;
	BigDecimal price;
	BigDecimal retailPrice;
	BigDecimal salePrice;
	String sku;
	SellerJson seller;
	BrandJson brand;
	CategoryJson category;	

	public ProductJson(Product product) {
		super();
		this.id = product.getId();
		//this.advertiserId = product.getAdvertiserId();
		//this.advertiserCategory = product.getAdvertiserCategory();
		this.buyURL = product.getBuyURL();
		this.description = product.getDescription();
		this.imageURL = product.getImageURL();
		this.inStock = product.getInStock();
		this.manufacturerName = product.getManufacturerName();
		this.name = product.getName();
		this.price = product.getPrice();
		this.retailPrice = product.getRetailPrice();
		this.salePrice = product.getSalePrice();
		this.sku = product.getSku();
		if (product.getSeller() != null) {
			this.seller = (SellerJson) TransformUtils.toJson(product
					.getSeller());
		}
		if (product.getBrand() != null) {
			this.brand = (BrandJson) TransformUtils.toJson(product.getBrand());
		}
		if (product.getCategory() != null) {
			this.category = (CategoryJson) TransformUtils.toJson(product
					.getCategory());
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ProductJson)) {
			return false;
		}
		final ProductJson other = (ProductJson) obj;
		return this.id.equals(other.id);
	}

	@Override
	public int hashCode() {
		StringBuilder stringBuilder = new StringBuilder();
		if (this != null && this.id != null) {
			stringBuilder.append(this.id);
		}
		return stringBuilder.toString().hashCode();
	}
}
