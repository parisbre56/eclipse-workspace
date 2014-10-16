/**
 * 
 */
package exceptions;

/**
 * This is the basic superclass exception extended by all other exceptions used 
 * by the net traffic monitor program. 
 * @author Parisbre56
 *
 */
public class NTMonException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6498801218265036211L;

	/**
	 * 
	 */
	public NTMonException() {
		super("Unknown exception in the Net Traffic Monitor");
	}

	/**
	 * @param message
	 */
	public NTMonException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public NTMonException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NTMonException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public NTMonException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
