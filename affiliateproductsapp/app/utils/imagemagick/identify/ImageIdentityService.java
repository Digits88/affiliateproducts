package utils.imagemagick.identify;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.im4java.core.CommandException;
import org.im4java.core.IMOperation;
import org.im4java.core.IdentifyCmd;
import org.im4java.process.ArrayListOutputConsumer;
import org.im4java.process.ProcessStarter;

import play.Play;

/**
 * @author Greg Drogaline
 * 
 */
public class ImageIdentityService {

	static Logger logger = LogManager.getLogger(ImageIdentityService.class);
	
	private static String imagemagick;
	static {
		imagemagick = Play.configuration.getProperty("imagemagick.home.path");
	}
	
			
	public static ImageIdentityService getInstance(){
		return new ImageIdentityService();
	}
	
	private ImageIdentityService() {
		
	}

	public List<ImageIdentity>  getIdentityFromFileList(List<String> files ){
		logger.info("###### Debug Start ######");
		ProcessStarter.setGlobalSearchPath(imagemagick);
		
		if(files == null || files.isEmpty()) return null;
		
		List<ImageIdentity> lst = null;
	    IdentifyCmd identify = new IdentifyCmd();
	    ArrayListOutputConsumer output = new ArrayListOutputConsumer();
	    identify.setOutputConsumer(output);
	    
	    IMOperation iso = new IMOperation();
	    iso.verbose();
	    for(String file:files)
	    	iso.addImage(file);
	    
	    try {
	      identify.run(iso);
	      
	    } catch (CommandException ce) {
	    	logger.error(ce);
	      ArrayList<String> cmdError = ce.getErrorText();
	      for (String line:cmdError) {
	    	  logger.error(line);
	      }
	    } catch (Exception e) {
	    	logger.error(e);
	    }
	    lst = FormatHelper.getListIdentityFromOutputConsumer(output);
	    logger.info("###### Debug lst size  is " + lst.size());
	    logger.info("###### Debug lst first is " + lst.get(0));
	    logger.info("###### Debug Finish ######");
	    return lst;
	}
}
