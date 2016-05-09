package models.searskmart;

import java.io.Serializable;
import java.math.BigDecimal;

import models.cj.CJProduct;

public class SearsKmartProduct implements Serializable {

	private static final long serialVersionUID = 2L;
	
	private Long advertiserId;
	private String advertiserName;
	private String partnumber;
	private String brand;
	private String productName;
	private String shorDescription;
	private String category;
	
	private String installation;
	private String protectionPlan;
	private String maintenanceAgreement;
	
	private String manufacturerName;
	private String manufacturePartnumber;
	
	private String imageName;
	private String productURL;
	
	private BigDecimal regularPrice;
	private BigDecimal sellingPrice;
	
	// sku
	private String parentName;
	
	private String mapPriceIndicator;
	private String SaveStory;
	private String upc;
	private String otherAttributes;
	
	private Boolean inStock;
	
	public Boolean getInStock() {
		return inStock;
	}

	public void setInStock(Boolean inStock) {
		this.inStock = inStock;
	}

	public Long getAdvertiserId() {
		return advertiserId;
	}

	public void setAdvertiserId(Long advertiserId) {
		this.advertiserId = advertiserId;
	}

	public String getAdvertiserName() {
		return advertiserName;
	}

	public void setAdvertiserName(String advertiserName) {
		this.advertiserName = advertiserName;
	}

	public String getPartnumber() {
		return partnumber;
	}

	public void setPartnumber(String partnumber) {
		this.partnumber = partnumber;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getShorDescription() {
		return shorDescription;
	}

	public void setShorDescription(String shorDescription) {
		this.shorDescription = shorDescription;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getManufacturerName() {
		return manufacturerName;
	}

	public void setManufacturerName(String manufacturerName) {
		this.manufacturerName = manufacturerName;
	}

	public String getManufacturePartnumber() {
		return manufacturePartnumber;
	}

	public void setManufacturePartnumber(String manufacturePartnumber) {
		this.manufacturePartnumber = manufacturePartnumber;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public String getProductURL() {
		return productURL;
	}

	public void setProductURL(String productURL) {
		this.productURL = productURL;
	}

	public BigDecimal getRegularPrice() {
		return regularPrice;
	}

	public void setRegularPrice(BigDecimal regularPrice) {
		this.regularPrice = regularPrice;
	}

	public BigDecimal getSellingPrice() {
		return sellingPrice;
	}

	public void setSellingPrice(BigDecimal sellingPrice) {
		this.sellingPrice = sellingPrice;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public String getMapPriceIndicator() {
		return mapPriceIndicator;
	}

	public void setMapPriceIndicator(String mapPriceIndicator) {
		this.mapPriceIndicator = mapPriceIndicator;
	}

	public String getSaveStory() {
		return SaveStory;
	}

	public void setSaveStory(String saveStory) {
		SaveStory = saveStory;
	}

	public String getUpc() {
		return upc;
	}

	public void setUpc(String upc) {
		this.upc = upc;
	}

	public String getOtherAttributes() {
		return otherAttributes;
	}

	public void setOtherAttributes(String otherAttributes) {
		this.otherAttributes = otherAttributes;
	}

	public String getInstallation() {
		return installation;
	}

	public void setInstallation(String installation) {
		this.installation = installation;
	}

	public String getProtectionPlan() {
		return protectionPlan;
	}

	public void setProtectionPlan(String protectionPlan) {
		this.protectionPlan = protectionPlan;
	}

	public String getMaintenanceAgreement() {
		return maintenanceAgreement;
	}

	public void setMaintenanceAgreement(String maintenanceAgreement) {
		this.maintenanceAgreement = maintenanceAgreement;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SearsKmartProduct)) {
			return false;
		}
		final SearsKmartProduct other = (SearsKmartProduct) obj;
		return this.advertiserId.equals(other.advertiserId) && this.parentName.equals(other.parentName);
	}

	@Override
	public int hashCode() {
		StringBuilder stringBuilder = new StringBuilder();

		if (this != null && this.advertiserId != null && this.parentName != null) {
			stringBuilder.append(this.advertiserId);
			stringBuilder.append(this.parentName);
		}
		return stringBuilder.toString().hashCode();
	}

	@Override
	public String toString() {
		return "CJProduct [advertiserId=" + advertiserId + ", advertiserName="
				+ advertiserName + ", advertiserCategory=" + category
				+ ", productName=" + productName + ", shorDescription=" + shorDescription
				+ ", imageURL=" + imageName + ", inStock=" + inStock
				+ ", manufacturerName=" + manufacturerName + ", manufacturePartnumber=" + manufacturePartnumber
				+ ", imageName=" + imageName + ", productURL=" + productURL
				+ ", regularPrice=" + regularPrice + ", sellingPrice=" + sellingPrice + ", parentName="
				+ parentName + "]";
	}
	
	
}
