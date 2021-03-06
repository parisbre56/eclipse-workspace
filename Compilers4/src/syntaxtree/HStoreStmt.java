//
// Generated by JTB 1.3.2 DIT@UoA patched
//

package syntaxtree;

import visitor.VisitorException;

/**
 * Grammar production:
 * <PRE>
 * nodeToken -> "HSTORE"
 * exp -> Exp()
 * integerLiteral -> IntegerLiteral()
 * exp1 -> Exp()
 * </PRE>
 */
public class HStoreStmt implements Node {
   /**
	 * 
	 */
	private static final long serialVersionUID = -7797811895184563654L;
private Node parent;
   public NodeToken nodeToken;
   public Exp exp;
   public IntegerLiteral integerLiteral;
   public Exp exp1;

   public HStoreStmt(NodeToken n0, Exp n1, IntegerLiteral n2, Exp n3) {
      nodeToken = n0;
      if ( nodeToken != null ) nodeToken.setParent(this);
      exp = n1;
      if ( exp != null ) exp.setParent(this);
      integerLiteral = n2;
      if ( integerLiteral != null ) integerLiteral.setParent(this);
      exp1 = n3;
      if ( exp1 != null ) exp1.setParent(this);
   }

   public HStoreStmt(Exp n0, IntegerLiteral n1, Exp n2) {
      nodeToken = new NodeToken("HSTORE");
      if ( nodeToken != null ) nodeToken.setParent(this);
      exp = n0;
      if ( exp != null ) exp.setParent(this);
      integerLiteral = n1;
      if ( integerLiteral != null ) integerLiteral.setParent(this);
      exp1 = n2;
      if ( exp1 != null ) exp1.setParent(this);
   }

   public void accept(visitor.Visitor v) {
      v.visit(this);
   }
   public <R,A> R accept(visitor.GJVisitor<R,A> v, A argu) throws VisitorException {
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

