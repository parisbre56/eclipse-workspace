/**
 * 
 */
package visitor;

import java.io.PrintWriter;
import java.util.Enumeration;

import syntaxtree.ALoadStmt;
import syntaxtree.AStoreStmt;
import syntaxtree.BinOp;
import syntaxtree.CJumpStmt;
import syntaxtree.CallStmt;
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
import syntaxtree.PassArgStmt;
import syntaxtree.PrintStmt;
import syntaxtree.Procedure;
import syntaxtree.Reg;
import syntaxtree.SimpleExp;
import syntaxtree.SpilledArg;
import syntaxtree.Stmt;
import syntaxtree.StmtList;

/**
 * @author Parisbre56
 *
 */
public class MIPSTranslatorVisitor implements GJVisitor<String, String> {
	public static class ProcData {
		public Integer numOfArgs;
		public Integer stackNeeded;
		public Integer maxArgsCalled;

		public ProcData(Integer cnumOfArgs, Integer cstackNeeded, Integer cmaxArgsCalled) {
			this.numOfArgs = cnumOfArgs;
			this.stackNeeded = cstackNeeded;
			this.maxArgsCalled = cmaxArgsCalled;
		}

		public ProcData(IntegerLiteral l, IntegerLiteral l1, IntegerLiteral l2) {
			numOfArgs=Integer.decode(l.nodeToken.tokenImage);
			stackNeeded=Integer.decode(l1.nodeToken.tokenImage);
			maxArgsCalled=Integer.decode(l2.nodeToken.tokenImage);
		}
	}

	PrintWriter writer = null;
	ProcData procData = null;
	/** The next available error number,
	 * used for error printing
	 */
	Integer freeErrno = 1;

	/**
	 * @param writer 
	 * 
	 */
	public MIPSTranslatorVisitor(PrintWriter cWriter) {
		writer = cWriter;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.NodeList, java.lang.Object)
	 */
	@Override
	public String visit(NodeList n, String argu) {
		for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
			e.nextElement().accept(this,argu);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.NodeListOptional, java.lang.Object)
	 */
	@Override
	public String visit(NodeListOptional n, String argu) {
		if (n.present()) {
			for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
				e.nextElement().accept(this,argu);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.NodeOptional, java.lang.Object)
	 */
	@Override
	public String visit(NodeOptional n, String argu) {
		if ( n.present() ) {
			n.node.accept(this,argu);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.NodeSequence, java.lang.Object)
	 */
	@Override
	public String visit(NodeSequence n, String argu) {
		for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
			e.nextElement().accept(this,argu);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.NodeToken, java.lang.Object)
	 */
	@Override
	public String visit(NodeToken n, String argu) {
		return n.tokenImage;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Goal, java.lang.Object)
	 */
	@Override
	public String visit(Goal n, String argu) {
		//Print error string data
		writer.println("\t.data");
		writer.println("error_string: .asciiz \"ERROR \"");
		writer.println();
		
		//Process main
		procData = new ProcData(n.integerLiteral,n.integerLiteral1,n.integerLiteral2);
		writer.println("\t.text");
		//Procedure label
		writer.println("main:");
		//Find out how much stack we need(+1 for the return address)
		Integer stackAlloc=procData.stackNeeded+1;
		//Allocate stack
		writer.println("\tadd $sp, $sp, "+(-stackAlloc*4));
		//Store return adress
		writer.println("\tsw $ra, 4($sp)");
		//Process statements
		n.stmtList.accept(this,null);
		//Retrieve return address
		writer.println("\tlw $ra, 4($sp)");
		//Deallocate stack
		writer.println("\tadd $sp, $sp, "+(stackAlloc*4));
		//Return 0
		writer.println("\tli $v0, 0");
		writer.println("\tjr $ra");
		writer.println();
		//Clear data field
		procData=null;
		//======================================
		
		//Process other procedures
		n.nodeListOptional.accept(this,null);
		
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.StmtList, java.lang.Object)
	 */
	@Override
	public String visit(StmtList n, String argu) {
		n.nodeListOptional.accept(this,".:STMT:.");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Procedure, java.lang.Object)
	 */
	@Override
	public String visit(Procedure n, String argu) {
		procData = new ProcData(n.integerLiteral,n.integerLiteral1,n.integerLiteral2);
		//Procedure label
		writer.println(n.label.nodeToken.tokenImage+":");
		//Find out how much stack we need (+1 for storing the return address)
		Integer stackAlloc=procData.stackNeeded+1;
		//Allocate stack
		writer.println("\tadd $sp, $sp, "+(-stackAlloc*4));
		//Store return adress
		writer.println("\tsw $ra, 4($sp)");
		//Process statements
		n.stmtList.accept(this,null);
		//Retrieve return address
		writer.println("\tlw $ra, 4($sp)");
		//Deallocate stack
		writer.println("\tadd $sp, $sp, "+(stackAlloc*4));
		//Return
		writer.println("\tjr $ra");
		writer.println();
		//Clear data field
		procData=null;
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Stmt, java.lang.Object)
	 */
	@Override
	public String visit(Stmt n, String argu) {
		n.nodeChoice.accept(this,null);
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.NoOpStmt, java.lang.Object)
	 */
	@Override
	public String visit(NoOpStmt n, String argu) {
		//Noops are simply ignored
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ErrorStmt, java.lang.Object)
	 */
	@Override
	public String visit(ErrorStmt n, String argu) {
		//Print error
		writer.println("\tli $v0, 4"); //4 -> print_string
		writer.println("\tla $a0, error_string"); //String contains space, so no need to print it
		writer.println("\tsyscall");
		//Print errno
		writer.println("\tli $v0, 1"); //1 -> print_int
		writer.println("\tli $a0, "+freeErrno);
		writer.println("\tsyscall");
		//Print newline
		writer.println("\tli $v0, 11"); //11 -> print_char
		writer.println("\tli $a0, 10"); //10 is ascii for newline
		writer.println("\tsyscall");
		//And return 1 for failure
		writer.println("\tli $v0, 17"); //17 -> exit2 -> terminate with return value
		writer.println("\tli $a0, "+freeErrno);
		writer.println("\tsyscall");
		++freeErrno;
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.CJumpStmt, java.lang.Object)
	 */
	@Override
	public String visit(CJumpStmt n, String argu) {
		//If the register is not exactly equal to 1, do not move to the next statement, but jump to the label
		writer.println("\tbne "+n.reg.accept(this,null)+", 1, "+n.label.accept(this,null));
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.JumpStmt, java.lang.Object)
	 */
	@Override
	public String visit(JumpStmt n, String argu) {
		writer.println("\tj "+n.label.accept(this,null));
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.HStoreStmt, java.lang.Object)
	 */
	@Override
	public String visit(HStoreStmt n, String argu) {
		writer.println("\tsw "+n.reg1.accept(this,null)+", "
				+n.integerLiteral.nodeToken.tokenImage+"("+n.reg.accept(this,null)+")");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.HLoadStmt, java.lang.Object)
	 */
	@Override
	public String visit(HLoadStmt n, String argu) {
		writer.println("\tlw "+n.reg.accept(this,null)+", "
				+n.integerLiteral.nodeToken.tokenImage+"("+n.reg1.accept(this,null)+")");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.MoveStmt, java.lang.Object)
	 */
	@Override
	public String visit(MoveStmt n, String argu) {
		//Just give the Exp the register the result should be put it. 
		//Depending on the type of the expression, it should know what to do.
		n.exp.accept(this,n.reg.accept(this,null));
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.PrintStmt, java.lang.Object)
	 */
	@Override
	public String visit(PrintStmt n, String argu) {
		//Print number
		//Process the simple expression and store the result to $a0.
		n.simpleExp.accept(this,"$a0");
		writer.println("\tli $v0, 1"); //1 -> print_int
		writer.println("\tsyscall");
		//Print newline
		writer.println("\tli $v0, 11"); //11 -> print_char
		writer.println("\tli $a0, 10"); //10 is ascii for newline
		writer.println("\tsyscall");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ALoadStmt, java.lang.Object)
	 */
	@Override
	public String visit(ALoadStmt n, String argu) {
		//For stack req = 7
		//4*((7-0)+1) , 4*((7-1)+1) 
		//0->32 , 1->28 , 2->24 , 3->20 , 4->16 , 5->12 , 6->8 , 7->4 (where 7 is the return address)
		Integer spilledArg = Integer.decode(n.spilledArg.integerLiteral.nodeToken.tokenImage);
		writer.println("\tlw "+n.reg.accept(this, null)+", "+(4*(procData.stackNeeded-spilledArg+1))+"($sp)");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.AStoreStmt, java.lang.Object)
	 */
	@Override
	public String visit(AStoreStmt n, String argu) {
		Integer spilledArg = Integer.decode(n.spilledArg.integerLiteral.nodeToken.tokenImage);
		writer.println("\tsw "+n.reg.accept(this, null)+", "+(4*(procData.stackNeeded-spilledArg+1))+"($sp)");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.PassArgStmt, java.lang.Object)
	 */
	@Override
	public String visit(PassArgStmt n, String argu) {
		Integer passArgNum = Integer.decode(n.integerLiteral.nodeToken.tokenImage);
		writer.println("\tsw "+n.reg.accept(this,null)+" "+(-(passArgNum-1)*4)+"($sp)");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.CallStmt, java.lang.Object)
	 */
	@Override
	public String visit(CallStmt n, String argu) {
		String expString = n.simpleExp.accept(this,null);
		if(expString.charAt(0)=='$') {
			writer.println("\tjalr "+expString);
		}
		else {
			writer.println("\tjal "+expString);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Exp, java.lang.Object)
	 */
	@Override
	public String visit(Exp n, String argu) {
		//Just tell the expression where to return the result and it will take care of it
		n.nodeChoice.accept(this,argu);
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.HAllocate, java.lang.Object)
	 */
	@Override
	public String visit(HAllocate n, String argu) {
		//Tell the simple expression visitor to put the result in $a0
		n.simpleExp.accept(this,"$a0");
		writer.println("\tli $v0, 9");
		writer.println("\tsyscall");
		//The result of memory allocation is now in v0. If necessary, move it to another register
		if(!"$v0".equals(argu)) {
			writer.println("\tmove "+argu+", $v0");
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.BinOp, java.lang.Object)
	 */
	@Override
	public String visit(BinOp n, String argu) {
		String operator = n.operator.accept(this,null);
		String reg = n.reg.accept(this,null);
		String expString = n.simpleExp.accept(this,null);
		if("LT".equals(operator)) {
			if(expString.charAt(0)=='$') {
				writer.println("\tslt "+argu+", "+reg+", "+expString);
			}
			else {
				writer.println("\tslti "+argu+", "+reg+", "+expString);
			}
		}
		else if ("PLUS".equals(operator)) {
			if(expString.charAt(0)=='$') {
				writer.println("\tadd "+argu+", "+reg+", "+expString);
			}
			else {
				writer.println("\taddi "+argu+", "+reg+", "+expString);
			}
		}
		else if ("MINUS".equals(operator)) {
			if(expString.charAt(0)=='$') {
				writer.println("\tsub "+argu+", "+reg+", "+expString);
			}
			else {
				writer.println("\taddi "+argu+", "+reg+", -"+expString);
			}
		}
		else if ("TIMES".equals(operator)) {
			if(expString.charAt(0)=='$') {
				writer.println("\tmul "+argu+", "+reg+", "+expString);
			}
			else {
				//Since $ra will be restored from the stack before we return, there's no problem using it
				//as a temp variable
				writer.println("\tli $ra, "+expString);
				writer.println("\tmul "+argu+", "+reg+", $ra");
			}
		}
		else {
			throw new NullPointerException("CHECK YOUR CODE!");
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Operator, java.lang.Object)
	 */
	@Override
	public String visit(Operator n, String argu) {
		return n.nodeChoice.accept(this,null);
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.SpilledArg, java.lang.Object)
	 */
	@Override
	public String visit(SpilledArg n, String argu) {
		return n.integerLiteral.nodeToken.tokenImage;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.SimpleExp, java.lang.Object)
	 */
	@Override
	public String visit(SimpleExp n, String argu) {
		return n.nodeChoice.accept(this,argu);
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Reg, java.lang.Object)
	 */
	@Override
	public String visit(Reg n, String argu) {
		if(argu==null) {
			return "$"+n.nodeChoice.accept(this,null);
		}
		writer.println("\tmove "+argu+", $"+n.nodeChoice.accept(this,null));		
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.IntegerLiteral, java.lang.Object)
	 */
	@Override
	public String visit(IntegerLiteral n, String argu) {
		if(argu==null) {
			return n.nodeToken.tokenImage;
		}
		writer.println("\tli "+argu+", "+n.nodeToken.tokenImage);
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Label, java.lang.Object)
	 */
	@Override
	public String visit(Label n, String argu) {
		//If this is a statement, then just print the label
		if(".:STMT:.".equals(argu)) {
			writer.println(n.nodeToken.tokenImage+":");
		}
		else if(argu==null) {
			return n.nodeToken.tokenImage;
		}
		else {
			writer.println("\tla "+argu+", "+n.nodeToken.tokenImage);
		}
		return null;
	}

}
