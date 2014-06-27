//
// Generated by JTB 1.3.2 DIT@UoA patched
//

package visitor;
import syntaxtree.*;

/**
 * All void visitors must implement this interface.
 */

public interface Visitor {

   //
   // void Auto class visitors
   //

   public void visit(NodeList n);
   public void visit(NodeListOptional n);
   public void visit(NodeOptional n);
   public void visit(NodeSequence n);
   public void visit(NodeToken n);

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
   public void visit(Goal n);

   /**
    * <PRE>
    * nodeListOptional -> ( ( Label() )? Stmt() )*
    * </PRE>
    */
   public void visit(StmtList n);

   /**
    * <PRE>
    * label -> Label()
    * nodeToken -> "["
    * integerLiteral -> IntegerLiteral()
    * nodeToken1 -> "]"
    * stmtExp -> StmtExp()
    * </PRE>
    */
   public void visit(Procedure n);

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
   public void visit(Stmt n);

   /**
    * <PRE>
    * nodeToken -> "NOOP"
    * </PRE>
    */
   public void visit(NoOpStmt n);

   /**
    * <PRE>
    * nodeToken -> "ERROR"
    * </PRE>
    */
   public void visit(ErrorStmt n);

   /**
    * <PRE>
    * nodeToken -> "CJUMP"
    * exp -> Exp()
    * label -> Label()
    * </PRE>
    */
   public void visit(CJumpStmt n);

   /**
    * <PRE>
    * nodeToken -> "JUMP"
    * label -> Label()
    * </PRE>
    */
   public void visit(JumpStmt n);

   /**
    * <PRE>
    * nodeToken -> "HSTORE"
    * exp -> Exp()
    * integerLiteral -> IntegerLiteral()
    * exp1 -> Exp()
    * </PRE>
    */
   public void visit(HStoreStmt n);

   /**
    * <PRE>
    * nodeToken -> "HLOAD"
    * temp -> Temp()
    * exp -> Exp()
    * integerLiteral -> IntegerLiteral()
    * </PRE>
    */
   public void visit(HLoadStmt n);

   /**
    * <PRE>
    * nodeToken -> "MOVE"
    * temp -> Temp()
    * exp -> Exp()
    * </PRE>
    */
   public void visit(MoveStmt n);

   /**
    * <PRE>
    * nodeToken -> "PRINT"
    * exp -> Exp()
    * </PRE>
    */
   public void visit(PrintStmt n);

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
   public void visit(Exp n);

   /**
    * <PRE>
    * nodeToken -> "BEGIN"
    * stmtList -> StmtList()
    * nodeToken1 -> "RETURN"
    * exp -> Exp()
    * nodeToken2 -> "END"
    * </PRE>
    */
   public void visit(StmtExp n);

   /**
    * <PRE>
    * nodeToken -> "CALL"
    * exp -> Exp()
    * nodeToken1 -> "("
    * nodeListOptional -> ( Exp() )*
    * nodeToken2 -> ")"
    * </PRE>
    */
   public void visit(Call n);

   /**
    * <PRE>
    * nodeToken -> "HALLOCATE"
    * exp -> Exp()
    * </PRE>
    */
   public void visit(HAllocate n);

   /**
    * <PRE>
    * operator -> Operator()
    * exp -> Exp()
    * exp1 -> Exp()
    * </PRE>
    */
   public void visit(BinOp n);

   /**
    * <PRE>
    * nodeChoice -> "LT"
    *       | "PLUS"
    *       | "MINUS"
    *       | "TIMES"
    * </PRE>
    */
   public void visit(Operator n);

   /**
    * <PRE>
    * nodeToken -> "TEMP"
    * integerLiteral -> IntegerLiteral()
    * </PRE>
    */
   public void visit(Temp n);

   /**
    * <PRE>
    * nodeToken -> &lt;INTEGER_LITERAL&gt;
    * </PRE>
    */
   public void visit(IntegerLiteral n);

   /**
    * <PRE>
    * nodeToken -> &lt;IDENTIFIER&gt;
    * </PRE>
    */
   public void visit(Label n);

}

