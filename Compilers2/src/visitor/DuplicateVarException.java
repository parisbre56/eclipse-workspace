/**
 * 
 */
package visitor;

/**
 * @author Parisbre56
 *
 */
public class DuplicateVarException extends VisitorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 844162512492164234L;

	/**
	 * 
	 */
	public DuplicateVarException() {
		super("Duplicate variable declaration");
	}

	/**
	 * @param message
	 */
	public DuplicateVarException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public DuplicateVarException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DuplicateVarException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public DuplicateVarException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
