//
// Generated by JTB 1.3.2 DIT@UoA patched
//

package syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeToken -> "HLOAD"
 * reg -> Reg()
 * reg1 -> Reg()
 * integerLiteral -> IntegerLiteral()
 * </PRE>
 */
public class HLoadStmt implements Node {
   private Node parent;
   public NodeToken nodeToken;
   public Reg reg;
   public Reg reg1;
   public IntegerLiteral integerLiteral;

   public HLoadStmt(NodeToken n0, Reg n1, Reg n2, IntegerLiteral n3) {
      nodeToken = n0;
      if ( nodeToken != null ) nodeToken.setParent(this);
      reg = n1;
      if ( reg != null ) reg.setParent(this);
      reg1 = n2;
      if ( reg1 != null ) reg1.setParent(this);
      integerLiteral = n3;
      if ( integerLiteral != null ) integerLiteral.setParent(this);
   }

   public HLoadStmt(Reg n0, Reg n1, IntegerLiteral n2) {
      nodeToken = new NodeToken("HLOAD");
      if ( nodeToken != null ) nodeToken.setParent(this);
      reg = n0;
      if ( reg != null ) reg.setParent(this);
      reg1 = n1;
      if ( reg1 != null ) reg1.setParent(this);
      integerLiteral = n2;
      if ( integerLiteral != null ) integerLiteral.setParent(this);
   }

   public void accept(visitor.Visitor v) {
      v.visit(this);
   }
   public <R,A> R accept(visitor.GJVisitor<R,A> v, A argu) {
      return v.visit(this,argu);
   }
   public <R> R accept(visitor.GJNoArguVisitor<R> v) {
      return v.visit(this);
   }
   public <A> void accept(visitor.GJVoidVisitor<A> v, A argu) {
      v.visit(this,argu);
   }
   public void setParent(Node n) { parent = n; }
   public Node getParent()       { return parent; }
}

