package models;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "AFFILIATE_SELLER")
@NamedQueries({
	@NamedQuery(name = "JPQL_GET_SELLERS", query = "SELECT s FROM Seller s")
	})
public class Seller extends VersionedEntity {

	private static final long serialVersionUID = 2L;
	public static final String CACHE_PREFIX = Seller.class.getSimpleName();

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "advertiser_id")
	private Long advertiserId;

	@Column(name = "name")
	private String name;

	@OneToOne
	private Affiliate affiliate;
	
	@Column(name = "min_comm")
	private BigDecimal minCommission;
	
	@Column(name = "max_comm")
	private BigDecimal maxCommission;
	
	@Column(name = "image_url")
	private String imageUrl;
	
	@Column(name = "local_image_url")
	private String localImageUrl;
	
	public String getLocalImageUrl() {
		return localImageUrl;
	}

	public void setLocalImageUrl(String localImageUrl) {
		this.localImageUrl = localImageUrl;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Seller(String name) {
		this.name = name;
	}

	public Long getAdvertiserId() {
		return advertiserId;
	}

	public void setAdvertiserId(Long advertiserId) {
		this.advertiserId = advertiserId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getMinCommission() {
		return minCommission;
	}

	public void setMinCommission(BigDecimal minCommission) {
		this.minCommission = minCommission;
	}

	public BigDecimal getMaxCommission() {
		return maxCommission;
	}

	public void setMaxCommission(BigDecimal maxCommission) {
		this.maxCommission = maxCommission;
	}

	public Affiliate getAffiliate() {
		return affiliate;
	}

	@Override
	public String toString() {
		return "Seller [id=" + id + ", advertiserId=" + advertiserId
				+ ", name=" + name + "]";
	}

}
