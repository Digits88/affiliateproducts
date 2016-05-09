package services.cj.impl;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;
import play.Play;
import com.google.common.io.Files;
import constants.CJProductsConstants;
import services.FileService;
import services.impl.DefaultSKProductService;
import utils.log.Log;

public class CJFileService extends FileService {

	private static Logger logger = Logger.getLogger(CJFileService.class);

	final String host = Play.configuration.getProperty("cron.job.sync.products.ftp.host");
	final String user = Play.configuration.getProperty("cron.job.sync.products.ftp.user");
	final String pwd = Play.configuration.getProperty("cron.job.sync.products.ftp.password");

	final String remoteDirPath = Play.configuration.getProperty("cron.job.sync.products.ftp.path.remote");
	final String saveDirPath = Play.configuration.getProperty("affiliate.cj.product.feed.input.location");
	final String unGzipTo = Play.configuration.getProperty("affiliate.cj.product.feed.input.location");

	final String propertiesPath = Play.configuration.getProperty("cron.job.sync.products.ftp.properties");

	private FTPClient ftpClient = null;
	private Map<String, Map<String, Boolean>> cjSellerTable;

	public CJFileService() {
		try {
			ftpClient = new FTPClient();
			boolean connected = getFTPConnection();
			if (!connected) {
				logger.error(Log.message(
						"### Cannot get FTP Connection, Please Try Again Later ### " + ftpClient.getReplyCode()));
			} else {
				logger.info(Log.message("Connected to FTP Server ? " + ftpClient.isConnected()));
				int replyCode = ftpClient.getReplyCode();
				logger.info(Log.message("Connected Reply Code : " + replyCode));
			}
		} catch (Exception e) {
			logger.error(Log.message("### Initialization has issue ### " + e.getMessage()));
			e.printStackTrace();
		}

	}

	private boolean getFTPConnection() {
		boolean connected = false;
		try {
			ftpClient.connect(host);
			ftpClient.login(user, pwd);
			ftpClient.enterLocalPassiveMode();
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

	public void downloadCJFeeds() {
		try {
			logger.info(Log.message("+++ Start downloading the CJ feeds +++"));
			// get the map
			cjSellerTable = readProductDetailsFromProp(propertiesPath);
			// download the feeds
			downloadfiles(remoteDirPath, saveDirPath, cjSellerTable);
			closeFTPConnection();
			findUndownloadedFile(cjSellerTable);
			// upzip
			logger.info(Log.message("+++ Decompression is Start +++"));
			File directory = new File(saveDirPath);
			List<File> largeFiles = new ArrayList<File>();
			decompressFilesInFolder(directory, unGzipTo, largeFiles);
			// split if needed
			for (File f : largeFiles) {
				split(f);
			}
			logger.info(Log.message("+++ Finish downloading the CJ feeds +++"));
		} catch (Exception e) {
			logger.error(Log.message(
					"### Issues in downloading feeds : " + ftpClient.getReplyCode() + " ### " + e.getMessage()));
			e.printStackTrace();
		}
	}

	private void findUndownloadedFile(Map<String, Map<String, Boolean>> sellerTable) {
		if (sellerTable == null || sellerTable.size() == 0) {
			logger.error(Log.message("### Map is invalid !!### "));
			return;
		}
		int count = 0;
		for (String key : sellerTable.keySet()) {
			Map<String, Boolean> map = sellerTable.get(key);
			for (String file : map.keySet()) {
				if (!map.get(file).booleanValue()) {
					logger.info(Log.message("--> File : " + file + " --> IS NOT Downloaded!!!"));
					count++;
				}
			}
		}
		logger.info(Log.message("--> Total Count Of NOT Downloaded File : " + count + " <--- "));
	}

	public void split(File targetFile) {
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		BufferedWriter out = null;
		try {
			System.out.println("Split -- " + targetFile.getAbsolutePath() + " is Start");

			// Get the total Line number of file
			FileInputStream fs_countLine = new FileInputStream(targetFile);
			DataInputStream in_countLine = new DataInputStream(fs_countLine);
			BufferedReader br_countLine = new BufferedReader(new InputStreamReader(in_countLine));

			String titleBar = br_countLine.readLine();
			int count = 1;
			while (br_countLine.readLine() != null) {
				count++;
			}
			br_countLine.close();
			in_countLine.close();
			fs_countLine.close();

			double nol = 5000;

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

			boolean checkReadDone = false;

			for (int j = 1; j <= nof; j++) {

				if (checkReadDone) {
					break;
				}

				FileWriter fstream1 = new FileWriter(spliteFileName + j + ".txt"); // Destination
																					// File
																					// Location

				int bufferSize = 8 * 1024;

				out = new BufferedWriter(fstream1, bufferSize);

				// 10 14 -- Home Depot
				out.write(titleBar);
				out.newLine();

				for (int i = 1; i <= nol; i++) {
					strLine = br.readLine();
					if (strLine != null) {
						String[] list = strLine.split("\t");
						if (list[36].trim().equalsIgnoreCase("yes")) {
							out.write(strLine);
							out.flush();
							if (i != nol) {
								out.newLine();
							}
						}
					} else {
						checkReadDone = true;
						break;
					}
				}
				out.flush();
				out.close();
			}

			System.out.println("Split -- " + targetFile.getName() + " is done");
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

	// Use for Chicco
	private File removeDuplicatesForChicco(File targetFile) {
		System.out.println(targetFile.getName());
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
				// check if issue feed with sale price > price
				if (line[13] != null && line[13].trim().length() > 0) {
					if (line[14] != null && line[14].trim().length() > 0
							&& Double.parseDouble(line[13]) > Double.parseDouble(line[14])) {
						continue;
					}
					if (line[15] != null && line[15].trim().length() > 0
							&& Double.parseDouble(line[13]) > Double.parseDouble(line[15])) {
						continue;
					}
				} else {
					if (line[14] != null && line[14].trim().length() > 0 && line[15] != null
							&& line[15].trim().length() > 0
							&& Double.parseDouble(line[14]) > Double.parseDouble(line[15])) {
						continue;
					}
				}
				String[] sku_line = line[7].split("_");
				String sku = (sku_line.length == 1) ? sku_line[0] : sku_line[0] + "_" + sku_line[1];
				if (!set.contains(sku)) {
					set.add(sku);
					writer.write(currentLine);
					writer.newLine();
				}
			}
		} catch (IOException e) {
			logger.error(Log.message("Issue in removeDuplicatesForChicco : " + e.getMessage()));
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
					// tempFile.renameTo(temp);
					temp = null;
				}
				System.out.println("Remove Dup is done.");
			} catch (IOException e) {
				logger.error(Log.message("Issues in removeDuplicatesForChicco : " + e.getMessage()));
				e.printStackTrace();
			}
		}
		return tempFile;
	}

	// Use for Ugg Feed
	private File removeDuplicatesForUGG(File targetFile) {
		System.out.println(targetFile.getName());
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
				// check if issue feed with sale price > price
				if (line[13] != null && line[13].trim().length() > 0) {
					if (line[14] != null && line[14].trim().length() > 0
							&& Double.parseDouble(line[13]) > Double.parseDouble(line[14])) {
						continue;
					}
					if (line[15] != null && line[15].trim().length() > 0
							&& Double.parseDouble(line[13]) > Double.parseDouble(line[15])) {
						continue;
					}
				} else {
					if (line[14] != null && line[14].trim().length() > 0 && line[15] != null
							&& line[15].trim().length() > 0
							&& Double.parseDouble(line[14]) > Double.parseDouble(line[15])) {
						continue;
					}
				}
				String[] sku_line = line[7].split("-");
				String sku = (sku_line.length == 1) ? sku_line[0] : sku_line[0] + "-" + sku_line[1];
				if (!set.contains(sku)) {
					set.add(sku);
					writer.write(currentLine);
					writer.newLine();
				}
			}
		} catch (IOException e) {
			logger.error(Log.message("Issues in removeDuplicatesForUGG : " + e.getMessage()));
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
					// tempFile.renameTo(temp);
					temp = null;
				}
				System.out.println("Remove Dup is done.");
			} catch (IOException e) {
				logger.error(Log.message("Issues in removeDuplicatesForUGG : " + e.getMessage()));
				e.printStackTrace();
			}
		}
		return tempFile;
	}

	// Used for BR Feed
	private File removeDuplicatesForBR(File targetFile) {
		System.out.println(targetFile.getName());
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
				// check if issue feed with sale price > price
				if (line[13] != null && line[13].trim().length() > 0) {
					if (line[14] != null && line[14].trim().length() > 0
							&& Double.parseDouble(line[13]) > Double.parseDouble(line[14])) {
						continue;
					}
					if (line[15] != null && line[15].trim().length() > 0
							&& Double.parseDouble(line[13]) > Double.parseDouble(line[15])) {
						continue;
					}
				} else {
					if (line[14] != null && line[14].trim().length() > 0 && line[15] != null
							&& line[15].trim().length() > 0
							&& Double.parseDouble(line[14]) > Double.parseDouble(line[15])) {
						continue;
					}
				}
				String br_SKU = line[7].split("_")[0];
				if (!set.contains(br_SKU)) {
					set.add(br_SKU);
					writer.write(currentLine);
					writer.newLine();
				}
			}
		} catch (IOException e) {
			logger.error(Log.message("Issues in removeDuplicatesForBR : " + e.getMessage()));
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
					// tempFile.renameTo(temp);
					temp = null;
				}
				System.out.println("Remove Dup is done.");
			} catch (IOException e) {
				logger.error(Log.message("Issues in removeDuplicatesForBR : " + e.getMessage()));
				e.printStackTrace();
			}
		}
		return tempFile;
	}

	/*
	 * BR, Old Navy
	 */
	private File removeDuplicatesByPassSKUPattern(File targetFile, String pattern) {
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
				// check if issue feed with sale price > price
				if (line[13] != null && line[13].trim().length() > 0) {
					if (line[14] != null && line[14].trim().length() > 0
							&& Double.parseDouble(line[13]) > Double.parseDouble(line[14])) {
						continue;
					}
					if (line[15] != null && line[15].trim().length() > 0
							&& Double.parseDouble(line[13]) > Double.parseDouble(line[15])) {
						continue;
					}
				} else {
					if (line[14] != null && line[14].trim().length() > 0 && line[15] != null
							&& line[15].trim().length() > 0
							&& Double.parseDouble(line[14]) > Double.parseDouble(line[15])) {
						continue;
					}
				}
				String br_SKU = line[7].split(pattern)[0];
				if (!set.contains(br_SKU)) {
					set.add(br_SKU);
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
					// tempFile.renameTo(temp);
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

	private File removeDuplicatesByPassName(File targetFile) {
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
				// check if issue feed with sale price > price
				if (line[13] != null && line[13].trim().length() > 0) {
					if (line[14] != null && line[14].trim().length() > 0
							&& Double.parseDouble(line[13]) > Double.parseDouble(line[14])) {
						continue;
					}
					if (line[15] != null && line[15].trim().length() > 0
							&& Double.parseDouble(line[13]) > Double.parseDouble(line[15])) {
						continue;
					}
				} else {
					if (line[14] != null && line[14].trim().length() > 0 && line[15] != null
							&& line[15].trim().length() > 0
							&& Double.parseDouble(line[14]) > Double.parseDouble(line[15])) {
						continue;
					}
				}
				String br_name = line[4];
				if (!set.contains(br_name)) {
					set.add(br_name);
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
					// tempFile.renameTo(temp);
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

	static Map<String, String> toReplace = new HashMap<String, String>();

	static {
		toReplace.put("Ã¢Â€Â™", "'");
		toReplace.put("Ã¢Â„Â¢", "™");
		toReplace.put("â¢", "™");
		toReplace.put("Ã‚Â®", "®");
		toReplace.put("Â®", "®");
		toReplace.put("Ã‚Â", "");
		toReplace.put("Â", "");
		toReplace.put("â", "'");
		toReplace.put("â", "–");
		toReplace.put("├é┬╜", "½-");
		toReplace.put("├ó┬Ç┬£", "“");
		toReplace.put("├ó┬Ç┬£", "”");
	}

	private String getModifiedName(String name) {
		if (name != null) {
			for (Map.Entry<String, String> entry : toReplace.entrySet()) {
				name = name.replaceAll(entry.getKey(), entry.getValue());
			}
		}
		return name;
	}

	private File removeDuplicatesForSurLaTable(File targetFile) {

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
				// check if issue feed with sale price > price
				if (line[13] != null && line[13].trim().length() > 0) {
					if (line[14] != null && line[14].trim().length() > 0
							&& Double.parseDouble(line[13]) > Double.parseDouble(line[14])) {
						continue;
					}
					if (line[15] != null && line[15].trim().length() > 0
							&& Double.parseDouble(line[13]) > Double.parseDouble(line[15])) {
						continue;
					}
				} else {
					if (line[14] != null && line[14].trim().length() > 0 && line[15] != null
							&& line[15].trim().length() > 0
							&& Double.parseDouble(line[14]) > Double.parseDouble(line[15])) {
						continue;
					}
				}
				String br_name = getModifiedName(line[4]);
				if (!set.contains(br_name)) {
					set.add(br_name);
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
					// tempFile.renameTo(temp);
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

	/**
	 * Get CJ Feeds information based on properties file
	 */
	private Map<String, Map<String, Boolean>> readProductDetailsFromProp(String propertiesPath) {
		Map<String, Map<String, Boolean>> productTable = new Hashtable<String, Map<String, Boolean>>();
		Properties prop = new Properties();
		InputStream input = null;
		int count = 0;
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
					logger.info(Log.message("+++ Need Download File --> " + key + " -- " + token + " +++"));
					needDownloadFiles.put(token, false);
					count++;
				}
				productTable.put(key, needDownloadFiles);
				logger.info(Log.message("+++ Total Number of Seller --> " + count + " +++"));
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
	 * Download Files
	 */
	private void downloadfiles(String parentDir, String saveDir, Map<String, Map<String, Boolean>> map) {
		Set<String> keys = map.keySet();

		for (String key : keys) {
			Map<String, Boolean> files = map.get(key);
			try {
				downloadDirectory(parentDir, key, saveDir, files);
			} catch (IOException e) {
				logger.error(Log.message("Issues in downloadfiles : " + e.getMessage()));
				e.printStackTrace();
			}
		}
	}

	private void downloadDirectory(String parentDir, String currentDir, String saveDir, Map<String, Boolean> map)
			throws IOException {

		logger.info(Log.message("Start Downloading..."));
		// logger.info(Log.message(ftpClient.isAvailable()));
		String dirToList = parentDir;
		if (!currentDir.equals("")) {
			dirToList += "/" + currentDir + "/";
		}
		logger.info(Log.message(dirToList));

		ftpClient.changeWorkingDirectory(dirToList);

		FTPFile[] subFiles = ftpClient.listFiles();

		if (subFiles != null && subFiles.length > 0) {
			for (FTPFile aFile : subFiles) {
				Boolean isDownloaded = map.get(aFile.getName());
				if (isDownloaded == null || isDownloaded.booleanValue() == true) {
					continue;
				}
				map.put(aFile.getName(), true);
				String currentFileName = aFile.getName();
				if (currentFileName.equals(".") || currentFileName.equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}
				String filePath = parentDir + "/" + currentDir + "/" + currentFileName;
				if (currentDir.equals("")) {
					filePath = parentDir + "/" + currentFileName;
				}

				// 10 14 -- Home Depot
				/*
				 * String newDirPath = saveDir + parentDir + File.separator +
				 * currentDir + File.separator + currentFileName;
				 */

				String newDirPath = saveDir + File.separator + currentFileName;

				if (currentDir.equals("")) {
					newDirPath = saveDir + parentDir + File.separator + currentFileName;
				}

				if (aFile.isDirectory()) {
					// create the directory in saveDir
					File newDir = new File(newDirPath);
					boolean created = newDir.mkdirs();
					if (created) {
						logger.info(Log.message("CREATED the directory: " + newDirPath));

					} else {
						logger.error(Log.message("COULD NOT create the directory: " + newDirPath));

					}

					// download the sub directory
					downloadDirectory(dirToList, currentFileName, saveDir, map);
				} else {
					// download the file
					boolean success = downloadSingleFile(ftpClient, filePath, newDirPath);
					if (success) {
						logger.info(Log.message("DOWNLOADED the file: " + filePath));
					} else {
						logger.error(Log.message("COULD NOT download the file: " + filePath));

					}
				}
			}
		}
	}

	private static boolean downloadSingleFile(FTPClient ftpClient, String remoteFilePath, String savePath)
			throws IOException {
		boolean res = false;
		File downloadFile = new File(savePath);

		File parentDir = downloadFile.getParentFile();
		if (!parentDir.exists()) {
			parentDir.mkdir();
		}

		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
		try {
			// logger.info(Log.message("Start downloading"));
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			res = ftpClient.retrieveFile(remoteFilePath, outputStream);
		} catch (IOException ex) {
			logger.error(Log.message("Issue in downloadSingleFile : " + ex.getMessage()));
			ex.printStackTrace();
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
			logger.info(Log.message("Finish downloading"));
		}
		return res;
	}

	/**
	 * UnZip Files Functions are listed below
	 */
	public void decompressFilesInFolder(File folder, String unGzipTo, List<File> list) {
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

	private void unGzip(File infile, boolean deleteGzipfileOnSuccess, String unGzipTo, List<File> list) {

		logger.info(Log.message("+++ Start UnZip File : " + infile.getName() + " +++"));

		File folder = new File(unGzipTo);
		GZIPInputStream gin = null;
		File outFile = null;
		FileOutputStream fos = null;
		int len;
		try {
			gin = new GZIPInputStream(new FileInputStream(infile));
			String infileWithoutGZName = infile.getName().replaceAll("\\.gz$", "");
			outFile = new File(folder + "/" + infileWithoutGZName.replaceAll("\\.txt$", ""));

			outFile.mkdir();

			outFile = new File(
					folder + "/" + infileWithoutGZName.replaceAll("\\.txt$", "") + "/" + infileWithoutGZName);

			logger.info(Log.message("DeCompressing the file: \"" + outFile.getName() + "\" is Started."));

			fos = new FileOutputStream(outFile);
			byte[] buf = new byte[1024]; // Buffer size is a matter of
											// taste and application...
			while ((len = gin.read(buf)) > 0) {
				fos.write(buf, 0, len);
			}
			gin.close();
			fos.close();
			logger.info(Log.message("+++ DeCompressing is Done. +++"));

			if (deleteGzipfileOnSuccess) {
				infile.delete();
			}

			if (outFile.getName().equals(CJProductsConstants.BABABA_REPUBLIC_PRODUCT_CATALOG)) {
				outFile = removeDuplicatesForBR(outFile);
			} else if (outFile.getName().equals(CJProductsConstants.CHICCO_SHOP_PRODUCT_CATALOG)) {
				outFile = removeDuplicatesForChicco(outFile);
			} else if (outFile.getName().equals(CJProductsConstants.UGG_AUSTRALIA_PRODUCT_CATALOG)) {
				outFile = removeDuplicatesForUGG(outFile);
			} else if (outFile.getName().equals(CJProductsConstants.OLD_NAVY_PRODUCT_CATALOG)) {
				outFile = removeDuplicatesByPassSKUPattern(outFile, CJProductsConstants.UNDERSCORE);
			} else if (outFile.getName().equals(CJProductsConstants.TED_BAKER_PRODUCT_CATALOG)) {
				outFile = removeDuplicatesByPassName(outFile);
			} else if (outFile.getName().equals(CJProductsConstants.ATHLETA_PRODUCT_CATALOG)) {
				outFile = removeDuplicatesByPassSKUPattern(outFile, CJProductsConstants.UNDERSCORE);
			} else if (outFile.getName().equals(CJProductsConstants.GAP_PRODUCT_CATALOG)) {
				outFile = removeDuplicatesByPassSKUPattern(outFile, CJProductsConstants.UNDERSCORE);
			} else if (outFile.getName().equals(CJProductsConstants.SUR_LA_TABLE_PRODUCT_CATALOG)) {
				outFile = removeDuplicatesForSurLaTable(outFile);
			} else if (outFile.getName().equals(CJProductsConstants.QVC_COM_PRODUCT_CATALOG)) {
				outFile = removeDuplicatesByPassName(outFile);
			} else if (outFile.getName().equals(CJProductsConstants.FOREVER_21_PRODUCT_CATALOG)) {
				outFile = removeDuplicatesByPassName(outFile);
			} else if (outFile.getName().equals(CJProductsConstants.BARNES_NOBLE_PRODUCT_CATALOG)) {
				outFile = formatBarnesNoblesFeed(outFile);
			} else {
				outFile = removeIfSalePriceGreaterThanPrice(outFile);
			}
			logger.info(outFile.getName() + " | Length : " + (outFile.length() / 1024));
			if ((outFile.length() / 1024) > 3500) {
				list.add(outFile);
			}

			logger.info("How Many files need to split ?? " + list.size());

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

	// Add function to remove out of stock item
	public static File removeOutOfStock(File targetFile) {
		System.out.println(targetFile.getName());
		String path = targetFile.getAbsolutePath();
		File tempFile = null;
		File temp = new File(path);

		BufferedReader reader = null;
		BufferedWriter writer = null;

		int count = 0;
		try {

			String tempPath = targetFile.getAbsolutePath().replaceAll("\\.txt$", "_");
			tempFile = new File(tempPath + "InStock.txt");

			reader = new BufferedReader(new FileReader(targetFile));
			writer = new BufferedWriter(new FileWriter(tempFile));

			// Add the title bar into the new file
			String currentLine = reader.readLine();
			if (currentLine != null) {
				writer.write(currentLine);
				writer.newLine();
			}

			while ((currentLine = reader.readLine()) != null) {
				String[] line = currentLine.split("\t");
				if (line[36].trim().equalsIgnoreCase("yes")) {
					writer.write(currentLine);
					writer.newLine();
				} else {
					count++;
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
				if (targetFile.exists()) {
					targetFile.delete();
					// tempFile.renameTo(temp);
					temp = null;
				}
				logger.info(Log.message(targetFile.getName() + " has " + count + " OUt Of Stock Products."));
				logger.info(Log.message("Remove Out Of Stock for -- " + targetFile.getName() + "is done."));
			} catch (IOException e) {
				logger.error(Log.message("Issue in removeOutOfStock : " + e.getMessage()));
				e.printStackTrace();
			}
		}
		return tempFile;
	}

	private File removeIfSalePriceGreaterThanPrice(File targetFile) {
		System.out.println(targetFile.getName());
		String path = targetFile.getAbsolutePath();
		File tempFile = null;
		// File temp = new File(path);

		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {

			String tempPath = targetFile.getAbsolutePath().replaceAll("\\.txt$", "_");
			tempFile = new File(tempPath + "NoDup.txt");

			reader = new BufferedReader(new FileReader(targetFile));
			writer = new BufferedWriter(new FileWriter(tempFile));

			// Add the title bar into the new file
			String currentLine = reader.readLine();
			if (currentLine != null) {
				writer.write(currentLine);
				writer.newLine();
			}

			while ((currentLine = reader.readLine()) != null) {
				String[] line = currentLine.split("\t");
				// check if issue feed with sale price > price
				if (line[13] != null && line[13].trim().length() > 0) {
					if (line[14] != null && line[14].trim().length() > 0
							&& Double.parseDouble(line[13]) > Double.parseDouble(line[14])) {
						continue;
					}
					if (line[15] != null && line[15].trim().length() > 0
							&& Double.parseDouble(line[13]) > Double.parseDouble(line[15])) {
						continue;
					}
				} else {
					if (line[14] != null && line[14].trim().length() > 0 && line[15] != null
							&& line[15].trim().length() > 0
							&& Double.parseDouble(line[14]) > Double.parseDouble(line[15])) {
						continue;
					}
				}
				writer.write(currentLine);
				writer.newLine();
			}
		} catch (IOException e) {
			logger.error(Log.message("Issues in removeDuplicatesForBR : " + e.getMessage()));
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
					// tempFile.renameTo(temp);
					// temp = null;
				}
				System.out.println("Remove Dup is done.");
			} catch (IOException e) {
				logger.error(Log.message("Issues in removeDuplicatesForBR : " + e.getMessage()));
				e.printStackTrace();
			}
		}
		return tempFile;
	}

	private void showServerReply(FTPClient ftpClient) {
		String[] replies = ftpClient.getReplyStrings();
		if (replies != null && replies.length > 0) {
			for (String aReply : replies) {
				logger.info(Log.message("SERVER: " + aReply));
			}
		}
	}

	// Use this set to store required format
	public static Set<String> bnFormat = new HashSet<String>();

	static {
		bnFormat.add("Arts & Craft");
		bnFormat.add("Bath Book");
		bnFormat.add("Board Book");
		// bnFormat.add("Book and Toy");
		// bnFormat.add("Book-a-zine");
		bnFormat.add("Coloring Book");
		bnFormat.add("Hardcover");
		bnFormat.add("Interactive Book");
		// bnFormat.add("Magazine");
		bnFormat.add("Paperback");
		bnFormat.add("Pop Up Book");
		// bnFormat.add("Poster");
		bnFormat.add("Sticker Book");
	}

	public static File formatBarnesNoblesFeed(File targetFile) {
		System.out.println(targetFile.getName());
		String path = targetFile.getAbsolutePath();
		File tempFile = null;
		File temp = new File(path);

		BufferedReader reader = null;
		BufferedWriter writer = null;

		int countFormatItem = 0;
		int countExclude = 0;
		int count = 0;
		int totalCount = 0;

		int acCount = 0, babCount = 0, bbCount = 0, cbCount = 0, hcCount = 0, ibCount = 0, pbCount = 0, pubCount = 0,
				sbCount = 0;

		try {

			String tempPath = targetFile.getAbsolutePath().replaceAll("\\.txt$", "_");
			tempFile = new File(tempPath + "format.txt");

			reader = new BufferedReader(new FileReader(targetFile));
			writer = new BufferedWriter(new FileWriter(tempFile));

			// Add the title bar into the new file
			String currentLine = reader.readLine();
			if (currentLine != null) {
				writer.write(currentLine);
				writer.newLine();
			}

			while ((currentLine = reader.readLine()) != null) {
				String[] line = currentLine.split("\t");
				if (line[36].trim().equalsIgnoreCase("yes")) {
					if (bnFormat.contains(line[28])) {
						writer.write(currentLine);
						writer.newLine();
						countFormatItem++;
						if (line[28].equals("Arts & Craft")) {
							acCount++;
						} else if (line[28].equals("Bath Book")) {
							babCount++;
						} else if (line[28].equals("Board Book")) {
							bbCount++;
						} else if (line[28].equals("Coloring Book")) {
							cbCount++;
						} else if (line[28].equals("Hardcover")) {
							hcCount++;
						} else if (line[28].equals("Interactive Book")) {
							ibCount++;
						} else if (line[28].equals("Paperback")) {
							pbCount++;
						} else if (line[28].equals("Pop Up Book")) {
							pubCount++;
						} else {
							sbCount++;
						}
					} else {
						// logger.info("Format --> " + line[28]);
						countExclude++;
					}
				} else {
					count++;
				}
				totalCount++;
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
				if (targetFile.exists()) {
					targetFile.delete();
					// tempFile.renameTo(temp);
					temp = null;
				}
				logger.info(targetFile.getName() + " has " + acCount + " Arts & Craft");
				logger.info(targetFile.getName() + " has " + babCount + " Bath Book");
				logger.info(targetFile.getName() + " has " + bbCount + " Board Book");
				logger.info(targetFile.getName() + " has " + cbCount + " Coloring Book");
				logger.info(targetFile.getName() + " has " + hcCount + " Hardcover");
				logger.info(targetFile.getName() + " has " + ibCount + " Interactive Book");
				logger.info(targetFile.getName() + " has " + pbCount + " Paperback");
				logger.info(targetFile.getName() + " has " + pubCount + " Pop Up Book");
				logger.info(targetFile.getName() + " has " + sbCount + " Sticker Book");

				logger.info(targetFile.getName() + " has " + countFormatItem + " Products will be imported into DB");
				logger.info(targetFile.getName() + " has " + countExclude + " Products will NOT be imported into DB");
				logger.info(targetFile.getName() + " has " + count + " OUt Of Stock Products.");
				logger.info(targetFile.getName() + " has " + totalCount + " Products Total.");
				logger.info("Remove Out Of Stock and Format for -- " + targetFile.getName() + "is done.");
			} catch (IOException e) {
				logger.error(Log.message("Issue in removeOutOfStock : " + e.getMessage()));
				e.printStackTrace();
			}
		}
		logger.info("tempFile size is " + tempFile.length()/1024);
		return tempFile;
	}

	final String superPropertiesPath = Play.configuration.getProperty("cron.job.sync.products.ftp.superproperties");

	public void downloadCJSuperFeeds() {
		try {
			logger.info(Log.message("+++ Start downloading the CJ Super feeds +++"));
			// get the map
			cjSellerTable = readProductDetailsFromProp(superPropertiesPath);
			// download the feeds
			downloadfiles(remoteDirPath, saveDirPath, cjSellerTable);
			closeFTPConnection();
			// findUndownloadedFile(cjSellerTable);
			// upzip
			logger.info(Log.message("+++ Decompression is Start +++"));
			File directory = new File(saveDirPath);
			List<File> largeFiles = new ArrayList<File>();
			decompressFilesInFolder(directory, unGzipTo, largeFiles);
			// split if needed
			for (File f : largeFiles) {
				split(f);
			}
			logger.info(Log.message("+++ Finish downloading the CJ Super feeds +++"));
		} catch (Exception e) {
			logger.error(Log.message("### Issues in downloading CJ Super feeds : " + ftpClient.getReplyCode() + " ### "
					+ e.getMessage()));
			e.printStackTrace();
		}
	}
	
	public Set<String> getCJBrandFile(File targetFile) {
		logger.info("+++ Get CJ Brand " + targetFile.getName() + " is Started +++");
		BufferedReader reader = null;
		// BufferedWriter writer = null;
		int count = 0;
		Set<String> set = null;
		try {
			set = new HashSet<String>();
			reader = new BufferedReader(new FileReader(targetFile));
			String brand_name = null;
			String currentLine = reader.readLine();
			while ((currentLine = reader.readLine()) != null) {
				String[] line = currentLine.split("\t");
				brand_name = line[8];
				if (!set.contains(brand_name)) {
					count++;
					set.add(brand_name);
				}
			}
		} catch (IOException e) {
			logger.error(Log.message("### " + e.getMessage()));
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
					reader = null;
				}
				logger.info("+++ Get CJ Brand " + targetFile.getName() + " is Finished +++");
				logger.info("+++ Total Brand in " + targetFile.getName() + " is " + count + " +++");
			} catch (IOException e) {
				logger.error(Log.message("### " + e.getMessage()));
				e.printStackTrace();
			}
		}
		return set;
	}
	
	public Set<String> getCJCategoryFile(File targetFile) {
		logger.info("+++ Get CJ Category " + targetFile.getName() + " is Started +++");
		BufferedReader reader = null;
		// BufferedWriter writer = null;
		int count = 0;
		Set<String> set = null;
		try {
			set = new HashSet<String>();
			reader = new BufferedReader(new FileReader(targetFile));
			String category_name = null;
			String currentLine = reader.readLine();
			while ((currentLine = reader.readLine()) != null) {
				String[] line = currentLine.split("\t");
				category_name = line[20];
				if (!set.contains(category_name)) {
					count++;
					set.add(category_name);
				}
			}
		} catch (IOException e) {
			logger.error(Log.message("### " + e.getMessage()));
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
					reader = null;
				}
				logger.info("+++ Get CJ Category " + targetFile.getName() + " is Finished +++");
				logger.info("+++ Total Category in " + targetFile.getName() + " is " + count + " +++");
			} catch (IOException e) {
				logger.error(Log.message("### " + e.getMessage()));
				e.printStackTrace();
			}
		}
		return set;
	}
}
