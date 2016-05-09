package models;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import constants.CJProductsConstants;
import constants.ImpactRadiusConstants;
import constants.RakutenConstants;
import models.cj.CJProduct;
import models.impactradius.ImpactRadiusProduct;
import models.rakuten.RakutenProduct;
import models.searskmart.SearsKmartProduct;
import utils.PriceUtils;
import models.searskmart.SKRatedSimilarProduct;

@Entity
@Table(name = "AFFILIATE_PRODUCT")
@NamedQueries({ @NamedQuery(name = "JPQL_GET_PRODUCTS_OF_SELLER", query = "SELECT p FROM Product p where p.seller=?1"),
		@NamedQuery(name = "JPQL_GET_PRODUCTS_OF_BRAND", query = "SELECT p FROM Product p where p.brand.id=?1"),
		@NamedQuery(name = "JPQL_GET_UNIQUE_CATEGORY_NAMES", query = "SELECT distinct(p.advertiserCategory) FROM Product p"),
		@NamedQuery(name = "JPQL_GET_SKUS", query = "SELECT p.sku FROM Product p where p.seller.advertiserId=?1"),
		@NamedQuery(name = "JPQL_GET_SKUS_BASED_ON_CATEGORY", query = "SELECT p.sku FROM Product p where p.seller.advertiserId=?1 and p.advertiserCategory=?2"),
		@NamedQuery(name = "JPQL_GET_PRODUCTS_BASED_ON_CATEGORY", query = "SELECT p FROM Product p where p.seller.advertiserId=?1 and p.advertisercategory.id=?2"),
		@NamedQuery(name = "JPQL_GET_PRODUCTS_BASED_ON_CATEGORYNAME", query = "SELECT p FROM Product p where p.seller.advertiserId=?1 and p.advertiserCategory=?2"),
		@NamedQuery(name = "JPQL_GET_PRODUCTS_BASED_ON_BRANDNAME", query = "SELECT p FROM Product p where p.seller.advertiserId=?1 and p.manufacturerName=?2"),
		@NamedQuery(name = "JPQL_GET_PRODUCTS_BASED_LIKE_CATEGORY", query = "SELECT p FROM Product p where p.seller.advertiserId=?1 and p.advertiserCategory LIKE ?2")})

public class Product extends VersionedEntity {

	private static final long serialVersionUID = 2L;
	public static final String CACHE_PREFIX = Product.class.getSimpleName();

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "advertiser_id")
	private Long advertiserId;

	@Column(name = "advertiser_category_name")
	private String advertiserCategory;

	@Column(name = "buy_url")
	private String buyURL;

	@Column(name = "description")
	private String description;

	@Column(name = "image_url")
	private String imageURL;

	@Column(name = "in_stock")
	private Boolean inStock;

	@Column(name = "manufacturer_name")
	private String manufacturerName;

	@Column(name = "name")
	private String name;

	@Column(name = "price")
	private BigDecimal price;

	@Column(name = "retail_price")
	private BigDecimal retailPrice;

	@Column(name = "sale_price")
	private BigDecimal salePrice;

	@Column(name = "final_price")
	private BigDecimal finalPrice;

	@Column(name = "sale")
	private Integer sale;

	@Column(name = "sku")
	private String sku;

	@Column(name = "keywords")
	private String keywords;
	
	@Column(name= "prime")
	private Boolean prime;
	
	@OneToOne
	private Seller seller;

	@OneToOne
	private AdvertiserCategory advertisercategory;

	@OneToOne
	private Brand brand;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getAdvertiserId() {
		return advertiserId;
	}

	public void setAdvertiserId(Long advertiserId) {
		this.advertiserId = advertiserId;
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

	public BigDecimal getFinalPrice() {
		return finalPrice;
	}

	public void setFinalPrice(BigDecimal finalPrice) {
		this.finalPrice = finalPrice;
	}

	public Integer getSale() {
		return sale;
	}

	public void setSale(Integer sale) {
		this.sale = sale;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public Seller getSeller() {
		return seller;
	}

	public void setSeller(Seller seller) {
		this.seller = seller;
	}

	public AdvertiserCategory getCategory() {
		return advertisercategory;
	}

	public void setCategory(AdvertiserCategory category) {
		this.advertisercategory = category;
	}

	public Brand getBrand() {
		return brand;
	}

	public void setBrand(Brand brand) {
		this.brand = brand;
	}

	public Boolean getPrime() {
		return prime;
	}

	public void setPrime(Boolean prime) {
		this.prime = prime;
	}

	// Impact Radius Feed
	public Product(ImpactRadiusProduct impactRadiusProduct, Seller seller, Brand brand, AdvertiserCategory category) {
		this.seller = seller;
		this.brand = brand;
		this.advertisercategory = category;

		this.advertiserId = impactRadiusProduct.getAdvertiserId();
		this.advertiserCategory = impactRadiusProduct.getCategory();

		this.buyURL = impactRadiusProduct.getProductURL();
		this.description = impactRadiusProduct.getProductDescription();
		this.imageURL = impactRadiusProduct.getImageURL();

		if (impactRadiusProduct.isStockAvailability()) {
			this.inStock = true;
		} else {
			this.inStock = false;
		}
		this.manufacturerName = impactRadiusProduct.getManufacturer();
		this.name = impactRadiusProduct.getProductName();

		/**
		 * Price Logic For Impact Radius Feeds Price = original Price sale price
		 * = current price ? original
		 */
		this.price = impactRadiusProduct.getOriginalPrice();
		this.salePrice = (impactRadiusProduct.getCurrentPrice() != null
				&& impactRadiusProduct.getCurrentPrice().compareTo(BigDecimal.ZERO) == 1)
						? impactRadiusProduct.getCurrentPrice() : impactRadiusProduct.getOriginalPrice();

		this.finalPrice = (impactRadiusProduct.getCurrentPrice() != null
				&& impactRadiusProduct.getCurrentPrice().compareTo(BigDecimal.ZERO) == 1)
						? impactRadiusProduct.getCurrentPrice() : impactRadiusProduct.getOriginalPrice();

		this.sale = PriceUtils.getSale(this.price, this.finalPrice);
		this.sku = impactRadiusProduct.getUniqueMerchantSKU();

		if (this.advertiserId == ImpactRadiusConstants.TARGET_ADVERTISERID
				&& impactRadiusProduct.getProductType() != null && impactRadiusProduct.getProductType().length() > 0) {
			this.keywords = impactRadiusProduct.getProductType();
		}
	}

	// Rakuten Feeds
	public Product(RakutenProduct rakutenProduct, Seller seller, Brand brand, AdvertiserCategory category,
			Map<String, String> colorSKUMap) {
		this.seller = seller;
		this.brand = brand;
		this.advertisercategory = category;

		/**
		 * Set Category - if 13565 (Pet Smart) -- set secondary category as
		 * category - if 13867 (Bloomingdale's) -- set primary and secondary if
		 * 38605 (Kohl's) -- set primary and secondary if 788 (JCPenny) -- set
		 * primary and secondary combined as category
		 */
		if (rakutenProduct.getMerchantId() == RakutenConstants.PETSMART_ADVERTISERID) {
			this.advertiserCategory = rakutenProduct.getSecondaryCategory();
		} else if (rakutenProduct.getMerchantId() == RakutenConstants.BLOOMINGDALES_ADVERTISERID
				|| rakutenProduct.getMerchantId() == RakutenConstants.KOHLS_ADVERTISERID
				|| rakutenProduct.getMerchantId() == RakutenConstants.JCPENNY_ADVERTISERID) {
			if (rakutenProduct.getSecondaryCategory() != null
					&& rakutenProduct.getSecondaryCategory().trim().length() > 0) {
				this.advertiserCategory = rakutenProduct.getPrimaryCategory() + " > "
						+ rakutenProduct.getSecondaryCategory();
			} else {
				this.advertiserCategory = rakutenProduct.getPrimaryCategory();
			}
		} else if (rakutenProduct.getMerchantId() == RakutenConstants.WALMART_ADVERTISERID) {
			if (rakutenProduct.getPrimaryCategory() != null && rakutenProduct.getSecondaryCategory() != null) {
				this.advertiserCategory = rakutenProduct.getSecondaryCategory()
						.concat(RakutenConstants.CATEGORY_LINK_WITH_SPACE).concat(rakutenProduct.getPrimaryCategory());
			} else if (rakutenProduct.getSecondaryCategory() != null) {
				this.advertiserCategory = rakutenProduct.getSecondaryCategory();
			} else {
				this.advertiserCategory = rakutenProduct.getPrimaryCategory();
			}
		} else if (rakutenProduct.getMerchantId() == RakutenConstants.NIKE_ADVERTISERID) {
			if (rakutenProduct.getSecondaryCategory() != null
					&& rakutenProduct.getSecondaryCategory().trim().length() > 0) {
				this.advertiserCategory = rakutenProduct.getPrimaryCategory() + RakutenConstants.CATEGORY_LINK_WITH_SPACE
						+ rakutenProduct.getSecondaryCategory().replaceAll("~~", RakutenConstants.CATEGORY_LINK_WITH_SPACE);
			} else {
				this.advertiserCategory = rakutenProduct.getPrimaryCategory();
			}
		} else {
			this.advertiserCategory = rakutenProduct.getPrimaryCategory();
		}

		this.advertiserId = rakutenProduct.getMerchantId();

		this.buyURL = rakutenProduct.getProductURL();
		this.imageURL = rakutenProduct.getImageURL();

		/**
		 * Set Category if 2814 (Macy) -- set short description as description
		 */
		if (rakutenProduct.getMerchantId() == 3184) {
			this.description = rakutenProduct.getShortDescription();
		} else {
			this.description = rakutenProduct.getLongDescription();
		}

		this.inStock = rakutenProduct.getAvailability();
		this.manufacturerName = rakutenProduct.getManufacturerName();

		/**
		 *  Set name for Nike Product:
		 *  If (name.contains('Size')), then get the part before 'Size' as the name
		 *  Else use the full name
		 */
		if (rakutenProduct.getMerchantId() == RakutenConstants.NIKE_ADVERTISERID && rakutenProduct.getName().contains(" Size ")) {
			this.name = rakutenProduct.getName().split(" Size ")[0];
		} else {
			this.name = rakutenProduct.getName();			
		}

		/**
		 * Price Logic For Rakuten Feeds Price = Regular Price retail price =
		 * Regular Price sale price = selling price
		 */
		this.price = rakutenProduct.getRetailPrice() != null ? rakutenProduct.getRetailPrice()
				: rakutenProduct.getSalePrice();
		this.salePrice = rakutenProduct.getSalePrice();
		/*
		 * this.finalPrice = rakutenProduct.getSalePrice() != null ?
		 * rakutenProduct .getSalePrice() : rakutenProduct.getRetailPrice();
		 */
		if (rakutenProduct.getSalePrice() != null && rakutenProduct.getSalePrice().compareTo(BigDecimal.ZERO) == 1) {
			this.finalPrice = rakutenProduct.getSalePrice();
		} else {
			this.finalPrice = rakutenProduct.getRetailPrice();
		}
		this.sale = PriceUtils.getSale(this.price, this.finalPrice);

		/**
		 * Set SKU if - 1237 (Nordstrom) -- set
		 * category-manufacturerName-partNumber as SKU number - 38501
		 * (Shoes.com) -- set product name as the SKU number - 788 (JCPenny) --
		 * set name + partnumber as the sku
		 * Nike (38660) -- use part_number
		 */
		if (rakutenProduct.getMerchantId() == RakutenConstants.NORDSTROM_ADVERTISERID) {
			this.sku = rakutenProduct.getPrimaryCategory() + "-" + rakutenProduct.getManufacturerName() + "-"
					+ rakutenProduct.getPartNumber();
		} else if (rakutenProduct.getMerchantId() == RakutenConstants.SHOESCOM_ADVERTISERID) {
			this.sku = rakutenProduct.getName();
		} else if (rakutenProduct.getMerchantId() == RakutenConstants.KOHLS_ADVERTISERID) {
			String name = rakutenProduct.getName().split(",")[0];
			String partName = "";
			if (rakutenProduct.getPartNumber() != null && rakutenProduct.getPartNumber().trim().length() > 0) {
				partName = "-" + rakutenProduct.getPartNumber();
			}
			String cate = rakutenProduct.getPrimaryCategory();
			this.sku = cate + "-" + name + partName;
		} else if (rakutenProduct.getMerchantId() == RakutenConstants.JCPENNY_ADVERTISERID) {
			String color = colorSKUMap.get(rakutenProduct.getPrimaryCategory() + "-" + rakutenProduct.getName() + "-"
					+ rakutenProduct.getSku());
			this.sku = rakutenProduct.getPrimaryCategory() + "-" + rakutenProduct.getName() + "-" + color;
		} else if (rakutenProduct.getMerchantId() == RakutenConstants.NIKE_ADVERTISERID) {
			this.sku = rakutenProduct.getPartNumber();
		} else {
			this.sku = rakutenProduct.getSku();
		}

		this.keywords = rakutenProduct.getKeywords();
	}

	// Rakuten Super Feed, like Walmart
	public Product(RakutenProduct rakutenProduct, Seller seller, Brand brand, AdvertiserCategory category) {
		this.seller = seller;
		this.brand = brand;
		this.advertisercategory = category;

		// Set category
		if (rakutenProduct.getMerchantId() == RakutenConstants.WALMART_ADVERTISERID) {
			if (rakutenProduct.getPrimaryCategory() != null && rakutenProduct.getSecondaryCategory() != null) {
				this.advertiserCategory = rakutenProduct.getSecondaryCategory()
						.concat(RakutenConstants.CATEGORY_LINK_WITH_SPACE).concat(rakutenProduct.getPrimaryCategory());
			} else if (rakutenProduct.getSecondaryCategory() != null) {
				this.advertiserCategory = rakutenProduct.getSecondaryCategory();
			} else {
				this.advertiserCategory = rakutenProduct.getPrimaryCategory();
			}
		} else {
			this.advertiserCategory = rakutenProduct.getPrimaryCategory();
		}

		this.advertiserId = rakutenProduct.getMerchantId();

		this.buyURL = rakutenProduct.getProductURL();
		this.imageURL = rakutenProduct.getImageURL();

		/**
		 * Set Category if 2814 (Macy) -- set short description as description
		 */
		if (rakutenProduct.getMerchantId() == 3184) {
			this.description = rakutenProduct.getShortDescription();
		} else {
			this.description = rakutenProduct.getLongDescription();
		}

		this.inStock = rakutenProduct.getAvailability();
		this.manufacturerName = rakutenProduct.getManufacturerName();

		this.name = rakutenProduct.getName();

		/**
		 * Price Logic For Rakuten Feeds Price = Regular Price retail price =
		 * Regular Price sale price = selling price
		 */
		this.price = rakutenProduct.getRetailPrice() != null ? rakutenProduct.getRetailPrice()
				: rakutenProduct.getSalePrice();
		this.salePrice = rakutenProduct.getSalePrice();
		/*
		 * this.finalPrice = rakutenProduct.getSalePrice() != null ?
		 * rakutenProduct .getSalePrice() : rakutenProduct.getRetailPrice();
		 */
		if (rakutenProduct.getSalePrice() != null && rakutenProduct.getSalePrice().compareTo(BigDecimal.ZERO) == 1) {
			this.finalPrice = rakutenProduct.getSalePrice();
		} else {
			this.finalPrice = rakutenProduct.getRetailPrice();
		}
		this.sale = PriceUtils.getSale(this.price, this.finalPrice);
		this.sku = rakutenProduct.getSku();

		this.keywords = rakutenProduct.getKeywords();

		// identify if this product is coming from latest feed.
		// this.inLatestFeed = RakutenConstants.PRODUCT_IN_FEED;
	}

	// Sears Kmart Feeds
	public Product(SearsKmartProduct skProduct, Seller seller, Brand brand, AdvertiserCategory category) {
		this.seller = seller;
		this.brand = brand;
		this.advertisercategory = category;

		this.advertiserCategory = skProduct.getCategory();
		this.advertiserId = skProduct.getAdvertiserId();

		this.buyURL = skProduct.getProductURL();
		this.description = skProduct.getShorDescription();
		this.imageURL = skProduct.getImageName();
		this.inStock = skProduct.getInStock();
		this.manufacturerName = skProduct.getManufacturerName();
		this.name = skProduct.getProductName();

		/**
		 * Price Logic For Sears/Kmart Feeds Price = Regular Price retail price
		 * = Regular Price sale price = selling price
		 */
		this.price = skProduct.getRegularPrice() != null ? skProduct.getRegularPrice() : skProduct.getSellingPrice();
		this.salePrice = skProduct.getSellingPrice();
		this.finalPrice = (skProduct.getSellingPrice() != null
				&& skProduct.getSellingPrice().compareTo(BigDecimal.ZERO) == 1) ? skProduct.getSellingPrice()
						: skProduct.getRegularPrice();
		this.sale = PriceUtils.getSale(this.price, this.finalPrice);

		// this.sku = skProduct.getPartnumber();
		this.sku = skProduct.getParentName();
		// this.keywords = "";
		this.prime = true;
	}

	public Product(CJProduct cjProduct) {
		this.advertiserCategory = cjProduct.getAdvertiserCategory();
		this.advertiserId = cjProduct.getAdvertiserId();
		this.buyURL = cjProduct.getBuyURL();
		this.description = cjProduct.getDescription();
		this.imageURL = cjProduct.getImageURL();
		this.inStock = cjProduct.getInStock();
		this.manufacturerName = cjProduct.getManufacturerName();
		this.name = cjProduct.getName();
		if (cjProduct.getAdvertiserId() == CJProductsConstants.SHOE_METRO_ADVERTISERID) {
			this.price = cjProduct.getRetailPrice();
			this.salePrice = cjProduct.getPrice();
		} else {
			this.price = cjProduct.getPrice();
			this.salePrice = cjProduct.getSalePrice();
		}
		this.retailPrice = cjProduct.getRetailPrice();
		this.finalPrice = (cjProduct.getSalePrice() != null && cjProduct.getSalePrice().compareTo(BigDecimal.ZERO) == 1)
				? cjProduct.getSalePrice() : cjProduct.getPrice();
		this.sku = cjProduct.getSku();
		this.keywords = cjProduct.getKeywords();
		this.sale = PriceUtils.getSale(this.price, this.finalPrice);
	}

	public Product(CJProduct cjProduct, Seller seller, Brand brand, AdvertiserCategory category) {
		this.seller = seller;
		this.brand = brand;
		this.advertisercategory = category;
		this.advertiserCategory = cjProduct.getAdvertiserCategory();
		this.advertiserId = cjProduct.getAdvertiserId();
		this.buyURL = cjProduct.getBuyURL();
		this.description = cjProduct.getDescription();
		this.imageURL = cjProduct.getImageURL();
		this.inStock = cjProduct.getInStock();
		this.manufacturerName = cjProduct.getManufacturerName();
		this.name = cjProduct.getName();
		if (cjProduct.getAdvertiserId() == CJProductsConstants.SHOE_METRO_ADVERTISERID) {
			this.price = cjProduct.getRetailPrice();
			this.salePrice = cjProduct.getPrice();
		} else {
			this.price = cjProduct.getPrice();
			this.salePrice = cjProduct.getSalePrice();
		}
		this.retailPrice = cjProduct.getRetailPrice();
		this.finalPrice = (cjProduct.getSalePrice() != null && cjProduct.getSalePrice().compareTo(BigDecimal.ZERO) == 1)
				? cjProduct.getSalePrice() : cjProduct.getPrice();
		this.sku = cjProduct.getSku();
		this.keywords = cjProduct.getKeywords();
		this.sale = PriceUtils.getSale(this.price, this.finalPrice);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Product)) {
			return false;
		}
		final Product other = (Product) obj;
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
		return "Product [id=" + id + ", advertiserId=" + advertiserId + ", advertiserCategory=" + advertiserCategory
				+ ", buyURL=" + buyURL + ", description=" + description + ", imageURL=" + imageURL + ", inStock="
				+ inStock + ", manufacturerName=" + manufacturerName + ", name=" + name + ", price=" + price
				+ ", retailPrice=" + retailPrice + ", salePrice=" + salePrice + ", finalPrice=" + finalPrice + ", sale="
				+ sale + ", sku=" + sku + ", keywords=" + keywords + ", seller=" + seller + ", category=" + advertisercategory
				+ ", brand=" + brand + "]";
	}

	
}
