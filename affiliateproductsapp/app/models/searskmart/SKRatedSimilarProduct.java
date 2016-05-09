package models.searskmart;

import java.io.Serializable;

import models.Product;
import models.SKSimilarProduct;

public class SKRatedSimilarProduct implements Serializable {

private static final long serialVersionUID = 2L;
	
	private SKSimilarProduct skSimilarProduct;
	private Double simalarRate;
	
	public SKRatedSimilarProduct(SKSimilarProduct skSimilarProduct, double simalarRate) {
		super();
		this.skSimilarProduct = skSimilarProduct;
		this.simalarRate = simalarRate;
	}

	public SKSimilarProduct getSkSimilarProduct() {
		return skSimilarProduct;
	}

	public void setSkSimilarProduct(SKSimilarProduct skSimilarProduct) {
		this.skSimilarProduct = skSimilarProduct;
	}

	public Double getSimalarRate() {
		return simalarRate;
	}

	public void setSimalarRate(Double simalarRate) {
		this.simalarRate = simalarRate;
	}

	
	
}
