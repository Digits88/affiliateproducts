package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "affiliate_category")
public class Category extends VersionedEntity {

	private static final long serialVersionUID = 2L;
	public static final String CACHE_PREFIX = AdvertiserCategory.class.getSimpleName();

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "parent")
	private Long parent;

	@Column(name = "google_taxonomy")
	private Long googleTaxonomy;

	@Column(name = "parent_name")
	private String parentName;

	public Long getId() {
		return id;
	}

	public Category(String name, Long parent, Long googleTaxonomy,
			String parentName) {
		super();
		this.name = name;
		this.parent = parent;
		this.googleTaxonomy = googleTaxonomy;
		this.parentName = parentName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getParent() {
		return parent;
	}

	public void setParent(Long parent) {
		this.parent = parent;
	}

	public Long getGoogleTaxonomy() {
		return googleTaxonomy;
	}

	public void setGoogleTaxonomy(Long googleTaxonomy) {
		this.googleTaxonomy = googleTaxonomy;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	@Override
	public String toString() {
		return "CategoryV1 [id=" + id + ", name=" + name + ", parent=" + parent
				+ ", googleTaxonomy=" + googleTaxonomy + ", parentName="
				+ parentName + "]";
	}

}
