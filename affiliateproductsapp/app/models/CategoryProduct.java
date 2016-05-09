package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.google.common.base.Objects;

@Entity
@Table(name = "AFFILIATE_CATEGORY_PRODUCT")
public class CategoryProduct extends VersionedEntity {

	private static final long serialVersionUID = 2L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	public AdvertiserCategory category;

	@ManyToOne
	public Product product;

	public CategoryProduct(AdvertiserCategory category, Product product) {
		this.category = category;
		this.product = product;
	}

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CategoryProduct)) {
			return false;
		}
		final CategoryProduct other = (CategoryProduct) obj;
		return this.category.equals(other.category)
				&& this.product.equals(other.product);
	}

	@Override
	public int hashCode() {
		StringBuilder stringBuilder = new StringBuilder();

		if (this != null && this.category != null && this.product != null) {
			stringBuilder.append(this.category.getId());
			stringBuilder.append(this.product.getId());
		}
		return stringBuilder.toString().hashCode();
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("categoryId", this.category.getId())
				.add("productId", this.product.getId()).toString();
	}
}
