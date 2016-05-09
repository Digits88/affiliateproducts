package batch.jobs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.inject.Inject;

import models.Product;
import models.Seller;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import play.Play;
import play.libs.F.Promise;
import repositories.Repository;
import utils.PriceUtils;
import utils.log.Log;
import constants.AffiliateConstants;
import constants.RakutenConstants;

public class RakutenCheckDupJob extends AbstractBatchJob {

	private static Logger logger = Logger.getLogger(RakutenCheckDupJob.class);

	File targetFile;
	List<File> largeFiles;
	Map<String, String> colorSKUMap;

	public RakutenCheckDupJob(File targetFile, List<File> largeFiles, Map<String, String> colorSKUMap) {
		super();
		this.targetFile = targetFile;
		this.largeFiles = largeFiles;
		this.colorSKUMap = colorSKUMap;
	}

	@Override
	public void doJob() throws Exception {
		runJob();
	}

	private void runJob() {
		logger.info(Log
				.message("Start remove duplicates and out-stock items in "
						+ targetFile.getName()));
		File tempFile = null;
		BufferedReader reader = null;
		BufferedWriter writer = null;
		String currentLine = null;
		String tailLine = null;
		Map<String, String> colorMap = null;
		try {

			String tempPath = targetFile.getAbsolutePath().replaceAll(
					"\\.xml$", "_");
			tempFile = new File(tempPath + "NoDup.xml");

			reader = new BufferedReader(new FileReader(targetFile));
			writer = new BufferedWriter(new FileWriter(tempFile));

			Set<String> set = new HashSet<String>();
			// Add the title bar into the new file
			currentLine = reader.readLine();
			if (currentLine != null) {
				writer.write(currentLine);
				writer.newLine();
			}

			long advertiserID = getRakutenAdvertiserID(targetFile.getName());
			if (advertiserID == 0) {
				logger.error(Log.message(targetFile.getName()
						+ " is a invalid file, please check the source !!!"));
				return;
			}
			
			/**
			 * if advertiserID:
			 * - 38501 (Shoes.com)		--> 	No need to check availability
			 * - others 				--> 	check availability first
			 */
			while ((currentLine = reader.readLine()) != null) {
				if ((advertiserID == RakutenConstants.SHOESCOM_ADVERTISERID && currentLine.contains("sku_number=")) || (advertiserID != RakutenConstants.SHOESCOM_ADVERTISERID && currentLine.contains("<availability>in-stock</availability>"))) {
					String identifyNumber = getIdentifyNumberNew(currentLine, advertiserID, colorMap);
					if (!set.contains(identifyNumber)) {
						set.add(identifyNumber);
						writer.write(currentLine);
						writer.newLine();
					} else {
					}
				} else {
					tailLine = currentLine;
				}
			}
			writer.write(tailLine);
			logger.info("NoDup Tail Line " + tailLine
					+ " -- In file : " + targetFile.getName());

			if ((tempFile.length() / 1024) > 3500) {
				largeFiles.add(tempFile);
			}
		} catch (IOException e) {
			logger.error(Log.message("Current Line : " + currentLine
					+ "  ------->  At " + targetFile.getName()));
			logger.error(Log
					.message("Issues in removeDuplicatesSKUandNonInStock : "
							+ e.getMessage()));
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
				}
				logger.info("How Many files need to split ?? "
						+ largeFiles.size());
				logger.info(Log
						.message("Finish remove duplicates and out-stock items in "
								+ targetFile.getName()));
			} catch (IOException e) {
				logger.error(Log
						.message("Issues in removeDuplicatesSKUandNonInStock : "
								+ e.getMessage()));
				e.printStackTrace();
			}
		}
	}

	private String getIdentifyNumberNew(String currentLine, long advertiserID, Map<String, String> colorMap) {
		if (currentLine == null || currentLine.length() == 0) {
			System.out.println("Issues in getSKU : " + currentLine);
			return null;
		}
		String res = null;
		int steps;
		int indexBegin;
		int indexEnd;
		String partNumber = "";
		String manuName;
		String name;
		String color = "";
		String prodNumberURL;
		try {
			/**
			 *  adverstiser 
			 *  - Shoes.com (38501) 	--> 	use product name as identify name
			 *  - Sephora (2417)		-->		use product name as identify name
			 *  - NordStrom (1237)		-->		use manufacturer name and part number combination as identify name
			 *  - JCPenny (788)			-->		use name + "-" + color as identify name
			 *  - Kohl's (38605)		-->		use name + color as identify name 
			 *  - Nike (38660)			-->		use part number as identify name 
			 *  - others				-->		use sku as identify name
			 */
			if (advertiserID == RakutenConstants.SHOESCOM_ADVERTISERID || advertiserID == RakutenConstants.SEPHORA_ADVERTISERID) {
				steps = 6;
				indexBegin = currentLine.indexOf("name='") + steps;
				indexEnd = getSKULastIndex(currentLine, indexBegin);
				res = currentLine.substring(indexBegin, indexEnd);
			} else if (advertiserID == RakutenConstants.NORDSTROM_ADVERTISERID) {
				steps = 13;
				indexBegin = currentLine.indexOf("part_number='") + steps;
				indexEnd = getSKULastIndex(currentLine, indexBegin);
				partNumber = currentLine.substring(indexBegin, indexEnd);
	
				if (!currentLine.contains("manufacturer_name='")) {
					res =  partNumber;
				} else {
					steps = 19;
					indexBegin = currentLine.indexOf("manufacturer_name='")
							+ steps;
					indexEnd = getSKULastIndex(currentLine, indexBegin);
					manuName = currentLine.substring(indexBegin, indexEnd);
					res =  manuName + "-" + partNumber;
				}
			} else if (advertiserID == RakutenConstants.KOHLS_ADVERTISERID) {
				name = StringEscapeUtils.unescapeXml(getSubString(currentLine, "name='")).split(",")[0];
				if(currentLine.contains("part_number='")) {
					partNumber = "-" + StringEscapeUtils.unescapeXml(getSubString(currentLine, "part_number='"));
				}
				String cate = StringEscapeUtils.unescapeXml(getSubString(currentLine, "<primary>"));
				res =  cate + "-" + name + partNumber;
			} else if (advertiserID == RakutenConstants.JCPENNY_ADVERTISERID) {
				name = StringEscapeUtils.unescapeXml(getSubString(currentLine, "name='"));
				if(currentLine.contains("<Color>")) {
					color = getSubString(currentLine, "<Color>");
				}
				String sku = StringEscapeUtils.unescapeXml(getSubString(currentLine, "sku_number='"));
				String category = StringEscapeUtils.unescapeXml(getSubString(currentLine, "<primary>"));
				String key = category + "-" + name + "-" + sku;
				colorSKUMap.put(key, color);
				// logger.info("Color MAP Added: [ " + key + " -- " + color + " ]"));
				res =  category + "-" + name + '-' + color;
			} else if (advertiserID == RakutenConstants.NIKE_ADVERTISERID) {
				steps = 13;
				indexBegin = currentLine.indexOf("part_number='") + steps;
				indexEnd = getSKULastIndex(currentLine, indexBegin);
				res = currentLine.substring(indexBegin, indexEnd);
			} else {
				steps = 12;
				indexBegin = currentLine.indexOf("sku_number='") + steps;
				indexEnd = getSKULastIndex(currentLine, indexBegin);
				res =  currentLine.substring(indexBegin, indexEnd);
			}
		} catch(Exception e) {
			logger.error(Log
					.message("Issues in getIdentifyNumberNew : "
							+ e.getMessage()));
			e.printStackTrace();
		}
		return res;
	}
	
	private String getSubString(String currentLine, String pattern) {
		int steps = 0;
		if (pattern.equalsIgnoreCase("name='")) {
			steps = 6;
		} else if (pattern.equalsIgnoreCase("part_number='")) {
			steps = 13;
		} else if (pattern.equalsIgnoreCase("manufacturer_name='")) {
			steps = 19;
		} else if (pattern.equalsIgnoreCase("%2Fprd-")) {
			steps = 7;
		} else if (pattern.equalsIgnoreCase("<Color>")) {
			steps = 7;
		} else if (pattern.equalsIgnoreCase("<primary>")) {
			steps = 9;
		} else {
			steps = 12;
		}
		int indexBegin = currentLine.indexOf(pattern) + steps;
		int indexEnd = indexBegin;
		if(pattern.equalsIgnoreCase("%2Fprd-")) {
			indexEnd = getProdNumberLastIndexFromURL(currentLine, indexBegin);			
		} else if(pattern.equalsIgnoreCase("<Color>")) {
			indexEnd = getColerLastIndex(currentLine, indexBegin);			
		} else if(pattern.equalsIgnoreCase("<primary>")) {
			indexEnd = getColerLastIndex(currentLine, indexBegin);			
		} else {
			indexEnd = getSKULastIndex(currentLine, indexBegin);
		}
		return currentLine.substring(indexBegin, indexEnd).trim();
	}
	
	private static int getProdNumberLastIndexFromURL(String line, int index) {
		int res = index;
		for (int i = index; i < line.length(); i++) {
			if (line.charAt(i) == '%') {
				res = i;
				break;
			}
		}
		return res;
	}
	
	private static int getColerLastIndex(String line, int index) {
		int res = index;
		for (int i = index; i < line.length(); i++) {
			if (line.charAt(i) == '<') {
				res = i;
				break;
			}
		}
		return res;
	}

	private String getIdentifyNumber(String currentLine, String pattern) {
		if (currentLine == null || currentLine.length() == 0) {
			System.out.println("Issues in getSKU : " + currentLine);
			return null;
		}
		int steps;
		if (pattern.equalsIgnoreCase("part_number='")) {
			steps = 13;
		} else {
			steps = 12;
		}
		int skuIndexBegin = currentLine.indexOf(pattern) + steps;
		int skuIndexEnd = getSKULastIndex(currentLine, skuIndexBegin);
		return currentLine.substring(skuIndexBegin, skuIndexEnd);
	}

	private int getSKULastIndex(String line, int index) {
		int res = index;
		for (int i = index; i < line.length(); i++) {
			if (line.charAt(i) == '\'') {
				res = i;
				break;
			}
		}
		return res;
	}

	// get Rakuten advertiser ID
	private long getRakutenAdvertiserID(String name) {
		if (name == null || name.length() <= 0) {
			return 0;
		}
		String[] list = name.split("_");
		return Long.parseLong(list[0]);
	}

	public static Map<String, String> readProductColorFromProp() {
		String propertiesPath = Play.configuration.getProperty("cron.job.sync.rakuten.color.properties");
		Map<String, String> colorTable = new Hashtable<String, String>();
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(propertiesPath);
			prop.load(input);
			for (String key : prop.stringPropertyNames()) {
				if (!colorTable.containsKey(key)) {
					colorTable.put(key, key);
					// logger.info("Added Color -- " + key));
				}
			}
		} catch (IOException e) {
			logger.error(Log
					.message("Issues in getRakutenSellserMapFromProp : "
							+ e.getMessage()));
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
					input = null;
				} catch (IOException e) {
					logger.error(Log
							.message("Reading Properties file has issue"
									+ e.getMessage()));
					e.printStackTrace();
				}
			}
		}
		return colorTable;
	}
}
