package models.rakuten;

import java.io.Serializable;
import java.math.BigDecimal;

import utils.rakuten.classfromxsd.AttributeClass;

public class RakutenProduct implements Serializable {

	private static final long serialVersionUID = 2L;

	// advertiserID
	private Long merchantId;

	// advertiserName
	private String merchantName;

	// createdOn
	private String createdOn;

	// product_id -- same as SKU
	private String product_id;

	// name
	private String name;

	// SKU
	private String sku;

	// namufacuturer_name
	private String manufacturerName;

	// part_number
	private String partNumber;

	// primary category
	private String primaryCategory;

	// secondary category
	private String secondaryCategory;

	// product url
	private String productURL;

	// image url
	private String imageURL;

	// short description
	private String shortDescription;

	// long description
	private String longDescription;

	// currency
	private String currency;

	// type
	private String type;

	// currency2
	private String currency2;

	// sale price
	private BigDecimal salePrice;

	// retail price
	private BigDecimal retailPrice;

	// brand name
	private String brand;

	// availability -- stock
	private Boolean availability;

	// upc
	private String upc;

	// pixel
	private String pixel;

	// number of products
	private int numberOfProducts;
	
	// keywords
	private String keywords;
	
	// ----- Getter and Setter -----

	public Long getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Long merchantId) {
		this.merchantId = merchantId;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public String getProduct_id() {
		return product_id;
	}

	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getManufacturerName() {
		return manufacturerName;
	}

	public void setManufacturerName(String manufacturerName) {
		this.manufacturerName = manufacturerName;
	}

	public String getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(String partNumber) {
		this.partNumber = partNumber;
	}

	public String getPrimaryCategory() {
		return primaryCategory;
	}

	public void setPrimaryCategory(String primaryCategory) {
		this.primaryCategory = primaryCategory;
	}

	public String getSecondaryCategory() {
		return secondaryCategory;
	}

	public void setSecondaryCategory(String secondaryCategory) {
		this.secondaryCategory = secondaryCategory;
	}

	public String getProductURL() {
		return productURL;
	}

	public void setProductURL(String productURL) {
		this.productURL = productURL;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public String getLongDescription() {
		return longDescription;
	}

	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCurrency2() {
		return currency2;
	}

	public void setCurrency2(String currency2) {
		this.currency2 = currency2;
	}

	public BigDecimal getSalePrice() {
		return salePrice;
	}

	public void setSalePrice(BigDecimal salePrice) {
		this.salePrice = salePrice;
	}

	public BigDecimal getRetailPrice() {
		return retailPrice;
	}

	public void setRetailPrice(BigDecimal retailPrice) {
		this.retailPrice = retailPrice;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public Boolean getAvailability() {
		return availability;
	}

	public void setAvailability(Boolean availability) {
		this.availability = availability;
	}

	public String getUpc() {
		return upc;
	}

	public void setUpc(String upc) {
		this.upc = upc;
	}

	public String getPixel() {
		return pixel;
	}

	public void setPixel(String pixel) {
		this.pixel = pixel;
	}

	public int getNumberOfProducts() {
		return numberOfProducts;
	}

	public void setNumberOfProducts(int numberOfProducts) {
		this.numberOfProducts = numberOfProducts;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	
	@Override
	public String toString() {
		return "Product:: merchantID = " + this.merchantId 
				+ ", Name =" + this.merchantName 
				+ ", createOn =" + this.createdOn 
				+ ", SKU = " + this.sku
				+ ", productURL = " + this.productURL
				+ ", Brand = " + this.brand
				+ ", Price = " + this.salePrice
				+ ", retail = " + this.retailPrice;
	}
}
