/**
 * 
 */
package visitor;

/**
 * @author Parisbre56
 *
 */
public class UndeclaredMethodException extends VisitorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2272772207959513397L;

	/**
	 * 
	 */
	public UndeclaredMethodException() {
		super("Undeclared method encountered");
	}

	/**
	 * @param message
	 */
	public UndeclaredMethodException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public UndeclaredMethodException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UndeclaredMethodException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public UndeclaredMethodException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
