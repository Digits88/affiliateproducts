package batch.jobs;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

import constants.AffiliateConstants;
import models.Product;
import play.db.jpa.JPA;
import play.libs.F.Promise;
import repositories.Repository;
import services.CacheService;
import utils.log.Log;

public class UpdateSimilarityProductsForSK extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(UpdateSimilarityProductsForSK.class);

	@Inject
	protected static Repository repository;

	@Transient
	private List<Promise> childJobs = null;

	@Transient
	private Boolean allChildJobsCompleted = false;

	@Transient
	private Long skSeller_id;

	@Transient
	private String skCategory;

	@Transient
	private Long otherSeller_id;

	@Transient
	private String otherCategory;

	public UpdateSimilarityProductsForSK(Long skSeller_id, String skCategory, Long otherSeller_id,
			String otherCategory) {
		super();
		this.skSeller_id = skSeller_id;
		this.skCategory = skCategory;
		this.otherSeller_id = otherSeller_id;
		this.otherCategory = otherCategory;
	}

	@Override
	public void doJob() throws Exception {
		logger.info("Start Job");
		long startTime = System.currentTimeMillis();
		runJob();
		logger.info(Log.message("!!! Finish Job !!!"));
		logger.info(Log.message("Total Time Used: " + (System.currentTimeMillis() - startTime)));
	}

	private void runJob() {
		// Get List of all the sk id based on category
		// get all other seller id based on category
		// split sk list into sub-list, base on each list calculate the
		// similarity

		try {
			Query query = null;
			logger.info("Get SK Product List For : " + skSeller_id.toString() + " | Category : " + skCategory);
			query = JPA.em()
					.createQuery("SELECT p FROM Product p where seller_id=:id AND advertiser_category LIKE :category");
			
			query.setParameter("id", skSeller_id.toString());
			// query.setParameter("stock", "1");
			query.setParameter("category", skCategory+"%");
			List<Product> skList = (List<Product>) query.getResultList();
			logger.info("SK Product List Size : " + skList.size());
			
			logger.info("Spliting SK Product List");
			List<List<Product>> skProductsLists = Lists.partition(skList, 1000);
			logger.info("Sub SK Product List Size : " + skProductsLists.size());
			
			
			// Prepare other Products List
			logger.info("Get Other Product List For : " + otherSeller_id.toString() + " | Category : " + otherCategory);
			query = JPA.em()
					.createQuery("SELECT p FROM Product p where seller_id=:id AND advertiser_category LIKE :category");
			
			query.setParameter("id", otherSeller_id.toString());
			// query.setParameter("stock", "1");
			query.setParameter("category", otherCategory+"%");
			List<Product> otherProductsList = (List<Product>) query.getResultList();
			logger.info("Other Product List Size : " + otherProductsList.size());
			
			
			childJobs = new ArrayList<Promise>();
			// Ready to call updateSimilarityProductsForSKChildJob to calculate
			for (int i = 0; i < skProductsLists.size(); i++) {
				List<Product> skProductsList = skProductsLists.get(i);
				Promise promise = new UpdateSimilarityProductsForSKChildJob(skProductsList, otherProductsList, "Thread_"+i).now();
				childJobs.add(promise);
			}
	
			while (!allChildJobsCompleted) {
				logger.info("Waiting for each Calculation Jobto complete...");
				allChildJobsCompleted = true;
				Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
				for (Promise promise : childJobs) {
					allChildJobsCompleted = allChildJobsCompleted & promise.isDone();
				}
			}		
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Log.message(e.getMessage()));
		}
	}

}
