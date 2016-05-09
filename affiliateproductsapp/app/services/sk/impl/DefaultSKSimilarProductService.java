package services.sk.impl;

import java.util.ArrayList;
import java.util.List;

import models.Product;
import models.SKSimilarProduct;
import services.SKSimiliarProductService;

public class DefaultSKSimilarProductService implements SKSimiliarProductService {

	@Override
	public List<Product> getSameProducts(Long productId, int pageNumber, int maxResults) {
		List<Product> sameProducts = new ArrayList<Product>();
		Product product = Product.findById(productId);
		if (product != null) {
			sameProducts = SKSimilarProduct
					.find("SELECT sksp.similarProduct FROM SKSimilarProduct sksp WHERE sksp.skProduct = ?1 AND sksp.similarity=1",
							product).fetch(pageNumber, maxResults);
		}
		return sameProducts;
	}

	@Override
	public List<Product> getSimilarProducts(Long productId, int pageNumber, int maxResults) {
		List<Product> similarProducts = new ArrayList<Product>();
		Product product = Product.findById(productId);
		if (product != null) {
			similarProducts = SKSimilarProduct
					.find("SELECT sksp.similarProduct FROM SKSimilarProduct sksp WHERE sksp.skProduct = ?1 AND sksp.similarity BETWEEN 0.30 AND 0.99",
							product).fetch(pageNumber, maxResults);
		}
		return similarProducts;
	}

}
