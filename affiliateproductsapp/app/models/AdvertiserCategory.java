package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "AFFILIATE_ADVERTISER_CATEGORY")
public class AdvertiserCategory extends VersionedEntity {

	private static final long serialVersionUID = 2L;
	public static final String CACHE_PREFIX = AdvertiserCategory.class.getSimpleName();

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "name")
	private String name;
	
	@Column(name = "syw_tag_id")
	private Long sywtagId;
	
	@OneToOne
//	@Column(name = "category1_id")
	private Category category;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AdvertiserCategory(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getSywTagId() {
		return sywtagId;
	}

	public void setSywTagId(Long sywtagId) {
		this.sywtagId = sywtagId;
	}

	public Category getCategoryV1() {
		return category;
	}

	public void setCategoryV1(Category categoryV1) {
		this.category = categoryV1;
	}

	@Override
	public String toString() {
		return "AdvertiserCategory [id=" + id + ", name=" + name + ", sywtagId="
				+ sywtagId + "]";
	}
	
}
