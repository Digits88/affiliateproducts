package product.filter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.Product;

public class SaleFilter extends BaseProductFilter {

	public SaleFilter(CriteriaBuilder cb, CriteriaQuery cq,
			Root<Product> product) {
		super(cb, cq, product);
	}

	@Override
	public List<Predicate> filter(List<Long> sellerIds, List<Long> brandIds, List<Long> categoryIds,
			BigDecimal minPrice, BigDecimal maxPrice, Integer sale, String keyword) {

		List<Predicate> predicates = new ArrayList<Predicate>();
		if (sale != null) {
			Predicate predicate = cb.ge(product.<Integer> get("sale"), sale);
			predicates.add(predicate);
		}
		return predicates;
	}

}
