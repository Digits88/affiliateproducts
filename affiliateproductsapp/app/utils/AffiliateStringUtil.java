package utils;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;

public class AffiliateStringUtil {
	
	public static String afterUnEscapeHtmlXml(String string) {
		if (string != null) {
			for (int i = 0; i < 5; i++) {
				string = StringEscapeUtils.unescapeHtml(string);
				string = StringEscapeUtils.unescapeXml(string);
			}
		}
		return string;
	}
	
	public static void main(String args[]){
		
		//ChefÃ¢Â€Â™sChoice Professional Sharpening Station 130
		//Bormioli Rocco Ã¢Â€ÂœQuattro StagioniÃ¢Â€Â Jar
		//String product = "Sur La TableÂ® Platinum Professional Mini Muffin Pan";
		String product = "ChefÃ¢Â€Â™sChoice Professional Sharpening Station 130";
		product = product.replace("Ã‚Â®", "®");
		product = product.replace("Ã¢Â€Â™", "'");
		System.out.println(product);
		
	}

}
