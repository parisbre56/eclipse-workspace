/**
 * 
 */
package visitor;

/**
 * @author Parisbre56
 *
 */
public class VariableData {
	/** Contains the assigned register for this variable<br>
	 * NotYetAssigned means no register has been assigned yet<br>
	 * Stack means that the value is stored in the stack and processed via tempvars or similar
	 * @author Parisbre56
	 * 
	 */
	public enum RegisterAs {
		/** Register not yet assigned
		 */
		NotYetAssigned,
		/** Stored in stack
		 */
		Stack,
		/** Return values and stack operations
		 */
		v0,v1,
		a0,a1,a2,a3,
		t0,t1,t2,t3,t4,t5,t6,t7,t8,t9,
		s0,s1,s2,s3,s4,s5,s6,s7
	}

	/** Is set to true if there is a procedure call
	 * during the liveness of this variable.
	 */
	Boolean procCallInterjected = false;
	
	/** Assigned register.<br>
	 * Note that the special value STACK indicates usage of the stackPos field
	 */
	public RegisterAs regAs = RegisterAs.NotYetAssigned;
	
	public Integer stackPos = -1;
	
	/** The name of this variable (just the integer literal for tempvars)
	 */
	String name;

	/**
	 * 
	 */
	public VariableData(String cName) {
		name=cName;
	}

}
