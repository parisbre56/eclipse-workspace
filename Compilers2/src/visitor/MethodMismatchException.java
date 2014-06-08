/**
 * 
 */
package visitor;

/**
 * @author Parisbre56
 *
 */
public class MethodMismatchException extends VisitorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6455403768902226727L;

	/**
	 * 
	 */
	public MethodMismatchException() {
		super("Mismatched method types or variables between local and parent declaration");
	}

	/**
	 * @param message
	 */
	public MethodMismatchException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public MethodMismatchException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MethodMismatchException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public MethodMismatchException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
