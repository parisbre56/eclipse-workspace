/**
 * 
 */
package visitor;

import java.io.PrintWriter;
import java.util.Enumeration;

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
import visitor.VariableData.RegisterAs;

/**
 * 
 */

/**
 * @author Parisbre56
 *
 */
public class StatementFinder implements GJNoArguVisitor<String> {
	CodeBlockSpiglet blk=null;
	PrintWriter writer=null;
	
	/** Shows to the tempVar visitor that we are processing a procedure 
	 */
	Boolean inCall=false;
	/** Shows to the tempVar visitor which argument we are processing (matters if inCall is set to true)
	 */
	Integer argCount=0;
	/** Set to true if the v0 register is already in use for a stack operation
	 */
	Boolean v0used;
	
	public StatementFinder(CodeBlockSpiglet cBlk, PrintWriter cWriter) {
		blk=cBlk;
		writer=cWriter;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.NodeList)
	 */
	@Override
	public String visit(NodeList n) {
		for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
			e.nextElement().accept(this);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.NodeListOptional)
	 */
	@Override
	public String visit(NodeListOptional n) {
		if (n.present()) {
			for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
				e.nextElement().accept(this);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.NodeOptional)
	 */
	@Override
	public String visit(NodeOptional n) {
		if ( n.present() ) {
			n.node.accept(this);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.NodeSequence)
	 */
	@Override
	public String visit(NodeSequence n) {
		for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
			e.nextElement().accept(this);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.NodeToken)
	 */
	@Override
	public String visit(NodeToken n) {
		return n.tokenImage;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.Goal)
	 */
	@Override
	public String visit(Goal n) {
		//Should never get here
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.StmtList)
	 */
	@Override
	public String visit(StmtList n) {
		//Should never get here
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.Procedure)
	 */
	@Override
	public String visit(Procedure n) {
		//Should never get here
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.Stmt)
	 */
	@Override
	public String visit(Stmt n) {
		//Should never get here
		return n.nodeChoice.accept(this);
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.NoOpStmt)
	 */
	@Override
	public String visit(NoOpStmt n) {
		writer.println("\tNOOP ");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.ErrorStmt)
	 */
	@Override
	public String visit(ErrorStmt n) {
		writer.println("\tERROR ");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.CJumpStmt)
	 */
	@Override
	public String visit(CJumpStmt n) {
		String tempReg = n.temp.accept(this);
		String label = n.label.accept(this);
		writer.println("\tCJUMP "+tempReg+" "+label+" ");
		clearVars();
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.JumpStmt)
	 */
	@Override
	public String visit(JumpStmt n) {
		String label = n.label.accept(this);
		writer.println("\tJUMP "+label+" ");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.HStoreStmt)
	 */
	@Override
	public String visit(HStoreStmt n) {
		String tempReg = n.temp.accept(this);
		String integerLit = n.integerLiteral.accept(this);
		String tempReg1 = n.temp1.accept(this);
		writer.println("\tHSTORE "+tempReg+" "+integerLit+" "+tempReg1+" ");
		clearVars();
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.HLoadStmt)
	 */
	@Override
	public String visit(HLoadStmt n) {
		String tempReg = n.temp.accept(this);
		String tempReg1 = n.temp1.accept(this);
		String integerLit = n.integerLiteral.accept(this);
		writer.println("\tHLOAD "+tempReg+" "+tempReg1+" "+integerLit+" ");
		clearVars();
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.MoveStmt)
	 */
	@Override
	public String visit(MoveStmt n) {
		if(blk.containsProcCall) {
			n.exp.accept(this);
			//If the var we assign a value to is in the stack 
			if(blk.varAssigned.regAs==RegisterAs.Stack) {
				writer.println("\tASTORE SPILLEDARG "+blk.varAssigned.stackPos+" "+RegisterAs.v0.toString()+" ");
			}
			//Else if this is in a normal register
			else {
				writer.println("\tMOVE "+blk.varAssigned.regAs.toString()+" "+RegisterAs.v0.toString()+" ");
			}
		}
		else {
			String expString = n.exp.accept(this);
			//If the var we assign a value to is in the stack 
			if(blk.varAssigned.regAs==RegisterAs.Stack) {
				writer.println("\tMOVE "+RegisterAs.v0.toString()+" "+expString+" ");
				writer.println("\tASTORE SPILLEDARG "+blk.varAssigned.stackPos+" "+RegisterAs.v0.toString()+" ");
			}
			//Else if this is in a normal register
			else {
				writer.println("\tMOVE "+blk.varAssigned.regAs.toString()+" "+expString+" ");
			}
		}
		clearVars();
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.PrintStmt)
	 */
	@Override
	public String visit(PrintStmt n) {
		String expString = n.simpleExp.accept(this);
		writer.println("\tPRINT "+expString+" ");
		clearVars();
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.Exp)
	 */
	@Override
	public String visit(Exp n) {
		if(blk.containsProcCall) {
			n.nodeChoice.accept(this);
			return null;
		}
		return n.nodeChoice.accept(this);
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.StmtExp)
	 */
	@Override
	public String visit(StmtExp n) {
		//Should never get here
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.Call)
	 */
	@Override
	public String visit(Call n) {
		inCall=true;
		argCount=0;
		n.nodeListOptional.accept(this);
		inCall=false;
		//Now all vars should be loaded
		//Just call the expression
		String expString = n.simpleExp.accept(this);
		writer.println("\tCALL "+expString+" ");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.HAllocate)
	 */
	@Override
	public String visit(HAllocate n) {
		String expString = n.simpleExp.accept(this);
		return "HALLOCATE "+expString;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.BinOp)
	 */
	@Override
	public String visit(BinOp n) {
		String op = n.operator.accept(this);
		String tempReg = n.temp.accept(this);
		String expString = n.simpleExp.accept(this);
		return op+" "+tempReg+" "+expString;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.Operator)
	 */
	@Override
	public String visit(Operator n) {
		return n.nodeChoice.accept(this);
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.SimpleExp)
	 */
	@Override
	public String visit(SimpleExp n) {
		return n.nodeChoice.accept(this);
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.Temp)
	 */
	@Override
	public String visit(Temp n) {
		VariableData var = blk.varUsed.get(n.integerLiteral.nodeToken.tokenImage);
		//If this variable is one of the arguments for a procedure call
		if(inCall) {
			if(argCount<4) {
				RegisterAs argReg = RegisterAs.values()[RegisterAs.a0.ordinal()+argCount];
				if(var.regAs==RegisterAs.Stack) {
					//Move it from the stack to the register
					writer.println("\tALOAD "+argReg.toString()+" SPILLEDARG "+var.stackPos+" ");
				}
				else {
					//Move it from the register to the register
					writer.println("\tMOVE "+argReg.toString()+" "+var.regAs.toString()+" ");
				}
			}
			else {
				if(var.regAs==RegisterAs.Stack) {
					//Move it from the stack to the passarg (remember the +1)
					writer.println("\tALOAD "+RegisterAs.v0.toString()+" SPILLEDARG "+var.stackPos+" ");
					writer.println("\tPASSARG "+(argCount+1)+" "+RegisterAs.v0.toString()+" ");
				}
				else {
					//Move it from the register to the passarg (remember the +1)
					writer.println("\tPASSARG "+(argCount+1)+" "+var.regAs.toString()+" ");
				}
			}
			++argCount;
			return null;
		}
		//Else if this is simply a request for a normal variable. return its name
		else {
			if(var.regAs==RegisterAs.Stack) {
				RegisterAs retReg;
				if(!v0used) {
					retReg=RegisterAs.v0;
					v0used=true;
				}
				else {
					retReg=RegisterAs.v1;
				}
				writer.println("\tALOAD "+retReg.toString()+" SPILLEDARG "+var.stackPos+" ");
				return retReg.toString();
			}
			else {
				return var.regAs.toString(); 
			}
		}
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.IntegerLiteral)
	 */
	@Override
	public String visit(IntegerLiteral n) {
		return n.nodeToken.tokenImage;
	}

	/* (non-Javadoc)
	 * @see visitor.Visitor#visit(syntaxtree.Label)
	 */
	@Override
	public String visit(Label n) {
		return n.nodeToken.tokenImage;
	}
	
	/** 
	 * Clears any var data necessary
	 */
	private void clearVars() {
		v0used=false;		
	}
}
