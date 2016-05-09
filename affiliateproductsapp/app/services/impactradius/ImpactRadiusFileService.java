package services.impactradius;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import com.google.common.io.Files;

import constants.ImpactRadiusConstants;
import play.Play;
import utils.log.Log;

public class ImpactRadiusFileService {

	private static Logger logger = Logger.getLogger(ImpactRadiusFileService.class);

	final static String host = Play.configuration.getProperty("cron.job.sync.impactradius.products.ftp.host");
	final static String user = Play.configuration.getProperty("cron.job.sync.impactradius.products.ftp.user");
	final static String pwd = Play.configuration.getProperty("cron.job.sync.impactradius.products.ftp.password");

	final static String remoteDirPath = Play.configuration
			.getProperty("cron.job.sync.impactradius.products.ftp.path.remote");
	final static String saveDirPath = Play.configuration.getProperty("affiliate.cj.product.feed.input.location");
	final static String unGzipTo = Play.configuration.getProperty("affiliate.cj.product.feed.input.location");

	final static String impactRadiusProductPropertiesPath = Play.configuration
			.getProperty("cron.job.sync.impactradius.products.ftp.properties");

	private FTPClient ftpClient = null;
	// private Map<String, String> impactRadiusSellerTable;

	public ImpactRadiusFileService() {
		ftpClient = new FTPClient();
	}

	private boolean getFTPConnection() {
		boolean connected = false;
		try {
			ftpClient.connect(host);
			ftpClient.login(user, pwd);
			this.ftpClient.enterLocalPassiveMode();
			showServerReply(ftpClient);
			logger.info(Log.message("Get FTP Connection !" + ftpClient.isConnected()));
			connected = true;
		} catch (Exception e) {
			logger.error(Log.message("Issues in getFTPConnection : " + e.getMessage()));
			e.printStackTrace();
		}
		return connected;
	}

	private boolean closeFTPConnection() {
		boolean closed = false;
		try {
			if (ftpClient != null) {
				ftpClient.logout();
				ftpClient.disconnect();
				closed = true;
			}
			logger.info(Log.message("### Still Connected FTP Server ?? ###" + ftpClient.isConnected()));
		} catch (Exception e) {
			logger.error(Log.message(
					"### Issues in closeFTPConnection : " + ftpClient.getReplyCode() + " ### " + e.getMessage()));
			e.printStackTrace();
		}
		return closed;
	}

	public void fetchImpactRadiusFeeds(Map<String, String> impactRadiusSellerTable) {

		try {
			// get connection:
			boolean connected = getFTPConnection();
			if (!connected) {
				logger.error("### Cannot get FTP Connection, Please Try Again Later ### " + ftpClient.getReplyCode());
			} else {
				logger.info("Connected to FTP Server ? " + ftpClient.isConnected());
				int replyCode = ftpClient.getReplyCode();
				logger.info("Connected Reply Code : " + replyCode);
			}

			for (String seller : impactRadiusSellerTable.keySet()) {
				// Download
				logger.info("+++ Start Download Process for Seller : " + seller + " +++");
				if (!ftpClient.isConnected()) {
					logger.info("Reconnect FTP");
					getFTPConnection();
				}
				downloadImpactRadiusFeed(seller);
				logger.info("+++ Finish Download Process for Seller : " + seller + " +++");
			}
			closeFTPConnection();
			logger.info("--> Closed Connection <--");

			// unzip
			File feed = new File(saveDirPath);
			File[] sellerFolders = feed.listFiles();
			for (File sellerFolder : sellerFolders) {
				String seller = sellerFolder.getName();
				List<File> largeFiles = new ArrayList<File>();
				decompressFilesInFolder(new File(saveDirPath + File.separator + seller),
						unGzipTo + File.separator + seller, largeFiles);
				logger.info("+++ DeCompressing " + seller + " is done +++");

				// split
				logger.info("+++ Spliting " + seller + " is Started +++");
				for (File f : largeFiles) {
					if (seller.trim().equals(ImpactRadiusConstants.DIAPERSCOM_FOLDER)) {
						splitDiapersFeed(f);
					} else if (seller.trim().equals(ImpactRadiusConstants.SOAPCOM_FOLDER)) {
						splitFeedByProductNameColor(f);
					} else if (seller.trim().equals(ImpactRadiusConstants.BEAUTYBARCOM_FOLDER)
							|| seller.trim().equals(ImpactRadiusConstants.WAGCOM_FOLDER)
							|| seller.trim().equals(ImpactRadiusConstants.YOYO_FOLDER)) {
						splitFeedByProductName(f);
					} else if (seller.trim().equals(ImpactRadiusConstants.AFTERSCHOOL_FOLDER)) {
						splitFeedByParentNumber(f);
					} else {
						splitFeedByAvailability(f);
					}
				}
			}

		} catch (Exception e) {
			logger.error(Log.message("### Issue ### " + e.getMessage()));
			e.printStackTrace();
		}
	}

	private void decompressFilesInFolder(File folder, String unGzipTo, List<File> largeFiles) {
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				decompressFilesInFolder(fileEntry, unGzipTo, largeFiles);
			} else {
				if (isValidFileToDeCompress(fileEntry, "gz"))
					unGzip(fileEntry, true, unGzipTo, largeFiles);
				else
					return;
			}
		}
	}

	private boolean isValidFileToDeCompress(File inputFile, String fileExtension) {
		if (inputFile.isFile()) {
			if (Files.getFileExtension(inputFile.getAbsolutePath().toString()).equals(fileExtension)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private void unGzip(File infile, boolean deleteGzipfileOnSuccess, String unGzipTo, List<File> largeFiles) {
		File folder = new File(unGzipTo);
		File outFile = null;
		FileOutputStream fos = null;
		FileInputStream fin = null;
		GZIPInputStream gin = null;
		int len;
		try {
			fin = new FileInputStream(infile);
			gin = new GZIPInputStream(fin);
			String infileWithoutGZName = infile.getName().replaceAll("\\.gz$", "");
			outFile = new File(folder + "/" + infileWithoutGZName);
			logger.info(Log.message("+++ DeCompressing : " + infile.getName() + " is Started +++"));
			fos = new FileOutputStream(outFile);
			byte[] buf = new byte[1024]; // Buffer size is a matter of
											// taste and application...
			while ((len = gin.read(buf)) > 0) {
				fos.write(buf, 0, len);
			}
			gin.close();
			fos.close();
			logger.info(Log.message("+++ DeCompressing : " + infile.getName() + " is Done. +++"));

			if (deleteGzipfileOnSuccess) {
				infile.delete();
			}
			if ((outFile.length() / 1024) > 3500) {
				largeFiles.add(outFile);
			}
			logger.info(Log.message("+++ How Many files need to split ?? " + largeFiles.size()));
		} catch (EOFException e) {
			logger.error(Log.message("### Issue in unGzip : " + e.getMessage()));
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(Log.message("### Issue in unGzip : " + e.getMessage()));
			e.printStackTrace();
		}
	}

	private void downloadImpactRadiusFeed(String seller) {
		try {
			logger.info(Log.message("+++ Enter downloadImpactRadiusFeed +++"));
			ftpClient.changeToParentDirectory();
			boolean isChangedDir = ftpClient.changeWorkingDirectory(seller);
			if (isChangedDir) {
				logger.info(Log.message("+++ Working Directory " + remoteDirPath + File.separator + seller));
			} else {
				logger.error(Log.message("+++ Unable to change + " + remoteDirPath + File.separator + seller));
			}
			showServerReply(ftpClient);
			FTPFile[] ftpFiles = ftpClient.listFiles();
			logger.info(Log.message("+++ Number of FTPFiles inside : " + ftpFiles.length));
			logger.info(Log.message("+++ Ready For loop download Impact Radius Feed +++"));
			// record the number of files
			int count = 0;
			String rem = null;
			for (FTPFile ftpFile : ftpFiles) {
				String neededFileName = ftpFile.getName();
				logger.info(Log.message("+++ File Name : " + ftpFile.getName()));
				if (neededFileName.endsWith("_IR.xml.gz")) {
					if (seller.equals(ImpactRadiusConstants.TARGET_FOLDER)) {
						if (neededFileName.equals(ImpactRadiusConstants.TARGET_NEEDFILE)) {
							rem = neededFileName;
						} else {
							continue;
						}
					} else {
						rem = neededFileName;
					}
					String sav = saveDirPath + seller + File.separator + neededFileName;
					logger.info(Log.message("+++ Remote Path " + rem));
					logger.info(Log.message("+++ Save Path " + sav));
					if (downloadSingleFile(rem, sav)) {
						count++;
						logger.info(Log.message("+++ Number of Downloaded Files is " + count));
					} else {
						logger.error(Log.message("### Unable to download " + seller + " feed ### "));
					}
				} else {
					logger.info(Log.message("+++ Skip file : " + neededFileName));
				}
			}
		} catch (IOException e) {
			logger.error(Log.message("### downloadImpactRadiusFeed Has Issue! ### " + e.getMessage()));
			e.printStackTrace();
		}
	}

	private boolean downloadSingleFile(String remoteFilePath, String savePath) throws IOException {
		logger.info(Log.message("+++ Start downloading +++"));
		boolean res = false;
		File downloadFile = new File(savePath);
		logger.info(Log.message("+++ Check if Parent Dir is existing +++"));
		File parentDir = downloadFile.getParentFile();
		if (!parentDir.exists()) {
			boolean mkdir = parentDir.mkdir();
			if (mkdir) {
				logger.info(Log.message("+++ Create Parent Dir " + parentDir.getAbsolutePath()));
			} else {
				logger.error(Log.message("### Create Parent Dir FAILED!!!" + parentDir.getAbsolutePath()
						+ " Reply Code : " + this.ftpClient.getReplyCode()));
			}
		}

		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(savePath));
		try {

			this.ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			res = this.ftpClient.retrieveFile(remoteFilePath, outputStream);
			if (res) {
				logger.info(Log.message("+++ Successfully downloaded : " + remoteFilePath));
			} else {
				logger.info(Log.message("+++ Unable to downloaded : " + remoteFilePath));
				logger.info(Log.message("+++ Reply Code : " + this.ftpClient.getReplyCode()));
			}
			outputStream.close();
		} catch (Exception ex) {
			logger.error(Log.message("### Issue in downloadSingleFile : " + ex.getMessage() + " Reply Code : "
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

	private Map<String, String> getImpactRadiusProductsMap(String propertiesPath) {
		logger.info(Log.message("--> Start getting Impact Radius Product File Map  <--"));
		if (propertiesPath == null || propertiesPath.length() == 0) {
			logger.error(Log.message("Properties Path is invalid : " + propertiesPath));
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(propertiesPath);
			prop.load(input);
			for (String key : prop.stringPropertyNames()) {
				String advertiserID = prop.getProperty(key);
				if (map.get(key) == null) {
					map.put(key, advertiserID);
				}
			}
		} catch (IOException e) {
			logger.error(Log.message("Issues in getImpactRadiusProductsMap : " + e.getMessage()));
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
		logger.info(Log.message("--> Finish getting Impact Radius Product File Map  <--"));
		return map;
	}

	public void cleanFiles(File feedFolder) {
		try {
			if (!feedFolder.exists()) {
				logger.error(Log.message("### Exiting the process as no such folder exists : " + feedFolder.getName()));
				return;
			}
			FileUtils.cleanDirectory(feedFolder);
		} catch (IOException e) {
			logger.error(Log.message("### Errors in cleaning folder :  + " + feedFolder.getName()));
			e.printStackTrace();
		}
		logger.info(Log.message("+++ Cleaned " + feedFolder.getAbsolutePath() + " +++"));
	}

	public void splitImpactRadiusFeed(File targetFile) {
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		BufferedWriter out = null;
		try {
			logger.info(Log.message("+++ Spliting ---> " + targetFile.getAbsolutePath() + " is Started +++"));

			// Get the total Line number of file
			FileInputStream fs_countLine = new FileInputStream(targetFile);
			DataInputStream in_countLine = new DataInputStream(fs_countLine);
			BufferedReader br_countLine = new BufferedReader(new InputStreamReader(in_countLine));

			String firstLine = br_countLine.readLine();
			String productsStart = br_countLine.readLine();
			String productsEnd = "</Products>";
			String eachLine = null;
			int count = 0;
			while ((eachLine = br_countLine.readLine()) != null) {
				if (eachLine.trim().equals("</Product>")) {
					count++;
				}
			}
			br_countLine.close();
			in_countLine.close();
			fs_countLine.close();

			double nop = 5000;

			// Log number of lines in the input file.
			logger.info(Log.message("+++ Product in the file: " + count));

			double temp = (count / nop);
			int temp1 = (int) temp;
			int nof = 0;
			if (temp1 == temp) {
				nof = temp1;
			} else {
				nof = temp1 + 1;
			}
			// Log number of files to be generated
			logger.info(Log.message("+++ No. of files to be generated :" + nof));

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
				if (j != 1) {
					out.write(firstLine);
					out.newLine();
					out.write(productsStart);
					out.newLine();
				}

				int i = 0;

				while (i <= nop) {
					strLine = br.readLine();
					if (strLine != null && strLine.length() > 0) {
						if (strLine.trim().equals("</Product>")) {
							i++;
						}
						out.write(strLine);
						out.newLine();
					} else {
						checkReadDone = true;
						break;
					}
				}

				if (j != nof) {
					out.write(productsEnd);
				}
				out.flush();
				out.close();
			}

			logger.info(Log.message("+++ Spliting ---> " + targetFile.getName() + " is done"));
			// logger.info(Log.message("Split -- " + targetFile.getName() +
			// " is done"));
		} catch (Exception e) {
			System.err.println("### Error: " + e.getMessage());
			e.printStackTrace();
			logger.info(Log.message("### Issue in split : " + targetFile.getName()));
			logger.info(Log.message("### Issue in split : " + e.getMessage()));
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

	private File removeDuplicatesByNameAndColor(File targetFile) {
		logger.info(Log.message("+++ Remove Duplicates In " + targetFile.getName() + " is Started +++"));
		String path = targetFile.getAbsolutePath();
		File tempFile = null;
		File temp = new File(path);

		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {

			String tempPath = targetFile.getAbsolutePath().replaceAll("\\.txt$", "_");
			tempFile = new File(tempPath + "NoDup.txt");

			reader = new BufferedReader(new FileReader(targetFile));
			writer = new BufferedWriter(new FileWriter(tempFile));

			Set<String> set = new HashSet<String>();
			// Add the title bar into the new file
			String currentLine = reader.readLine();
			if (currentLine != null) {
				writer.write(currentLine);
				writer.newLine();
			}

			while ((currentLine = reader.readLine()) != null) {
				String[] line = currentLine.split("\t");
				String product_name = line[4];
				// color column will be changed if different sellers
				String color = (line[33] != null && line[33].trim().length() > 0) ? line[33] : "";
				String dup = product_name.concat("-").concat(color);
				if (!set.contains(dup)) {
					set.add(dup);
					writer.write(currentLine);
					writer.newLine();
				}
			}
		} catch (IOException e) {
			logger.error(Log.message("### " + e.getMessage()));
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
				if (targetFile.exists()) {
					targetFile.delete();
					tempFile.renameTo(temp);
					temp = null;
				}
				logger.info(Log.message("+++ Remove Duplicates In " + targetFile.getName() + " is Finished +++"));
			} catch (IOException e) {
				logger.error(Log.message("### " + e.getMessage()));
				e.printStackTrace();
			}
		}
		return tempFile;
	}

	public void splitDiapersFeed(File targetFile) {
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		BufferedWriter out = null;
		try {
			logger.info("+++ Spliting ---> " + targetFile.getAbsolutePath() + " is Started +++");

			// Get the total Line number of file
			FileInputStream fs_countLine = new FileInputStream(targetFile);
			DataInputStream in_countLine = new DataInputStream(fs_countLine);
			BufferedReader br_countLine = new BufferedReader(new InputStreamReader(in_countLine));

			String firstLine = br_countLine.readLine();
			String productsStart = br_countLine.readLine();
			String productsEnd = "</Products>";
			String eachLine = null;
			int count = 0;
			while ((eachLine = br_countLine.readLine()) != null) {
				if (eachLine.trim().equals("</Product>")) {
					count++;
				}
			}
			br_countLine.close();
			in_countLine.close();
			fs_countLine.close();

			double nop = 1000;

			// Log number of lines in the input file.
			logger.info("+++ Product in the file: " + count);

			double temp = (count / nop);
			int temp1 = (int) temp;
			int nof = 0;
			if (temp1 == temp) {
				nof = temp1;
			} else {
				nof = temp1 + 1;
			}
			// Log number of files to be generated
			logger.info("+++ No. of files to be generated :" + nof);

			// ---------------------------------------------------------------------------------------------------------

			// Actual splitting of file into smaller files
			fstream = new FileInputStream(targetFile);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));

			String spliteFileName = targetFile.getAbsolutePath().replaceAll("\\.xml$", "_");
			String strLine = null;
			boolean checkReadDone = false;
			String currentLine = "";
			boolean canAdd = true;
			Set<String> checkDuplicates = new HashSet<String>();
			for (int j = 1; j <= nof; j++) {
				if (checkReadDone) {
					break;
				}
				FileWriter fstream1 = new FileWriter(spliteFileName + j + ".xml"); // Destination
																					// File
																					// Location
				int bufferSize = 8 * 1024;
				out = new BufferedWriter(fstream1, bufferSize);
				if (j != 1) {
					out.write(firstLine);
					out.newLine();
					out.write(productsStart);
					out.newLine();
				}

				int i = 0;

				while (i <= nop) {
					strLine = br.readLine();
					if (strLine != null && strLine.length() > 0) {
						if (strLine.contains("<Product_URL>")) {
							String[] url = strLine.split("-");
							String subUrl = url[url.length - 1];
							if (checkDuplicates.contains(subUrl)) {
								canAdd = false;
								;
							} else {
								checkDuplicates.add(subUrl);
								currentLine = currentLine.concat(strLine);
							}
						} else if (strLine.trim().equals("</Product>")) {
							currentLine = currentLine.concat(strLine);
							if (canAdd) {
								out.write(currentLine);
								out.newLine();
							}
							currentLine = "";
							canAdd = true;
							i++;
						} else {
							currentLine = currentLine.concat(strLine);
						}
					} else {
						checkReadDone = true;
						out.write(productsEnd);
						break;
					}
				}

				if (j != nof) {
					out.write(productsEnd);

				}
				out.flush();
				out.close();

				logger.info("+++ Spliting ---> " + targetFile.getName() + " is done");
				logger.info("checkDuplicates : " + checkDuplicates.size());
			}
		} catch (Exception e) {
			System.err.println("### Error: " + e.getMessage());
			e.printStackTrace();
			logger.info(Log.message("### Issue in split : " + targetFile.getName()));
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

	public void splitFeedByProductNameColor(File targetFile) {
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		BufferedWriter out = null;
		try {
			logger.info("+++ Spliting ---> " + targetFile.getAbsolutePath() + " is Started +++");
			logger.info("++++++++++++++++> Split By Peoduct Name and Color <++++++++++++++++");

			// Get the total Line number of file
			FileInputStream fs_countLine = new FileInputStream(targetFile);
			DataInputStream in_countLine = new DataInputStream(fs_countLine);
			BufferedReader br_countLine = new BufferedReader(new InputStreamReader(in_countLine));

			String firstLine = br_countLine.readLine();
			String productsStart = br_countLine.readLine();
			String productsEnd = "</Products>";
			String eachLine = null;
			int count = 0;
			while ((eachLine = br_countLine.readLine()) != null) {
				if (eachLine.trim().equals("</Product>")) {
					count++;
				}
			}
			br_countLine.close();
			in_countLine.close();
			fs_countLine.close();

			double nop = 1000;

			// Log number of lines in the input file.
			logger.info("+++ Product in the file: " + count);

			double temp = (count / nop);
			int temp1 = (int) temp;
			int nof = 0;
			if (temp1 == temp) {
				nof = temp1;
			} else {
				nof = temp1 + 1;
			}
			// Log number of files to be generated
			logger.info("+++ No. of files to be generated :" + nof);

			// ---------------------------------------------------------------------------------------------------------

			// Actual splitting of file into smaller files
			fstream = new FileInputStream(targetFile);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));

			String spliteFileName = targetFile.getAbsolutePath().replaceAll("\\.xml$", "_");
			String strLine = null;
			boolean checkReadDone = false;
			String currentLine = "";
			Set<String> checkDuplicates = new HashSet<String>();

			// Use to avoid duplicates
			String uniquekey = "";

			for (int j = 1; j <= nof; j++) {
				if (checkReadDone) {
					break;
				}
				FileWriter fstream1 = new FileWriter(spliteFileName + j + ".xml"); // Destination
																					// File
																					// Location
				int bufferSize = 8 * 1024;
				out = new BufferedWriter(fstream1, bufferSize);
				if (j != 1) {
					out.write(firstLine);
					out.newLine();
					out.write(productsStart);
					out.newLine();
				}

				int i = 0;

				while (i <= nop) {
					strLine = br.readLine();
					if (strLine != null && strLine.length() > 0) {
						if (strLine.contains("<Product_Name>")) {
							uniquekey = uniquekey.concat(strLine);
							currentLine = currentLine.concat(strLine);
						} else if (strLine.contains("<Color>")) {
							uniquekey = uniquekey.concat(strLine);
							currentLine = currentLine.concat(strLine);
						} else if (strLine.contains("</Product>")) {
							currentLine = currentLine.concat(strLine);
							if (!checkDuplicates.contains(uniquekey)) {
								checkDuplicates.add(uniquekey);
								out.write(currentLine);
								out.newLine();
							}
							currentLine = "";
							uniquekey = "";
							i++;
						} else {
							currentLine = currentLine.concat(strLine);
						}
					} else {
						checkReadDone = true;
						out.write(productsEnd);
						break;
					}
				}

				if (j != nof) {
					out.write(productsEnd);

				}
				out.flush();
				out.close();

				logger.info("+++ Spliting ---> " + targetFile.getName() + " is done");
				logger.info("checkDuplicates : " + checkDuplicates.size());
				// logger.info(Log.message("Split -- " +
				// targetFile.getName() +
				// " is done"));
			}
		} catch (Exception e) {
			System.err.println("### Error: " + e.getMessage());
			e.printStackTrace();
			logger.info(Log.message("### Issue in split : " + targetFile.getName()));
			logger.info(Log.message("### Issue in split : " + e.getMessage()));
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

	public void splitFeedByProductName(File targetFile) {
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		BufferedWriter out = null;
		try {
			logger.info("+++ Spliting ---> " + targetFile.getAbsolutePath() + " is Started +++");
			logger.info("++++++++++++++++> Split By Product Name <++++++++++++++++");

			// Get the total Line number of file
			FileInputStream fs_countLine = new FileInputStream(targetFile);
			DataInputStream in_countLine = new DataInputStream(fs_countLine);
			BufferedReader br_countLine = new BufferedReader(new InputStreamReader(in_countLine));

			String firstLine = br_countLine.readLine();
			String productsStart = br_countLine.readLine();
			String productsEnd = "</Products>";
			String eachLine = null;
			int count = 0;
			while ((eachLine = br_countLine.readLine()) != null) {
				if (eachLine.trim().equals("</Product>")) {
					count++;
				}
			}
			br_countLine.close();
			in_countLine.close();
			fs_countLine.close();

			double nop = 1000;

			// Log number of lines in the input file.
			logger.info("+++ Product in the file: " + count);

			double temp = (count / nop);
			int temp1 = (int) temp;
			int nof = 0;
			if (temp1 == temp) {
				nof = temp1;
			} else {
				nof = temp1 + 1;
			}
			// Log number of files to be generated
			logger.info("+++ No. of files to be generated :" + nof);

			// ---------------------------------------------------------------------------------------------------------

			// Actual splitting of file into smaller files
			fstream = new FileInputStream(targetFile);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));

			String spliteFileName = targetFile.getAbsolutePath().replaceAll("\\.xml$", "_");
			String strLine = null;
			boolean checkReadDone = false;
			String currentLine = "";
			boolean canAdd = true;

			// use to avoid out of stock items
			boolean inStock = true;

			int productNameCount = 0;

			Set<String> checkDuplicates = new HashSet<String>();
			for (int j = 1; j <= nof; j++) {
				if (checkReadDone) {
					break;
				}
				FileWriter fstream1 = new FileWriter(spliteFileName + j + ".xml"); // Destination
																					// File
																					// Location
				int bufferSize = 8 * 1024;
				out = new BufferedWriter(fstream1, bufferSize);
				if (j != 1) {
					out.write(firstLine);
					out.newLine();
					out.write(productsStart);
					out.newLine();
				}

				int i = 0;

				while (i <= nop) {
					strLine = br.readLine();
					if (strLine != null && strLine.length() > 0) {
						if (strLine.contains("<Product_Name>")) {
							productNameCount++;
							if (checkDuplicates.contains(strLine)) {
								canAdd = false;
							} else {
								checkDuplicates.add(strLine);
								currentLine = currentLine.concat(strLine);
							}
						} else if (strLine.contains("<Stock_Availability>N</Stock_Availability>")) {
							inStock = false;
						} else if (strLine.contains("</Product>")) {
							currentLine = currentLine.concat(strLine);
							if (canAdd && inStock) {
								out.write(currentLine);
								out.newLine();
							}
							currentLine = "";
							canAdd = true;
							inStock = true;
							i++;
						} else {
							currentLine = currentLine.concat(strLine);
						}
					} else {
						checkReadDone = true;
						out.write(productsEnd);
						break;
					}
				}

				if (j != nof) {
					out.write(productsEnd);
				}
				out.flush();
				out.close();
				logger.info("Total Count of Product Name : " + productNameCount);
				logger.info("+++ Spliting ---> " + targetFile.getName() + " is done");
				logger.info("checkDuplicates : " + checkDuplicates.size());
			}
		} catch (Exception e) {
			System.err.println("### Error: " + e.getMessage());
			e.printStackTrace();
			logger.info(Log.message("### Issue in split : " + targetFile.getName()));
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

	public void splitFeedByAvailability(File targetFile) {
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		BufferedWriter out = null;
		try {
			logger.info("+++ Spliting ---> " + targetFile.getAbsolutePath() + " is Started +++");
			logger.info("++++++++++++++++> Split By Availability <++++++++++++++++");

			// Get the total Line number of file
			FileInputStream fs_countLine = new FileInputStream(targetFile);
			DataInputStream in_countLine = new DataInputStream(fs_countLine);
			BufferedReader br_countLine = new BufferedReader(new InputStreamReader(in_countLine));

			String firstLine = br_countLine.readLine();
			String productsStart = br_countLine.readLine();
			String productsEnd = "</Products>";
			String eachLine = null;
			int count = 0;
			while ((eachLine = br_countLine.readLine()) != null) {
				if (eachLine.trim().equals("</Product>")) {
					count++;
				}
			}
			br_countLine.close();
			in_countLine.close();
			fs_countLine.close();

			double nop = 5000;

			// Log number of lines in the input file.
			logger.info("+++ Product in the file: " + count);

			double temp = (count / nop);
			int temp1 = (int) temp;
			int nof = 0;
			if (temp1 == temp) {
				nof = temp1;
			} else {
				nof = temp1 + 1;
			}
			// Log number of files to be generated
			logger.info("+++ No. of files to be generated :" + nof);

			// ---------------------------------------------------------------------------------------------------------

			// Actual splitting of file into smaller files
			fstream = new FileInputStream(targetFile);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));

			String spliteFileName = targetFile.getAbsolutePath().replaceAll("\\.xml$", "_");
			String strLine = null;
			boolean checkReadDone = false;
			String currentLine = "";
			boolean canAdd = true;
			// Set<String> checkDuplicates = new HashSet<String>();
			for (int j = 1; j <= nof; j++) {
				if (checkReadDone) {
					break;
				}
				FileWriter fstream1 = new FileWriter(spliteFileName + j + ".xml"); // Destination
																					// File
																					// Location
				int bufferSize = 8 * 1024;
				out = new BufferedWriter(fstream1, bufferSize);
				if (j != 1) {
					out.write(firstLine);
					out.newLine();
					out.write(productsStart);
					out.newLine();
				}

				int i = 0;

				while (i <= nop) {
					strLine = br.readLine();
					if (strLine != null && strLine.length() > 0) {
						if (strLine.contains("<Stock_Availability>N</Stock_Availability>")) {
							canAdd = false;
						} else if (strLine.trim().equals("</Product>")) {
							currentLine = currentLine.concat(strLine);
							if (canAdd) {
								out.write(currentLine);
								out.newLine();
							}
							currentLine = "";
							canAdd = true;
							i++;
						} else {
							currentLine = currentLine.concat(strLine);
						}
					} else {
						checkReadDone = true;
						out.write(productsEnd);
						break;
					}
				}

				if (j != nof) {
					out.write(productsEnd);

				}
				out.flush();
				out.close();

				logger.info("+++ Spliting ---> " + targetFile.getName() + " is done");
				// logger.info("checkDuplicates : " + checkDuplicates.size());
			}
		} catch (Exception e) {
			System.err.println("### Error: " + e.getMessage());
			e.printStackTrace();
			logger.info(Log.message("### Issue in split : " + targetFile.getName()));
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

	public void splitFeedByParentNumber(File targetFile) {
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		BufferedWriter out = null;
		try {
			logger.info("+++ Spliting ---> " + targetFile.getAbsolutePath() + " is Started +++");
			logger.info("++++++++++++++++> Split By Parent Number <++++++++++++++++");

			// Get the total Line number of file
			FileInputStream fs_countLine = new FileInputStream(targetFile);
			DataInputStream in_countLine = new DataInputStream(fs_countLine);
			BufferedReader br_countLine = new BufferedReader(new InputStreamReader(in_countLine));

			String firstLine = br_countLine.readLine();
			String productsStart = br_countLine.readLine();
			String productsEnd = "</Products>";
			String eachLine = null;
			int count = 0;
			while ((eachLine = br_countLine.readLine()) != null) {
				if (eachLine.trim().equals("</Product>")) {
					count++;
				}
			}
			br_countLine.close();
			in_countLine.close();
			fs_countLine.close();

			double nop = 1000;

			// Log number of lines in the input file.
			logger.info("+++ Product in the file: " + count);

			double temp = (count / nop);
			int temp1 = (int) temp;
			int nof = 0;
			if (temp1 == temp) {
				nof = temp1;
			} else {
				nof = temp1 + 1;
			}
			// Log number of files to be generated
			logger.info("+++ No. of files to be generated :" + nof);

			// ---------------------------------------------------------------------------------------------------------

			// Actual splitting of file into smaller files
			fstream = new FileInputStream(targetFile);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));

			String spliteFileName = targetFile.getAbsolutePath().replaceAll("\\.xml$", "_");
			String strLine = null;
			boolean checkReadDone = false;
			String currentLine = "";
			boolean canAdd = true;

			// use to ignore out-of-stock items
			boolean inStock = true;

			int parentSKUCount = 0;

			Set<String> checkDuplicates = new HashSet<String>();
			for (int j = 1; j <= nof; j++) {
				if (checkReadDone) {
					break;
				}
				FileWriter fstream1 = new FileWriter(spliteFileName + j + ".xml"); // Destination
																					// File
																					// Location
				int bufferSize = 8 * 1024;
				out = new BufferedWriter(fstream1, bufferSize);
				if (j != 1) {
					out.write(firstLine);
					out.newLine();
					out.write(productsStart);
					out.newLine();
				}

				int i = 0;

				while (i <= nop) {
					strLine = br.readLine();
					if (strLine != null && strLine.length() > 0) {
						if (strLine.contains("<Parent_SKU>")) {
							parentSKUCount++;
							if (checkDuplicates.contains(strLine)) {
								canAdd = false;
							} else {
								checkDuplicates.add(strLine);
								currentLine = currentLine.concat(strLine);
							}
						} else if (strLine.contains("<Stock_Availability>N</Stock_Availability>")) {
							inStock = false;
						} else if (strLine.contains("</Product>")) {
							currentLine = currentLine.concat(strLine);
							if (canAdd && inStock) {
								out.write(currentLine);
								out.newLine();
							}
							currentLine = "";
							canAdd = true;
							inStock = true;
							i++;
						} else {
							currentLine = currentLine.concat(strLine);
						}
					} else {
						checkReadDone = true;
						out.write(productsEnd);
						break;
					}
				}

				if (j != nof) {
					out.write(productsEnd);
				}
				out.flush();
				out.close();
				logger.info("Total Parent_SKU Count : " + parentSKUCount);
				logger.info("+++ Spliting ---> " + targetFile.getName() + " is done");
				logger.info("checkDuplicates : " + checkDuplicates.size());
			}
		} catch (Exception e) {
			System.err.println("### Error: " + e.getMessage());
			e.printStackTrace();
			logger.info(Log.message("### Issue in split : " + targetFile.getName()));
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
}
