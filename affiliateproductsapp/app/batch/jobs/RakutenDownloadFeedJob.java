package batch.jobs;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.persistence.Transient;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import services.rakuten.RakutenFileService;
import utils.log.Log;


public class RakutenDownloadFeedJob extends AbstractBatchJob{
	
	private static Logger logger = Logger
			.getLogger(RakutenDownloadFeedJob.class);

	@Transient
	private RakutenFileService rakutenFileService;

	@Transient
	private String remoteFile;
	
	@Transient
	private String saveToLocal;
	
	@Transient
	private int count;
	
	public RakutenDownloadFeedJob(String remoteFile, String saveToLocal,
			int count) {
		super();
		this.remoteFile = remoteFile;
		this.saveToLocal = saveToLocal;
		this.count = count;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		try {
			rakutenFileService = new RakutenFileService();
			boolean isFinished = rakutenFileService.downloadSingleFile(remoteFile, saveToLocal);
			if(isFinished) {
				count--;
				logger.info("Download " + remoteFile + " -- left " + count + " need to be downloaded !");
			}
			rakutenFileService.closeFTPConnection();
		} catch (IOException e) {
			logger.error(Log.message("!!! Issue : " + remoteFile + " ---- " + e.getMessage()));
			e.printStackTrace();
		}
	}

}
