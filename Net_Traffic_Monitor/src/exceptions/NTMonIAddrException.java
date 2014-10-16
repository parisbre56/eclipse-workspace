/**
 * 
 */
package exceptions;


/**
 * Indicates a problem encountered while processing an internet address.
 * @author Parisbre56
 *
 */
public class NTMonIAddrException extends NTMonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2882048102289256587L;

	/**
	 * 
	 */
	public NTMonIAddrException() {
		super("Error while processing an Internet Address");
	}

	/**
	 * @param message
	 */
	public NTMonIAddrException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public NTMonIAddrException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NTMonIAddrException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public NTMonIAddrException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
