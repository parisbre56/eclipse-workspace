/**
 * 
 */
package visitor;

import java.io.PrintWriter;
import java.io.Writer;

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
 * @author Parisbre56
 *
 */
public class ReturnExpFinder implements Visitor {
	CodeBlockSpiglet blk=null;
	PrintWriter writer=null;
	
	
	public ReturnExpFinder(CodeBlockSpiglet cBlk, PrintWriter cWriter) {
		blk=cBlk;
		writer=cWriter;
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.NodeList)
	 */
	@Override
	public void visit(NodeList n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.NodeListOptional)
	 */
	@Override
	public void visit(NodeListOptional n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.NodeOptional)
	 */
	@Override
	public void visit(NodeOptional n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.NodeSequence)
	 */
	@Override
	public void visit(NodeSequence n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.NodeToken)
	 */
	@Override
	public void visit(NodeToken n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.Goal)
	 */
	@Override
	public void visit(Goal n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.StmtList)
	 */
	@Override
	public void visit(StmtList n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.Procedure)
	 */
	@Override
	public void visit(Procedure n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.Stmt)
	 */
	@Override
	public void visit(Stmt n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.NoOpStmt)
	 */
	@Override
	public void visit(NoOpStmt n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.ErrorStmt)
	 */
	@Override
	public void visit(ErrorStmt n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.CJumpStmt)
	 */
	@Override
	public void visit(CJumpStmt n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.JumpStmt)
	 */
	@Override
	public void visit(JumpStmt n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.HStoreStmt)
	 */
	@Override
	public void visit(HStoreStmt n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.HLoadStmt)
	 */
	@Override
	public void visit(HLoadStmt n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.MoveStmt)
	 */
	@Override
	public void visit(MoveStmt n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.PrintStmt)
	 */
	@Override
	public void visit(PrintStmt n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.Exp)
	 */
	@Override
	public void visit(Exp n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.StmtExp)
	 */
	@Override
	public void visit(StmtExp n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.Call)
	 */
	@Override
	public void visit(Call n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.HAllocate)
	 */
	@Override
	public void visit(HAllocate n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.BinOp)
	 */
	@Override
	public void visit(BinOp n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.Operator)
	 */
	@Override
	public void visit(Operator n) {
		//Not used
		
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.SimpleExp)
	 */
	@Override
	public void visit(SimpleExp n) {
		n.nodeChoice.accept(this);
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.Temp)
	 */
	@Override
	public void visit(Temp n) {
		//Get the variable we need
		VariableData var = blk.varUsed.get(n.integerLiteral.nodeToken.tokenImage);
		//If it's in the stack, put it in the variable v0
		if(var.regAs==RegisterAs.Stack) {
			writer.println("\tALOAD "+RegisterAs.v0.toString()+" SPILLEDARG "+var.stackPos+" ");
		}
		//Else if it isn't in v0, just put its value in v0
		//(There should be no reason for it to be in v0, but just in case)
		else if (var.regAs!=RegisterAs.v0) {
			writer.println("\tMOVE v0 "+var.regAs.toString()+" ");
		}
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.IntegerLiteral)
	 */
	@Override
	public void visit(IntegerLiteral n) {
		writer.println("\tMOVE v0 "+n.nodeToken.tokenImage+" ");
	}

	/* (non-Javadoc)
	 * @see visitor.GJNoArguVisitor#visit(syntaxtree.Label)
	 */
	@Override
	public void visit(Label n) {
		writer.println("\tMOVE v0 "+n.nodeToken.tokenImage+" ");
	}

}
