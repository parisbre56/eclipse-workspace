/**
 * 
 */
package visitor;

/**
 * @author Parisbre56
 *
 */
public class VisitorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -342554789121954736L;

	/**
	 * 
	 */
	public VisitorException() {
		super("Unknown visitor exception");
	}

	/**
	 * @param message
	 */
	public VisitorException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public VisitorException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public VisitorException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public VisitorException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
