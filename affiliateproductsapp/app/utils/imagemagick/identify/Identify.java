/**
 * 
 */
package utils.imagemagick.identify;



/**
 * @author Greg Drogaline
 * 
 */
public interface Identify {

	public static final String TOKEN  = ":";
	public static final String IMAGE  = "Image:";
	public static final String FORMAT = "Format:";
	public static final String GEOMETRY = "Geometry:";
	
	public static enum SUPPORTED_FORMATS {
		PNG, JPEG;

		public static boolean contains(String s) {
			for (SUPPORTED_FORMATS SUPPORTED_FORMATS : values())
				if (SUPPORTED_FORMATS.name().equals(s))
					return true;
			return false;
		}
	}


}
