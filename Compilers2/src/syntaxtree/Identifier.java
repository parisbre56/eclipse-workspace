//
// Generated by JTB 1.3.2
//

package syntaxtree;

import visitor.VisitorException;

/**
 * Grammar production:
 * f0 -> <IDENTIFIER>
 */
public class Identifier implements Node {
   /**
	 * 
	 */
	private static final long serialVersionUID = -3495871282771096655L;
public NodeToken f0;

   public Identifier(NodeToken n0) {
      f0 = n0;
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

