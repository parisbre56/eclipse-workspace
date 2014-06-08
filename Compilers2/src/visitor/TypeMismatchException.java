/**
 * 
 */
package visitor;

/**
 * @author Parisbre56
 *
 */
public class TypeMismatchException extends VisitorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6739472805047182887L;

	/**
	 * 
	 */
	public TypeMismatchException() {
		super("Type mismatch exception");
	}

	/**
	 * @param message
	 */
	public TypeMismatchException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public TypeMismatchException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public TypeMismatchException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public TypeMismatchException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
