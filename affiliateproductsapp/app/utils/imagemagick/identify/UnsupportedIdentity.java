/**
 * 
 */
package utils.imagemagick.identify;

/**
 * @author Greg Drogaline
 *
 */
public class UnsupportedIdentity extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3670534768919551676L;

	/**
	 * 
	 */
	public UnsupportedIdentity() {
	}

	/**
	 * @param arg0
	 */
	public UnsupportedIdentity(String arg0) {
		super(arg0);
		
	}

	/**
	 * @param arg0
	 */
	public UnsupportedIdentity(Throwable arg0) {
		super(arg0);
	
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public UnsupportedIdentity(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
