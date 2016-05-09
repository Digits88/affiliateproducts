package models.cj;

import java.io.Serializable;
import java.math.BigDecimal;

public class CJProduct implements Serializable {

	private static final long serialVersionUID = 2L;

	private Long advertiserId;
	private String advertiserName;
	private String advertiserCategory;
	private String buyURL;
	private String description;
	private String imageURL;
	private Boolean inStock;
	private String manufacturerName;
	private String name;
	private BigDecimal price;
	private BigDecimal retailPrice;
	private BigDecimal salePrice;
	private String sku;
	private String currency;
	private String keywords;

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

	public String getAdvertiserCategory() {
		return advertiserCategory;
	}

	public void setAdvertiserCategory(String advertiserCategory) {
		this.advertiserCategory = advertiserCategory;
	}

	public String getBuyURL() {
		return buyURL;
	}

	public void setBuyURL(String buyURL) {
		this.buyURL = buyURL;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public Boolean getInStock() {
		return inStock;
	}

	public void setInStock(Boolean inStock) {
		this.inStock = inStock;
	}

	public String getManufacturerName() {
		return manufacturerName;
	}

	public void setManufacturerName(String manufacturerName) {
		this.manufacturerName = manufacturerName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public BigDecimal getRetailPrice() {
		return retailPrice;
	}

	public void setRetailPrice(BigDecimal retailPrice) {
		this.retailPrice = retailPrice;
	}

	public BigDecimal getSalePrice() {
		return salePrice;
	}

	public void setSalePrice(BigDecimal salePrice) {
		this.salePrice = salePrice;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}
	
	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	/**
	 * Special methods to invoke through reflection while creating the CJProduct object
	 * @param advertiserId
	 */
	public void setAdvertiserIdReflection(String advertiserId) {
		this.advertiserId = new Long(advertiserId);
	}

	public void setInStockReflection(String inStock) {
		this.inStock = new Boolean(inStock);
	}

	public void setPriceReflection(String price) {
		this.price = new BigDecimal(price);
	}

	public void setRetailPriceReflection(String retailPrice) {
		this.retailPrice = new BigDecimal(retailPrice);
	}

	public void setSalePriceReflection(String salePrice) {
		this.salePrice = new BigDecimal(salePrice);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CJProduct)) {
			return false;
		}
		final CJProduct other = (CJProduct) obj;
		return this.advertiserId.equals(other.advertiserId) && this.sku.equals(other.sku);
	}

	@Override
	public int hashCode() {
		StringBuilder stringBuilder = new StringBuilder();

		if (this != null && this.advertiserId != null && this.sku != null) {
			stringBuilder.append(this.advertiserId);
			stringBuilder.append(this.sku);
		}
		return stringBuilder.toString().hashCode();
	}

	@Override
	public String toString() {
		return "CJProduct [advertiserId=" + advertiserId + ", advertiserName="
				+ advertiserName + ", advertiserCategory=" + advertiserCategory
				+ ", buyURL=" + buyURL + ", description=" + description
				+ ", imageURL=" + imageURL + ", inStock=" + inStock
				+ ", manufacturerName=" + manufacturerName + ", name=" + name
				+ ", price=" + price + ", retailPrice=" + retailPrice
				+ ", salePrice=" + salePrice + ", sku=" + sku + ", currency="
				+ currency + ", keywords=" + keywords + "]";
	}	
}
