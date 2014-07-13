/**
 * 
 */
package visitor;

import java.util.Enumeration;
import java.util.LinkedHashMap;

import syntaxtree.BinOp;
import syntaxtree.CJumpStmt;
import syntaxtree.Call;
import syntaxtree.ErrorStmt;
import syntaxtree.Exp;
import syntaxtree.Goal;
import syntaxtree.HAllocate;
import syntaxtree.HLoadStmt;
import syntaxtree.HStoreStmt;
import syntaxtree.IntegerLiteral;
import syntaxtree.JumpStmt;
import syntaxtree.Label;
import syntaxtree.MoveStmt;
import syntaxtree.NoOpStmt;
import syntaxtree.Node;
import syntaxtree.NodeList;
import syntaxtree.NodeListOptional;
import syntaxtree.NodeOptional;
import syntaxtree.NodeSequence;
import syntaxtree.NodeToken;
import syntaxtree.Operator;
import syntaxtree.PrintStmt;
import syntaxtree.Procedure;
import syntaxtree.SimpleExp;
import syntaxtree.Stmt;
import syntaxtree.StmtExp;
import syntaxtree.StmtList;
import syntaxtree.Temp;

/**
 * @author Parisbre56
 *
 */
public class BlockCreatorVisitor implements Visitor {
	/** Keeps info on all procedures, including all their instructions once the visitor is done
	 * with its processing.
	 */
	public LinkedHashMap<String, ProcedureData> procData = new LinkedHashMap<String, ProcedureData>();
	/** Temp var that holds the current procedure
	 */
	ProcedureData currProc = null;
	/** Temp var that holds the current instruction block
	 */
	CodeBlockSpiglet currBlock = null;

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.NodeList)
	 */
	@Override
	public void visit(NodeList n) {
		for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
			e.nextElement().accept(this);
		}
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.NodeListOptional)
	 */
	@Override
	public void visit(NodeListOptional n) {
		if (n.present()) {
			for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
				e.nextElement().accept(this);
			}
		}
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.NodeOptional)
	 */
	@Override
	public void visit(NodeOptional n) {
		if ( n.present() ) {
			n.node.accept(this);
		}
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.NodeSequence)
	 */
	@Override
	public void visit(NodeSequence n) {
		for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
			e.nextElement().accept(this);
		}
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.NodeToken)
	 */
	@Override
	public void visit(NodeToken n) {
		return;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.Goal)
	 */
	@Override
	public void visit(Goal n) {
		currProc = new ProcedureData("MAIN",0);
		procData.put(currProc.name, currProc);
		n.stmtList.accept(this);
		currProc = null;
		n.nodeListOptional.accept(this);
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.StmtList)
	 */
	@Override
	public void visit(StmtList n) {
		n.nodeListOptional.accept(this);
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.Procedure)
	 */
	@Override
	public void visit(Procedure n) {
		currProc = new ProcedureData(n.label.nodeToken.tokenImage,Integer.decode(n.integerLiteral.nodeToken.tokenImage));
		procData.put(currProc.name, currProc);
		n.stmtExp.accept(this);
		currProc = null;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.Stmt)
	 */
	@Override
	public void visit(Stmt n) {
		currBlock=new CodeBlockSpiglet(n.nodeChoice.choice);
		//If this is a valid fallthrough block for the previous statement
		//(that means the previous block is not a jump statement)
		//create the links to indicate so
		if(!currProc.blockData.isEmpty()) {
			CodeBlockSpiglet prevBlock=currProc.blockData.getLast();
			if(prevBlock.stType!=StatementTypeSpiglet.JumpStmt) {
				currBlock.prevBlocks.add(prevBlock);
				prevBlock.fallthroughLink=currBlock;
			}	
		}
		//Process the statement
		n.nodeChoice.accept(this);
		//Remember that the current block is added to the block list AFTER it is processed 
		currProc.blockData.add(currBlock);
		//Clear currBlock for later use
		currBlock=null;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.NoOpStmt)
	 */
	@Override
	public void visit(NoOpStmt n) {
		currBlock.stType=StatementTypeSpiglet.NoOpStmt;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.ErrorStmt)
	 */
	@Override
	public void visit(ErrorStmt n) {
		currBlock.stType=StatementTypeSpiglet.ErrorStmt;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.CJumpStmt)
	 */
	@Override
	public void visit(CJumpStmt n) {
		currBlock.stType=StatementTypeSpiglet.CJumpStmt;
		//Create var if it doesn't exist
		String varName=n.temp.integerLiteral.nodeToken.tokenImage;
		VariableData varData = createVariableIfNecessaryAndReturnIt(varName);
		//Put used var in the set of used vars and the set of live vars
		currBlock.varUsed.put(varName, varData);
		currBlock.liveVars.put(varName, varData);
		//Add label
		currBlock.label=n.label.nodeToken.tokenImage;
		//search for the label we must link to and add it if possible
		currBlock.addJumpLabelLink(currProc.blockData);
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.JumpStmt)
	 */
	@Override
	public void visit(JumpStmt n) {
		currBlock.stType=StatementTypeSpiglet.JumpStmt;
		//Add label
		currBlock.label=n.label.nodeToken.tokenImage;
		//search for the label we must link to and add it if possible
		currBlock.addJumpLabelLink(currProc.blockData);
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.HStoreStmt)
	 */
	@Override
	public void visit(HStoreStmt n) {
		currBlock.stType=StatementTypeSpiglet.HStoreStmt;
		//Create var if it doesn't exist
		String varName=n.temp.integerLiteral.nodeToken.tokenImage;
		String varName2=n.temp1.integerLiteral.nodeToken.tokenImage;
		VariableData varData = createVariableIfNecessaryAndReturnIt(varName);
		VariableData varData2 = createVariableIfNecessaryAndReturnIt(varName2);
		//Put used var in the set of used vars and the set of live vars
		currBlock.varUsed.put(varName, varData);
		currBlock.liveVars.put(varName, varData);
		currBlock.varUsed.put(varName2, varData2);
		currBlock.liveVars.put(varName2, varData2);
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.HLoadStmt)
	 */
	@Override
	public void visit(HLoadStmt n) {
		currBlock.stType=StatementTypeSpiglet.HLoadStmt;
		//Create var if it doesn't exist
		String varAssignedName=n.temp.integerLiteral.nodeToken.tokenImage;
		String varUsedName=n.temp1.integerLiteral.nodeToken.tokenImage;
		VariableData varAssignedData = createVariableIfNecessaryAndReturnIt(varAssignedName);
		VariableData varUsedData = createVariableIfNecessaryAndReturnIt(varUsedName);
		//Put assigned var in appropriate pointer (not live at this point, since it's assigned a value)
		currBlock.varAssigned=varAssignedData;
		//Put used var in the set of used vars and the set of live vars (if they are both the same var, then 
		//the previously not added var will be added to the set of live vars)
		currBlock.varUsed.put(varUsedName, varUsedData);
		currBlock.liveVars.put(varUsedName, varUsedData);
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.MoveStmt)
	 */
	@Override
	public void visit(MoveStmt n) {
		currBlock.stType=StatementTypeSpiglet.MoveStmt;
		//Create var if it doesn't exist
		String varAssignedName=n.temp.integerLiteral.nodeToken.tokenImage;
		VariableData varAssignedData = createVariableIfNecessaryAndReturnIt(varAssignedName);
		//Put assigned var in appropriate pointer (not live at this point, since it's assigned a value)
		currBlock.varAssigned=varAssignedData;
		//Process the rest of the statement to see if there are any other used vars.
		n.exp.accept(this);
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.PrintStmt)
	 */
	@Override
	public void visit(PrintStmt n) {
		currBlock.stType=StatementTypeSpiglet.PrintStmt;
		//Process the rest of the statement to see if there are any other used vars.
		n.simpleExp.accept(this);
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.Exp)
	 */
	@Override
	public void visit(Exp n) {
		n.nodeChoice.accept(this);
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.StmtExp)
	 */
	@Override
	public void visit(StmtExp n) {
		//Process the statement list normally
		n.stmtList.accept(this);
		
		//=============
		//Process the return statement
		currBlock=new CodeBlockSpiglet(n.simpleExp);
		currBlock.stType=StatementTypeSpiglet.Return;
		//If this is a valid fallthrough block for the previous statement
		//(that means the previous block is not a jump statement)
		//create the links to indicate so
		if(!currProc.blockData.isEmpty()) {
			CodeBlockSpiglet prevBlock=currProc.blockData.getLast();
			if(prevBlock.stType!=StatementTypeSpiglet.JumpStmt) {
				currBlock.prevBlocks.add(prevBlock);
				prevBlock.fallthroughLink=currBlock;
			}	
			else {
				System.err.println("ERROR: Unreachable return statement.");
				System.exit(1);
			}
		}
		//Process the statement
		n.simpleExp.accept(this);
		//Remember that the current block is added to the block list AFTER it is processed 
		currProc.blockData.add(currBlock);
		//Clear currBlock for later use
		currBlock=null;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.Call)
	 */
	@Override
	public void visit(Call n) {
		currBlock.containsProcCall=true;
		n.simpleExp.accept(this);
		n.nodeListOptional.accept(this);
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.HAllocate)
	 */
	@Override
	public void visit(HAllocate n) {
		n.simpleExp.accept(this);
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.BinOp)
	 */
	@Override
	public void visit(BinOp n) {
		//Create var if it doesn't exist
		String varName=n.temp.integerLiteral.nodeToken.tokenImage;
		VariableData varData = createVariableIfNecessaryAndReturnIt(varName);
		//Put used var in the set of used vars and the set of live vars
		currBlock.varUsed.put(varName, varData);
		currBlock.liveVars.put(varName, varData);
		//Process the rest of the statement
		n.simpleExp.accept(this);
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.Operator)
	 */
	@Override
	public void visit(Operator n) {
		//Should never get here
		return;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.SimpleExp)
	 */
	@Override
	public void visit(SimpleExp n) {
		n.nodeChoice.accept(this);
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.Temp)
	 */
	@Override
	public void visit(Temp n) {
		//Create var if it doesn't exist
		String varName=n.integerLiteral.nodeToken.tokenImage;
		VariableData varData = createVariableIfNecessaryAndReturnIt(varName);
		//Put used var in the set of used vars and the set of live vars
		currBlock.varUsed.put(varName, varData);
		currBlock.liveVars.put(varName, varData);
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.IntegerLiteral)
	 */
	@Override
	public void visit(IntegerLiteral n) {
		//Do nothing
		return;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.Label)
	 */
	@Override
	public void visit(Label n) {
		//Only process labels before statements, not labels inside statements
		if(currBlock!=null) {
			return;
		}
		//Else
		currBlock=new CodeBlockSpiglet(n);
		currBlock.stType=StatementTypeSpiglet.Label;
		//If this is a valid fallthrough block for the previous statement
		//(that means the previous block is not a jump statement)
		//create the links to indicate so
		if(!currProc.blockData.isEmpty()) {
			CodeBlockSpiglet prevBlock=currProc.blockData.getLast();
			if(prevBlock.stType!=StatementTypeSpiglet.JumpStmt) {
				currBlock.prevBlocks.add(prevBlock);
				prevBlock.fallthroughLink=currBlock;
			}	
		}
		//Add label
		currBlock.label=n.nodeToken.tokenImage;
		//search for the label we must link to and add it if possible
		currBlock.addJumpLabelLink(currProc.blockData);
		//Remember that the current block is added to the block list AFTER it is processed 
		currProc.blockData.add(currBlock);
		//Clear currBlock for later use
		currBlock=null;
	}

	/** 
	 * @param varName is the name of the variable we want. If it doesn't exist in the list of variables, it will be created
	 * @return The VariableData object for the varName variable. If it doesn't exist in the list of variables, it will be created
	 */
	private VariableData createVariableIfNecessaryAndReturnIt(String varName) {
		VariableData varData;
		if(!currProc.varData.containsKey(varName)) {
			varData=new VariableData(varName);
			currProc.varData.put(varName, varData);
		}
		else {
			 varData = currProc.varData.get(varName);
		}
		return varData;
	}
}
