package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.google.common.base.Objects;

@Entity
@Table(name = "AFFILIATE_SELLER_PRODUCT")
public class SellerProduct extends VersionedEntity {

	private static final long serialVersionUID = 2L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	public Seller seller;

	@ManyToOne
	public Product product;

	public SellerProduct(Seller seller, Product product) {
		this.seller = seller;
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
		if (!(obj instanceof SellerProduct)) {
			return false;
		}
		final SellerProduct other = (SellerProduct) obj;
		return this.seller.equals(other.seller)
				&& this.product.equals(other.product);
	}

	@Override
	public int hashCode() {
		StringBuilder stringBuilder = new StringBuilder();

		if (this != null && this.seller != null && this.product != null) {
			stringBuilder.append(this.seller.getId());
			stringBuilder.append(this.product.getId());
		}
		return stringBuilder.toString().hashCode();
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("sellerId", this.seller.getId())
				.add("productId", this.product.getId()).toString();
	}
}
