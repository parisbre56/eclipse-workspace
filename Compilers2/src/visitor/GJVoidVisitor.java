//
// Generated by JTB 1.3.2
//

package visitor;
import syntaxtree.*;

/**
 * All GJ void visitors must implement this interface.
 */

public interface GJVoidVisitor<A> {

   //
   // GJ void Auto class visitors
   //

   public void visit(NodeList n, A argu) throws VisitorException;
   public void visit(NodeListOptional n, A argu) throws VisitorException;
   public void visit(NodeOptional n, A argu) throws VisitorException;
   public void visit(NodeSequence n, A argu) throws VisitorException;
   public void visit(NodeToken n, A argu);

   //
   // User-generated visitor methods below
   //

   /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
 * @throws VisitorException 
    */
   public void visit(Goal n, A argu) throws VisitorException;

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> "public"
    * f4 -> "static"
    * f5 -> "void"
    * f6 -> "main"
    * f7 -> "("
    * f8 -> "String"
    * f9 -> "["
    * f10 -> "]"
    * f11 -> Identifier()
    * f12 -> ")"
    * f13 -> "{"
    * f14 -> ( VarDeclaration() )*
    * f15 -> ( Statement() )*
    * f16 -> "}"
    * f17 -> "}"
 * @throws VisitorException 
    */
   public void visit(MainClass n, A argu) throws VisitorException;

   /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
 * @throws VisitorException 
    */
   public void visit(TypeDeclaration n, A argu) throws VisitorException;

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
 * @throws VisitorException 
    */
   public void visit(ClassDeclaration n, A argu) throws VisitorException;

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "extends"
    * f3 -> Identifier()
    * f4 -> "{"
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*
    * f7 -> "}"
 * @throws VisitorException 
    */
   public void visit(ClassExtendsDeclaration n, A argu) throws VisitorException;

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
 * @throws VisitorException 
    */
   public void visit(VarDeclaration n, A argu) throws VisitorException;

   /**
    * f0 -> "public"
    * f1 -> Type()
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( FormalParameterList() )?
    * f5 -> ")"
    * f6 -> "{"
    * f7 -> ( VarDeclaration() )*
    * f8 -> ( Statement() )*
    * f9 -> "return"
    * f10 -> Expression()
    * f11 -> ";"
    * f12 -> "}"
 * @throws VisitorException 
    */
   public void visit(MethodDeclaration n, A argu) throws VisitorException;

   /**
    * f0 -> FormalParameter()
    * f1 -> FormalParameterTail()
 * @throws VisitorException 
    */
   public void visit(FormalParameterList n, A argu) throws VisitorException;

   /**
    * f0 -> Type()
    * f1 -> Identifier()
 * @throws VisitorException 
    */
   public void visit(FormalParameter n, A argu) throws VisitorException;

   /**
    * f0 -> ( FormalParameterTerm() )
 * @throws VisitorException *
    */
   public void visit(FormalParameterTail n, A argu) throws VisitorException;

   /**
    * f0 -> ","
    * f1 -> FormalParameter()
 * @throws VisitorException 
    */
   public void visit(FormalParameterTerm n, A argu) throws VisitorException;

   /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
 * @throws VisitorException 
    */
   public void visit(Type n, A argu) throws VisitorException;

   /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
   public void visit(ArrayType n, A argu);

   /**
    * f0 -> "boolean"
    */
   public void visit(BooleanType n, A argu);

   /**
    * f0 -> "int"
    */
   public void visit(IntegerType n, A argu);

   /**
    * f0 -> Block()
    *       | AssignmentStatement()
    *       | ArrayAssignmentStatement()
    *       | IfStatement()
    *       | WhileStatement()
    *       | PrintStatement()
 * @throws VisitorException 
    */
   public void visit(Statement n, A argu) throws VisitorException;

   /**
    * f0 -> "{"
    * f1 -> ( Statement() )*
    * f2 -> "}"
 * @throws VisitorException 
    */
   public void visit(Block n, A argu) throws VisitorException;

   /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
 * @throws VisitorException 
    */
   public void visit(AssignmentStatement n, A argu) throws VisitorException;

   /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
 * @throws VisitorException 
    */
   public void visit(ArrayAssignmentStatement n, A argu) throws VisitorException;

   /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
 * @throws VisitorException 
    */
   public void visit(IfStatement n, A argu) throws VisitorException;

   /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
 * @throws VisitorException 
    */
   public void visit(WhileStatement n, A argu) throws VisitorException;

   /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
 * @throws VisitorException 
    */
   public void visit(PrintStatement n, A argu) throws VisitorException;

   /**
    * f0 -> AndExpression()
    *       | CompareExpression()
    *       | PlusExpression()
    *       | MinusExpression()
    *       | TimesExpression()
    *       | ArrayLookup()
    *       | ArrayLength()
    *       | MessageSend()
    *       | Clause()
 * @throws VisitorException 
    */
   public void visit(Expression n, A argu) throws VisitorException;

   /**
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
 * @throws VisitorException 
    */
   public void visit(AndExpression n, A argu) throws VisitorException;

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
 * @throws VisitorException 
    */
   public void visit(CompareExpression n, A argu) throws VisitorException;

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
 * @throws VisitorException 
    */
   public void visit(PlusExpression n, A argu) throws VisitorException;

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
 * @throws VisitorException 
    */
   public void visit(MinusExpression n, A argu) throws VisitorException;

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
 * @throws VisitorException 
    */
   public void visit(TimesExpression n, A argu) throws VisitorException;

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
 * @throws VisitorException 
    */
   public void visit(ArrayLookup n, A argu) throws VisitorException;

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
 * @throws VisitorException 
    */
   public void visit(ArrayLength n, A argu) throws VisitorException;

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
 * @throws VisitorException 
    */
   public void visit(MessageSend n, A argu) throws VisitorException;

   /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
 * @throws VisitorException 
    */
   public void visit(ExpressionList n, A argu) throws VisitorException;

   /**
    * f0 -> ( ExpressionTerm() )
 * @throws VisitorException *
    */
   public void visit(ExpressionTail n, A argu) throws VisitorException;

   /**
    * f0 -> ","
    * f1 -> Expression()
 * @throws VisitorException 
    */
   public void visit(ExpressionTerm n, A argu) throws VisitorException;

   /**
    * f0 -> NotExpression()
    *       | PrimaryExpression()
 * @throws VisitorException 
    */
   public void visit(Clause n, A argu) throws VisitorException;

   /**
    * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | BracketExpression()
 * @throws VisitorException 
    */
   public void visit(PrimaryExpression n, A argu) throws VisitorException;

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public void visit(IntegerLiteral n, A argu);

   /**
    * f0 -> "true"
    */
   public void visit(TrueLiteral n, A argu);

   /**
    * f0 -> "false"
    */
   public void visit(FalseLiteral n, A argu);

   /**
    * f0 -> <IDENTIFIER>
    */
   public void visit(Identifier n, A argu);

   /**
    * f0 -> "this"
    */
   public void visit(ThisExpression n, A argu);

   /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
 * @throws VisitorException 
    */
   public void visit(ArrayAllocationExpression n, A argu) throws VisitorException;

   /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
   public void visit(AllocationExpression n, A argu);

   /**
    * f0 -> "!"
    * f1 -> Clause()
 * @throws VisitorException 
    */
   public void visit(NotExpression n, A argu) throws VisitorException;

   /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
 * @throws VisitorException 
    */
   public void visit(BracketExpression n, A argu) throws VisitorException;

}

