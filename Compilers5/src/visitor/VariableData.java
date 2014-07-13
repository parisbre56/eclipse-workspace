/**
 * 
 */
package visitor;

/**
 * @author Parisbre56
 *
 */
public class VariableData {
	/** Is set to true if there is a procedure call
	 * during the liveness of this variable.
	 */
	Boolean procCallInterjected = false;
	
	/** The name of this variable (just the integer literal for tempvars)
	 * 
	 */
	String name;

	/**
	 * 
	 */
	public VariableData(String cName) {
		name=cName;
	}

}
