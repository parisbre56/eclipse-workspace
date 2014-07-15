//
// Generated by JTB 1.3.2 DIT@UoA patched
//

package visitor;
import syntaxtree.*;
import java.util.*;

/**
 * All GJ void visitors must implement this interface.
 */

public interface GJVoidVisitor<A> {

   //
   // GJ void Auto class visitors
   //

   public void visit(NodeList n, A argu);
   public void visit(NodeListOptional n, A argu);
   public void visit(NodeOptional n, A argu);
   public void visit(NodeSequence n, A argu);
   public void visit(NodeToken n, A argu);

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
   public void visit(Goal n, A argu);

   /**
    * <PRE>
    * nodeListOptional -> ( ( Label() )? Stmt() )*
    * </PRE>
    */
   public void visit(StmtList n, A argu);

   /**
    * <PRE>
    * label -> Label()
    * nodeToken -> "["
    * integerLiteral -> IntegerLiteral()
    * nodeToken1 -> "]"
    * stmtExp -> StmtExp()
    * </PRE>
    */
   public void visit(Procedure n, A argu);

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
   public void visit(Stmt n, A argu);

   /**
    * <PRE>
    * nodeToken -> "NOOP"
    * </PRE>
    */
   public void visit(NoOpStmt n, A argu);

   /**
    * <PRE>
    * nodeToken -> "ERROR"
    * </PRE>
    */
   public void visit(ErrorStmt n, A argu);

   /**
    * <PRE>
    * nodeToken -> "CJUMP"
    * temp -> Temp()
    * label -> Label()
    * </PRE>
    */
   public void visit(CJumpStmt n, A argu);

   /**
    * <PRE>
    * nodeToken -> "JUMP"
    * label -> Label()
    * </PRE>
    */
   public void visit(JumpStmt n, A argu);

   /**
    * <PRE>
    * nodeToken -> "HSTORE"
    * temp -> Temp()
    * integerLiteral -> IntegerLiteral()
    * temp1 -> Temp()
    * </PRE>
    */
   public void visit(HStoreStmt n, A argu);

   /**
    * <PRE>
    * nodeToken -> "HLOAD"
    * temp -> Temp()
    * temp1 -> Temp()
    * integerLiteral -> IntegerLiteral()
    * </PRE>
    */
   public void visit(HLoadStmt n, A argu);

   /**
    * <PRE>
    * nodeToken -> "MOVE"
    * temp -> Temp()
    * exp -> Exp()
    * </PRE>
    */
   public void visit(MoveStmt n, A argu);

   /**
    * <PRE>
    * nodeToken -> "PRINT"
    * simpleExp -> SimpleExp()
    * </PRE>
    */
   public void visit(PrintStmt n, A argu);

   /**
    * <PRE>
    * nodeChoice -> Call()
    *       | HAllocate()
    *       | BinOp()
    *       | SimpleExp()
    * </PRE>
    */
   public void visit(Exp n, A argu);

   /**
    * <PRE>
    * nodeToken -> "BEGIN"
    * stmtList -> StmtList()
    * nodeToken1 -> "RETURN"
    * simpleExp -> SimpleExp()
    * nodeToken2 -> "END"
    * </PRE>
    */
   public void visit(StmtExp n, A argu);

   /**
    * <PRE>
    * nodeToken -> "CALL"
    * simpleExp -> SimpleExp()
    * nodeToken1 -> "("
    * nodeListOptional -> ( Temp() )*
    * nodeToken2 -> ")"
    * </PRE>
    */
   public void visit(Call n, A argu);

   /**
    * <PRE>
    * nodeToken -> "HALLOCATE"
    * simpleExp -> SimpleExp()
    * </PRE>
    */
   public void visit(HAllocate n, A argu);

   /**
    * <PRE>
    * operator -> Operator()
    * temp -> Temp()
    * simpleExp -> SimpleExp()
    * </PRE>
    */
   public void visit(BinOp n, A argu);

   /**
    * <PRE>
    * nodeChoice -> "LT"
    *       | "PLUS"
    *       | "MINUS"
    *       | "TIMES"
    * </PRE>
    */
   public void visit(Operator n, A argu);

   /**
    * <PRE>
    * nodeChoice -> Temp()
    *       | IntegerLiteral()
    *       | Label()
    * </PRE>
    */
   public void visit(SimpleExp n, A argu);

   /**
    * <PRE>
    * nodeToken -> "TEMP"
    * integerLiteral -> IntegerLiteral()
    * </PRE>
    */
   public void visit(Temp n, A argu);

   /**
    * <PRE>
    * nodeToken -> &lt;INTEGER_LITERAL&gt;
    * </PRE>
    */
   public void visit(IntegerLiteral n, A argu);

   /**
    * <PRE>
    * nodeToken -> &lt;IDENTIFIER&gt;
    * </PRE>
    */
   public void visit(Label n, A argu);

}
