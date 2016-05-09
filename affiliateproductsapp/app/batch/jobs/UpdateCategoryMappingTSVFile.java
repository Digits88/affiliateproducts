package batch.jobs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.inject.Inject;

import models.AdvertiserCategory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import play.Play;
import play.modules.guice.InjectSupport;
import repositories.Repository;
import services.ProductService;
import utils.log.Log;

@InjectSupport
public class UpdateCategoryMappingTSVFile extends AbstractBatchJob {

	@Inject
	protected static ProductService productService;

	private static Logger logger = Logger
			.getLogger(UpdateCategoryMappingTSVFile.class);

	@Inject
	protected static Repository repository;

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		String categoryMappingEntry = null;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(
					Play.configuration
					.getProperty("affiliate.cj.product.syw.category.mapping")));
			while ((categoryMappingEntry = reader.readLine()) != null) {
				String advertiserCategory = categoryMappingEntry.split("\t")[0];
				Long sywTagId = new Long(categoryMappingEntry.split("\t")[1]);
				AdvertiserCategory category = AdvertiserCategory.find("byName", advertiserCategory)
						.first();
				if (category != null) {
					if (category.getSywTagId() != null) {
						if (!category.getSywTagId().equals(sywTagId)) {
							logger.error(Log
									.message("Category exists but the sywTagIds are not same : "
											+ category));
						}
					} else {
						category.setSywTagId(sywTagId);
						repository.update(category);
					}
				} else {
					logger.error(Log
							.message("Category does not exist which should not be the case !!! : "
									+ advertiserCategory));
				}
			}
			logger.info(Log
					.message("Successfully completed updating the Category table for the  SYWTag Id Mapping"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static void main(String args[]){
		String categoryMappingEntry = null;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("C:\\Users\\imthiyaz\\Desktop\\tmp\\cj\\sywcategorymapping\\categorymapping.txt"));
			while ((categoryMappingEntry = reader.readLine()) != null) {
				System.out.println(categoryMappingEntry);
				System.out.println(StringEscapeUtils.unescapeJava(categoryMappingEntry));
				
	}
		}
			catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
}