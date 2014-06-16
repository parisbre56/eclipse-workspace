//
// Generated by JTB 1.3.2
//

package syntaxtree;

import visitor.VisitorException;

/**
 * Grammar production:
 * f0 -> Clause()
 * f1 -> "&&"
 * f2 -> Clause()
 */
public class AndExpression implements Node {
   /**
	 * 
	 */
	private static final long serialVersionUID = 5730350197952484538L;
public Clause f0;
   public NodeToken f1;
   public Clause f2;

   public AndExpression(Clause n0, NodeToken n1, Clause n2) {
      f0 = n0;
      f1 = n1;
      f2 = n2;
   }

   public AndExpression(Clause n0, Clause n1) {
      f0 = n0;
      f1 = new NodeToken("&&");
      f2 = n1;
   }

   public void accept(visitor.Visitor v) throws VisitorException {
      v.visit(this);
   }
   public <R,A> R accept(visitor.GJVisitor<R,A> v, A argu) throws VisitorException {
      return v.visit(this,argu);
   }
   public <R> R accept(visitor.GJNoArguVisitor<R> v) throws VisitorException {
      return v.visit(this);
   }
   public <A> void accept(visitor.GJVoidVisitor<A> v, A argu) throws VisitorException {
      v.visit(this,argu);
   }
}

