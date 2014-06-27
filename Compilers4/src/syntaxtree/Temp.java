//
// Generated by JTB 1.3.2 DIT@UoA patched
//

package syntaxtree;

import visitor.VisitorException;

/**
 * Grammar production:
 * <PRE>
 * nodeToken -> "TEMP"
 * integerLiteral -> IntegerLiteral()
 * </PRE>
 */
public class Temp implements Node {
   /**
	 * 
	 */
	private static final long serialVersionUID = 6586326337347199970L;
private Node parent;
   public NodeToken nodeToken;
   public IntegerLiteral integerLiteral;

   public Temp(NodeToken n0, IntegerLiteral n1) {
      nodeToken = n0;
      if ( nodeToken != null ) nodeToken.setParent(this);
      integerLiteral = n1;
      if ( integerLiteral != null ) integerLiteral.setParent(this);
   }

   public Temp(IntegerLiteral n0) {
      nodeToken = new NodeToken("TEMP");
      if ( nodeToken != null ) nodeToken.setParent(this);
      integerLiteral = n0;
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

