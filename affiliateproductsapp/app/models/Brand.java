package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "AFFILIATE_BRAND")
public class Brand extends VersionedEntity {

	private static final long serialVersionUID = 2L;
	public static final String CACHE_PREFIX = Brand.class.getSimpleName();

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "name")
	private String name;
	
	@Column(name = "image_url")
	private String imageUrl;
	
	@Column(name = "local_image_url")
	private String localImageUrl;

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getLocalImageUrl() {
		return localImageUrl;
	}

	public void setLocalImageUrl(String localImageUrl) {
		this.localImageUrl = localImageUrl;
	}

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

	public Brand(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Brand [id=" + id + ", name=" + name + "]";
	}

}
