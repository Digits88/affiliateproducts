package product.filter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.Product;

public class PriceFilter extends BaseProductFilter {

	public PriceFilter(CriteriaBuilder cb, CriteriaQuery cq,
			Root<Product> product) {
		super(cb, cq, product);
	}

	@Override
	public List<Predicate> filter(List<Long> sellerIds, List<Long> brandIds, List<Long> categoryIds,
			BigDecimal minPrice, BigDecimal maxPrice, Integer sale, String keyword) {

		List<Predicate> predicates = new ArrayList<Predicate>();
		if (minPrice != null && maxPrice != null) {
			Predicate predicate1 = cb.ge(product.<BigDecimal> get("price"),
					minPrice);
			Predicate predicate2 = cb.le(product.<BigDecimal> get("price"),
					maxPrice);
			predicates.add(predicate1);
			predicates.add(predicate2);
		}
		return predicates;
	}

}
