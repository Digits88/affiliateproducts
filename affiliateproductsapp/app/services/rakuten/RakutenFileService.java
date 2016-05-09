package services.rakuten;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import batch.jobs.RakutenCheckDupJob;
import batch.jobs.RakutenDownloadFeedJob;
import batch.jobs.SyncRakutenProductsDetailsDeleteOutOfSync;

import com.google.common.io.Files;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;

import constants.AffiliateConstants;
import constants.RakutenConstants;
import play.Play;
import play.libs.F.Promise;
import services.cj.impl.CJFileService;
import utils.log.Log;

public class RakutenFileService {

	private static Logger logger = Logger.getLogger(RakutenFileService.class);

	private FTPClient ftpClient = null;

	final static String host = Play.configuration.getProperty("cron.job.sync.rakuten.products.ftp.host");
	final static String user = Play.configuration.getProperty("cron.job.sync.rakuten.products.ftp.user");
	final static String pwd = Play.configuration.getProperty("cron.job.sync.rakuten.products.ftp.password");

	final static String rakutenProperties = Play.configuration
			.getProperty("cron.job.sync.rakuten.products.ftp.properties");

	// walmart
	final static String rakutenSuperFeedProperties = Play.configuration
			.getProperty("cron.job.sync.rakuten.super.products.ftp.properties");

	final static String remoteDirPath = Play.configuration
			.getProperty("cron.job.sync.rakuten.products.ftp.path.remote");
	final static String saveDirPath = Play.configuration.getProperty("affiliate.cj.product.feed.input.location");
	final static String unGzipTo = Play.configuration.getProperty("affiliate.cj.product.feed.input.location");
	
	final static String patternPath = Play.configuration.getProperty("cron.job.sync.rakuten.super.pattern.ftp.properties");

	private Map<String, String> colorSKUMap;
	
	private Map<String, String> patternTable;
	
	private static RakutenFileService rakutenFileService = null;;
	
	public static RakutenFileService getInstance() {
		if (rakutenFileService == null) {
			rakutenFileService = new RakutenFileService();
		}
		return rakutenFileService;
	}

	public RakutenFileService() {
		ftpClient = new FTPClient();
		patternTable = getRakutenAvoidDupPatternMapFromProp();
	}

	public RakutenFileService(Map<String, String> colorSKUMap) {
		this.colorSKUMap = colorSKUMap;
		try {
			ftpClient = new FTPClient();
			getFTPConnection(ftpClient);
			logger.info(Log.message("Connected to FTP Server ? " + ftpClient.isConnected()));
			int replyCode = ftpClient.getReplyCode();
			System.out.println(replyCode);
		} catch (Exception e) {
			logger.error(Log.message("Issues in RakutenFileService Constructor : " + e.getMessage()));
			e.printStackTrace();
		}
	}

	private void getFTPConnection(FTPClient ftpClient) {
		try {
			ftpClient.connect(host);
			ftpClient.login(user, pwd);
			this.ftpClient.enterLocalPassiveMode();
			showServerReply(ftpClient);
			logger.info(Log.message("Get FTP Connection !" + ftpClient.isConnected()));
		} catch (Exception e) {
			logger.error(Log.message("Issues in getFTPConnection : " + e.getMessage()));
			e.printStackTrace();
		}
	}

	public void closeFTPConnection() {
		try {
			if (ftpClient != null) {
				ftpClient.logout();
				ftpClient.disconnect();
			}
			logger.info(Log.message("Still Connected FTP Server ?? " + ftpClient.isConnected()));
		} catch (IOException e) {
			logger.error(Log.message("Issues in closeFTPConnection : " + e.getMessage()));
			e.printStackTrace();
		}
	}

	/**
	 * 1. use the properties to get a seller map 2. use the map to go to each
	 * folder to download the files
	 */
	public void downloadRakutenFeed() {

		try {
			// create a seller table based on properties file
			Map<String, Boolean> sellerTable = getRakutenSellserMapFromProp(rakutenProperties);

			// based on sellerTable, download the needed xml files
			for (String seller : sellerTable.keySet()) {
				logger.info(Log
						.message("+++++++++++++++ Start Download Process for Seller : " + seller + " +++++++++++++++"));
				if (!sellerTable.get(seller)) {
					if (!ftpClient.isConnected()) {
						logger.info(Log.message("Reconnect FTP"));
						getFTPConnection(ftpClient);
					}
					downloadFeedXMLProcess(sellerTable, seller);
					sellerTable.put(seller, true);
					logger.info(Log.message(
							"+++++++++++++++ Finish Download Process for Seller : " + seller + " +++++++++++++++"));
				}
			}

			closeFTPConnection();

			File feed = new File(saveDirPath);
			File[] sellerFolders = feed.listFiles();

			for (File sellerFolder : sellerFolders) {
				List<Promise> jobs = new ArrayList<Promise>();
				boolean jobsDone = false;
				String seller = sellerFolder.getName();
				decompressFilesInFolder(new File(saveDirPath + File.separator + seller),
						unGzipTo + File.separator + seller);
				logger.info("@@@ Decompression is done @@@");
				logger.info("Start Check and Remove Duplicates Process...");

				List<File> largeFiles = new ArrayList<File>();
				// add a job to check and remove the duplicates
				File[] filesNeedCheckDup = new File(unGzipTo + File.separator + seller).listFiles();
				for (File f : filesNeedCheckDup) {
					Promise promise = new RakutenCheckDupJob(f, largeFiles, colorSKUMap).now();
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
				for (File f : largeFiles) {
					splitRakutenFeed(f);
				}
				logger.info("......... Finish Split Process .......");
			}
		} catch (Exception e) {
			logger.error(Log.message("Issues in downloadRakutenFeed : " + e.getMessage()));
			e.printStackTrace();
		}
	}

	public Map<String, Boolean> getRakutenSellserMapFromProp(String path) {
		Map<String, Boolean> sellerTable = new Hashtable<String, Boolean>();
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(path);
			prop.load(input);
			for (String key : prop.stringPropertyNames()) {
				/* logger.info(Log.message("Folder: " + key)); */
				System.out.println(key);
				if (!sellerTable.containsKey(key)) {
					sellerTable.put(key, false);
				}
			}
		} catch (IOException e) {
			logger.error(Log.message("Issues in getRakutenSellserMapFromProp : " + e.getMessage()));
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
					input = null;
				} catch (IOException e) {
					logger.error(Log.message("Reading Properties file has issue" + e.getMessage()));
					e.printStackTrace();
				}
			}
		}
		return sellerTable;
	}

	/**
	 * 
	 * @param map
	 *            each string as key looks like this : "3860626567774cmp.xml.gz"
	 * @param subFolder
	 * 
	 *            In Rakuten feed looks like this:
	 *            38606_3259389_26567774_cmp.xml.gz | | | | 0 1 2 3 combine part
	 *            0, 2, 3 and check if it's a key in map
	 */
	public void downloadFeedXMLProcess(Map<String, Boolean> map, String subFolder) {
		try {
			logger.info("Enter downloadFeedXMLProcess ----->");
			ftpClient.changeToParentDirectory();
			logger.info("Changed to ParentDirectory -----> " + ftpClient.getReplyCode());
			boolean isChangedDir = ftpClient.changeWorkingDirectory(subFolder);
			if (isChangedDir) {
				logger.info("Working Directory " + remoteDirPath + File.separator + subFolder);
			} else {
				logger.error(Log.message("Unable to change + " + remoteDirPath + File.separator + subFolder));
			}
			showServerReply(ftpClient);
			FTPFile[] ftpFiles = ftpClient.listFiles();
			logger.info("Number of FTPFiles inside : " + ftpFiles.length);
			logger.info("Ready For loop downloadFeedXMLProcess ----->");
			// record the number of files
			int count = 0;
			for (FTPFile ftpFile : ftpFiles) {
				String neededFileName = ftpFile.getName();
				// logger.info("File Name : " + ftpFile.getName()));
				if (neededFileName.endsWith("_cmp.xml.gz")) {
					// use relative path here !!!???
					String rem = neededFileName;
					String sav = saveDirPath + subFolder + File.separator + neededFileName;
					logger.info("Target File : " + rem);
					// logger.info("Save Path " + sav);
					downloadSingleFile(rem, sav);
					count++;
					logger.info("Number of Downloaded Files is " + count);
					logger.info("------------------------------------------------------------");
				}
			}
		} catch (IOException e) {
			logger.error(Log.message("Issue - Message 		: " + e.getMessage()));
			logger.error(Log.message("Issue - Reply Code 	: " + ftpClient.getReplyCode()));
			logger.error(Log.message("Issue - Reply String 	: " + ftpClient.getReplyString()));
			e.printStackTrace();
		}
	}

	public boolean downloadSingleFile(String remoteFilePath, String savePath) throws IOException {
		OutputStream outputStream = null;
		boolean res = false;
		try {
			logger.info(Log.message("Start downloading --> " + remoteFilePath));
			File downloadFile = new File(savePath);
			logger.info(Log.message("Check if Parent Dir is existing..."));
			File parentDir = downloadFile.getParentFile();
			if (!parentDir.exists()) {
				boolean mkdir = parentDir.mkdir();
				if (mkdir) {
					logger.info(Log.message("Create Parent Dir " + parentDir.getAbsolutePath()));
				} else {
					logger.error(Log.message("Create Parent Dir FAILED!!! " + parentDir.getAbsolutePath()
							+ " Reply Code : " + this.ftpClient.getReplyCode()));
					return false;
				}
			}

			outputStream = new BufferedOutputStream(new FileOutputStream(savePath));

			this.ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			res = this.ftpClient.retrieveFile(remoteFilePath, outputStream);
			if (res) {
				logger.info(Log.message("Successfully downloaded : " + remoteFilePath));
			} else {
				logger.info(Log.message("Unable to downloaded : " + remoteFilePath));
				logger.info(Log.message("Reply Code : " + this.ftpClient.getReplyCode()));
			}
			outputStream.close();
		} catch (Exception ex) {
			logger.error(Log.message("Issue in downloadSingleFile : " + ex.getMessage() + " Reply Code : "
					+ this.ftpClient.getReplyCode()));
			ex.printStackTrace();
		} finally {
			if (outputStream != null) {
				outputStream.close();
				outputStream = null;
			}
		}
		return res;
	}

	public void decompressFilesInFolder(File folder, String unGzipTo) {
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				decompressFilesInFolder(fileEntry, unGzipTo);
			} else {
				if (isValidFileToDeCompress(fileEntry, "gz"))
					unGzip(fileEntry, true, unGzipTo);
				else
					return;
			}
		}
	}

	private static Boolean isValidFileToDeCompress(File inputFile, String fileExtension) {
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

	private static void unGzip(File infile, boolean deleteGzipfileOnSuccess, String unGzipTo) {

		logger.info(Log.message("Start UnZip File"));

		File folder = new File(unGzipTo);
		File outFile = null;
		FileOutputStream fos = null;
		FileInputStream fin = null;
		GZIPInputStream gin = null;
		int len;
		try {
			logger.info(Log.message("infile name : " + infile.getName()));
			fin = new FileInputStream(infile);
			gin = new GZIPInputStream(fin);
			String infileWithoutGZName = infile.getName().replaceAll("\\.gz$", "");
			outFile = new File(folder + "/" + infileWithoutGZName);

			logger.info(Log.message("DeCompressing the file: \"" + outFile.getName() + "\" is Started."));

			fos = new FileOutputStream(outFile);
			byte[] buf = new byte[1024]; // Buffer size is a matter of
											// taste and application...
			while ((len = gin.read(buf)) > 0) {
				fos.write(buf, 0, len);
			}
			gin.close();
			fos.close();
			logger.info(Log.message("@@@@@@ DeCompressing is Done. @@@@@@"));

			if (deleteGzipfileOnSuccess) {
				infile.delete();
			}
		} catch (EOFException e) {
			logger.error(Log.message("Issue in unGzip : " + e.getMessage()));
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(Log.message("Issue in unGzip : " + e.getMessage()));
			e.printStackTrace();
		}
	}

	/**
	 * Clean all the folders
	 */
	public void cleanFiles(File feedFolder) {

		try {
			if (!feedFolder.exists()) {
				logger.error(Log.message("Exiting the process as no such folder exists : " + feedFolder.getName()));
				return;
			}
			FileUtils.cleanDirectory(feedFolder);
		} catch (IOException e) {
			logger.error(Log.message("Errors in cleaning folder :  + " + feedFolder.getName()));
			e.printStackTrace();
		}
		logger.info(Log.message("Cleaned " + feedFolder.getAbsolutePath()));
	}

	public static Map<String, Map<String, Boolean>> readProductDetailsFromProp(String propertiesPath) {
		Map<String, Map<String, Boolean>> productTable = new Hashtable<String, Map<String, Boolean>>();
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(propertiesPath);
			prop.load(input);
			for (String key : prop.stringPropertyNames()) {
				/* logger.info(Log.message("Folder: " + key)); */
				Map<String, Boolean> needDownloadFiles = new Hashtable<String, Boolean>();
				String files = prop.getProperty(key);
				System.out.println(files);
				StringTokenizer st = new StringTokenizer(files, ",");
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					// logger.info(Log.message(token));
					needDownloadFiles.put(token, false);
				}
				productTable.put(key, needDownloadFiles);
			}
		} catch (IOException e) {
			logger.error(Log.message("Issues in readProductDetailsFromProp : " + e.getMessage()));
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
					input = null;
				} catch (IOException e) {
					logger.error(Log.message("Reading Properties file has issue" + e.getMessage()));
					e.printStackTrace();
				}
			}
		}
		return productTable;
	}

	/**
	 * Split each big file
	 */
	public static void splitRakutenFeed(File targetFile) {
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		BufferedWriter out = null;
		try {
			logger.info(Log.message("Split -- " + targetFile.getAbsolutePath() + " is Start"));

			// Get the total Line number of file
			FileInputStream fs_countLine = new FileInputStream(targetFile);
			DataInputStream in_countLine = new DataInputStream(fs_countLine);
			BufferedReader br_countLine = new BufferedReader(new InputStreamReader(in_countLine));

			String titleBar = br_countLine.readLine();
			String tempLine = "";
			String tailLine = "";
			int count = 0;
			while ((tempLine = br_countLine.readLine()) != null) {
				tailLine = tempLine;
				count++;
			}
			logger.info(Log.message("Tail Line : " + tailLine));
			br_countLine.close();
			in_countLine.close();
			fs_countLine.close();

			double nol = 5000;

			// Log number of lines in the input file.
			logger.info(Log.message("Lines in the file: " + count));

			double temp = (count / nol);
			int temp1 = (int) temp;
			int nof = 0;
			if (temp1 == temp) {
				nof = temp1;
			} else {
				nof = temp1 + 1;
			}
			// Log number of files to be generated
			logger.info(Log.message("No. of files to be generated :" + nof));

			// ---------------------------------------------------------------------------------------------------------

			// Actual splitting of file into smaller files
			fstream = new FileInputStream(targetFile);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));

			String spliteFileName = targetFile.getAbsolutePath().replaceAll("\\.xml$", "_");

			String strLine = null;

			boolean checkReadDone = false;

			for (int j = 1; j <= nof; j++) {

				if (checkReadDone) {
					break;
				}

				FileWriter fstream1 = new FileWriter(spliteFileName + j + ".xml"); // Destination
																					// File
																					// Location

				int bufferSize = 8 * 1024;

				out = new BufferedWriter(fstream1, bufferSize);

				out.write(titleBar);
				// out.write(headerLine);
				out.newLine();

				for (int i = 1; i <= nol; i++) {
					strLine = br.readLine();
					if (strLine != null) {
						if (strLine.contains("sku_number=")) {
							out.write(strLine);
							out.flush();
							out.newLine();
						}
					} else {
						checkReadDone = true;
						break;
					}
				}
				out.write(tailLine);
				out.flush();
				out.close();
			}

			logger.info(Log.message("Split -- " + targetFile.getName() + " is done"));
			// logger.info(Log.message("Split -- " + targetFile.getName() +
			// " is done"));
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
			logger.error(Log.message("Issue in split : " + targetFile.getName()));
			logger.error(Log.message("Issue in split : " + e.getMessage()));
		} finally {
			try {
				if (out != null) {
					out.close();
					out = null;
				}
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

	private void showServerReply(FTPClient ftpClient) {
		String[] replies = ftpClient.getReplyStrings();
		if (replies != null && replies.length > 0) {
			for (String aReply : replies) {
				logger.info(Log.message("SERVER: " + aReply));
			}
		}
	}

	// walmart
	public void fetchSuperProductFeed() {
		try {
			// create a seller table based on properties file
			Map<String, Boolean> sellerTable = getRakutenSellserMapFromProp(rakutenSuperFeedProperties);

			// based on sellerTable, download the needed xml files
			for (String seller : sellerTable.keySet()) {
				logger.info("+++++++++++++++ Start Download Process for Seller : " + seller + " +++++++++++++++");
				if (!sellerTable.get(seller)) {
					if (!ftpClient.isConnected()) {
						logger.info("Connect FTP");
						getFTPConnection(ftpClient);
					}
					// downloadSuperFeedProcess(sellerTable, seller);
					downloadFeedXMLProcess(sellerTable, seller);
					sellerTable.put(seller, true);
					logger.info("+++++++++++++++ Finish Download Process for Seller : " + seller + " +++++++++++++++");
				}
			}
			closeFTPConnection();
		} catch (Exception e) {
			logger.error(Log.message("Issues in downloadRakutenFeed : " + e.getMessage()));
			e.printStackTrace();
		}
	}

	public void splitSuperFeedBasedOnCategory(File targetFile, String pattern) {
		try {
			if (targetFile == null || targetFile.length() == 0) {
				logger.error(Log.message("Invalid seller folder, Please check first !!"));
				return;
			}
			List<File> largeFiles = new ArrayList<File>();
			unGzipForSuper(targetFile, targetFile.getParent(), true, largeFiles);
			logger.info("@@@ Decompression is done @@@");
			logger.info("Start Check and Remove Duplicates Process...");

			logger.info("......... Start Split Process ........");
			for (File f : largeFiles) {
				splitWalmartFeed(f, pattern);
			}
			logger.info("......... Finish Split Process .......");
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
			e.printStackTrace();
		}
	}
	
	
	
	public void splitSuperFeedBasedOnCategoryNew(File targetFile) {
		try {
			if (targetFile == null || targetFile.length() == 0) {
				logger.error(Log.message("Invalid seller folder, Please check first !!"));
				return;
			}
			List<File> largeFiles = new ArrayList<File>();
			for (File eachFile : targetFile.listFiles()) {
				unGzipForSuper(eachFile, eachFile.getParent(), true, largeFiles);
			}
			logger.info("@@@ Decompression is done @@@");
			logger.info("Start Check and Remove Duplicates Process...");
			
			logger.info("......... Start Split Process ........");
			for (File f : largeFiles) {
				String pattern = getPattern(f.getName(), patternTable);
				splitWalmartFeed(f, pattern);
			}
			logger.info("......... Finish Split Process .......");
		} catch (Exception e) {
			logger.error(Log.message(e.getMessage()));
			e.printStackTrace();
		}
	}
	
	private String getPattern(String fileName, Map<String, String> patternTable) {
		String category = fileName.split(RakutenConstants.CATEGORY_NAME_UNDERLINE)[2];
		if(patternTable.get(category) != null) {
			logger.info("---> " + fileName + " --- will be splited by Product Name");
			return RakutenConstants.PRODUCT_PATTERN_FOR_NAME;
		} else {
			logger.info("---> " + fileName + " --- will be splited by Image URL");
			return RakutenConstants.IMAGEURL_PATTERN;
		}
	}

	public void unGzipForSuper(File infile, String unGzipTo, boolean deleteGzipfileOnSuccess, List<File> largeFiles) {

		logger.info(Log.message("Start UnZip File"));

		File folder = new File(unGzipTo);
		File outFile = null;
		FileOutputStream fos = null;
		FileInputStream fin = null;
		GZIPInputStream gin = null;
		int len;
		try {
			logger.info(Log.message("infile name : " + infile.getName()));
			fin = new FileInputStream(infile);
			gin = new GZIPInputStream(fin);
			String infileWithoutGZName = infile.getName().replaceAll("\\.gz$", "");
			outFile = new File(folder + "/" + infileWithoutGZName);

			logger.info(Log.message("DeCompressing the file: \"" + outFile.getName() + "\" is Started."));

			fos = new FileOutputStream(outFile);
			byte[] buf = new byte[1024]; // Buffer size is a matter of
											// taste and application...
			while ((len = gin.read(buf)) > 0) {
				fos.write(buf, 0, len);
			}
			gin.close();
			fos.close();
			logger.info(Log.message("@@@@@@ DeCompressing is Done. @@@@@@"));

			if ((outFile.length() / 1024) > 3500) {
				largeFiles.add(outFile);
			}
			
			if (deleteGzipfileOnSuccess) {
				infile.delete();
			}
		} catch (EOFException e) {
			logger.error(Log.message("Issue in unGzip : " + e.getMessage()));
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(Log.message("Issue in unGzip : " + e.getMessage()));
			e.printStackTrace();
		}
	}
	// walmart
	private boolean downloadSuperFeedProcess(Map<String, Boolean> sellerTable, String seller) {
		try {
			logger.info("Enter Download Super FeedProcess ----->");
			
			File sellerFolder = new File(saveDirPath.concat(seller));
			if(!sellerFolder.exists()) {
				boolean mkdir = sellerFolder.mkdir();
				if (mkdir) {
					logger.info(Log.message("Create Seller Folder " + sellerFolder.getAbsolutePath()));
				} else {
					logger.error(Log.message("Create Seller Folder Dir is FAILED!!! " + sellerFolder.getAbsolutePath()
							+ " Reply Code : " + this.ftpClient.getReplyCode()));
					return false;
				}
			}
			
			ftpClient.changeToParentDirectory();
			boolean isChangedDir = ftpClient.changeWorkingDirectory(seller);
			if (isChangedDir) {
				logger.info("Working Directory " + remoteDirPath + File.separator + seller);
			} else {
				logger.error(Log.message("Unable to change + " + remoteDirPath + File.separator + seller));
				return false;
			}
			showServerReply(ftpClient);
			FTPFile[] ftpFiles = ftpClient.listFiles();
			logger.info("Number of FTPFiles inside : " + ftpFiles.length);
			logger.info("Ready For loop Download Super Feed Process ----->");
			// record the number of files
			int count = 0;
			for (FTPFile ftpFile : ftpFiles) {
				String neededFileName = ftpFile.getName();
				// logger.info("File Name : " + ftpFile.getName()));
				if (neededFileName.endsWith("_cmp.xml.gz")) {
					// use relative path here !!!???
					String subFolder = neededFileName.replaceAll("_cmp.xml.gz$", "");
					// /appl/sywafl_prod/feed/2149/2149_xxxxxx_xxxxxx/2149_xxxxxx_xxxxxx_cmp.xml.gz
					String savePath = saveDirPath.concat(seller).concat(File.separator).concat(subFolder);
					File parentDir = new File(savePath);
					if (!parentDir.exists()) {
						boolean mkdir = parentDir.mkdir();
						if (mkdir) {
							logger.info(Log.message("Create Parent Dir " + parentDir.getAbsolutePath()));
						} else {
							logger.error(Log.message("Create Parent Dir FAILED!!! " + parentDir.getAbsolutePath()
									+ " Reply Code : " + this.ftpClient.getReplyCode()));
							return false;
						}
					}
					
					savePath = savePath.concat(File.separator).concat(neededFileName);
					
					logger.info("Target File : " + neededFileName);
					logger.info("Save Path " + savePath);
					if (!downloadSingleFile(neededFileName, savePath)) {
						return false;
					}
					count++;
					logger.info("Number of Downloaded Files is " + count);
					logger.info("------------------------------------------------------------");
				}
			}
		} catch (IOException e) {
			logger.error(Log.message(e.getMessage()));
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * New split function for Super feed (like: Walmart)
	 */
	public void splitWalmartFeed(File targetFile, String pattern) {
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		BufferedWriter out = null;
		Set<String> set = null;
		DOMParser dp = null;
		InputSource is = null;
		try {
			logger.info(Log.message("Split -- " + targetFile.getAbsolutePath() + " is Start"));

			// Get the total Line number of file
			FileInputStream fs_countLine = new FileInputStream(targetFile);
			DataInputStream in_countLine = new DataInputStream(fs_countLine);
			BufferedReader br_countLine = new BufferedReader(new InputStreamReader(in_countLine));

			String titleBar = br_countLine.readLine();
			String tempLine = "";
			String tailLine = "";
			int count = 0;
			while ((tempLine = br_countLine.readLine()) != null) {
				tailLine = tempLine;
				count++;
			}
			logger.info(Log.message("Tail Line : " + tailLine));
			br_countLine.close();
			in_countLine.close();
			fs_countLine.close();

			double nol = 5000;

			// Log number of lines in the input file.
			logger.info(Log.message("Lines in the file: " + count));

			double temp = (count / nol);
			int temp1 = (int) temp;
			int nof = 0;
			if (temp1 == temp) {
				nof = temp1;
			} else {
				nof = temp1 + 1;
			}
			// Log number of files to be generated
			logger.info(Log.message("No. of files to be generated :" + nof));

			// ---------------------------------------------------------------------------------------------------------

			// Actual splitting of file into smaller files
			fstream = new FileInputStream(targetFile);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));

			String spliteFileName = targetFile.getAbsolutePath().replaceAll("\\.xml$", "_");

			String strLine = null;

			boolean checkReadDone = false;
			
			set = new HashSet<String>();
			dp = new DOMParser();

			for (int j = 1; j <= nof; j++) {

				if (checkReadDone) {
					break;
				}

				FileWriter fstream1 = new FileWriter(spliteFileName + j + ".xml"); // Destination File Location
				int bufferSize = 8 * 1024;
				out = new BufferedWriter(fstream1, bufferSize);

				out.write(titleBar);
				out.newLine();

				for (int i = 1; i <= nol; i++) {
					strLine = br.readLine();
					if (strLine != null) {
						if(strLine.equals(titleBar) || strLine.equals(tailLine)) {
							continue;
						}
						is = new InputSource(new StringReader(strLine));
						String uniqueKey = getKeyForAvoidDuplicates(dp, is, pattern);
						if (!set.contains(uniqueKey)) {
							out.write(strLine);
							out.flush();
							out.newLine();
							set.add(uniqueKey);
						}
					} else {
						checkReadDone = true;
						break;
					}
				}
				out.write(tailLine);
				out.flush();
				out.close();
			}

			logger.info(Log.message("Split -- " + targetFile.getName() + " is done"));
			// logger.info(Log.message("Split -- " + targetFile.getName() +
			// " is done"));
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
			logger.error(Log.message("Issue in split : " + targetFile.getName()));
			logger.error(Log.message("Issue in split : " + e.getMessage()));
		} finally {
			try {
				set = null;
				dp = null;
				is = null;
				
				if (out != null) {
					out.close();
					out = null;
				}
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
	
	private String getKeyForAvoidDuplicates(DOMParser dp, InputSource is, String pattern) {		
		String key = "";
		try {
			dp.parse(is);
			Document doc = dp.getDocument();
			NodeList nl = doc.getElementsByTagName(pattern);
			if(pattern.equals(RakutenConstants.PRODUCT_PATTERN_FOR_NAME)) {
				key = nl.item(0).getAttributes().getNamedItem(RakutenConstants.NAME_PATTERN).getFirstChild().getTextContent();
			} else {
				key = nl.item(0).getTextContent();
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return key;
	}
	
	public Map<String, String> getRakutenAvoidDupPatternMapFromProp() {
		Map<String, String> patternTable = new Hashtable<String, String>();
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(patternPath);
			prop.load(input);
			for (String key : prop.stringPropertyNames()) {
				logger.info(Log.message(key + " - " + prop.getProperty(key)));
				if (!patternTable.containsKey(key)) {
					patternTable.put(key, prop.getProperty(key));
				}
			}
		} catch (IOException e) {
			logger.error(Log.message(e.getMessage()));
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
					input = null;
				} catch (IOException e) {
					logger.error(Log.message(e.getMessage()));
					e.printStackTrace();
				}
			}
		}
		return patternTable;
	}
		
	public void splitRakutenFeedByName(File targetFile) {
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		BufferedWriter out = null;
		Set<String> set = null;
		DOMParser dp = null;
		InputSource is = null;
		try {
			logger.info(Log.message("Split -- " + targetFile.getAbsolutePath() + " is Start"));

			// Get the total Line number of file
			FileInputStream fs_countLine = new FileInputStream(targetFile);
			DataInputStream in_countLine = new DataInputStream(fs_countLine);
			BufferedReader br_countLine = new BufferedReader(new InputStreamReader(in_countLine));

			String titleBar = br_countLine.readLine();
			String tempLine = "";
			String tailLine = "";
			int count = 0;
			while ((tempLine = br_countLine.readLine()) != null) {
				tailLine = tempLine;
				count++;
			}
			logger.info(Log.message("Tail Line : " + tailLine));
			br_countLine.close();
			in_countLine.close();
			fs_countLine.close();

			double nol = 5000;

			// Log number of lines in the input file.
			logger.info(Log.message("Lines in the file: " + count));

			double temp = (count / nol);
			int temp1 = (int) temp;
			int nof = 0;
			if (temp1 == temp) {
				nof = temp1;
			} else {
				nof = temp1 + 1;
			}
			// Log number of files to be generated
			logger.info(Log.message("No. of files to be generated :" + nof));

			// ---------------------------------------------------------------------------------------------------------

			// Actual splitting of file into smaller files
			fstream = new FileInputStream(targetFile);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));

			String spliteFileName = targetFile.getAbsolutePath().replaceAll("\\.xml$", "_");

			String strLine = null;

			boolean checkReadDone = false;
			
			set = new HashSet<String>();
			dp = new DOMParser();

			for (int j = 1; j <= nof; j++) {

				if (checkReadDone) {
					break;
				}

				FileWriter fstream1 = new FileWriter(spliteFileName + j + ".xml"); // Destination File Location
				int bufferSize = 8 * 1024;
				out = new BufferedWriter(fstream1, bufferSize);

				out.write(titleBar);
				out.newLine();

				for (int i = 1; i <= nol; i++) {
					strLine = br.readLine();
					if (strLine != null) {
						if(strLine.equals(titleBar) || strLine.equals(tailLine)) {
							continue;
						}
						is = new InputSource(new StringReader(strLine));
						String uniqueKey = getKeyForAvoidDuplicates(dp, is, RakutenConstants.PRODUCT_PATTERN_FOR_NAME);
						if (!set.contains(uniqueKey)) {
							out.write(strLine);
							out.flush();
							out.newLine();
							set.add(uniqueKey);
						}
					} else {
						checkReadDone = true;
						break;
					}
				}
				out.write(tailLine);
				out.flush();
				out.close();
			}

			logger.info(Log.message("Split -- " + targetFile.getName() + " is done"));
			// logger.info(Log.message("Split -- " + targetFile.getName() +
			// " is done"));
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
			logger.error(Log.message("Issue in split : " + targetFile.getName()));
			logger.error(Log.message("Issue in split : " + e.getMessage()));
		} finally {
			try {
				set = null;
				dp = null;
				is = null;
				
				if (out != null) {
					out.close();
					out = null;
				}
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
	
}
