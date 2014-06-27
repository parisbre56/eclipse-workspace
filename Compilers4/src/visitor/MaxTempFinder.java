/**
 * 
 */
package visitor;

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
public class MaxTempFinder extends GJNoArguDepthFirst<Integer> {
	//
	   // Auto class visitors--probably don't need to be overridden.
	   //
	   public Integer visit(NodeList n) {
	      if (n.size() == 1)
	         return n.elementAt(0).accept(this);
	      Integer _ret=0;
	      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
	         _ret=Math.max(e.nextElement().accept(this),_ret);
	      }
	      return _ret;
	   }

	   public Integer visit(NodeListOptional n) {
	      if ( n.present() ) {
	         if (n.size() == 1)
	            return n.elementAt(0).accept(this);
	         Integer _ret=0;
	         for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
	            _ret=Math.max(e.nextElement().accept(this),_ret);
	         }
	         return _ret;
	      }
	      else
	         return 0;
	   }

	   public Integer visit(NodeOptional n) {
	      if ( n.present() )
	         return n.node.accept(this);
	      else
	         return 0;
	   }

	   public Integer visit(NodeSequence n) {
	      if (n.size() == 1)
	         return n.elementAt(0).accept(this);
	      Integer _ret=0;
	      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
	         _ret=Math.max(e.nextElement().accept(this),_ret);
	      }
	      return _ret;
	   }

	   public Integer visit(NodeToken n) { return 0; }

	   //
	   // User-generated visitor methods below
	   //

	   /**
	    * <PRE>
	    * nodeToken -> "MAIN"
	    * stmtList -> StmtList()
	    * nodeToken1 -> "END"
	    * nodeListOptional -> ( Procedure() )*
	    * nodeToken2 -> &lt;EOF&gt;
	    * </PRE>
	    */
	   public Integer visit(Goal n) {
	      Integer _ret=0;
	      _ret=n.stmtList.accept(this);
	      _ret=Math.max(n.nodeListOptional.accept(this),_ret);
	      return _ret;
	   }

	   /**
	    * <PRE>
	    * nodeListOptional -> ( ( Label() )? Stmt() )*
	    * </PRE>
	    */
	   public Integer visit(StmtList n) {
	      return n.nodeListOptional.accept(this);
	   }

	   /**
	    * <PRE>
	    * label -> Label()
	    * nodeToken -> "["
	    * integerLiteral -> IntegerLiteral()
	    * nodeToken1 -> "]"
	    * stmtExp -> StmtExp()
	    * </PRE>
	    */
	   public Integer visit(Procedure n) {
	      Integer _ret=Integer.decode(n.integerLiteral.nodeToken.tokenImage);
	      _ret=Math.max(n.stmtExp.accept(this),_ret);
	      return _ret;
	   }

	   /**
	    * <PRE>
	    * nodeChoice -> NoOpStmt()
	    *       | ErrorStmt()
	    *       | CJumpStmt()
	    *       | JumpStmt()
	    *       | HStoreStmt()
	    *       | HLoadStmt()
	    *       | MoveStmt()
	    *       | PrintStmt()
	    * </PRE>
	    */
	   public Integer visit(Stmt n) {
	      return n.nodeChoice.accept(this);
	   }

	   /**
	    * <PRE>
	    * nodeToken -> "NOOP"
	    * </PRE>
	    */
	   public Integer visit(NoOpStmt n) {
	      return n.nodeToken.accept(this);
	   }

	   /**
	    * <PRE>
	    * nodeToken -> "ERROR"
	    * </PRE>
	    */
	   public Integer visit(ErrorStmt n) {
	      return n.nodeToken.accept(this);
	   }

	   /**
	    * <PRE>
	    * nodeToken -> "CJUMP"
	    * exp -> Exp()
	    * label -> Label()
	    * </PRE>
	    */
	   public Integer visit(CJumpStmt n) {
	      return n.exp.accept(this);
	   }

	   /**
	    * <PRE>
	    * nodeToken -> "JUMP"
	    * label -> Label()
	    * </PRE>
	    */
	   public Integer visit(JumpStmt n) {
	      return 0;
	   }

	   /**
	    * <PRE>
	    * nodeToken -> "HSTORE"
	    * exp -> Exp()
	    * integerLiteral -> IntegerLiteral()
	    * exp1 -> Exp()
	    * </PRE>
	    */
	   public Integer visit(HStoreStmt n) {
	      return Math.max(n.exp.accept(this), n.exp1.accept(this));
	   }

	   /**
	    * <PRE>
	    * nodeToken -> "HLOAD"
	    * temp -> Temp()
	    * exp -> Exp()
	    * integerLiteral -> IntegerLiteral()
	    * </PRE>
	    */
	   public Integer visit(HLoadStmt n) {
	      return Math.max(n.temp.accept(this), n.exp.accept(this));
	   }

	   /**
	    * <PRE>
	    * nodeToken -> "MOVE"
	    * temp -> Temp()
	    * exp -> Exp()
	    * </PRE>
	    */
	   public Integer visit(MoveStmt n) {
	      return Math.max(n.temp.accept(this), n.exp.accept(this));
	   }

	   /**
	    * <PRE>
	    * nodeToken -> "PRINT"
	    * exp -> Exp()
	    * </PRE>
	    */
	   public Integer visit(PrintStmt n) {
	      return n.exp.accept(this);
	   }

	   /**
	    * <PRE>
	    * nodeChoice -> StmtExp()
	    *       | Call()
	    *       | HAllocate()
	    *       | BinOp()
	    *       | Temp()
	    *       | IntegerLiteral()
	    *       | Label()
	    * </PRE>
	    */
	   public Integer visit(Exp n) {
	      return n.nodeChoice.accept(this);
	   }

	   /**
	    * <PRE>
	    * nodeToken -> "BEGIN"
	    * stmtList -> StmtList()
	    * nodeToken1 -> "RETURN"
	    * exp -> Exp()
	    * nodeToken2 -> "END"
	    * </PRE>
	    */
	   public Integer visit(StmtExp n) {
	      return Math.max(n.exp.accept(this), n.stmtList.accept(this));
	   }

	   /**
	    * <PRE>
	    * nodeToken -> "CALL"
	    * exp -> Exp()
	    * nodeToken1 -> "("
	    * nodeListOptional -> ( Exp() )*
	    * nodeToken2 -> ")"
	    * </PRE>
	    */
	   public Integer visit(Call n) {
	      return Math.max(n.exp.accept(this), n.nodeListOptional.accept(this));
	   }

	   /**
	    * <PRE>
	    * nodeToken -> "HALLOCATE"
	    * exp -> Exp()
	    * </PRE>
	    */
	   public Integer visit(HAllocate n) {
	      return n.exp.accept(this);
	   }

	   /**
	    * <PRE>
	    * operator -> Operator()
	    * exp -> Exp()
	    * exp1 -> Exp()
	    * </PRE>
	    */
	   public Integer visit(BinOp n) {
	      return Math.max(n.exp.accept(this), n.exp1.accept(this));
	   }

	   /**
	    * <PRE>
	    * nodeChoice -> "LT"
	    *       | "PLUS"
	    *       | "MINUS"
	    *       | "TIMES"
	    * </PRE>
	    */
	   public Integer visit(Operator n) {
	      return n.nodeChoice.accept(this);
	   }

	   /**
	    * <PRE>
	    * nodeToken -> "TEMP"
	    * integerLiteral -> IntegerLiteral()
	    * </PRE>
	    */
	   public Integer visit(Temp n) {
	      return Integer.decode(n.integerLiteral.nodeToken.tokenImage);
	   }

	   /**
	    * <PRE>
	    * nodeToken -> &lt;INTEGER_LITERAL&gt;
	    * </PRE>
	    */
	   public Integer visit(IntegerLiteral n) {
	      return 0;
	   }

	   /**
	    * <PRE>
	    * nodeToken -> &lt;IDENTIFIER&gt;
	    * </PRE>
	    */
	   public Integer visit(Label n) {
	      return 0;
	   }
}
