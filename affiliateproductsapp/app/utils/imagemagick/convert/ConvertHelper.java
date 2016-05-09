package utils.imagemagick.convert;

import java.util.ArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.im4java.core.CommandException;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;

/**
 * @author Greg Drogaline
 * 
 */
public class ConvertHelper {

	static Logger logger = LogManager.getLogger(ConvertHelper.class);
	
	private ConvertHelper() {

	}

	/**
	 * @param op
	 *            - IMOperation command input
	 * @return true on success
	 */
	public static boolean conver(IMOperation op) {

		ConvertCmd convert = new ConvertCmd();

		try {
			convert.createScript("convert.sh", op);
			convert.run(op);

		} catch (CommandException ce) {
			logger.error(ce);
			ArrayList<String> cmdError = ce.getErrorText();
			for (String line : cmdError) {
				logger.error(line);
			}
			return false;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}

		return true;
	}
}
