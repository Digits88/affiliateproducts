package product.filter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.AdvertiserCategory;
import models.Product;

import com.google.inject.internal.Lists;

public class CategoryFilter extends BaseProductFilter {

	public CategoryFilter(CriteriaBuilder cb, CriteriaQuery cq,
			Root<Product> product) {
		super(cb, cq, product);
	}

	@Override
	public List<Predicate> filter(List<Long> sellerIds, List<Long> brandIds,
			List<Long> categoryIds, BigDecimal minPrice, BigDecimal maxPrice,
			Integer sale, String keyword) {

		List<Predicate> predicates = new ArrayList<Predicate>();
		List<AdvertiserCategory> categories = Lists.newArrayList();
		if (categoryIds != null && categoryIds.size() > 0) {
			categories = repository.findList(AdvertiserCategory.class,
					"from Category where id in ?1", categoryIds);
			Expression<AdvertiserCategory> parameterExpression = product.get("category");
			Predicate predicate = parameterExpression.in(categories);
			if (categories != null && categories.size() > 0) {
				predicates.add(predicate);
			} else {
				predicates.add(parameterExpression.isNull());
			}
		}
		return predicates;
	}

}
