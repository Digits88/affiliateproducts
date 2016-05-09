package image_test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.validator.routines.UrlValidator;

import com.google.gson.Gson;

import utils.AffiliateStringUtil;

public class ImageTest {

	public static void main(String[] args) throws MalformedURLException, IOException {
		/*
		 * String orig = "1481930";
		 * 
		 * // encoding byte array into base 64 byte[] encoded =
		 * Base64.encodeBase64(orig.getBytes());
		 * 
		 * String en = new String(Base64.encodeBase64(orig.getBytes()));
		 * 
		 * System.out.println("Original String: " + orig); System.out.println(
		 * "Base64 Encoded String : " + en);
		 * 
		 * // decoding byte array into base64 byte[] decoded =
		 * Base64.decodeBase64(encoded); String de = new
		 * String(Base64.decodeBase64(en)); System.out.println(
		 * "Base 64 Decoded  String : " + de);
		 * 
		 * 
		 * String url =
		 * "http://athleta.gap.com/webcontent/0010/365/012/cn10365012.jpg";
		 * System.out.println(URLEncoder.encode(url, "UTF-8"));
		 * 
		 * 
		 * UrlValidator urlValidator = new UrlValidator();
		 * System.out.println(urlValidator.isValid(
		 * "http://athleta.gap.com/webcontent/0010/365/012/cn10365012.jpg"));
		 * System.out.println(new
		 * URL(url).openConnection().getContentLength()/1024);
		 * 
		 * 
		 * String tmpPath =
		 * "C:\\Users\\lwan0\\Desktop\\image\\tmp\\2015-mustang.jpg";
		 * Thumbnails.of(new File(tmpPath)).size(400,
		 * 400).outputFormat("jpg").toFile(new File(tmpPath.replace(".jpg",
		 * "")+"400x400.jpg"));
		 */

		/*
		 * String sss =
		 * "http%3A%2F%2Fimages.tedbakerimages.com%2FDA4M_ROCKALL_99-WHITE_1.jpg.jpg%3Fo%3DBEOjnFFUSFZYTNKE4jMbZSIShrEj%26V%3Dy3p1%26";
		 * sss = URLDecoder.decode(sss, "UTF-8"); System.out.println(sss);
		 */

		/*String s = "https://scontent.cdninstagram.com/hphotos-xaf1/t51.2885-19/11203306_1104977476184370_1805942895_a.jpg";
		String t = new String(Base64.encodeBase64(s.getBytes()));
		System.out.println(t);

		String k = new String(Base64.decodeBase64("60cde86fde774630591f3310d620c14c"));
		System.out.println(k);

		String imageUrl = "https://scontent-lga3-1.cdninstagram.com/hphotos-xfa1/t51.2885-19/11378954_941146375917043_792136484_a.jpg";
		imageUrl = URLEncoder.encode(imageUrl, "UTF-8");
		System.out.println(imageUrl);

		imageUrl = URLDecoder.decode(imageUrl, "UTF-8");
		System.out.println(imageUrl);

		String input = "GameStop, Inc.";
		String alphaAndDigits = input.replaceAll("[^a-zA-Z0-9]+", "");
		System.out.println(alphaAndDigits);*/

		/*
		 * URL url = new URL("https://www.facebook.com/images/spacer.gif"); int
		 * x = url.openConnection().getContentLength(); System.out.println(x);
		 */

		/*File f = new File("C:\\Users\\lwan0\\Desktop\\image\\Seller\\Oakley.jpg");
		long y = f.length();
		System.out.println(y);

		String google = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
		String search = "stackoverflow";
		String charset = "UTF-8";

		URL url = new URL("https://ajax.googleapis.com/ajax/services/search/images?"
				+ "v=1.0&q=barack%20obama&userip=INSERT-USER-IP");
		URLConnection connection = url.openConnection();
		connection.addRequestProperty("Referer", "www.google.com");

		String line;
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		System.out.println(builder.toString());*/
		// JSONObject json = new JSONObject(builder.toString());
		// now have some fun with the results...
		
		String destinationFile = "C:\\Users\\lwan0\\Desktop\\abc.jpg";
		storeUserProfilePicture(binaryData, destinationFile);
		System.out.println(">>>>> Done <<<<<");
	}
	
	public static boolean storeUserProfilePicture(String binaryData, String destinationFile) {
		// Check if the image url is valid or not
		if (binaryData == null || binaryData.trim().length() == 0) {
			System.out.println("##### " + binaryData + " is null or empty value, please check ! #####");
			return false;
		}
		
		// download image
		InputStream is = null;
		OutputStream os = null;
		try {

			File downloadFile = new File(destinationFile);

			File parentDir = downloadFile.getParentFile();
			if (!parentDir.exists()) {
				parentDir.mkdir();
			}
			String imageDataBytes = binaryData.substring(binaryData.indexOf(",") + 1);
			byte[] image = Base64.decodeBase64(imageDataBytes.getBytes());
			/*is = new ByteArrayInputStream(image);
			os = new FileOutputStream(destinationFile);
			byte[] b = new byte[2048];
			int length;
			while ((length = is.read(b)) != -1) {
				os.write(b, 0, length);
			}*/
			OutputStream stream = new FileOutputStream(destinationFile);
			stream.write(image);
			stream.close();
		} catch (IOException e) {
			System.out.println("##### " + e.getMessage() + " #####");
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (is != null) {
					is.close();
					is = null;
				}
				if (os != null) {
					os.close();
					os = null;
				}
				// check if download if successful
				if (new File(destinationFile).length() > 0) {
					System.out.println(">>>>> Downloaded -- " + destinationFile + " <<<<<");
				} else {
					System.out.println(
							"##### The size of download file is not same as remote file, please check !  #####");
					return false;
				}
			} catch (IOException e) {
				System.out.println("##### " + e.getMessage() + " #####");
				e.printStackTrace();
			}
		}
		return true;
	}

}