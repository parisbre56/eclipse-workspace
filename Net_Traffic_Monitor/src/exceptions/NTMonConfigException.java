/**
 * 
 */
package exceptions;


/** This indicates that there was an error when loading or changing the configuration class.
 * @author Parisbre56
 *
 */
public class NTMonConfigException extends NTMonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4314647078507292614L;

	/**
	 * 
	 */
	public NTMonConfigException() {
		super("Error occured during a change in the configuration");
	}

	/**
	 * @param message
	 */
	public NTMonConfigException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public NTMonConfigException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NTMonConfigException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public NTMonConfigException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
