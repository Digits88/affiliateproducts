package batch.jobs;

import java.io.File;

import javax.persistence.Transient;

import org.apache.log4j.Logger;

import play.Play;
import services.cj.impl.CJFileService;

public class SyncCJSuperProductFeedDownloadJob extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(SyncCJSuperProductFeedDownloadJob.class);
	
	@Transient
	private CJFileService cjFileService;
	
	@Override
	public void doJob() throws Exception {
		logger.info("========== READY TO START DOWNLOAD CJ SUPER FEED JOB ==========");
		runJob();
		logger.info("========== FINISHE DOWNLOAD CJ SUPER FEED JOB  ==========");
	}

	private void runJob() {
		
		File folder = new File(Play.configuration.getProperty("affiliate.cj.product.feed.input.location"));
		
		cjFileService = new CJFileService();
		// clean the folder first
		cjFileService.cleanFiles(folder);
		// download the files
		cjFileService.downloadCJSuperFeeds();
	}
}
