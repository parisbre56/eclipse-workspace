//
// Generated by JTB 1.3.2 DIT@UoA patched
//

package syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeToken -> "MOVE"
 * temp -> Temp()
 * exp -> Exp()
 * </PRE>
 */
public class MoveStmt implements Node {
   private Node parent;
   public NodeToken nodeToken;
   public Temp temp;
   public Exp exp;

   public MoveStmt(NodeToken n0, Temp n1, Exp n2) {
      nodeToken = n0;
      if ( nodeToken != null ) nodeToken.setParent(this);
      temp = n1;
      if ( temp != null ) temp.setParent(this);
      exp = n2;
      if ( exp != null ) exp.setParent(this);
   }

   public MoveStmt(Temp n0, Exp n1) {
      nodeToken = new NodeToken("MOVE");
      if ( nodeToken != null ) nodeToken.setParent(this);
      temp = n0;
      if ( temp != null ) temp.setParent(this);
      exp = n1;
      if ( exp != null ) exp.setParent(this);
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

