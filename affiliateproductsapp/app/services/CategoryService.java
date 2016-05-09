package services;

import java.util.List;
import java.util.Map;

import models.AdvertiserCategory;
import models.Category;

public interface CategoryService {

	List<AdvertiserCategory> getSellerCategories(Long sellerId, int pageNumber,
			int maxResults);

	List<Category> getChildCategories(Long categoryId);

	void mapCategory(Map<Long, Long> categoryMap);
}
