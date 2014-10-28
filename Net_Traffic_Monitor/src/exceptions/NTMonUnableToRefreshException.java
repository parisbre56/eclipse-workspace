/**
 * 
 */
package exceptions;

/**
 * @author Parisbre56
 *
 */
public class NTMonUnableToRefreshException extends NTMonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -526473805521191280L;

	/**
	 * 
	 */
	public NTMonUnableToRefreshException() {
		super("Unable to refresh connection");
	}

	/**
	 * @param message
	 */
	public NTMonUnableToRefreshException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public NTMonUnableToRefreshException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NTMonUnableToRefreshException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public NTMonUnableToRefreshException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
