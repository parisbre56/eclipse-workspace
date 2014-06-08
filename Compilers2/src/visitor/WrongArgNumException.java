/**
 * 
 */
package visitor;

/**
 * @author Parisbre56
 *
 */
public class WrongArgNumException extends VisitorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2596266294416291172L;

	/**
	 * 
	 */
	public WrongArgNumException() {
		super("There was a wrong number of arguements provided");
	}

	/**
	 * @param message
	 */
	public WrongArgNumException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public WrongArgNumException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public WrongArgNumException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public WrongArgNumException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
