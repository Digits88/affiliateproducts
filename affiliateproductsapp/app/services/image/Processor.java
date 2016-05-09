/**
 * 
 */
package services.image;

import java.io.File;
import java.io.IOException;

/**
 * @author gregd
 *
 */
public abstract class Processor implements ImageProcessor {



	
	public String makeDirs(String path) throws IOException {
		File out = new File(path);
		if (!out.exists()) {
			if (!out.mkdirs()) {
				throw new IOException("Couldn't create path: " + path);
			}
		}
		return out.getCanonicalPath();
	}

}
