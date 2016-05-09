package services;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import models.Product;
import models.cj.CJProduct;

public interface ProductService {

	Product createOrUpdate(CJProduct cjProduct);

	Map<String, Object> getFilteredProducts(List<Long> sellerIds,
			List<Long> brandIds, List<Long> categoryIds, BigDecimal minPrice,
			BigDecimal maxPrice, Integer sale, String keyword, Integer start,
			Integer limit);

	Map<String, Object> searchProducts(String keyword, Integer start,
			Integer limit);

	Map<String, Object> getBySeller(Long sellerId, Integer offset,
			Integer maxResults);

	Map<String, Object> getSellers(Integer offset, Integer maxResults);

	void createOrUpdate(CJProduct cjProduct, File inputFile);

	void createOrUpdate(CJProduct cjProduct, File inputFile, String thread);
}
