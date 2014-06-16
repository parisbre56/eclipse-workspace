/**
 * 
 */
package visitor;

/**
 * @author Parisbre56
 *
 */
public class DuplicateClassException extends VisitorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8473141765786170494L;

	/**
	 * 
	 */
	public DuplicateClassException() {
		super("Duplicate class declaration");
	}

	/**
	 * @param arg0
	 */
	public DuplicateClassException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public DuplicateClassException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public DuplicateClassException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public DuplicateClassException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
		// TODO Auto-generated constructor stub
	}

}
