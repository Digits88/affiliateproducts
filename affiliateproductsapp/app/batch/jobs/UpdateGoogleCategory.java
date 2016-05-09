package batch.jobs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import models.Category;

import org.apache.log4j.Logger;

import play.Play;
import repositories.Repository;
import utils.log.Log;

public class UpdateGoogleCategory extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(UpdateGoogleCategory.class);

	private static final String CATEGORY_DELIMITER = " > ";
	private static final String TAB_DELIMITER = "\t";

	@Inject
	protected static Repository repository;

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		BufferedReader reader = null;
		String line = null;
		List<Long> googleTaxoIds = new ArrayList<Long>();
		
		try {
			reader = new BufferedReader(new FileReader(
					Play.configuration.getProperty("google.category.file")));
			while (isValid(line = reader.readLine())) {
				line = line.replaceAll("\"", "");
				String[] taxonomies = line.split(TAB_DELIMITER);
				Long googleTaxoId = Long.parseLong(taxonomies[0]);
				String parentName = "";
				String categoryName = taxonomies[taxonomies.length - 1];
				Long parent = null;
				if (taxonomies.length > 2) {
					for (int i = 1; i < taxonomies.length - 2; i++) {
						parentName = parentName + taxonomies[i]
								+ CATEGORY_DELIMITER;
					}
					parentName = parentName + taxonomies[taxonomies.length - 2];
				} else {
					parentName = null;
					// Parent ID will be 0 if the category is root
					parent = 0L;
				}
				googleTaxoIds.add(googleTaxoId);
				Category category = Category.find("byGoogleTaxonomy",
						googleTaxoId).first();
				if (category != null) {
					if (category.getParentName() != null
							&& !category.getParentName().equals(parentName)) {
						logger.error(Log
								.message("Discripency in inserting this record: "
										+ line));
					}

				} else {
					category = new Category(categoryName, parent,
							googleTaxoId, parentName);
					repository.create(category);
				}

			}

			logger.info(Log
					.message("Successfully created all the categories from google file. Will be kicking off update parent ID's job in 5 seconds"));

			// Triggering update parent ID's job in 5 seconds
			new UpdateParentIdInCategory(googleTaxoIds).in(5);

		} catch (FileNotFoundException e) {
			logger.info(Log.message("File not found: "
					+ Play.configuration.getProperty("google.category.file")));
			e.printStackTrace();
		} catch (IOException e) {
			logger.info(Log.message("Unable to read file: "
					+ Play.configuration.getProperty("google.category.file")));
			e.printStackTrace();
		}
	}

	private static boolean isValid(String line) {
		if (line != null && line.trim() != "") {
			return true;
		}
		return false;
	}

	public static void main(String[] args) {
		BufferedReader reader = null;
		String line = null;
		try {
			reader = new BufferedReader(new FileReader(
					"/Users/ryellap/Desktop/Files/taxonomy-with-ids.en-US.txt"));
			// String line = reader.readLine();
			while (isValid(line = reader.readLine())) {
				String[] taxonomies = line.split(TAB_DELIMITER);
				Long googleTaxoId = Long.parseLong(taxonomies[0]);
				String parentName = "";
				String categoryName = taxonomies[taxonomies.length - 1];
				Long parent = null;
				if (taxonomies.length > 2) {
					for (int i = 1; i < taxonomies.length - 2; i++) {
						parentName = parentName + taxonomies[i]
								+ CATEGORY_DELIMITER;
					}
					parentName = parentName + taxonomies[taxonomies.length - 2];
				} else {
					parentName = null;
					parent = 0L;
				}

				// CategoryV1 category = CategoryV1.find("byGoogleTaxonomy",
				// googleTaxoId).first();
				// if (category != null) {
				// if(!category.getParentName().equals(parentName)){
				// logger.error(Log.message("Discripency in inserting this record: "+line));
				// }
				//
				// } else {
				Category category = new Category(categoryName, parent,
						googleTaxoId, parentName);
				// repository.create(category);
				// }

				System.out.println(category.toString());
			}

		} catch (FileNotFoundException e) {
			logger.info(Log.message("File not found: "
					+ Play.configuration.getProperty("google.category.file")));
			e.printStackTrace();
		} catch (IOException e) {
			logger.info(Log.message("Unable to read file: "
					+ Play.configuration.getProperty("google.category.file")));
			e.printStackTrace();
		}
	}
}
