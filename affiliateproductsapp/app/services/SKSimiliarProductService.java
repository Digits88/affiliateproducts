package services;

import java.util.List;

import models.Product;

public interface SKSimiliarProductService {

	List<Product> getSameProducts(Long productId, int pageNumber, int maxResults);

	List<Product> getSimilarProducts(Long productId,  int pageNumber, int maxResults);
}
