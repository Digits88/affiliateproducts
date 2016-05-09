package product.filter;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import models.Product;
import play.modules.guice.InjectSupport;
import repositories.Repository;

@InjectSupport
public abstract class BaseProductFilter implements ProductFilter {

	@Inject
	protected static Repository repository;

	CriteriaBuilder cb = null;
	CriteriaQuery cq = null;
	Root<Product> product = null;

	public BaseProductFilter(CriteriaBuilder cb, CriteriaQuery cq,
			Root<Product> product) {
		super();
		this.cb = cb;
		this.cq = cq;
		this.product = product;
	}

}
