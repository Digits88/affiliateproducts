package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "AFFILIATE_AFFILIATE")
public class Affiliate extends VersionedEntity {

	private static final long serialVersionUID = 2L;
	public static final String CACHE_PREFIX = Affiliate.class.getSimpleName();

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "name")
	private String name;

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

	public Affiliate(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Affiliate [id=" + id + ", name=" + name + "]";
	}

}
