/**
 * 
 */
package visitor;

/**
 * @author Parisbre56
 *
 */
public class DuplicateMethodException extends VisitorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7077766024149412708L;

	/**
	 * 
	 */
	public DuplicateMethodException() {
		super("Duplicate method declaration");
	}

	/**
	 * @param message
	 */
	public DuplicateMethodException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public DuplicateMethodException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DuplicateMethodException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public DuplicateMethodException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
