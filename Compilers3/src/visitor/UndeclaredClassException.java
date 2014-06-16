/**
 * 
 */
package visitor;

/**
 * @author Parisbre56
 *
 */
public class UndeclaredClassException extends VisitorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1938074146514176872L;

	/**
	 * 
	 */
	public UndeclaredClassException() {
		super("Undeclared class encountered.");
	}

	/**
	 * @param message
	 */
	public UndeclaredClassException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public UndeclaredClassException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UndeclaredClassException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public UndeclaredClassException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
