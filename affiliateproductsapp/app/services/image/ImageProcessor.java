package services.image;


import java.io.File;


/**
 * Interface for all image processing and optimizations.
 * 
 * @author Greg Drogaline
 *
 */
public interface ImageProcessor {

	public static String CONVERT_SCRIPT = "_convert.sh";
	public static String TRANSCODE_SCRIPT ="_transcode.sh";
	public static String PLANE  = "Plane";
	public static String LINE  = "Line";
	public static String DEFAULT_QUALITY = "72";
	/**
	 * @param source - file
	 * @param outputdirectory - optimized location file. if omitted same file location will be processed.
	 * @return true - if processed completed successfully.
	 */
	public boolean optimize(File source , File outputdirectory);
	
	/**
	 * @param sourceFilePath
	 * @param wide
	 * @param cropHeight
	 * @return - transcode file name ..
	 */
	public String transcode(File sourceFile , Long wide , Long cropHeight);
	
	public String getName();
	
}
