package batch.jobs;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import models.AdvertiserCategory;
import models.Product;
import models.Seller;
import play.db.jpa.JPA;
import repositories.Repository;

public class UpdateMissingCategoryChild extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(UpdateMissingCategoryChild.class);

	@Inject
	protected static Repository repository;

	@Transient
	private Seller seller;

	@Transient
	private String thread;

	public UpdateMissingCategoryChild(Seller seller, String thread) {
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

		List<String> missingCategories = (List<String>) JPA.em()
				.createQuery("SELECT DISTINCT(p.advertiserCategory) FROM Product p "
						+ "WHERE p.seller.id=" + seller.getId().toString()
						+" AND p.advertisercategory.id NOT IN "
						+ "(SELECT c.id FROM AdvertiserCategory c WHERE c.id IN(SELECT DISTINCT(p1.advertisercategory.id) FROM Product p1 WHERE p1.seller.id=" + seller.getId().toString() 
						+ " AND p1.advertisercategory IS NOT null))")
				.getResultList();
		if (missingCategories.size() == 0) {
			logger.info("No Missing Categories are found!!");
			return;
		}
		
		logger.info("Missing Category Name List Size : " + missingCategories.size());
		logger.info(missingCategories);
		
		List<String> missingCategoryIDs = (List<String>) JPA.em()
				.createQuery("SELECT DISTINCT(p.advertisercategory.id) FROM Product p "
						+ "WHERE p.seller.id=" + seller.getId().toString()
						+" AND p.advertisercategory.id NOT IN "
						+ "(SELECT c.id FROM AdvertiserCategory c WHERE c.id IN(SELECT DISTINCT(p1.advertisercategory.id) FROM Product p1 WHERE p1.seller.id=" + seller.getId().toString()
						+" AND p1.advertisercategory IS NOT null))")
				.getResultList();
		
		logger.info("Missing Category ID List Size : " + missingCategoryIDs.size());
		logger.info(missingCategoryIDs);
		
		Query query = JPA.em().createQuery("Update Product p set p.advertisercategory=null, p.brand=null where seller_id=:id AND p.advertisercategory.id in :missingCategoryIDs");
		query.setParameter("id", seller.getId());
		query.setParameter("missingCategoryIDs", missingCategoryIDs);
		int res = query.executeUpdate();
		logger.info("Updated " + res + " category to null value");
		
		
		for (String missingCategory : missingCategories) {
			AdvertiserCategory category = AdvertiserCategory.find("byName", missingCategory).first();
			if (category == null) {
				category = repository.create(new AdvertiserCategory(missingCategory));				
				logger.info("Created Category : " + category);
			} else {
				logger.info("Found Category : " + category);
			}
			
			List<Product> products = repository.createNamedQuery2("JPQL_GET_PRODUCTS_BASED_ON_CATEGORYNAME", seller.getAdvertiserId(), missingCategory);
			logger.info("Find the products with category : " + missingCategory);
			// logger.info(products);
			
			for (Product p : products) {
				p.setCategory(category);
				p = repository.update(p);
				logger.info("Updated p : " + p.getId() + ", " + p.getName() + ", " + p.getAdvertiserCategory() + ", " + p.getCategory().getId());
			}
		}

	}

}
