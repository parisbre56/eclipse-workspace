//
// Generated by JTB 1.3.2
//

package syntaxtree;

import visitor.VisitorException;

/**
 * Grammar production:
 * f0 -> "new"
 * f1 -> Identifier()
 * f2 -> "("
 * f3 -> ")"
 */
public class AllocationExpression implements Node {
   /**
	 * 
	 */
	private static final long serialVersionUID = -2851257793609650024L;
public NodeToken f0;
   public Identifier f1;
   public NodeToken f2;
   public NodeToken f3;

   public AllocationExpression(NodeToken n0, Identifier n1, NodeToken n2, NodeToken n3) {
      f0 = n0;
      f1 = n1;
      f2 = n2;
      f3 = n3;
   }

   public AllocationExpression(Identifier n0) {
      f0 = new NodeToken("new");
      f1 = n0;
      f2 = new NodeToken("(");
      f3 = new NodeToken(")");
   }

   public void accept(visitor.Visitor v) {
      v.visit(this);
   }
   public <R,A> R accept(visitor.GJVisitor<R,A> v, A argu) throws VisitorException {
      return v.visit(this,argu);
   }
   public <R> R accept(visitor.GJNoArguVisitor<R> v) throws VisitorException {
      return v.visit(this);
   }
   public <A> void accept(visitor.GJVoidVisitor<A> v, A argu) {
      v.visit(this,argu);
   }
}

