package product.filter;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.criteria.Predicate;

public interface ProductFilter {

	List<Predicate> filter(List<Long> sellerIds, List<Long> brandIds, List<Long> categoryIds,
			BigDecimal minPrice, BigDecimal maxPrice, Integer sale, String keyword);

}
