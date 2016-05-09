package models.impactradius;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlElement;

/**
 * This class is created based on Google based format. For detail, please check
 * url below: https://support.google.com/merchants/answer/188494?hl=en
 *
 */
public class ImpactRadiusProduct implements Serializable {

	private static final long serialVersionUID = 2L;

	private Long advertiserId;
	
	private String uniqueMerchantSKU;
	private String productName;
	private String productURL;
	private String imageURL;
	private BigDecimal currentPrice;
	private boolean stockAvailability;
	private String condition;
	private String ean;
	private String upc;
	private String isbn;
	private String mpn;
	private String shippingRate;
	private BigDecimal originalPrice;
	private String manufacturer;
	private String productDescription;
	private String productType;
	private String category;
	private String categoryID;
	private String parentSKU;
	private String parentName;
	private String gender;
	private String color;
	private String size;
	private String productWeight;
	private String shippingWeight;
	
	public Long getAdvertiserId() {
		return advertiserId;
	}
	public void setAdvertiserId(Long advertiserId) {
		this.advertiserId = advertiserId;
	}
	public String getUniqueMerchantSKU() {
		return uniqueMerchantSKU;
	}
	public void setUniqueMerchantSKU(String uniqueMerchantSKU) {
		this.uniqueMerchantSKU = uniqueMerchantSKU;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
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
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	public String getEan() {
		return ean;
	}
	public void setEan(String ean) {
		this.ean = ean;
	}
	public String getUpc() {
		return upc;
	}
	public void setUpc(String upc) {
		this.upc = upc;
	}
	public String getIsbn() {
		return isbn;
	}
	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	public String getMpn() {
		return mpn;
	}
	public void setMpn(String mpn) {
		this.mpn = mpn;
	}
	public String getShippingRate() {
		return shippingRate;
	}
	public void setShippingRate(String shippingRate) {
		this.shippingRate = shippingRate;
	}
	public String getManufacturer() {
		return manufacturer;
	}
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	public String getProductDescription() {
		return productDescription;
	}
	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}
	public String getProductType() {
		return productType;
	}
	public void setProductType(String productType) {
		this.productType = productType;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getCategoryID() {
		return categoryID;
	}
	public void setCategoryID(String categoryID) {
		this.categoryID = categoryID;
	}
	public String getParentSKU() {
		return parentSKU;
	}
	public void setParentSKU(String parentSKU) {
		this.parentSKU = parentSKU;
	}
	public String getParentName() {
		return parentName;
	}
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getProductWeight() {
		return productWeight;
	}
	public void setProductWeight(String productWeight) {
		this.productWeight = productWeight;
	}
	public String getShippingWeight() {
		return shippingWeight;
	}
	public void setShippingWeight(String shippingWeight) {
		this.shippingWeight = shippingWeight;
	}
	public BigDecimal getCurrentPrice() {
		return currentPrice;
	}
	public void setCurrentPrice(BigDecimal currentPrice) {
		this.currentPrice = currentPrice;
	}
	public BigDecimal getOriginalPrice() {
		return originalPrice;
	}
	public void setOriginalPrice(BigDecimal originalPrice) {
		this.originalPrice = originalPrice;
	}
	public boolean isStockAvailability() {
		return stockAvailability;
	}
	public void setStockAvailability(boolean stockAvailability) {
		this.stockAvailability = stockAvailability;
	}
	

}
