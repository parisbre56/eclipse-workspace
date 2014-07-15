/**
 * 
 */
package visitor;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import syntaxtree.Node;
import visitor.VariableData.RegisterAs;

/**
 * @author Parisbre56
 *
 */
public class CodeBlockSpiglet {
	
	/** True if this code block contains a procedure call
	 */
	public Boolean containsProcCall = false;

	/** Points to all the CodeBlocks that point to this one
	 */
	public LinkedList<CodeBlockSpiglet> prevBlocks = new LinkedList<CodeBlockSpiglet>();
	/** Points to the jump link, if applicable, else is null
	 */
	public CodeBlockSpiglet jumpLink = null;
	/** Points to the next instruction, if applicable, else is null
	 */
	public CodeBlockSpiglet fallthroughLink = null;
	
	/** Points to the statement contained in this block
	 */
	public Node statement = null;
	
	/** Contains the label of this codeBlock if it is a jump, cjump or a label
	 */
	public String label = null;
	
	/** An enumeration that holds the type of this statement.<br>
	 * Note that the RETURN value is special, in that its node is not cast into a Stmnt but a simpleExp.<br>
	 * Note that the LABEL value is special, in that its node is not cast into a Stmnt but a Label.<br>
	 */
	public StatementTypeSpiglet stType = null;
	
	/** Contains the variables used in this instruction, if applicable
	 */
	public LinkedHashMap<String, VariableData> varUsed = new LinkedHashMap<String, VariableData>();
	
	/** Contais the variable that is assigned a value in this instruction, if applicable, else null
	 */
	public VariableData varAssigned = null;
	
	/** Contains all the live vars up to this point
	 * Remember, this must not contain varAssigned, unless varAssigned is also in varUsed.
	 */
	public LinkedHashMap<String,VariableData> liveVars = new LinkedHashMap<String, VariableData>();

	/** The procedure this block belongs to
	 */
	private ProcedureData parent;

	private Boolean livePassChecked = false;
	
	/**
	 * @param n The statement this codeBlock represents
	 * 
	 */
	public CodeBlockSpiglet(Node n,ProcedureData cParent) {
		parent=cParent;
		statement = n;
	}

	/** Creates the appropriate links, whether this is a jump statement or a label
	 * @param blockData All the blocks of the current procedure
	 */
	public void addJumpLabelLink(LinkedList<CodeBlockSpiglet> blockData ) {
		if(this.stType==StatementTypeSpiglet.CJumpStmt||this.stType==StatementTypeSpiglet.JumpStmt) {
			for(CodeBlockSpiglet cbs : blockData) {
				if(cbs.label.equals(this.label)&&cbs.stType==StatementTypeSpiglet.Label) {
					cbs.prevBlocks.add(this);
					this.jumpLink=cbs;
					break;
				}
			}
		}
		else if(this.stType==StatementTypeSpiglet.Label) {
			for(CodeBlockSpiglet cbs : blockData) {
				if(cbs.label.equals(this.label)) {
					if(cbs.stType==StatementTypeSpiglet.CJumpStmt||cbs.stType==StatementTypeSpiglet.JumpStmt) {
						cbs.jumpLink=this;
						this.prevBlocks.add(cbs);
					}
					else {
						System.err.println("ERROR: Second label with same name encountered: "+this.label);
						System.exit(1);
					}
				}
			}
		}
		else {
			System.err.println("ERROR: Check codeBlockSpiglet:addJumpLabelLink usage\nERROR: Used with "+this.stType);
			System.exit(1);
		}
	}

	/** For each block linked to this block (for each previous block) update the list of live vars
	 * based on the live vars of this block. <br>
	 * Also sets the procCallInterjected flag of the variables if necessary
	 * @return True if a change occurred, false otherwise.
	 */
	public Boolean updateLiveness() {
		Boolean changeOccurred = false;
		
		//For each block leading to this one
		for(CodeBlockSpiglet blk:this.prevBlocks) {
			//For each live var of this block
			for(Entry<String,VariableData> entrLiv : this.liveVars.entrySet()) {
				//If a live var of this block is not in the live vars of the previous block
				//and that live var is not in assigned a value the previous block
				if(!blk.liveVars.containsKey(entrLiv.getKey())
						&&!blk.varAssigned.name.equals(entrLiv.getKey())) {
					//Add it to the list of live vars and note the change
					blk.liveVars.put(entrLiv.getKey(), entrLiv.getValue());
					changeOccurred = true;
					//If this is a block that contains a call, then make sure to mark these vars
					//so that they aren't assigned to temp registers
					entrLiv.getValue().procCallInterjected=true;
				}
			}
		}
		
		return changeOccurred;
	}

	/**
	 * Tries to assign vars to registers for all live vars of this block
	 * Also checks whether or not there are duplicate registry assigments in this block and tries to fix them
	 * @return True if completed successfully, else false
	 */
	public boolean assignVars() {
		//For all vars in the set of all live vars
		for(Entry<String, VariableData> varEntry : this.liveVars.entrySet()) {
			if(varEntry.getValue().regAs!=RegisterAs.NotYetAssigned
					&&varEntry.getValue().regAs!=RegisterAs.Stack) {
				//For all other variables that have a  register assigned
				for(Entry<String,VariableData> secVarEntry : this.liveVars.entrySet()) {
					if(!secVarEntry.equals(varEntry)
							&&secVarEntry.getValue().regAs!=RegisterAs.NotYetAssigned
							&&secVarEntry.getValue().regAs!=RegisterAs.Stack) {
						//Check to make sure that they don't both have the same value. If they do,
						//give the one that is live the longest a place in the stack and return false
						//so we can start all over
						if(varEntry.getValue().regAs==secVarEntry.getValue().regAs) {
							Integer liveBlocks = this.computeLivenessHeuristic(varEntry);
							Integer secLiveBlocks = this.computeLivenessHeuristic(secVarEntry);
							if(liveBlocks>secLiveBlocks) {
								varEntry.getValue().regAs=RegisterAs.Stack;
								varEntry.getValue().stackPos=parent.stack;
								parent.stack=parent.stack+1;
							}
							else {
								secVarEntry.getValue().regAs=RegisterAs.Stack;
								secVarEntry.getValue().stackPos=parent.stack;
								parent.stack=parent.stack+1;
							}
							return false;
						}
					}
				}
			}
		}
		//For all vars in the set of all live vars
		for(Entry<String, VariableData> varEntry : this.liveVars.entrySet()) {
			VariableData var = varEntry.getValue();
			//If this variable has not been assigned to a register
			if(var.regAs==RegisterAs.NotYetAssigned) {
				EnumSet<RegisterAs> availiableVars;
				//Only take sX registers if the var is going to be live during a procedure call
				if(var.procCallInterjected) {
					availiableVars = EnumSet.range(RegisterAs.s0, RegisterAs.s7);
				}
				else {
					availiableVars = EnumSet.range(RegisterAs.t0, RegisterAs.s7);
				}
				//Take Set of registers, remove the registers we don't want and then remove the 
				//registers used by other live vars. 
				for(Entry<String,VariableData> secVarEntry : this.liveVars.entrySet()) {
					if(!secVarEntry.equals(varEntry)
							&&secVarEntry.getValue().regAs!=RegisterAs.NotYetAssigned
							&&secVarEntry.getValue().regAs!=RegisterAs.Stack
							&&availiableVars.contains(secVarEntry.getValue().regAs)) {
						availiableVars.remove(secVarEntry.getValue().regAs);
					}
				}
				//If the remaining set is empty, find the longest living var not in the stack and put it in the stack,
				//then return false so that we can start all over
				if(availiableVars.isEmpty()) {
					VariableData longestLivingVar = this.getLongestLivingVar();
					longestLivingVar.regAs=RegisterAs.Stack;
					longestLivingVar.stackPos=parent.stack;
					parent.stack=parent.stack+1;
					return false;
				}
				//Else, give the first available var (so that we get tX registers first) and continue on with the next var
				var.regAs= availiableVars.iterator().next();
			}
			
		}
		return true;
	}

	/** Searches the variables that have been assigned to a register and returns the one with the 
	 * largest liveness
	 * @return The variable with the biggest liveness
	 */
	private VariableData getLongestLivingVar() {
		VariableData var=null;
		VariableData longestVar=null;
		Entry<String, VariableData> longestVarEntry = null;
		for(Entry<String,VariableData> varEntry : this.liveVars.entrySet()) {
			var=varEntry.getValue();
			if(var.regAs!=RegisterAs.NotYetAssigned
					&&var.regAs!=RegisterAs.Stack) {
				if(longestVar==null) {
					longestVar=var;
					longestVarEntry = varEntry;
					continue;
				}
				if(this.computeLivenessHeuristic(varEntry)>this.computeLivenessHeuristic(longestVarEntry)) {
					longestVarEntry=varEntry;
					longestVar=var;
				}
			}
		}
		return longestVar;
	}

	/**Computes the liveness of this var from this point onwards for all branches. Uses field
	 * this.livePassChecked to ensure each block is only passed through once for each branch
	 * @param varEntry The var whose liveness we want to check
	 * @return The computed liveness heuristic for varEntry
	 */
	private Integer computeLivenessHeuristic(
			Entry<String, VariableData> varEntry) {
		//Used to prevent infinite loops, by ensuring we check a block only once for each branch.
		if(this.livePassChecked) {
			return 0;
		}
		//If this doesn't contain the var, return 0
		if(!this.liveVars.containsKey(varEntry.getKey())) {
			return 0;
		}
		//Compute the liveness for the following links
		Integer jumpLiveBlocks=0;
		Integer liveBlocks=0;
		this.livePassChecked=true;
		if(this.fallthroughLink!=null) {
			liveBlocks=this.fallthroughLink.computeLivenessHeuristic(varEntry);
		}
		if(this.jumpLink!=null) {
			jumpLiveBlocks=this.fallthroughLink.computeLivenessHeuristic(varEntry);
		}
		this.livePassChecked=false;
		//Return the maximum liveness plus one for the current block
		return Math.max(liveBlocks, jumpLiveBlocks)+1;
	}
	
}
