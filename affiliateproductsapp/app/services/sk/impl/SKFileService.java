package services.sk.impl;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.io.CopyStreamAdapter;
import org.apache.log4j.Logger;

import batch.jobs.SyncRakutenProductsDetails;

import com.google.common.io.Files;

import play.Play;
import play.db.jpa.JPA;
import services.FileService;
import utils.log.Log;

public class SKFileService extends FileService {

	private static Logger logger = Logger.getLogger(SKFileService.class);

	final static String host = Play.configuration.getProperty("cron.job.sync.sears.kmart.ftp.host");
	final static String user = Play.configuration.getProperty("cron.job.sync.sears.kmart.ftp.user");
	final static String pwd = Play.configuration.getProperty("cron.job.sync.sears.kmart.ftp.password");

	final static String remoteSearsPath = Play.configuration.getProperty("cron.job.sync.sears.kmart.ftp.remotesears");

	final static String remoteKmartPath = Play.configuration.getProperty("cron.job.sync.sears.kmart.ftp.remotekmart");

	final static String saveDirPath = Play.configuration.getProperty("affiliate.cj.product.feed.input.location");
	final static String unGzipTo = Play.configuration.getProperty("affiliate.cj.product.feed.input.location");

	private FTPSClient ftpClient;

	// Get connection
	private boolean getConnection() {
		boolean connected = false;
		int reply;
		try {
			ftpClient = new FTPSClient("SSL");
			ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
			ftpClient.connect(host);
			reply = ftpClient.getReplyCode();
			if (FTPReply.isPositiveCompletion(reply)) {
				logger.info("FTP request login as " + user);
				if (ftpClient.login(user, pwd)) {
					connected = true;
					logger.info("Login successful");
				}
			} else {
				ftpClient.disconnect();
				logger.error(
						Log.message("Issue in getConnection : Disconnected : Reply Code " + ftpClient.getReplyCode()));
			}
		} catch (Exception e) {
			logger.error(Log.message("Issue in getConnection : Reply Code " + e.getMessage()));
			e.printStackTrace();
		}
		return connected;
	}

	// Close connection
	private void closeFTPConnection() {
		try {
			if (ftpClient != null) {
				ftpClient.logout();
				ftpClient.disconnect();
			}
			logger.info("Still Connected FTP Server ?? " + ftpClient.isConnected());
		} catch (IOException e) {
			logger.error(Log.message("Issues in closeFTPConnection : " + e.getMessage()));
			e.printStackTrace();
		}
	}

	// Download feeds
	private boolean downloadFeed(String remoteDirPath) {

		boolean res = false;
		FileOutputStream fos = null;
		try {
			if (!ftpClient.isConnected()) {
				logger.info("Reconnect the FTP connection " + ftpClient.getReplyCode());
				getConnection();
			}
			File downloadFile = new File(saveDirPath);
			File parentFile = downloadFile.getParentFile();
			if (!parentFile.exists()) {
				parentFile.mkdir();
			}
			// ftpClient.setCopyStreamListener(streamListener);
			ftpClient.changeWorkingDirectory(remoteDirPath);
			ftpClient.execPROT("P");
			ftpClient.setBufferSize(1024 * 1024 * 30);
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			ftpClient.enterLocalPassiveMode();
			long size = 1;
			for (FTPFile ff : ftpClient.listFiles()) {
				if (ff.getName().equals(remoteDirPath)) {
					size = ff.getSize();
				}
			}
			final long sizes = size;
			logger.info(remoteDirPath + " size is " + size);
			fos = new FileOutputStream(saveDirPath + File.separator + remoteDirPath);
			CountingOutputStream cos = new CountingOutputStream(fos) {
				protected void beforeWrite(int n) {
					super.beforeWrite(n);
					logger.info("Downloaded " + getCount() + "/" + sizes);
				}
			};
			res = ftpClient.retrieveFile(remoteDirPath, cos);
			cos.close();
			if (!ftpClient.completePendingCommand()) {
				ftpClient.logout();
				ftpClient.disconnect();
				logger.error(Log.message("File transfer failed." + ftpClient.getReplyCode()));
			}
			if (res) {
				logger.info("Downloaded " + remoteDirPath);
			} else {
				logger.error(Log.message("Download is failed !!" + ftpClient.getReplyCode()));
			}
		} catch (Exception e) {
			logger.error(Log.message("Issues in downloadFeed " + remoteDirPath) + "Reply Code : "
					+ ftpClient.getReplyCode());
			logger.error(Log.message("Issues in downloadFeed " + e.getMessage()));
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
					fos = null;
				}
			} catch (IOException e) {
				logger.error(Log.message("Issues in downloadFeed : Closing FileOutputStream : " + e.getMessage()));
				e.printStackTrace();
			}
		}
		return res;
	}

	/**
	 * UnZip Files Functions are listed below
	 */
	private void decompressFilesInFolder(File folder, String unGzipTo, List<File> list) {
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				decompressFilesInFolder(fileEntry, unGzipTo, list);
			} else {
				if (isValidFileToDeCompress(fileEntry, "gz"))
					unGzip(fileEntry, true, unGzipTo, list);
				else
					return;
			}
		}
	}

	private boolean isValidFileToDeCompress(File inputFile, String fileExtension) {
		if (inputFile.isFile()) {
			// System.out.println(Files.getFileExtension(inputFile.getAbsolutePath().toString()));
			if (Files.getFileExtension(inputFile.getAbsolutePath().toString()).equals(fileExtension)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private void unGzip(File infile, boolean deleteGzipfileOnSuccess, String unGzipTo, List<File> list) {

		logger.info("Start UnZip File");

		String subFolder;
		if (infile.getName().equalsIgnoreCase("socfeedKM.txt.gz")) {
			subFolder = "Kmart";
		} else {
			subFolder = "Sears";
		}

		File folder = new File(unGzipTo);
		GZIPInputStream gin = null;
		File outFile = null;
		FileOutputStream fos = null;
		int len;
		try {
			gin = new GZIPInputStream(new FileInputStream(infile));
			String infileWithoutGZName = infile.getName().replaceAll("\\.gz$", "");
			outFile = new File(folder + "/" + subFolder);

			outFile.mkdir();

			outFile = new File(folder + "/" + subFolder + "/" + infileWithoutGZName);

			logger.info("DeCompressing the file: \"" + outFile.getName() + "\" is Started.");

			fos = new FileOutputStream(outFile);
			byte[] buf = new byte[1024]; // Buffer size is a matter of
											// taste and application...
			while ((len = gin.read(buf)) > 0) {
				fos.write(buf, 0, len);
			}
			gin.close();
			fos.close();
			logger.info("@@@@@@ DeCompressing is Done. @@@@@@");

			if (deleteGzipfileOnSuccess) {
				infile.delete();
			}

			list.add(outFile);

			logger.info("How Many files need to split ?? " + list.size());

		} catch (IOException e) {
			logger.error(Log.message("Issue in unGzip : " + e.getMessage()));
			e.printStackTrace();
		}
	}

	// Download SK Feed -- download, decompress, split
	public void downloadSKFeeds() {

		try {
			// Clean the folder first
			cleanFolder(new File(saveDirPath));

			// download
			logger.info("Opening FTPS connection ...");
			boolean connected = getConnection();
			if (connected) {
				logger.info("Awesome, now let's continue downloading feeds");

				// Download the feed
				logger.info("downloading Sears feed ...");
				downloadFeed(remoteSearsPath);
				closeFTPConnection();
				getConnection();
				logger.info("downloading Kmart feed ...");
				downloadFeed(remoteKmartPath);

				// close connection
				logger.info("Closing FTPS connection ...");
				closeFTPConnection();
			} else {
				logger.error(Log.message("Issue in downloadSKFeeds : Cannot make connection !!! "));
				return;
			}

			List<File> list = new ArrayList<File>();
			// decompress
			decompressFilesInFolder(new File(saveDirPath), saveDirPath, list);
			// split
			for (File f : list) {
				splitForSKFeed(f);
			}
			//
		} catch (Exception e) {
			logger.error(Log.message("Issue in downloadSKFeeds : " + e.getMessage()));
			e.printStackTrace();
		}
	}

	// Split SK feed
	private void splitForSKFeed(File targetFile) {
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		try {
			System.out.println("Split -- " + targetFile.getName() + " is Start");

			double nol = 5000.0;

			// Get the total Line number of file
			FileInputStream fs_countLine = new FileInputStream(targetFile);
			DataInputStream in_countLine = new DataInputStream(fs_countLine);
			BufferedReader br_countLine = new BufferedReader(new InputStreamReader(in_countLine));

			int count = 0;
			while (br_countLine.readLine() != null) {
				count++;
			}
			br_countLine.close();
			in_countLine.close();
			fs_countLine.close();

			// Log number of lines in the input file.
			System.out.println("Lines in the file: " + count);
			// logger.info(Log.message("Lines in the file: " + count));

			double temp = (count / nol);
			int temp1 = (int) temp;
			int nof = 0;
			if (temp1 == temp) {
				nof = temp1;
			} else {
				nof = temp1 + 1;
			}
			// Log number of files to be generated
			System.out.println("No. of files to be generated :" + nof);
			// logger.info(Log.message("No. of files to be generated :" + nof));

			// ---------------------------------------------------------------------------------------------------------

			// Actual splitting of file into smaller files

			fstream = new FileInputStream(targetFile);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));

			String spliteFileName = targetFile.getAbsolutePath().replaceAll("\\.txt$", "_");

			String strLine = null;

			boolean allLinesAreRead = false;

			// Use a set to help to avoid duplicates
			Set<String> set = new HashSet<String>();

			for (int j = 1; j <= nof; j++) {
				if (allLinesAreRead) {
					break;
				}
				FileWriter fstream1 = new FileWriter(spliteFileName + j + ".txt"); // Destination
																					// File
																					// Location
				BufferedWriter out = new BufferedWriter(fstream1);
				for (int i = 1; i <= nol; i++) {
					strLine = br.readLine();
					if (strLine != null) {
						String[] strList = strLine.split("\\|");
						if (!set.contains(strList[17])) {
							set.add(strList[17]);
							out.write(strLine);
							out.newLine();
						}
					} else {
						allLinesAreRead = true;
						break;
					}
				}
				out.close();
			}

			System.out.println("Split -- " + targetFile.getName() + " is done");
			// logger.info(Log.message("Split -- " + targetFile.getName() +
			// " is done"));
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			// logger.error(Log.message("COULD NOT download the file: " +
			// targetFile.getName()));
		} finally {
			try {
				if (br != null) {
					br.close();
					br = null;
				}
				if (in != null) {
					in.close();
					in = null;
				}
				if (fstream != null) {
					fstream.close();
					fstream = null;
				}
				if (targetFile.exists()) {
					targetFile.delete();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * This Class will be used for Sears/Kmart's file service
	 */

	/**
	 * Remove the duplicates from Original file
	 */
	// Used for BR Feed
	private File removeDuplicatesForSKFeeds(File targetFile) {
		System.out.println(targetFile.getName());
		File tempFile = null;

		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {

			String tempPath = targetFile.getAbsolutePath().replaceAll("\\.txt$", "_");
			tempFile = new File(tempPath + "NoDup.txt");

			reader = new BufferedReader(new FileReader(targetFile));
			writer = new BufferedWriter(new FileWriter(tempFile));

			Set<String> set = new HashSet<String>();
			// Add the title bar into the new file
			String currentLine = null;
			while ((currentLine = reader.readLine()) != null) {
				if (currentLine.length() > 0) {
					String[] strList = currentLine.split("\\|");
					if (!set.contains(strList[17])) {
						set.add(strList[17]);
						writer.write(currentLine);
						writer.newLine();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
					writer = null;
				}
				if (reader != null) {
					reader.close();
					reader = null;
				}

				System.out.println("Remove Dup is done.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return tempFile;
	}

	public void cleanFolder(File folder) {
		try {
			if (folder.exists()) {
				logger.info("Start empty the folder : " + folder.getAbsolutePath());
				FileUtils.cleanDirectory(folder);
			}
			logger.info("Finish empty the folder : " + folder.getAbsolutePath());
		} catch (IOException e) {
			logger.error(Log.message("Issues in cleanFolder :  " + e.getMessage()));
			e.printStackTrace();
		}
	}

}