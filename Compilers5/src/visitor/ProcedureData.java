/**
 * 
 */
package visitor;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import syntaxtree.Call;
import syntaxtree.MoveStmt;
import visitor.VariableData.RegisterAs;

/**
 * @author Parisbre56
 *
 */
public class ProcedureData {

	/** The name of this function
	 */
	public String name;
	/** The number of arguments this function takes
	 */
	public Integer args;
	/** How much of the stack is used for variable storage (including spilled arguments) <br>
	 * Remember, this points to the first availiable spot in the stack <br>
	 * Remember, this does NOT include sX vars that must be saved in the stack during function calls
	 */
	public Integer stack;
	/** The maximum number of arguments of the functions called by this function.
	 */
	public Integer maxCalledVars;
	
	/** Contains all the instructions of the function
	 */
	public LinkedList<CodeBlockSpiglet> blockData = new LinkedList<CodeBlockSpiglet>();
	/** Contains all the temp variables of the function
	 */
	public LinkedHashMap<String,VariableData> varData = new LinkedHashMap<String, VariableData>();
	
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
	
	
	/** Tries to assign vars to all the vars in the procedure
	 * Also sets the maxCalledVars for the procedure
	 * @return True if completed successfully, else false
	 */
	public boolean AssignVarsToRegisters() {
		for(CodeBlockSpiglet blk : this.blockData) {
			//Keep looping until fully succesful
			while(!blk.assignVars()) {
				
			}
			if(blk.containsProcCall) {
				if(blk.stType != StatementTypeSpiglet.MoveStmt) {
					System.err.println("Procedure call in non-move statement.");
					System.exit(1);
				}
				//If this contains a procedure call
				MoveStmt mvStmt = (MoveStmt) blk.statement;
				Call cllStmt = (Call) mvStmt.exp.nodeChoice.choice;
				//If the procedure call has arguments, count them and update the max number of arguments
				if(cllStmt.nodeListOptional.present()) {
					this.maxCalledVars=Math.max(cllStmt.nodeListOptional.nodes.size(),this.maxCalledVars);
				}
			}
		}
		return true;
	}

	/** 
	 * Searches all the vars of this procedure and returns the set of sX registers used
	 * @return The set of sX registers used in this procedure
	 */
	public EnumSet<RegisterAs> usedSxRegisters() {
		EnumSet<RegisterAs> sigmaRegs = EnumSet.range(RegisterAs.s0, RegisterAs.s7);
		EnumSet<RegisterAs> usedSigmaRegs = EnumSet.noneOf(RegisterAs.class);
		//For all vars
		for(Entry<String, VariableData> varEntry : this.varData.entrySet()) {
			VariableData var = varEntry.getValue();
			//If this var is assigned to a sigma reg, which is not in the set of used regs, add it to it.
			if(sigmaRegs.contains(var.regAs)&&!usedSigmaRegs.contains(var.regAs)) {
				usedSigmaRegs.add(var.regAs);
			}
		}
		return usedSigmaRegs;
	}

}
