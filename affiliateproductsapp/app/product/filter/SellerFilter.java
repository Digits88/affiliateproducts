package product.filter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.Product;
import models.Seller;

import com.google.inject.internal.Lists;

public class SellerFilter extends BaseProductFilter {

	public SellerFilter(CriteriaBuilder cb, CriteriaQuery cq,
			Root<Product> product) {
		super(cb, cq, product);
	}

	@Override
	public List<Predicate> filter(List<Long> sellerIds, List<Long> brandIds,
			List<Long> categoryIds, BigDecimal minPrice, BigDecimal maxPrice,
			Integer sale, String keyword) {

		List<Predicate> predicates = new ArrayList<Predicate>();
		List<Seller> sellers = Lists.newArrayList();
		if (sellerIds != null && sellerIds.size() > 0) {
			sellers = repository.findList(Seller.class,
					"from Seller where id in ?1", sellerIds);
			Expression<Seller> parameterExpression = product.get("seller");
			Predicate predicate = parameterExpression.in(sellers);
			if (sellers != null && sellers.size() > 0) {
				predicates.add(predicate);
			} else {
				predicates.add(parameterExpression.isNull());
			}
		}
		return predicates;
	}

}
