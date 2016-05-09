/**
 * 
 */
package utils.imagemagick.mogrify;

import java.util.ArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.im4java.core.CommandException;
import org.im4java.core.IMOperation;
import org.im4java.core.MogrifyCmd;

/**
 * @author Greg Drogaline
 * 
 */
public class MogrifyHelper {

	static Logger logger = LogManager.getLogger(MogrifyHelper.class);
	
	/**
	 * 
	 */
	private MogrifyHelper() {
		
	}

	/**
	 * @param op
	 *            - IMOperation command input
	 * @return true on success
	 */
	public static boolean mogrify(IMOperation op) {

		MogrifyCmd mogrify = new MogrifyCmd();

		try {
			mogrify.createScript("mogrify.sh", op);
			mogrify.run(op);

		} catch (CommandException ce) {
			logger.error(ce);
			ArrayList<String> cmdError = ce.getErrorText();
			for (String line : cmdError) {
				System.err.println(line);
			}
			return false;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}

		return true;
	}
}
