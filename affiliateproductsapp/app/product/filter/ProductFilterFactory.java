package product.filter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import models.Product;
import enums.ProductFilterEntity;

public class ProductFilterFactory {

	public static ProductFilter getFilter(ProductFilterEntity entity,
			CriteriaBuilder cb, CriteriaQuery cq, Root<Product> root) {
		ProductFilter filter = null;
		switch (entity) {
		case SELLER:
			filter = new SellerFilter(cb, cq, root);
			break;
		case CATEGORY:
			filter = new CategoryFilter(cb, cq, root);
			break;
		case BRAND:
			filter = new BrandFilter(cb, cq, root);
			break;
		case PRICE:
			filter = new PriceFilter(cb, cq, root);
			break;
		case KEYWORD:
			filter = new KeywordFilter(cb, cq, root);
			break;
//		case DISCOUNT:
//			filter = new SaleFilter(cb, cq, root);
//			break;
		}
		return filter;
	}

}
