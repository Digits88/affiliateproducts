package services.impl;

import java.util.List;
import java.util.Map;

import models.AdvertiserCategory;
import models.Category;
import models.Product;
import models.Seller;
import repositories.Repository;
import services.CategoryService;

import com.google.inject.Inject;

public class DefaultCategoryService implements CategoryService {

	@Inject
	private Repository repository;

	@Override
	public List<AdvertiserCategory> getSellerCategories(Long sellerId, int pageNumber,
			int maxResults) {
		Seller seller = Seller.findById(sellerId);

		List<AdvertiserCategory> advertiserCategories = Product
				.find("select p.category from Product p where p.seller = ?1 group by p.category",
						seller).fetch(pageNumber, maxResults);
		return advertiserCategories;
	}

	@Override
	public List<Category> getChildCategories(Long categoryId) {
		Category category = Category.findById(categoryId);
		List<Category> childCategories = Category.find("byParent",
				category.getId()).fetch();
		return childCategories;
	}

	@Override
	public void mapCategory(Map<Long, Long> categoryMap) {
		if(!categoryMap.isEmpty()){
			for(Long advatiserCategoryId:categoryMap.keySet()){
				AdvertiserCategory advatiserCategory = AdvertiserCategory.findById(advatiserCategoryId);
				if(advatiserCategory!=null){
					Category category = Category.findById(categoryMap.get(advatiserCategoryId));
					advatiserCategory.setCategoryV1(category);
					repository.update(advatiserCategory);
				}
			}
		}
	}

}
