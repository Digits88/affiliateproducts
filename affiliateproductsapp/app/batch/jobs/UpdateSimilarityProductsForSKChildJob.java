package batch.jobs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import javax.inject.Inject;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import models.Product;
import models.SKSimilarProduct;
import repositories.Repository;
import utils.AffiliateProductUtil;
import utils.log.Log;
import utils.searskmart.SimilarProductComparator;

public class UpdateSimilarityProductsForSKChildJob extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(UpdateSimilarityProductsForSKChildJob.class);
	private static int maxRecords = 20;
	
	@Inject
	protected static Repository repository;
	
	@Transient
	private List<Product> skProductList;
	
	@Transient
	private List<Product> otherProductList;
	
	@Transient
	private String thread;
	
	public UpdateSimilarityProductsForSKChildJob(List<Product> skProductList, List<Product> otherProductList, String thread) {
		super();
		this.skProductList = skProductList;
		this.otherProductList = otherProductList;
		this.thread = thread;
	}

	@Override
	public void doJob() {
		// logger.info(thread + " - Start Child Job");
		runJob();
		// logger.info(thread + " - Finish Child Job");
	}
	
	private void runJob() {
		/**
		 * For each skProduct, we need to traversal every product in otherProductList to calculate the similarity
		 * 
		 */
		try {
			for (Product skProduct : skProductList) {
				// logger.info(thread + " --  Start Working on SK Product : " + skProduct.getId() + " -- " + skProduct.getName());
				//PriorityQueue<SKRatedSimilarProduct> minHeap = new PriorityQueue<SKRatedSimilarProduct>(20, new SimilarProductComparator());
				List<SKSimilarProduct> skRatedSimilarProductList = new ArrayList<SKSimilarProduct>();
				for (Product otherProduct : otherProductList) {
					// logger.info(thread + " --  Calculate with : " + otherProduct.getId() + " -- " + otherProduct.getName());
					// calculate the similarity -> parse skProductName and otherProduct
					SKSimilarProduct skSimilarProduct = AffiliateProductUtil.calculateSimilarityWithSKAndOther(skProduct, otherProduct);
					// logger.info(thread + " -- " + skProduct.getName() + " ==> " + skSimilarProduct.getSimilarity().toString() + " <== " + otherProduct.getName());
					if (skSimilarProduct.getSimilarity().doubleValue() > 0.70) {
						logger.info(thread + "Add This Into List : " + skSimilarProduct.toString());
						skRatedSimilarProductList.add(skSimilarProduct);												
					}
				}
				// logger.info(thread + " --  Get the Similar Product List : " + skProduct.getId() + " -- " + skProduct.getName());
				
				if (skRatedSimilarProductList.size() == 0) {
					logger.info(thread + " -- skRatedSimilarProductList is Empty! No Similar Products were found. ");
					return;
				}
				
				// createOrUpdate(skRatedSimilarProductList);
				
				// pick first 20 products
				Collections.sort(skRatedSimilarProductList, new SimilarProductComparator());
				if (skRatedSimilarProductList.size() > maxRecords) {
					createOrUpdate(skRatedSimilarProductList.subList(0, maxRecords));
				} else {
					createOrUpdate(skRatedSimilarProductList);
				}
			}
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
		}
	}
	
	private void createOrUpdate(List<SKSimilarProduct> skSimilarProductList) {
		// logger.info(thread + " Start Create Or Update");
		Product skProduct = null;
		Product similarProduct = null;
		try {
			for (SKSimilarProduct skSimilarProduct : skSimilarProductList) {
				skProduct = skSimilarProduct.getSkProduct();
				similarProduct = skSimilarProduct.getSimilarProduct();
				
				SKSimilarProduct skSimilarProductInDB = repository.find(SKSimilarProduct.class, "from SKSimilarProduct where skProduct_id=? and similarProduct_id=?",
						skProduct.getId(), similarProduct.getId());
				if (skSimilarProductInDB == null) {
					repository.create(new SKSimilarProduct(skProduct, similarProduct, skSimilarProduct.getSimilarity()));
					logger.info("+++++ Created SK Similar Product : " + skSimilarProduct.toString());
				}
			}
			// logger.info(thread + " Finishe Create Or Update");
		} catch (Exception e) {
			if (skProduct != null) {
				logger.error(Log.message("Issue : SK Product --> " + skProduct.getId() + " -- " + skProduct.getName()));
			}
			if (similarProduct != null) {
				logger.error(Log.message("Issue : Similar Product --> " + similarProduct.getId() + " -- " + similarProduct.getName()));
			}
			logger.error(Log.message(e.getMessage()));
		}
	}
}
