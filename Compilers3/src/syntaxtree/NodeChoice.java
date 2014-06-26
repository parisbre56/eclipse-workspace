//
// Generated by JTB 1.3.2
//

package syntaxtree;

import visitor.VisitorException;

/**
 * Represents a grammar choice, e.g. ( A | B )
 */
public class NodeChoice implements Node {
   /**
	 * 
	 */
	private static final long serialVersionUID = -1331046869102844458L;
public NodeChoice(Node node) {
      this(node, -1);
   }

   public NodeChoice(Node node, int whichChoice) {
      choice = node;
      which = whichChoice;
   }

   public void accept(visitor.Visitor v) throws VisitorException {
      choice.accept(v);
   }
   public <R,A> R accept(visitor.GJVisitor<R,A> v, A argu) throws VisitorException {
      return choice.accept(v,argu);
   }
   public <R> R accept(visitor.GJNoArguVisitor<R> v) throws VisitorException {
      return choice.accept(v);
   }
   public <A> void accept(visitor.GJVoidVisitor<A> v, A argu) throws VisitorException {
      choice.accept(v,argu);
   }

   public Node choice;
   public int which;
}
