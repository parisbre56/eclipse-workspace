/**
 * 
 */
package visitor;

import java.io.IOException;
import java.io.Writer;
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
import syntaxtree.Stmt;
import syntaxtree.StmtExp;
import syntaxtree.StmtList;
import syntaxtree.Temp;

/**
 * @author Parisbre56
 *
 */
public class ToSpigletVisitor implements GJVisitor<String, String> {
	//Pass what is needed to Exp and other functions Like "Exp" or "simpleExp" or "Temp"
	//That way they can know what to return. Also, make sure to check if we request a subset
	//make sure to modify list processors to concat strings
	//Return the appropriate value, be it integer, label, tempvar, etc, according to argu
	
	Integer tempCounter = 0;
	
	Writer output = null;
	Integer tabLevel = 0;
	Integer labelCounter = 0;

	/**
	 * @param writer used to output the translated and partially formatted spiglet code 
	 * 
	 */
	public ToSpigletVisitor(Writer writer) {
		output = writer;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.NodeList, java.lang.Object)
	 */
	@Override
	public String visit(NodeList n, String argu) throws VisitorException {
		String retString = new String();
		for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
			retString+=e.nextElement().accept(this,argu);
			if(e.hasMoreElements()) {
				retString+=" ";
			}
		}
		return retString;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.NodeListOptional, java.lang.Object)
	 */
	@Override
	public String visit(NodeListOptional n, String argu) throws VisitorException {
		if (n.present()) {
			String retString = new String();
			for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
				retString+=e.nextElement().accept(this,argu);
				if(e.hasMoreElements()) {
					retString+=" ";
				}
			}
			return retString;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.NodeOptional, java.lang.Object)
	 */
	@Override
	public String visit(NodeOptional n, String argu) throws VisitorException {
		if ( n.present() ) {
			return n.node.accept(this,argu);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.NodeSequence, java.lang.Object)
	 */
	@Override
	public String visit(NodeSequence n, String argu) throws VisitorException {
		String retString = new String();
		for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
			retString+=e.nextElement().accept(this,argu);
			if(e.hasMoreElements()) {
				retString+=" ";
			}
		}
		return retString;
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
	public String visit(Goal n, String argu) throws VisitorException {
		tempCounter = n.accept(new MaxTempFinder());
		
		
		writeToOutputFirst("MAIN\n");
		++tabLevel;
		n.stmtList.accept(this,null);
		--tabLevel;
		writeToOutputFirst("END\n\n");
		n.nodeListOptional.accept(this,null);
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.StmtList, java.lang.Object)
	 */
	@Override
	public String visit(StmtList n, String argu) throws VisitorException {
		return n.nodeListOptional.accept(this,argu);
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Procedure, java.lang.Object)
	 */
	@Override
	public String visit(Procedure n, String argu) throws VisitorException {
		writeToOutputFirst(n.label.nodeToken.tokenImage
				+" ["+n.integerLiteral.nodeToken.tokenImage+"]\n");
		++tabLevel;
		n.stmtExp.accept(this,null);
		--tabLevel;
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Stmt, java.lang.Object)
	 */
	@Override
	public String visit(Stmt n, String argu) throws VisitorException {
		return n.nodeChoice.accept(this,argu);
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.NoOpStmt, java.lang.Object)
	 */
	@Override
	public String visit(NoOpStmt n, String argu) throws VisitorException {
		writeToOutputFirst("NOOP\n");
		return "NOOP";
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ErrorStmt, java.lang.Object)
	 */
	@Override
	public String visit(ErrorStmt n, String argu) throws VisitorException {
		writeToOutputFirst("ERROR\n");
		return "ERROR";
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.CJumpStmt, java.lang.Object)
	 */
	@Override
	public String visit(CJumpStmt n, String argu) throws VisitorException {
		//TempStore holds the temp var that holds the result of exp
		String tempStore = n.exp.accept(this,"Temp");
		writeToOutputFirst("CJUMP "+tempStore+" "+n.label.nodeToken.tokenImage+"\n");
		return "CJUMP";
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.JumpStmt, java.lang.Object)
	 */
	@Override
	public String visit(JumpStmt n, String argu) throws VisitorException {
		writeToOutputFirst("JUMP "+n.label.nodeToken.tokenImage+"\n");
		return "JUMP";
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.HStoreStmt, java.lang.Object)
	 */
	@Override
	public String visit(HStoreStmt n, String argu) throws VisitorException {
		String tempStore1 = n.exp.accept(this,"Temp");
		String tempStore2 = n.exp1.accept(this,"Temp");
		writeToOutputFirst("HSTORE "+tempStore1+" "
				+n.integerLiteral.nodeToken.tokenImage+" "+tempStore2+"\n");
		return "HSTORE";
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.HLoadStmt, java.lang.Object)
	 */
	@Override
	public String visit(HLoadStmt n, String argu) throws VisitorException {
		String tempStore = n.exp.accept(this,"Temp");
		writeToOutputFirst("HLOAD TEMP "+n.temp.integerLiteral.nodeToken.tokenImage
				+" "+tempStore+" "+n.integerLiteral.nodeToken.tokenImage+"\n");
		return "HLOAD";
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.MoveStmt, java.lang.Object)
	 */
	@Override
	public String visit(MoveStmt n, String argu) throws VisitorException {
		String tempStore = n.exp.accept(this,"Exp");
		writeToOutputFirst("MOVE TEMP "+n.temp.integerLiteral.nodeToken.tokenImage
				+" "+tempStore+"\n");
		return "MOVE";
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.PrintStmt, java.lang.Object)
	 */
	@Override
	public String visit(PrintStmt n, String argu) throws VisitorException {
		String tempStore = n.exp.accept(this,"SimpleExp");
		writeToOutputFirst("PRINT "+tempStore+"\n");
		return "PRINT";
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Exp, java.lang.Object)
	 */
	@Override
	public String visit(Exp n, String argu) throws VisitorException {
		return n.nodeChoice.accept(this,argu);
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.StmtExp, java.lang.Object)
	 */
	@Override
	public String visit(StmtExp n, String argu) throws VisitorException {
		if(argu==null) {
			//argu == null means that this is a StmtExpr following a procedure deceleration
			//That means that we must print begin\end and stuff. Else we just need to simplify
			//it and return the simplified expression
			writeToOutputFirst("BEGIN\n");
			++tabLevel;
		}
		n.stmtList.accept(this,null);
		if(argu==null) {
			String tempStore = n.exp.accept(this,"SimpleExp");
			writeToOutputFirst("RETURN "+tempStore+"\n");
			--tabLevel;
			writeToOutputFirst("END\n\n");
			return tempStore;
		}
		return n.exp.accept(this,argu); //Return the appropriate value, be it integer, label, tempvar, etc, according to argu.
		//The returned value should be the one returned by the return statement
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Call, java.lang.Object)
	 */
	@Override
	public String visit(Call n, String argu) throws VisitorException {
		String tempStore = n.exp.accept(this,"SimpleExp");
		String tempList = n.nodeListOptional.accept(this,"Temp");
		if(tempList==null) {//null means this has no arguments
			tempList="";
		}
		if(argu.equals("SimpleExp")||argu.equals("Temp")) {
			Integer tempReturn = tempCounter;
			++tempCounter;
			writeToOutputFirst("MOVE TEMP "+tempReturn.toString()
					+" CALL "+tempStore+" ("+tempList+")\n");
			return "TEMP "+tempReturn.toString();
		}
		return "CALL "+tempStore+" ("+tempList+")";
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.HAllocate, java.lang.Object)
	 */
	@Override
	public String visit(HAllocate n, String argu) throws VisitorException {
		String tempStore = n.exp.accept(this,"SimpleExp");
		if(argu.equals("SimpleExp")||argu.equals("Temp")) {
			Integer tempReturn = tempCounter;
			++tempCounter;
			writeToOutputFirst("MOVE TEMP "+tempReturn.toString()
					+" HALLOCATE "+tempStore+"\n");
			return "TEMP "+tempReturn.toString();
		}
		return "HALLOCATE "+tempStore;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.BinOp, java.lang.Object)
	 */
	@Override
	public String visit(BinOp n, String argu) throws VisitorException {
		String tempStore1=n.exp.accept(this,"Temp");
		String tempStore2=n.exp1.accept(this,"SimpleExp");
		if(argu.equals("SimpleExp")||argu.equals("Temp")) {
			Integer tempReturn = tempCounter;
			++tempCounter;
			writeToOutputFirst("MOVE TEMP "+tempReturn.toString()
					+n.operator.accept(this,null)+" "+tempStore1+" "+tempStore2+"\n");
			return "TEMP "+tempReturn.toString();
		}
		return n.operator.accept(this,null)+" "+tempStore1+" "+tempStore2;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Operator, java.lang.Object)
	 */
	@Override
	public String visit(Operator n, String argu) throws VisitorException {
		return n.nodeChoice.accept(this,null);
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Temp, java.lang.Object)
	 */
	@Override
	public String visit(Temp n, String argu) {
		return "TEMP "+n.integerLiteral.nodeToken.tokenImage;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.IntegerLiteral, java.lang.Object)
	 */
	@Override
	public String visit(IntegerLiteral n, String argu) throws VisitorException {
		if(argu.equals("Temp")) {
			Integer tempReturn = tempCounter;
			++tempCounter;
			writeToOutputFirst("MOVE TEMP "+tempReturn.toString()+" "+n.nodeToken.tokenImage+"\n");
			return "TEMP "+tempReturn.toString();
		}
		return n.nodeToken.tokenImage;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Label, java.lang.Object)
	 */
	@Override
	public String visit(Label n, String argu) throws VisitorException {
		if(argu==null) {
			writeToOutputFirst(n.nodeToken.tokenImage+"\n");
			return n.nodeToken.tokenImage;
		}
		if(argu.equals("Temp")) {
			Integer tempReturn = tempCounter;
			++tempCounter;
			writeToOutputFirst("MOVE TEMP "+tempReturn.toString()+" "+n.nodeToken.tokenImage+"\n");
			return "TEMP "+tempReturn.toString();
		}
		return n.nodeToken.tokenImage;
	}
	
	/** 
	 * Writes tabs in front of the string according to tabLevel and then writes the string
	 * It also transforms IOExceptions into VisitorIOExceptions and puts the causing IOException in it as cause
	 * @param string to write
	 * @throws VisitorIOException which contains an IOException as its cause
	 */
	private void writeToOutputFirst(String string) throws VisitorIOException {
		try {
			for(int i=0;i<tabLevel;++i) {
				output.write("\t");
			}
			output.write(string);
		} catch (IOException e) {
			e.printStackTrace();
			throw new VisitorIOException(e);
		}
	}
	
	/** 
	 * Writes the string
	 * This transforms IOExceptions into VisitorIOExceptions and puts the causing IOException in it as cause
	 * @param string to write
	 * @throws VisitorIOException which contains an IOException as its cause 
	 */
	private void writeToOutput(String string) throws VisitorIOException {
		try {
			output.write(string);
		} catch (IOException e) {
			e.printStackTrace();
			throw new VisitorIOException(e);
		}
	}
}
