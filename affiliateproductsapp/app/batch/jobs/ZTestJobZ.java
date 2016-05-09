package batch.jobs;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import constants.AffiliateConstants;
import constants.RakutenConstants;
import models.Brand;
import models.AdvertiserCategory;
import models.Product;
import play.Play;
import play.db.jpa.JPA;
import play.libs.F.Promise;
import repositories.Repository;
import services.CacheService;
import services.cj.impl.CJFileService;
import services.impactradius.ImpactRadiusFileService;
import services.rakuten.RakutenFileService;
import utils.log.Log;

public class ZTestJobZ extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(ZTestJobZ.class);

	@Inject
	protected static Repository repository;

	@Inject
	private CacheService cacheService;

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	final static String saveDirPath = Play.configuration.getProperty("affiliate.cj.product.feed.input.location");

	private void runJob() throws InterruptedException, UnsupportedEncodingException {
			
		List<String> missingCategories = (List<String>) JPA.em()
				.createQuery("SELECT DISTINCT(p.advertiserCategory) FROM Product p "
						+ "WHERE p.seller.id=41 AND p.advertisercategory.id NOT IN "
						+ "(SELECT c.id FROM AdvertiserCategory c WHERE c.id IN(SELECT DISTINCT(p1.advertisercategory.id) FROM Product p1 WHERE p1.seller.id=41 AND p1.advertisercategory IS NOT null))")
				.getResultList();
		
		logger.info("Category Size : " + missingCategories.size());
		logger.info(missingCategories);
		
		List<String> missingCategoryIDs = (List<String>) JPA.em()
				.createQuery("SELECT DISTINCT(p.advertisercategory.id) FROM Product p "
						+ "WHERE p.seller.id=41 AND p.advertisercategory.id NOT IN "
						+ "(SELECT c.id FROM AdvertiserCategory c WHERE c.id IN(SELECT DISTINCT(p1.advertisercategory.id) FROM Product p1 WHERE p1.seller.id=41 AND p1.advertisercategory IS NOT null))")
				.getResultList();
		
		logger.info("Category Size : " + missingCategoryIDs.size());
		logger.info(missingCategoryIDs);
		
		Query query = JPA.em().createQuery("Update Product p set p.advertisercategory=null where seller_id=:id AND p.advertisercategory.id in :missingCategoryIDs");
		query.setParameter("id", "41");
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
			
			List<Product> products = repository.createNamedQuery2("JPQL_GET_PRODUCTS_BASED_ON_CATEGORYNAME", 9999999L, missingCategory);
			logger.info("Find the products with category : " + missingCategory);
			logger.info(products);
			
			for (Product p : products) {
				p.setCategory(category);
				p = repository.update(p);
				logger.info("Updated p : " + p);
			}
		}
		
		
		
		/*List<Product> missingCategoryProducts = repository.createNamedQuery2(
				"JPQL_GET_PRODUCTS_BASED_ON_CATEGORYNAME", 1461363l, "BLINDS AND WALLPAPER>WALL/DECOR>S/O WALLPAPER");
		
		logger.info("Products Size : " + missingCategoryProducts.size());
		logger.info(missingCategoryProducts);*/

		
		

		/*List<String> missingBrands = (List<String>) JPA.em()
				.createQuery("SELECT DISTINCT(p.manufacturerName) FROM Product p "
						+ "WHERE p.seller.id=46 AND p.brand.id NOT IN "
						+ "(SELECT b.id FROM Brand b WHERE b.id IN(SELECT DISTINCT(p1.brand.id) FROM Product p1 WHERE p1.seller.id=46 AND p1.brand IS NOT null))")
				.getResultList();
		
		
		logger.info("Brand Size : " + missingBrands.size());
		logger.info(missingBrands);*/
		
		/*StringEscapeUtils.escapeJava("┬┐Qu├⌐ es un Congreso?");
		
		for (String s : CJFileService.bnFormat) {
			logger.info(s);
		}
		
		
		File f = new File("C:\\Users\\lwan0\\Desktop\\tmp\\cj\\Barnes & Noble\\Test\\Barnes_Noble-Barnes_Noble_Product_Catalog.txt");
		CJFileService.formatBarnesNoblesFeed(f);*/
		
		/*List<Promise> jobs = new ArrayList<Promise>();
		boolean jobsDone = false;
		File[] filesNeedCheckDup = new File("C:\\Users\\lwan0\\Desktop\\tmp\\cj\\feed\\Nike").listFiles();
		List<File> largeFiles = new ArrayList<File>();
		for (File f : filesNeedCheckDup) {
			Promise promise = new RakutenCheckDupJob(f, largeFiles, null).now();
			jobs.add(promise);

		}
		while (!jobsDone) {
			logger.info("Waiting for each Job ( Check and Remove Duplicates ) to complete...");
			jobsDone = true;
			Thread.sleep(AffiliateConstants.JOB_STATUS_MONITOR_TIME_IN_SECONDS);
			for (Promise promise : jobs) {
				jobsDone = jobsDone & promise.isDone();
			}
		}
		logger.info("Finish Check and Remove Duplicates Process...");
		logger.info("......... Start Split Process ........");
		
		
		
		Brand b = getBrand("abcdefg", "abcdefg");
		System.out.println(b.getName());
		
		Category c = getCategory("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		System.out.println(c.getName());
		*/
		
		/*List<String> list = new ArrayList<String>();
		for (long i = 0; i < 11000000; i++) {
			list.add(String.valueOf(123456789L+i));
		}*/
		
		/*final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		  // Get a Properties object
		     Properties props = System.getProperties();
		     props.setProperty("mail.smtp.host", "smtp.gmail.com");
		     props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
		     props.setProperty("mail.smtp.socketFactory.fallback", "false");
		     props.setProperty("mail.smtp.port", "465");//465, 587
		     props.setProperty("mail.smtp.socketFactory.port", "465"); //465, 587
		     props.put("mail.smtp.auth", "true");
		     props.put("mail.debug", "true");
		     props.put("mail.store.protocol", "pop3");
		     props.put("mail.transport.protocol", "smtp");
		     final String username = "qateam.spree@gmail.com";
		     final String password = "spree2015";
		     try{
		         Session session = Session.getDefaultInstance(props, 
		                              new Authenticator(){
		                                 protected PasswordAuthentication getPasswordAuthentication() {
		                                    return new PasswordAuthentication(username, password);
		                                 }});*/
		     

	/*	   // -- Create a new message --
		     Message msg = new MimeMessage(session);

		  // -- Set the FROM and TO fields --
		     msg.setFrom(new InternetAddress("qateam.spree@gmail.com"));
		     msg.setRecipients(Message.RecipientType.TO, 
		                      InternetAddress.parse("li.wan@searshc.com",false));
		     msg.setSubject("Fount Referral");
		     String mail_body_refer = "abc";
		    msg.setContent(mail_body_refer, "text/html; charset=utf-8");	    	 
		     msg.setSentDate(new Date());
		     Transport.send(msg);
		  }catch (MessagingException e){ 
			  e.printStackTrace();
			  logger.info(e);
			  }
		
		
		
		System.out.println("Free Memory : " + Runtime.getRuntime().freeMemory() / (1024 * 1024) + " Mb");
		System.out.println("total Memory  : " + Runtime.getRuntime().totalMemory() / (1024 * 1024) + " Mb");
		long usedMem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
		System.out.println("Used Memory : " + usedMem + " Mb	<----");*/
		
		
		// System.out.println(objectSize/(1024 * 1024) + "Mb");
		// List<Product> skList = repository.createNamedQuery2("JPQL_GET_PRODUCTS_BASED_LIKE_CATEGORY", 88888888L, "Beauty%");
		
		/*Query query = JPA.em()
				.createQuery("SELECT p FROM Product p where seller_id=:id AND in_stock=:stock AND advertiser_category LIKE :category");
		
		query.setParameter("id", "42");
		query.setParameter("stock", "1");
		query.setParameter("category", "Beauty%");
		List<Product> existingProductSkusInDB = (List<Product>) query.getResultList();
		System.out.println(existingProductSkusInDB.size());
		System.out.println(existingProductSkusInDB.get(0).toString());*/
		
		/*String s = "Health+%26+Beauty";
		s = URLDecoder.decode(s, "UTF-8");
		System.out.println(s);*/
		
	}

	private String getPattern(String fileName, Map<String, String> patternTable) {
		String category = fileName.split(RakutenConstants.CATEGORY_NAME_UNDERLINE)[2];
		if (patternTable.get(category) != null) {
			logger.info("---> " + fileName + " --- will be splited by Product Name");
			return RakutenConstants.PRODUCT_PATTERN_FOR_NAME;
		} else {
			logger.info("---> " + fileName + " --- will be splited by Image URL");
			return RakutenConstants.IMAGEURL_PATTERN;
		}
	}

	private void cleanFolder(File folder) {
		try {
			if (folder.exists()) {
				logger.info(Log.message("Start empty the folder : " + folder.getAbsolutePath()));
				FileUtils.cleanDirectory(folder);
			}
			logger.info(Log.message("Finish empty the folder : " + folder.getAbsolutePath()));
		} catch (IOException e) {
			logger.error(Log.message("Issues in cleanFolder :  " + e.getMessage()));
			e.printStackTrace();
		}
	}

	private Brand getBrand(String brandName, String sellerName) {
		// If the brand_name is null, then treat the seller name as
		// manufacturer name
		Brand brand = null;
		try {
			if (brandName == null || brandName.trim().length() < 1) {
				brandName = sellerName;
			}
			String brandNameCacheKey = brandName;
			brandNameCacheKey = StringUtils.deleteWhitespace(brandName);
			if (brandNameCacheKey != null) {
				String cacheKey = Brand.CACHE_PREFIX + brandNameCacheKey;
				brand = cacheService.get(cacheKey, Brand.class);
				if (brand == null) {
					brand = Brand.find("byName", brandName).first();
					cacheService.addToLongCache(cacheKey, brand);
				}
			}
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
		}
		return brand;
	}

	private AdvertiserCategory getCategory(String categoryName) {
		AdvertiserCategory category = null;
		String categoryNameCacheKey = categoryName;
		categoryNameCacheKey = StringUtils.deleteWhitespace(categoryName);
		if (categoryNameCacheKey != null) {
			String cacheKey = AdvertiserCategory.CACHE_PREFIX + categoryNameCacheKey;
			// category = cacheService.get(cacheKey, Category.class);
			if (category == null) {
				category = AdvertiserCategory.find("byName", categoryName).first();
				if (category == null) {
					category = new AdvertiserCategory(categoryName);
					repository.create(category);	
					logger.info("Created Category : " + category.getName());
					category = AdvertiserCategory.find("byName", categoryName).first();
				}
				// cacheService.addToLongCache(cacheKey, category);
			}
		}
		return category;
	}
}
