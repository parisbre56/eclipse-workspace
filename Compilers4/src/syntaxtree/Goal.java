//
// Generated by JTB 1.3.2 DIT@UoA patched
//

package syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeToken -> "MAIN"
 * stmtList -> StmtList()
 * nodeToken1 -> "END"
 * nodeListOptional -> ( Procedure() )*
 * nodeToken2 -> &lt;EOF&gt;
 * </PRE>
 */
public class Goal implements Node {
   private Node parent;
   public NodeToken nodeToken;
   public StmtList stmtList;
   public NodeToken nodeToken1;
   public NodeListOptional nodeListOptional;
   public NodeToken nodeToken2;

   public Goal(NodeToken n0, StmtList n1, NodeToken n2, NodeListOptional n3, NodeToken n4) {
      nodeToken = n0;
      if ( nodeToken != null ) nodeToken.setParent(this);
      stmtList = n1;
      if ( stmtList != null ) stmtList.setParent(this);
      nodeToken1 = n2;
      if ( nodeToken1 != null ) nodeToken1.setParent(this);
      nodeListOptional = n3;
      if ( nodeListOptional != null ) nodeListOptional.setParent(this);
      nodeToken2 = n4;
      if ( nodeToken2 != null ) nodeToken2.setParent(this);
   }

   public Goal(StmtList n0, NodeListOptional n1) {
      nodeToken = new NodeToken("MAIN");
      if ( nodeToken != null ) nodeToken.setParent(this);
      stmtList = n0;
      if ( stmtList != null ) stmtList.setParent(this);
      nodeToken1 = new NodeToken("END");
      if ( nodeToken1 != null ) nodeToken1.setParent(this);
      nodeListOptional = n1;
      if ( nodeListOptional != null ) nodeListOptional.setParent(this);
      nodeToken2 = new NodeToken("");
      if ( nodeToken2 != null ) nodeToken2.setParent(this);
   }

   public void accept(visitor.Visitor v) {
      v.visit(this);
   }
   public <R,A> R accept(visitor.GJVisitor<R,A> v, A argu) {
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

