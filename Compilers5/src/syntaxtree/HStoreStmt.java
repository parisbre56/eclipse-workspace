//
// Generated by JTB 1.3.2 DIT@UoA patched
//

package syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeToken -> "HSTORE"
 * temp -> Temp()
 * integerLiteral -> IntegerLiteral()
 * temp1 -> Temp()
 * </PRE>
 */
public class HStoreStmt implements Node {
   private Node parent;
   public NodeToken nodeToken;
   public Temp temp;
   public IntegerLiteral integerLiteral;
   public Temp temp1;

   public HStoreStmt(NodeToken n0, Temp n1, IntegerLiteral n2, Temp n3) {
      nodeToken = n0;
      if ( nodeToken != null ) nodeToken.setParent(this);
      temp = n1;
      if ( temp != null ) temp.setParent(this);
      integerLiteral = n2;
      if ( integerLiteral != null ) integerLiteral.setParent(this);
      temp1 = n3;
      if ( temp1 != null ) temp1.setParent(this);
   }

   public HStoreStmt(Temp n0, IntegerLiteral n1, Temp n2) {
      nodeToken = new NodeToken("HSTORE");
      if ( nodeToken != null ) nodeToken.setParent(this);
      temp = n0;
      if ( temp != null ) temp.setParent(this);
      integerLiteral = n1;
      if ( integerLiteral != null ) integerLiteral.setParent(this);
      temp1 = n2;
      if ( temp1 != null ) temp1.setParent(this);
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

