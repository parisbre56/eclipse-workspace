/**
 * 
 */
package visitor;

/**
 * @author Parisbre56
 *
 */
public class VisitorIOException extends VisitorException {

	/**
	 * 
	 */
	public VisitorIOException() {
		super("An IO exception happened while translating");
	}

	/**
	 * @param message
	 */
	public VisitorIOException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public VisitorIOException(Throwable cause) {
		super("An IO exception happened while translating",cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public VisitorIOException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public VisitorIOException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
