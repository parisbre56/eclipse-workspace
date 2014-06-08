//
// Generated by JTB 1.3.2
//

package syntaxtree;

import visitor.VisitorException;

/**
 * Grammar production:
 * f0 -> MainClass()
 * f1 -> ( TypeDeclaration() )*
 * f2 -> <EOF>
 */
public class Goal implements Node {
   /**
	 * 
	 */
	private static final long serialVersionUID = 8010018641165519238L;
public MainClass f0;
   public NodeListOptional f1;
   public NodeToken f2;

   public Goal(MainClass n0, NodeListOptional n1, NodeToken n2) {
      f0 = n0;
      f1 = n1;
      f2 = n2;
   }

   public Goal(MainClass n0, NodeListOptional n1) {
      f0 = n0;
      f1 = n1;
      f2 = new NodeToken("");
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

