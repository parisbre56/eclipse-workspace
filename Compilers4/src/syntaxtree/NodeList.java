//
// Generated by JTB 1.3.2 DIT@UoA patched
//

package syntaxtree;

import java.util.*;

import visitor.VisitorException;

/**
 * Represents a grammar list, e.g. ( A )+
 */
public class NodeList implements NodeListInterface {
   /**
	 * 
	 */
	private static final long serialVersionUID = -4485004132183667312L;
public NodeList() {
      nodes = new Vector<Node>();
   }

   public NodeList(Node firstNode) {
      nodes = new Vector<Node>();
      addNode(firstNode);
   }

   public void addNode(Node n) {
      nodes.addElement(n);
      n.setParent(this);
   }

   public Enumeration<Node> elements() { return nodes.elements(); }
   public Node elementAt(int i)  { return nodes.elementAt(i); }
   public int size()             { return nodes.size(); }
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

   private Node parent;
   public Vector<Node> nodes;
}

