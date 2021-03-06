//
// Generated by JTB 1.3.2 DIT@UoA patched
//

package visitor;
import syntaxtree.*;
import java.util.*;

/**
 * Provides default methods which visit each node in the tree in depth-first
 * order.  Your visitors may extend this class.
 */
public class DepthFirstVisitor implements Visitor {
   //
   // Auto class visitors--probably don't need to be overridden.
   //
   public void visit(NodeList n) {
      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); )
         e.nextElement().accept(this);
   }

   public void visit(NodeListOptional n) {
      if ( n.present() )
         for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); )
            e.nextElement().accept(this);
   }

   public void visit(NodeOptional n) {
      if ( n.present() )
         n.node.accept(this);
   }

   public void visit(NodeSequence n) {
      for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); )
         e.nextElement().accept(this);
   }

   public void visit(NodeToken n) {}

   //
   // User-generated visitor methods below
   //

   /**
    * <PRE>
    * nodeToken -> "MAIN"
    * nodeToken1 -> "["
    * integerLiteral -> IntegerLiteral()
    * nodeToken2 -> "]"
    * nodeToken3 -> "["
    * integerLiteral1 -> IntegerLiteral()
    * nodeToken4 -> "]"
    * nodeToken5 -> "["
    * integerLiteral2 -> IntegerLiteral()
    * nodeToken6 -> "]"
    * stmtList -> StmtList()
    * nodeToken7 -> "END"
    * nodeListOptional -> ( Procedure() )*
    * nodeToken8 -> &lt;EOF&gt;
    * </PRE>
    */
   public void visit(Goal n) {
      n.nodeToken.accept(this);
      n.nodeToken1.accept(this);
      n.integerLiteral.accept(this);
      n.nodeToken2.accept(this);
      n.nodeToken3.accept(this);
      n.integerLiteral1.accept(this);
      n.nodeToken4.accept(this);
      n.nodeToken5.accept(this);
      n.integerLiteral2.accept(this);
      n.nodeToken6.accept(this);
      n.stmtList.accept(this);
      n.nodeToken7.accept(this);
      n.nodeListOptional.accept(this);
      n.nodeToken8.accept(this);
   }

   /**
    * <PRE>
    * nodeListOptional -> ( ( Label() )? Stmt() )*
    * </PRE>
    */
   public void visit(StmtList n) {
      n.nodeListOptional.accept(this);
   }

   /**
    * <PRE>
    * label -> Label()
    * nodeToken -> "["
    * integerLiteral -> IntegerLiteral()
    * nodeToken1 -> "]"
    * nodeToken2 -> "["
    * integerLiteral1 -> IntegerLiteral()
    * nodeToken3 -> "]"
    * nodeToken4 -> "["
    * integerLiteral2 -> IntegerLiteral()
    * nodeToken5 -> "]"
    * stmtList -> StmtList()
    * nodeToken6 -> "END"
    * </PRE>
    */
   public void visit(Procedure n) {
      n.label.accept(this);
      n.nodeToken.accept(this);
      n.integerLiteral.accept(this);
      n.nodeToken1.accept(this);
      n.nodeToken2.accept(this);
      n.integerLiteral1.accept(this);
      n.nodeToken3.accept(this);
      n.nodeToken4.accept(this);
      n.integerLiteral2.accept(this);
      n.nodeToken5.accept(this);
      n.stmtList.accept(this);
      n.nodeToken6.accept(this);
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
    *       | ALoadStmt()
    *       | AStoreStmt()
    *       | PassArgStmt()
    *       | CallStmt()
    * </PRE>
    */
   public void visit(Stmt n) {
      n.nodeChoice.accept(this);
   }

   /**
    * <PRE>
    * nodeToken -> "NOOP"
    * </PRE>
    */
   public void visit(NoOpStmt n) {
      n.nodeToken.accept(this);
   }

   /**
    * <PRE>
    * nodeToken -> "ERROR"
    * </PRE>
    */
   public void visit(ErrorStmt n) {
      n.nodeToken.accept(this);
   }

   /**
    * <PRE>
    * nodeToken -> "CJUMP"
    * reg -> Reg()
    * label -> Label()
    * </PRE>
    */
   public void visit(CJumpStmt n) {
      n.nodeToken.accept(this);
      n.reg.accept(this);
      n.label.accept(this);
   }

   /**
    * <PRE>
    * nodeToken -> "JUMP"
    * label -> Label()
    * </PRE>
    */
   public void visit(JumpStmt n) {
      n.nodeToken.accept(this);
      n.label.accept(this);
   }

   /**
    * <PRE>
    * nodeToken -> "HSTORE"
    * reg -> Reg()
    * integerLiteral -> IntegerLiteral()
    * reg1 -> Reg()
    * </PRE>
    */
   public void visit(HStoreStmt n) {
      n.nodeToken.accept(this);
      n.reg.accept(this);
      n.integerLiteral.accept(this);
      n.reg1.accept(this);
   }

   /**
    * <PRE>
    * nodeToken -> "HLOAD"
    * reg -> Reg()
    * reg1 -> Reg()
    * integerLiteral -> IntegerLiteral()
    * </PRE>
    */
   public void visit(HLoadStmt n) {
      n.nodeToken.accept(this);
      n.reg.accept(this);
      n.reg1.accept(this);
      n.integerLiteral.accept(this);
   }

   /**
    * <PRE>
    * nodeToken -> "MOVE"
    * reg -> Reg()
    * exp -> Exp()
    * </PRE>
    */
   public void visit(MoveStmt n) {
      n.nodeToken.accept(this);
      n.reg.accept(this);
      n.exp.accept(this);
   }

   /**
    * <PRE>
    * nodeToken -> "PRINT"
    * simpleExp -> SimpleExp()
    * </PRE>
    */
   public void visit(PrintStmt n) {
      n.nodeToken.accept(this);
      n.simpleExp.accept(this);
   }

   /**
    * <PRE>
    * nodeToken -> "ALOAD"
    * reg -> Reg()
    * spilledArg -> SpilledArg()
    * </PRE>
    */
   public void visit(ALoadStmt n) {
      n.nodeToken.accept(this);
      n.reg.accept(this);
      n.spilledArg.accept(this);
   }

   /**
    * <PRE>
    * nodeToken -> "ASTORE"
    * spilledArg -> SpilledArg()
    * reg -> Reg()
    * </PRE>
    */
   public void visit(AStoreStmt n) {
      n.nodeToken.accept(this);
      n.spilledArg.accept(this);
      n.reg.accept(this);
   }

   /**
    * <PRE>
    * nodeToken -> "PASSARG"
    * integerLiteral -> IntegerLiteral()
    * reg -> Reg()
    * </PRE>
    */
   public void visit(PassArgStmt n) {
      n.nodeToken.accept(this);
      n.integerLiteral.accept(this);
      n.reg.accept(this);
   }

   /**
    * <PRE>
    * nodeToken -> "CALL"
    * simpleExp -> SimpleExp()
    * </PRE>
    */
   public void visit(CallStmt n) {
      n.nodeToken.accept(this);
      n.simpleExp.accept(this);
   }

   /**
    * <PRE>
    * nodeChoice -> HAllocate()
    *       | BinOp()
    *       | SimpleExp()
    * </PRE>
    */
   public void visit(Exp n) {
      n.nodeChoice.accept(this);
   }

   /**
    * <PRE>
    * nodeToken -> "HALLOCATE"
    * simpleExp -> SimpleExp()
    * </PRE>
    */
   public void visit(HAllocate n) {
      n.nodeToken.accept(this);
      n.simpleExp.accept(this);
   }

   /**
    * <PRE>
    * operator -> Operator()
    * reg -> Reg()
    * simpleExp -> SimpleExp()
    * </PRE>
    */
   public void visit(BinOp n) {
      n.operator.accept(this);
      n.reg.accept(this);
      n.simpleExp.accept(this);
   }

   /**
    * <PRE>
    * nodeChoice -> "LT"
    *       | "PLUS"
    *       | "MINUS"
    *       | "TIMES"
    * </PRE>
    */
   public void visit(Operator n) {
      n.nodeChoice.accept(this);
   }

   /**
    * <PRE>
    * nodeToken -> "SPILLEDARG"
    * integerLiteral -> IntegerLiteral()
    * </PRE>
    */
   public void visit(SpilledArg n) {
      n.nodeToken.accept(this);
      n.integerLiteral.accept(this);
   }

   /**
    * <PRE>
    * nodeChoice -> Reg()
    *       | IntegerLiteral()
    *       | Label()
    * </PRE>
    */
   public void visit(SimpleExp n) {
      n.nodeChoice.accept(this);
   }

   /**
    * <PRE>
    * nodeChoice -> "a0"
    *       | "a1"
    *       | "a2"
    *       | "a3"
    *       | "t0"
    *       | "t1"
    *       | "t2"
    *       | "t3"
    *       | "t4"
    *       | "t5"
    *       | "t6"
    *       | "t7"
    *       | "s0"
    *       | "s1"
    *       | "s2"
    *       | "s3"
    *       | "s4"
    *       | "s5"
    *       | "s6"
    *       | "s7"
    *       | "t8"
    *       | "t9"
    *       | "v0"
    *       | "v1"
    * </PRE>
    */
   public void visit(Reg n) {
      n.nodeChoice.accept(this);
   }

   /**
    * <PRE>
    * nodeToken -> &lt;INTEGER_LITERAL&gt;
    * </PRE>
    */
   public void visit(IntegerLiteral n) {
      n.nodeToken.accept(this);
   }

   /**
    * <PRE>
    * nodeToken -> &lt;IDENTIFIER&gt;
    * </PRE>
    */
   public void visit(Label n) {
      n.nodeToken.accept(this);
   }

}
