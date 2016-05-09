/**
 * 
 */
package utils.imagemagick.identify;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.im4java.process.ArrayListOutputConsumer;

/**
 * @author Greg Drogaline
 * 
 */
public class FormatHelper {
	
	private FormatHelper(){};
	
	static Logger logger = LogManager.getLogger(FormatHelper.class);
	
	public static List<ImageIdentity> getListIdentityFromOutputConsumer( ArrayListOutputConsumer output) {
		logger.info("###### Debug Start ######");
		List<ImageIdentity> imid = new ArrayList<ImageIdentity>();
		if( output != null && output.getOutput() != null){
			ArrayList<String> out = output.getOutput();
		    for(int index = 0;index < out.size();index++){
		    	ImageIdentity id = null;
		    	String elem = out.get(index).trim();
		    	  
				if (elem.startsWith(Identify.IMAGE)) {
					
					String[] tk = elem.split(Identify.TOKEN);
					String image = tk[1].trim();
					File f = new File(image);
					long fsize = f.length();
					index ++;
					String format = null;
					Long wide = null;
					Long height = null;
					for(;index < out.size();index ++){
						String subelem = out.get(index).trim();
						// parse format parameters .. 
						// ex.> Format: PNG (Portable Network Graphics)
						if (subelem.startsWith(Identify.FORMAT)) {
							String[] stk = subelem.split(Identify.TOKEN);
							String[] tka = stk[1].trim().split(" ");
							format = tka[0];
						}
						// parse geometry parameters .. 
						// ex.> Geometry: 530x6588+0+0
						if (subelem.startsWith(Identify.GEOMETRY)) {
							String[] stk = subelem.split(Identify.TOKEN);
							String[] tka = stk[1].trim().split("[x\\-\\+]");
							if(tka.length > 1){
								wide = Long.valueOf(tka[0]);
								height = Long.valueOf(tka[1]);
							}
						}
						
						if (subelem.startsWith(Identify.IMAGE)){
							index --;
							break;
						}
					}
					try {
						id = new ImageIdentity(fsize,format, image , wide , height);
						imid.add(id);
					} catch (UnsupportedIdentity e) {
						e.printStackTrace();
					}
				}
		    }
		}
		logger.info("###### Debug Finish ######");
		return imid;
	}
	
}
