import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

import constants.RakutenConstants;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.KShingling;
import info.debatty.java.stringsimilarity.LongestCommonSubsequence;
import info.debatty.java.stringsimilarity.NGram;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import info.debatty.java.stringsimilarity.StringProfile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.*;
import play.test.*;
import services.impactradius.ImpactRadiusFileService;
import utils.AffiliateStringUtil;
import utils.log.Log;
import models.*;

public class BasicTest extends UnitTest {

	@Test
	public void aVeryImportantThingToTest() {
		assertEquals(2, 1 + 1);
	}

	public static void main(String[] args) throws Exception {
		
		String s = " /home/auto/syunus/image/Banner/506/506_Fount Marketing page_20160415_081045449_1200x300.gif";
		System.out.println(s.replaceAll("\\s+", ""));
		/*String s = AffiliateStringUtil.afterUnEscapeHtmlXml("20 Ãxitos Originales");
		String s1 = StringEscapeUtils.escapeJava("20 Ãxitos Originales");
		
		String s2 = StringEscapeUtils.unescapeJava(s1);
		
		System.out.println(s);
		System.out.println(s1);
		System.out.println(s2);*/
		
		/*
		 * System.gc(); System.out.println("Total : " +
		 * (Runtime.getRuntime().totalMemory())/(1024*1024));
		 * System.out.println("MAX : " +
		 * (Runtime.getRuntime().maxMemory())/(1024*1024)); System.out.println(
		 * "Free : " + (Runtime.getRuntime().freeMemory())/(1024*1024));
		 * 
		 * Set<String> set = new HashSet<String>(); String s =
		 * "http%3A%2F%2Fathleta.gap.com%2Fwebcontent%2F0008%2F214%2F960%2Fcn8214960.jpg&sid=11&id=Mjk4MQ==&w=300&h=300";
		 * for(int i = 0; i < 50000; i++) { set.add(s + String.valueOf(i));
		 * System.out.println((Runtime.getRuntime().totalMemory() -
		 * Runtime.getRuntime().freeMemory())/(1024*1024)); }
		 * System.out.println((Runtime.getRuntime().totalMemory() -
		 * Runtime.getRuntime().freeMemory())/(1024*1024));
		 */

		/*System.out.println(StringEscapeUtils.unescapeXml("Toddler Shop &gt; Bedding &gt; Kids Sheets"));

		System.out.println(StringEscapeUtils.unescapeXml("Toddler Shop &gt; Bedding &gt; Kids Sheets"));*/

		// splitSoapFeed(new
		// File("C:\\Users\\lwan0\\Desktop\\tmp\\cj\\tmp\\Soapcom\\Soapcom-Product-Catalog_IR.xml"));
		// splitDiapersFeed(new
		// File("C:\\Users\\lwan0\\Desktop\\tmp\\cj\\tmp\\Diaperscom\\Diaperscom-Product-Catalog_IR.xml"));

		/*String s = "<product product_id='13060412' name='Arsenal Gaming PS3 High Definition Kit, Black' sku_number='0069098115080' manufacturer_name='Arsenal Gaming' part_number='0069098115080'><category><primary>Play Station 3</primary><secondary>Video Games</secondary></category><URL><product>http://linksynergy.walmart.com/link?id=TferdD6Qzek&amp;offerid=223073.13060412&amp;type=15&amp;murl=http%3A%2F%2Fwww.walmart.com%2Fip%2FArsenal-Gaming-PS3-High-Definition-Kit-Black-Playstation-3%2F13060412</product><productImage>http://i5.walmartimages.com/dfw/dce07b8c-9b58/k2-_a13b8e6d-8904-49e7-89e6-a34d339c67c3.v1.jpg</productImage></URL><description><short>This kit comes complete with all the essentials accessories you need to better enhance your game.</short><long>Arsenal Gaming PS3 High Definition Kit: PS3 Bluetooth controller 6&apos; HDMI cable Docking station charges 2 Bluetooth controllers USB charging cable Color: black</long></description><discount currency='USD'><type>amount</type></discount><price currency='USD'><retail>29.88</retail></price><brand>Arsenal Gaming</brand><shipping><cost currency='USD'><amount>4.97</amount><currency>USD</currency></cost><information>4.97</information><availability>in-stock</availability></shipping><upc>00690981150803</upc><pixel>http://ad.linksynergy.com/fs-bin/show?id=TferdD6Qzek&amp;bids=223073.13060412&amp;type=15&amp;subid=0</pixel></product>";
		InputSource is = new InputSource(new StringReader(s));
		DOMParser dp = new DOMParser();
		dp.parse(is);
		Document doc = dp.getDocument();*/
		/*NodeList nl = doc.getElementsByTagName("primary");
		Node first = nl.item(0);
		NodeList n2 = doc.getElementsByTagName("secondary");
		Node second = n2.item(0);*/
		
		/*NodeList n3 = doc.getElementsByTagName("product");
		Node n = n3.item(0);
		NamedNodeMap nnm = n.getAttributes();
		System.out.println(nnm.getNamedItem("name").getFirstChild().getTextContent());*/

		/*System.out.println(first.getTextContent());
		System.out.println(second.getTextContent().concat(RakutenConstants.CATEGORY_LINK_WITH_SPACE).concat(first.getTextContent()));
		System.out.println(first.getNodeName());*/
		
		/*String res = getCategory(new File("C:\\Users\\lwan0\\Desktop\\tmp\\Rakuten\\Walmart\\2149_3259389_92563791_cmp.xml"));
		System.out.println(res);
		
		String f = "2149_3259389_92563791_cmp.xml.gz";
		System.out.println(f.replaceAll("_cmp.xml.gz$", ""));
		
		File file = new File("C:\\Users\\lwan0\\Desktop\\tmp\\Rakuten\\Walmart\\2149_3259389_92563791_cmp.xml.gz");
		System.out.println(file.getParent());*/

		/*		int x = StringUtils.getLevenshteinDistance("abc", "cba");
		System.out.println(x / 3);

		// produces 0.416666
		NGram twogram = new NGram();
		System.out.println(twogram.distance("ABCD", "ABTUIO"));
		*/
		/*String s1 = "Alpha Omega Elite Car Seat  Bromley";
		String s2 = "Safety 1st Alpha Omega Elite Convertible 3-in-1 Baby Car Seat - Chelsea";
		
		System.out.println("LCS: " + new LongestCommonSubsequence().distance(s1, s2));
		System.out.println("JW : " + new JaroWinkler().similarity(s1, s2));
		System.out.println("MLCS : " + new info.debatty.java.stringsimilarity.MetricLCS().distance(s1, s2));
		System.out.println("NG : " + new NGram().distance(s1, s2));
		
		File f = new File("C:\\Users\\lwan0\\Desktop\\tmp\\cj\\QVC_com\\split\\QVC_com-QVC_Product_Catalog.txt");
		split(f);
		KShingling ks = new KShingling(2);

        // For cosine similarity I need the profile of strings
        StringProfile profile1 = ks.getProfile(s1);
        StringProfile profile2 = ks.getProfile(s2);

        // Prints 0.516185
        System.out.println(profile1.cosineSimilarity(profile2));*/


		// produces 0.97222
		/*String s1 = "FFPD1821MB 18\" Portable Dishwasher - Black";
		String s2 = "Frigidaire - 18\" Portable Dishwasher - Black";
		NGram ngram = new NGram();
		System.out.println(ngram.distance(s1, s2));
		*/
		
	}

	private static Set<String> getCJBrandFile(File targetFile) {
		System.out.println("+++ Get CJ Brand " + targetFile.getName() + " is Started +++");
		File tempFile = null;
		// File temp = new File(path);

		BufferedReader reader = null;
		// BufferedWriter writer = null;
		int count = 0;
		Set<String> set = null;
		try {

			/*String tempPath = targetFile.getAbsolutePath().replaceAll("\\.txt$", "_");
			tempFile = new File(tempPath + "Brand.txt");*/

			reader = new BufferedReader(new FileReader(targetFile));
			// writer = new BufferedWriter(new FileWriter(tempFile));

			set = new HashSet<String>();
			// Add the title bar into the new file
			String currentLine = reader.readLine();
			/*if (currentLine != null) {
				writer.write(currentLine);
				writer.newLine();
			}*/
			String brand_name = null;
			while ((currentLine = reader.readLine()) != null) {
				String[] line = currentLine.split("\t");
				brand_name = line[8];
				if (!set.contains(brand_name)) {
					count++;
					set.add(brand_name);
					// writer.write(brand_name);
					// writer.newLine();
				}
			}
		} catch (IOException e) {
			System.out.println("### " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				/*if (writer != null) {
					writer.close();
					writer = null;
				}*/
				if (reader != null) {
					reader.close();
					reader = null;
				}
				/*if (targetFile.exists()) {
					targetFile.delete();
					tempFile.renameTo(temp);
					temp = null;
				}*/
				System.out.println("+++ Get CJ Brand " + targetFile.getName() + " is Finished +++");
				System.out.println("+++ Total Brand in " + targetFile.getName() + " is " + count + " +++");
			} catch (IOException e) {
				System.out.println("### " + e.getMessage());
				e.printStackTrace();
			}
		}
		return set;
	}
	
	public static void split(File targetFile) {
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
			// System.out.println("Lines in the file: " + count));

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
			// System.out.println("No. of files to be generated :" + nof));

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
			// System.out.println("Split -- " + targetFile.getName() +
			// " is done"));
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
			System.out.println("Issue in split : " + targetFile.getName());
			System.out.println("Issue in split : " + e.getMessage());
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
}
