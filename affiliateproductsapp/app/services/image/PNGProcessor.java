package services.image;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import play.Play;
import utils.imagemagick.identify.Identify;

import com.googlecode.pngtastic.PngtasticOptimizer;
import com.googlecode.pngtastic.core.PngImage;
import com.googlecode.pngtastic.core.PngOptimizer;


/**
 * @author Greg Drogaline
 * 
 */
public class PNGProcessor extends Processor {

	static Logger logger = LogManager.getLogger(PNGProcessor.class);
	
	//png.compression.level default 2 .. options: 0-9 allowed 
	private static Integer compressionLevel;
	//png.log.level : default none .. options: debug, info, or error
	private static String logLevel;
	


	static {
		compressionLevel = new Integer(Play.configuration.getProperty("png.compression.level" ,"2"));
		logLevel = Play.configuration.getProperty("png.log.level" , "none");

	}
	
	private PNGProcessor(){}
	
	public static ImageProcessor getImageProcessor(){
		return new PNGProcessor();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see service.compression.ImageProcessor#optimize(java.io.File,
	 * java.io.File)
	 */
	@Override
	public boolean optimize(File source, File outputdirectory) {

		if (source != null && source.exists()) {
			logger.debug(String.format("Found %s %s %s %s", source.getName(),  "at",source.getParentFile().getAbsolutePath(), "location")); 
			

			try {
				if (outputdirectory != null) {
					if (!outputdirectory.exists())
						outputdirectory.mkdirs();

					pngtasticOptimize(outputdirectory.getAbsolutePath(),
							source.getAbsolutePath(), null, false, null);

				} else {
					pngtasticOptimize(null, source.getAbsolutePath(), null,
							false, null);

				}
			} catch (Exception e) {
				return false;
			}
			return true;
		}

		return false;

	}

	private void pngtasticOptimize(String toDir, String fileName,
			String fileSuffix, Boolean removeGamma, 
			String compressor) throws Exception {

		long start = System.currentTimeMillis();

		PngOptimizer optimizer = new PngOptimizer(logLevel);
		optimizer.setCompressor(compressor);

		try {
			String outputPath = null;
			if (toDir != null && toDir.trim().length() > 0) {
				outputPath = toDir
						+ "/"
						+ fileName.substring(fileName.substring(
								0,
								(0 == fileName.lastIndexOf('/') ? 0 : (fileName
										.lastIndexOf('/') + 1))).length());
			} else {
				outputPath = fileName;
			}

			makeDirs(outputPath.substring(0, outputPath.lastIndexOf('/')));

			PngImage image = new PngImage(fileName);
			logger.debug(String.format( "Source file %s %s %s", image.getFileName() , "output file" ,outputPath ));
			optimizer.optimize(image, outputPath
					+ (null == fileSuffix ? "" : fileSuffix.trim()),
					removeGamma, compressionLevel);
		} catch (Exception e) {
			logger.error(String.format("PNG processing exception : %s", e.getMessage()));
			throw e;
		}

		logger.debug(String.format( "Processed %s %s %s %s %s %s",
						optimizer.getStats().size(), "files in", 
						System.currentTimeMillis() - start,
						"milliseconds, saving",
						optimizer.getTotalSavings(),
						"bytes"));
	}



	@Override
	public String getName() {
		return Identify.SUPPORTED_FORMATS.PNG.name();
	}

	@Override
	public String transcode(File sourceFile, Long wide, Long cropHeight) {
		throw new RuntimeException("PNGProcessor.transcode(File sourceFile, Long wide, Long cropHeight) not implemented..");
	}
}
