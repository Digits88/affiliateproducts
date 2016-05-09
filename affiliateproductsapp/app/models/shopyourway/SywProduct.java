package models.shopyourway;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class SywProduct implements Serializable {

	private static final long serialVersionUID = 2L;

	private Long id;
	private String name;
	private String imageUrl;
	private String productUrl;
	private BigDecimal regularPrice;
	private List<String> categoriesPath;
	/**
	 * sin - Sears Inventory Number
	 */
	private String sin;
	private String itemId;
	private List<Map<String, String>> facets;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getProductUrl() {
		return productUrl;
	}

	public void setProductUrl(String productUrl) {
		this.productUrl = productUrl;
	}

	public BigDecimal getPrice() {
		return regularPrice;
	}

	public void setPrice(BigDecimal price) {
		this.regularPrice = price;
	}

	public List<String> getCategoriesPath() {
		return categoriesPath;
	}

	public void setCategoriesPath(List<String> categoriesPath) {
		this.categoriesPath = categoriesPath;
	}

	public String getSin() {
		return sin;
	}

	public void setSin(String sin) {
		this.sin = sin;
	}

	public List<Map<String, String>> getFacets() {
		return facets;
	}

	public void setFacets(List<Map<String, String>> facets) {
		this.facets = facets;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
}
