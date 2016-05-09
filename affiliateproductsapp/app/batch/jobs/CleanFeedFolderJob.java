package batch.jobs;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import play.Play;
import repositories.Repository;
import utils.log.Log;

public class CleanFeedFolderJob extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(ZTestJobZ.class);

	final static String saveDirPath = Play.configuration.getProperty("affiliate.cj.product.feed.input.location");

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() throws InterruptedException {
		cleanFolder(new File(saveDirPath));		
		logger.info("!!! Clean Feed Folder Job Is Finished !!!");
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
}
