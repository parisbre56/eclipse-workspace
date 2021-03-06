//
// Generated by JTB 1.3.2 DIT@UoA patched
//

package syntaxtree;

import visitor.VisitorException;

/**
 * Grammar production:
 * <PRE>
 * nodeToken -> "HLOAD"
 * temp -> Temp()
 * exp -> Exp()
 * integerLiteral -> IntegerLiteral()
 * </PRE>
 */
public class HLoadStmt implements Node {
   /**
	 * 
	 */
	private static final long serialVersionUID = 5023206900448830970L;
private Node parent;
   public NodeToken nodeToken;
   public Temp temp;
   public Exp exp;
   public IntegerLiteral integerLiteral;

   public HLoadStmt(NodeToken n0, Temp n1, Exp n2, IntegerLiteral n3) {
      nodeToken = n0;
      if ( nodeToken != null ) nodeToken.setParent(this);
      temp = n1;
      if ( temp != null ) temp.setParent(this);
      exp = n2;
      if ( exp != null ) exp.setParent(this);
      integerLiteral = n3;
      if ( integerLiteral != null ) integerLiteral.setParent(this);
   }

   public HLoadStmt(Temp n0, Exp n1, IntegerLiteral n2) {
      nodeToken = new NodeToken("HLOAD");
      if ( nodeToken != null ) nodeToken.setParent(this);
      temp = n0;
      if ( temp != null ) temp.setParent(this);
      exp = n1;
      if ( exp != null ) exp.setParent(this);
      integerLiteral = n2;
      if ( integerLiteral != null ) integerLiteral.setParent(this);
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

