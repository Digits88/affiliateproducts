package batch.jobs;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import models.AdvertiserCategory;
import models.Brand;
import models.Product;
import models.Seller;
import play.db.jpa.JPA;
import repositories.Repository;

public class UpdateMissingBrandChild extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(UpdateMissingBrandChild.class);

	@Inject
	protected static Repository repository;

	@Transient
	private Seller seller;

	@Transient
	private String thread;

	public UpdateMissingBrandChild(Seller seller, String thread) {
		super();
		this.seller = seller;
		this.thread = thread;
	}

	public void doJob() {
		logger.info(thread + " -- Start Working on Seller ===>>> " + seller);
		runJob();
		logger.info(thread + " -- Finish Working on Seller ===>>> " + seller);
	}

	private void runJob() {

		List<String> missingBrands = (List<String>) JPA.em()
				.createQuery("SELECT DISTINCT(p.manufacturerName) FROM Product p "
						+ "WHERE p.seller.id=" + seller.getId().toString()
						+" AND p.brand.id NOT IN "
						+ "(SELECT b.id FROM Brand b WHERE b.id IN(SELECT DISTINCT(p1.brand.id) FROM Product p1 WHERE p1.seller.id=" + seller.getId().toString() 
						+ " AND p1.brand IS NOT null))")
				.getResultList();
		
		if (missingBrands.size() == 0) {
			logger.info("No Missing Brands are found!!");
			return;
		}
		
		logger.info("Missing Brand Name List Size : " + missingBrands.size());
		logger.info(missingBrands);
		
		List<String> missingBrandIDs = (List<String>) JPA.em()
				.createQuery("SELECT DISTINCT(p.brand.id) FROM Product p "
						+ "WHERE p.seller.id=" + seller.getId().toString()
						+" AND p.brand.id NOT IN "
						+ "(SELECT b.id FROM Brand b WHERE b.id IN(SELECT DISTINCT(p1.brand.id) FROM Product p1 WHERE p1.seller.id=" + seller.getId().toString()
						+" AND p1.brand IS NOT null))")
				.getResultList();
		
		logger.info("Missing Brand ID List Size : " + missingBrandIDs.size());
		logger.info(missingBrandIDs);
		
		Query query = JPA.em().createQuery("Update Product p set p.brand=null where seller_id=:id AND p.brand.id in :missingBrandIDs");
		query.setParameter("id", seller.getId());
		query.setParameter("missingBrandIDs", missingBrandIDs);
		int res = query.executeUpdate();
		logger.info("Updated " + res + " category to null value");
		
		
		for (String missingBrand : missingBrands) {
			Brand brand = Brand.find("byName", missingBrand).first();
			if (brand == null) {
				brand = repository.create(new Brand(missingBrand));				
				logger.info("Created Brand : " + brand);
			} else {
				logger.info("Found Brand : " + brand);
			}
			
			List<Product> products = repository.createNamedQuery2("JPQL_GET_PRODUCTS_BASED_ON_BRANDNAME", seller.getAdvertiserId(), missingBrand);
			logger.info("Find the products with category : " + missingBrand);
			// logger.info(products);
			
			for (Product p : products) {
				p.setBrand(brand);
				p = repository.update(p);
				logger.info("Updated p : " + p.getId() + ", " + p.getName() + ", " + p.getManufacturerName() + ", " + p.getBrand().getId());
			}
		}

	}
}
