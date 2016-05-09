package services.image;


import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.im4java.core.CommandException;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.MogrifyCmd;
import org.im4java.process.ProcessStarter;

import play.Play;
import play.libs.Codec;
import utils.imagemagick.identify.Identify;

/**
 * @author Greg Drogaline
 * 
 */
public class JPGProcessor extends Processor {

	static Logger logger = LogManager.getLogger(JPGProcessor.class);

	private static Double quality;
	private static String tempDir;
	private static String imagemagick;

	static {
		quality = Double.valueOf(Play.configuration.getProperty("jpg.quality",
				DEFAULT_QUALITY));
		tempDir = Play.configuration.getProperty("jpg.temp.dir", File.separator.concat("tmp"));
		imagemagick = Play.configuration.getProperty("imagemagick.home.path");
	}

	private JPGProcessor() {
	}

	public static ImageProcessor getImageProcessor() {
		return new JPGProcessor();
	}

	/* (non-Javadoc)
	 * @see service.image.ImageProcessor#optimize(java.io.File, java.io.File)
	 */
	@Override
	public boolean optimize(File source, File output) {
		//
		if (source == null || !source.exists() || !source.isFile() || output == null)
			return false;

		IMOperation op = new IMOperation();
		
		op.strip();
		op.interlace(PLANE);
		op.quality(quality);
		op.addImage(source.getAbsolutePath());
		op.addImage(output.getAbsolutePath());
		
		ConvertCmd convert = new ConvertCmd();

		try {
			logger.debug("jpeg convert " + op.toString());
			convert.createScript(
					source.getAbsolutePath().concat(CONVERT_SCRIPT), op);
			convert.run(op);
			
		} catch (CommandException ce) {
			logger.error(ce);
			ArrayList<String> cmdError = ce.getErrorText();
			for (String line : cmdError) {
				logger.error(line);
			}
			File script = new File(source.getAbsolutePath().concat(CONVERT_SCRIPT));
			if(script != null && script.exists()){
				script.delete();
			}
			return false;
		} catch (Exception e) {
			logger.error(e);
			File script = new File(source.getAbsolutePath().concat(CONVERT_SCRIPT));
			if(script != null && script.exists()){
				script.delete();
			}
			return false;
		}
		File script = new File(source.getAbsolutePath().concat(CONVERT_SCRIPT));
		if(script != null && script.exists()){
			script.delete();
		}
		return true;
	}

	@Override
	public String getName() {
		return Identify.SUPPORTED_FORMATS.JPEG.name();
	}

	
	/**
	 * @param sourceFilePath
	 * @param wide
	 * @param cropHeight
	 * @return
	 */
	public String transcode(File sourceFile , Long wide , Long cropHeight){
		
		ProcessStarter.setGlobalSearchPath(imagemagick);
		
		if (sourceFile == null || !sourceFile.exists() || !sourceFile.isFile() )
			return null;

		IMOperation op = new IMOperation();
		// convert -strip -interlace Plane -quality 72 imgOutFile.getAbsolutePath() 
		//      -resize dim[0]  -gravity Center -crop "x"dim[1]" /tmp/{uuid}.jpg
		//
		op.strip();
        op.interlace(PLANE);
		op.quality(quality);

		op.addImage(sourceFile.getAbsolutePath());
		op.resize(wide.intValue());
		
		if(cropHeight != null && cropHeight.intValue() > 0){
			op.gravity("Center");
			op.crop(wide.intValue(), cropHeight.intValue() , 0 , 0);
		}
		
		String file = tempDir.concat(File.separator).concat(Codec.UUID()).concat(".jpg");
		
		op.addImage(file);
		
		ConvertCmd convert = new ConvertCmd();

		try {
			logger.debug("Image file transcode options : " + op.toString());
			// convert.createScript(file.concat(TRANSCODE_SCRIPT), op);
			convert.run(op);
			
		} catch (CommandException ce) {
			logger.error(ce);
			ArrayList<String> cmdError = ce.getErrorText();
			for (String line : cmdError) {
				logger.error(line);
			}
			File script = new File(file.concat(TRANSCODE_SCRIPT));
			if(script != null && script.exists()){
				script.delete();
			}
			return null;
		} catch (Exception e) {
			logger.error(e);
			File script = new File(file.concat(TRANSCODE_SCRIPT));
			if(script != null && script.exists()){
				script.delete();
			}
			return null;
		}
		File script = new File(file.concat(TRANSCODE_SCRIPT));
		if(script != null && script.exists()){
			script.delete();
		}	
	
		return file;
	}
}
