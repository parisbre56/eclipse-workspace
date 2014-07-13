/**
 * 
 */
package visitor;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import syntaxtree.Node;
import syntaxtree.Stmt;

/**
 * @author Parisbre56
 *
 */
public class CodeBlockSpiglet {
	
	/** True if this code block contains a procedure call
	 */
	Boolean containsProcCall = false;

	/** Points to all the CodeBlocks that point to this one
	 */
	LinkedList<CodeBlockSpiglet> prevBlocks = new LinkedList<CodeBlockSpiglet>();
	/** Points to the jump link, if applicable, else is null
	 */
	CodeBlockSpiglet jumpLink = null;
	/** Points to the next instruction, if applicable, else is null
	 */
	CodeBlockSpiglet fallthroughLink = null;
	
	/** Points to the statement contained in this block
	 */
	Node statement = null;
	
	/** Contains the label of this codeBlock if it is a jump, cjump or a label
	 */
	String label = null;
	
	/** An enumeration that holds the type of this statement.<br>
	 * Note that the RETURN value is special, in that its node is not cast into a Stmnt but a simpleExp.<br>
	 * Note that the LABEL value is special, in that its node is not cast into a Stmnt but a Label.<br>
	 */
	StatementTypeSpiglet stType = null;
	
	/** Contains the first variable used in this instruction, if applicable, else null
	 */
	LinkedHashMap<String, VariableData> varUsed = new LinkedHashMap<String, VariableData>();
	VariableData varAssigned = null;
	
	/** Contains all the live vars up to this point
	 * Remember, this must not contain varAssigned, unless varAssigned is also in varUsed.
	 */
	LinkedHashMap<String,VariableData> liveVars = new LinkedHashMap<String, VariableData>();
	
	/**
	 * @param n The statement this codeBlock represents
	 * 
	 */
	public CodeBlockSpiglet(Node n) {
		statement = n;
	}

	/** Creates the appropriate links, whether this is a jump statement or a label
	 * @param blockData All the blocks of the current procedure
	 */
	public void addJumpLabelLink(LinkedList<CodeBlockSpiglet> blockData) {
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

}
