/**
 * 
 */
package visitor;

import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * @author Parisbre56
 *
 */
public class ProcedureData {

	/** The name of this function
	 */
	String name;
	/** The number of arguments this function takes
	 */
	Integer args;
	/** How much of the stack is used for variable storage (including spilled arguments)
	 * Remember, this points to the first availiable spot in the stack
	 */
	Integer stack;
	/** The maximum number of arguments of the functions called by this function.
	 */
	Integer maxCalledVars;
	
	/** Contains all the instructions of the function
	 */
	LinkedList<CodeBlockSpiglet> blockData = new LinkedList<CodeBlockSpiglet>();
	/** Contains all the temp variables of the function
	 */
	LinkedHashMap<String,VariableData> varData = new LinkedHashMap<String, VariableData>();
	
	/**
	 * @param cName is the name of the function
	 * @param cArgs is the number of arguments this function takes
	 */
	public ProcedureData(String cName, Integer cArgs) {
		name=cName;
		args=cArgs;
		if(cArgs>4) {
			stack=cArgs-4;
		}
		else {
			stack=0;
		}
		maxCalledVars=0;
	}

}
