//
// Generated by JTB 1.3.2
//

package syntaxtree;

import visitor.VisitorException;

/**
 * Grammar production:
 * f0 -> NotExpression()
 *       | PrimaryExpression()
 */
public class Clause implements Node {
   /**
	 * 
	 */
	private static final long serialVersionUID = 4166878296686173784L;
public NodeChoice f0;

   public Clause(NodeChoice n0) {
      f0 = n0;
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

