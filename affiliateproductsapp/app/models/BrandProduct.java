package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.google.common.base.Objects;

@Entity
@Table(name = "AFFILIATE_BRAND_PRODUCT")
public class BrandProduct extends VersionedEntity {

	private static final long serialVersionUID = 2L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	public Brand brand;

	@ManyToOne
	public Product product;

	public BrandProduct(Brand brand, Product product) {
		this.brand = brand;
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
		if (!(obj instanceof BrandProduct)) {
			return false;
		}
		final BrandProduct other = (BrandProduct) obj;
		return this.brand.equals(other.brand)
				&& this.product.equals(other.product);
	}

	@Override
	public int hashCode() {
		StringBuilder stringBuilder = new StringBuilder();

		if (this != null && this.brand != null && this.product != null) {
			stringBuilder.append(this.brand.getId());
			stringBuilder.append(this.product.getId());
		}
		return stringBuilder.toString().hashCode();
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("sellerId", this.brand.getId())
				.add("productId", this.product.getId()).toString();
	}
}
