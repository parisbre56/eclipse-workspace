//
// Generated by JTB 1.3.2 DIT@UoA patched
//

package visitor;
import syntaxtree.*;

/**
 * All GJ visitors with no argument must implement this interface.
 */

public interface GJNoArguVisitor<R> {

   //
   // GJ Auto class visitors with no argument
   //

   public R visit(NodeList n) ;
   public R visit(NodeListOptional n) ;
   public R visit(NodeOptional n) ;
   public R visit(NodeSequence n) ;
   public R visit(NodeToken n) ;

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
   public R visit(Goal n);

   /**
    * <PRE>
    * nodeListOptional -> ( ( Label() )? Stmt() )*
    * </PRE>
    */
   public R visit(StmtList n);

   /**
    * <PRE>
    * label -> Label()
    * nodeToken -> "["
    * integerLiteral -> IntegerLiteral()
    * nodeToken1 -> "]"
    * stmtExp -> StmtExp()
    * </PRE>
    */
   public R visit(Procedure n);

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
   public R visit(Stmt n);

   /**
    * <PRE>
    * nodeToken -> "NOOP"
    * </PRE>
    */
   public R visit(NoOpStmt n);

   /**
    * <PRE>
    * nodeToken -> "ERROR"
    * </PRE>
    */
   public R visit(ErrorStmt n);

   /**
    * <PRE>
    * nodeToken -> "CJUMP"
    * exp -> Exp()
    * label -> Label()
    * </PRE>
    */
   public R visit(CJumpStmt n);

   /**
    * <PRE>
    * nodeToken -> "JUMP"
    * label -> Label()
    * </PRE>
    */
   public R visit(JumpStmt n);

   /**
    * <PRE>
    * nodeToken -> "HSTORE"
    * exp -> Exp()
    * integerLiteral -> IntegerLiteral()
    * exp1 -> Exp()
    * </PRE>
    */
   public R visit(HStoreStmt n);

   /**
    * <PRE>
    * nodeToken -> "HLOAD"
    * temp -> Temp()
    * exp -> Exp()
    * integerLiteral -> IntegerLiteral()
    * </PRE>
    */
   public R visit(HLoadStmt n);

   /**
    * <PRE>
    * nodeToken -> "MOVE"
    * temp -> Temp()
    * exp -> Exp()
    * </PRE>
    */
   public R visit(MoveStmt n);

   /**
    * <PRE>
    * nodeToken -> "PRINT"
    * exp -> Exp()
    * </PRE>
    */
   public R visit(PrintStmt n);

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
   public R visit(Exp n);

   /**
    * <PRE>
    * nodeToken -> "BEGIN"
    * stmtList -> StmtList()
    * nodeToken1 -> "RETURN"
    * exp -> Exp()
    * nodeToken2 -> "END"
    * </PRE>
    */
   public R visit(StmtExp n);

   /**
    * <PRE>
    * nodeToken -> "CALL"
    * exp -> Exp()
    * nodeToken1 -> "("
    * nodeListOptional -> ( Exp() )*
    * nodeToken2 -> ")"
    * </PRE>
    */
   public R visit(Call n);

   /**
    * <PRE>
    * nodeToken -> "HALLOCATE"
    * exp -> Exp()
    * </PRE>
    */
   public R visit(HAllocate n);

   /**
    * <PRE>
    * operator -> Operator()
    * exp -> Exp()
    * exp1 -> Exp()
    * </PRE>
    */
   public R visit(BinOp n);

   /**
    * <PRE>
    * nodeChoice -> "LT"
    *       | "PLUS"
    *       | "MINUS"
    *       | "TIMES"
    * </PRE>
    */
   public R visit(Operator n);

   /**
    * <PRE>
    * nodeToken -> "TEMP"
    * integerLiteral -> IntegerLiteral()
    * </PRE>
    */
   public R visit(Temp n);

   /**
    * <PRE>
    * nodeToken -> &lt;INTEGER_LITERAL&gt;
    * </PRE>
    */
   public R visit(IntegerLiteral n);

   /**
    * <PRE>
    * nodeToken -> &lt;IDENTIFIER&gt;
    * </PRE>
    */
   public R visit(Label n);

}

