//
// Generated by JTB 1.3.2
//

package syntaxtree;

import visitor.VisitorException;

/**
 * The interface which NodeList, NodeListOptional, and NodeSequence
 * implement.
 */
public interface NodeListInterface extends Node {
   public void addNode(Node n);
   public Node elementAt(int i);
   public java.util.Enumeration<Node> elements();
   public int size();

   public void accept(visitor.Visitor v) throws VisitorException;
   public <R,A> R accept(visitor.GJVisitor<R,A> v, A argu) throws VisitorException;
   public <R> R accept(visitor.GJNoArguVisitor<R> v) throws VisitorException;
   public <A> void accept(visitor.GJVoidVisitor<A> v, A argu) throws VisitorException;
}
