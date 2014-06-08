/**
 * 
 */
package visitor;

/**
 * @author Parisbre56
 *
 */
public class UndeclaredVarException extends VisitorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5309301834398765223L;

	/**
	 * 
	 */
	public UndeclaredVarException() {
		super("Undeclared variable encountered");
	}

	/**
	 * @param message
	 */
	public UndeclaredVarException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public UndeclaredVarException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UndeclaredVarException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public UndeclaredVarException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
