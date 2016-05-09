package utils.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import play.Play;
import play.libs.Codec;

public class ImageUtils {

	static Logger logger = LogManager.getLogger(ImageUtils.class);

	private static String directory;
	private static String dwbdirectory;	
	private static String resourcedirectory;
	
	private static String imagecontext;
	private static String cachetime;
	private static boolean cacheenabled = false;
	private static boolean optimizationEnabled = false;
	private static boolean transcodenabled = false;
	
	private static String accesscontrolhost;

	static {
		directory = Play.configuration.getProperty("image.directory");
		accesscontrolhost = Play.configuration.getProperty("access.control.host","*.searshc.com");
		dwbdirectory = Play.configuration.getProperty("image.dwbdirectory",directory.concat(File.separator).concat("dwb").concat(File.separator));
		resourcedirectory = Play.configuration.getProperty("image.resourcedirectory",directory.concat(File.separator).concat("resources").concat(File.separator));
		
		imagecontext = Play.configuration.getProperty("image.server.context.path");
		cachetime = Play.configuration.getProperty("default.cache.time");
		cacheenabled = Boolean.valueOf(Play.configuration.getProperty("cache.enabled", "false"));
		optimizationEnabled = Boolean.valueOf(Play.configuration.getProperty("image.optimize.enabled", "false"));
		transcodenabled = Boolean.valueOf(Play.configuration.getProperty("image.transcode.enabled","false"));
	}

	final static int MIN_NAME_SIZE = "1/1.png".length();
	
	public static Map<String, String> getFileImageMime() {
		
		String [] types = Play.configuration.getProperty("mime.type.image","").split(",");
		Map<String, String>  map = new HashMap<String, String>();
		for(String type:types){
			String mime = Play.configuration.getProperty("mime.image.".concat(type),"");
			if(mime != null && !mime.isEmpty()){
				map.put(type, mime);
			}
		}
		
		
		return map;
	}

	public static Map<String, String> getFileTextMime(){
		String [] types = Play.configuration.getProperty("mime.type.text","").split(",");
		Map<String, String>  map = new HashMap<String, String>();
		for(String type:types){
			String mime = Play.configuration.getProperty("mime.text.".concat(type),"");
			if(mime != null && !mime.isEmpty()){
				map.put(type, mime);
			}
		}
		
		
		return map;
	}
	
	//
	public static Set<String> getEnvToCache(){
		String [] envs = Play.configuration.getProperty("environment.cache.enabled","").split(",");
		Set<String>  set = new HashSet<String>();
		for(String env:envs){
			set.add(env);
		}
		return set;
	}
	
	public static long getUserIdFromName(String imageName) {

		String name = getImageName(imageName);
		String id = name.substring(0, (name.indexOf(File.separator)));
		try {
			return Long.valueOf(id).longValue();
		} catch (NumberFormatException ex) {
			logger.error(ex);
		}

		return 0;
	}

	public static String getExtension(String imageName) {
		if (imageName != null && imageName.contains(".") && imageName.trim().length() > 2) {
			return imageName.substring(imageName.lastIndexOf(".") + 1)
					.toLowerCase();
		} else
			return "";
	}

	public static String getImageName(String absolutePath) {
		if (absolutePath != null
				&& absolutePath.trim().length() >= MIN_NAME_SIZE) {
			File dir = new File(absolutePath.substring(0,
					absolutePath.lastIndexOf(File.separator)));
			if (dir.exists() && dir.isDirectory()) {
				return dir.getName() + File.separator + new File(absolutePath).getName();
			}
		}
		return absolutePath;
	}

	public static String getNewTempName(String name) {
		return new StringBuilder().append(name).append(".")
				.append(Codec.UUID().hashCode()).append(".")
				.append(ImageUtils.getExtension(name)).toString();
	}

	public static boolean copyFromTo(File from, File to) {

		try {
			File f1 = from;
			File f2 = to;
			InputStream in = new FileInputStream(f1);
			// For Overwrite the file.
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (FileNotFoundException ex) {
			logger.error(ex);
			return false;
		} catch (IOException e) {
			logger.error(e);
			return false;
		}
		return true;
	}

	/**
	 * @return the directory
	 */
	public static final String getDirectory() {
		return directory;
	}

	/**
	 * @return the directory
	 */
	public static final String getDwbDirectory() {
		return dwbdirectory;
	}
	
	/**
	 * @return the imagecontext
	 */
	public static final String getImagecontext() {
		return imagecontext;
	}

	/**
	 * @return the cachetime
	 */
	public static final String getCachetime() {
		return cachetime;
	}

	/**
	 * @return the cacheenabled
	 */
	public static final boolean isCacheenabled() {
		return cacheenabled;
	}

	public static final boolean isOptimizationEnabled() {
		return optimizationEnabled;
	}
	
	public static final boolean isTranscodEnabled(){
		return transcodenabled;
	}

	public static String getResourceDirectory() {
		logger.debug(resourcedirectory);
		return resourcedirectory;
	}
	
	public static String getAccessControlHost() {
		logger.debug(resourcedirectory);
		return accesscontrolhost;
	}	
}
