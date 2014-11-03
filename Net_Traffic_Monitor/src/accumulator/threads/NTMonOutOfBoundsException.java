/**
 * 
 */
package accumulator.threads;

import exceptions.NTMonException;

/** Indicates that there was a problem with a value being out of its expected bounds
 * @author Parisbre56
 *
 */
public class NTMonOutOfBoundsException extends NTMonException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8099203095890362196L;

	/**
	 * 
	 */
	public NTMonOutOfBoundsException() {
		super("A value was out of the expected bounds");
	}

	/**
	 * @param message
	 */
	public NTMonOutOfBoundsException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public NTMonOutOfBoundsException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NTMonOutOfBoundsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public NTMonOutOfBoundsException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
