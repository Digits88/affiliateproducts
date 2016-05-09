/**
 * 
 */
package services.image;



import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import play.cache.Cache;
import utils.image.ImageUtils;
import utils.imagemagick.identify.Identify;
import utils.imagemagick.identify.ImageIdentity;
import utils.imagemagick.identify.ImageIdentityService;

/**
 * @author Greg Drogaline
 * 
 */
public class ImageHelper {

	private static final String PREFIX = "processor_";
	
	public static Logger logger = LogManager.getLogger(ImageHelper.class);

	public static ImageProcessor getProcessor(String imageName) {
		logger.debug(String
				.format("ImageHelper getProcessor for %s", imageName));
		String imageFormat = null;
		if (imageName != null) {
			
			imageFormat = (null == Cache.get(PREFIX.concat(imageName))?null:Cache.get(PREFIX.concat(imageName)).toString());
			
			if (imageFormat == null) {

				List<String> files = new ArrayList<String>();
				files.add(imageName);
				List<ImageIdentity> ids = ImageIdentityService.getInstance()
						.getIdentityFromFileList(files);
				if (ids != null && !ids.isEmpty()) {
					
					Cache.add(PREFIX.concat(imageName), ids.get(0).getFormat(),ImageUtils.getCachetime());
					
					imageFormat = ids.get(0).getFormat();	
				}
			}

			if(imageFormat != null && Identify.SUPPORTED_FORMATS.JPEG.name().equals(imageFormat)){
				return JPGProcessor.getImageProcessor();
			}else if (imageFormat != null && Identify.SUPPORTED_FORMATS.PNG.name().equals(imageFormat)){
				return PNGProcessor.getImageProcessor();
			}

		}
		return null;
	}

}
