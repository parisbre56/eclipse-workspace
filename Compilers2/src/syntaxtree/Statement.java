//
// Generated by JTB 1.3.2
//

package syntaxtree;

import visitor.VisitorException;

/**
 * Grammar production:
 * f0 -> Block()
 *       | AssignmentStatement()
 *       | ArrayAssignmentStatement()
 *       | IfStatement()
 *       | WhileStatement()
 *       | PrintStatement()
 */
public class Statement implements Node {
   /**
	 * 
	 */
	private static final long serialVersionUID = -6738509162810006453L;
public NodeChoice f0;

   public Statement(NodeChoice n0) {
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

