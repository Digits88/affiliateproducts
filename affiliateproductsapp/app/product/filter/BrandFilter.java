package product.filter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.Brand;
import models.Product;

import com.google.inject.internal.Lists;

public class BrandFilter extends BaseProductFilter {

	public BrandFilter(CriteriaBuilder cb, CriteriaQuery cq,
			Root<Product> product) {
		super(cb, cq, product);
	}

	@Override
	public List<Predicate> filter(List<Long> sellerIds, List<Long> brandIds,
			List<Long> categoryIds, BigDecimal minPrice, BigDecimal maxPrice,
			Integer sale, String keyword) {

		List<Brand> brands = Lists.newArrayList();
		List<Predicate> predicates = new ArrayList<Predicate>();
		if (brandIds != null && brandIds.size() > 0) {
			brands = repository.findList(Brand.class,
					"from Brand where id in ?1", brandIds);
			Expression<Brand> parameterExpression = product.get("brand");
			Predicate predicate = parameterExpression.in(brands);
			if (brands != null && brands.size() > 0) {
				predicates.add(predicate);
			} else {
				predicates.add(parameterExpression.isNull());
			}
		}
		return predicates;
	}

}
