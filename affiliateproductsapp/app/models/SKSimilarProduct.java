package models;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.google.common.base.Objects;

@Entity
@Table(name = "AFFILIATE_SK_SAME_PRODUCT")
public class SKSimilarProduct extends VersionedEntity {

	private static final long serialVersionUID = 2L;
	public static final String CACHE_PREFIX = Product.class.getSimpleName();
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected Long id;
	
	@OneToOne
	protected Product skProduct;
	
	@OneToOne
	protected Product similarProduct;
	
	@Column(name = "similarity")
	private BigDecimal similarity;

	public BigDecimal getSimilarity() {
		return similarity;
	}

	public void setSimilarity(BigDecimal similarity) {
		this.similarity = similarity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Product getSkProduct() {
		return skProduct;
	}

	public void setSkProduct(Product skProduct) {
		this.skProduct = skProduct;
	}

	public Product getSimilarProduct() {
		return similarProduct;
	}

	public void setSimilarProduct(Product similarProduct) {
		this.similarProduct = similarProduct;
	}
	
	public SKSimilarProduct(Product skProduct, Product similarProduct, BigDecimal similarity) {
		super();
		this.skProduct = skProduct;
		this.similarProduct = similarProduct;
		this.similarity = similarity;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SKSimilarProduct)) {
			return false;
		}
		final SKSimilarProduct other = (SKSimilarProduct) obj;
		return this.skProduct.equals(other.skProduct) && this.similarProduct.equals(other.similarProduct);
	}
	
	@Override
	public int hashCode() {
		StringBuilder stringBuilder = new StringBuilder();

		if (this != null && this.skProduct != null && this.similarProduct != null) {
			stringBuilder.append(this.skProduct.getId());
			stringBuilder.append(this.similarProduct.getId());
		}
		return stringBuilder.toString().hashCode();
	}
	
	@Override
	public String toString() {
		return "Product [skProduct=" + skProduct.getName() + ", similarProduct=" + similarProduct.getName() + ", similar=" + similarity.toString() + "]";
	}
}
