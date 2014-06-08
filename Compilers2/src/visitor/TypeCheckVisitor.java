/**
 * 
 */
package visitor;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import syntaxtree.AllocationExpression;
import syntaxtree.AndExpression;
import syntaxtree.ArrayAllocationExpression;
import syntaxtree.ArrayAssignmentStatement;
import syntaxtree.ArrayLength;
import syntaxtree.ArrayLookup;
import syntaxtree.ArrayType;
import syntaxtree.AssignmentStatement;
import syntaxtree.Block;
import syntaxtree.BooleanType;
import syntaxtree.BracketExpression;
import syntaxtree.ClassDeclaration;
import syntaxtree.ClassExtendsDeclaration;
import syntaxtree.Clause;
import syntaxtree.CompareExpression;
import syntaxtree.Expression;
import syntaxtree.ExpressionList;
import syntaxtree.ExpressionTail;
import syntaxtree.ExpressionTerm;
import syntaxtree.FalseLiteral;
import syntaxtree.FormalParameter;
import syntaxtree.FormalParameterList;
import syntaxtree.FormalParameterTail;
import syntaxtree.FormalParameterTerm;
import syntaxtree.Goal;
import syntaxtree.Identifier;
import syntaxtree.IfStatement;
import syntaxtree.IntegerLiteral;
import syntaxtree.IntegerType;
import syntaxtree.MainClass;
import syntaxtree.MessageSend;
import syntaxtree.MethodDeclaration;
import syntaxtree.MinusExpression;
import syntaxtree.Node;
import syntaxtree.NodeList;
import syntaxtree.NodeListOptional;
import syntaxtree.NodeOptional;
import syntaxtree.NodeSequence;
import syntaxtree.NodeToken;
import syntaxtree.NotExpression;
import syntaxtree.PlusExpression;
import syntaxtree.PrimaryExpression;
import syntaxtree.PrintStatement;
import syntaxtree.Statement;
import syntaxtree.ThisExpression;
import syntaxtree.TimesExpression;
import syntaxtree.TrueLiteral;
import syntaxtree.Type;
import syntaxtree.TypeDeclaration;
import syntaxtree.VarDeclaration;
import syntaxtree.WhileStatement;
import visitor.Context;
import visitor.GJVisitor;
import visitor.MiniJavaClass;
import visitor.MiniJavaMethod;
import visitor.TypeDeclVisitor;
import visitor.TypeMismatchException;
import visitor.UndeclaredClassException;
import visitor.VisitorException;

/**
 * 
 */

/**
 * @author Parisbre56
 * Takes a string describing the expected type. Returns the type it found.
 * Checks that variables and classes are used properly.
 */
public class TypeCheckVisitor implements GJVisitor<String, String> {
	LinkedHashMap<String,MiniJavaClass> Classes;
	MiniJavaBody currentBodyContext = null;
	MiniJavaClass currentClassContext = null;
	Set<Entry<String, String>> Vars = null;
	Iterator<Entry<String, String>> IterVar = null;
	boolean requestType = false;

	/**
	 * @param vd 
	 * Takes the class and variable declaration data gathered by the first visitor
	 * and constructs the new one.
	 * @throws DuplicateClassException 
	 */
	public TypeCheckVisitor(TypeDeclVisitor vd) throws DuplicateClassException {
		Classes = vd.getClassesMap();
		
		//Add types to classes
		MiniJavaClass intClass = new MiniJavaClass(null,"int");
		if(Classes.containsKey("int")) {
			throw new DuplicateClassException("int cannot be used as a class name");
		}
		Classes.put("int", intClass);
		
		MiniJavaClass intArrayClass = new MiniJavaClass(null,"int[]");
		intArrayClass.Vars.put("length", "int");
		if(Classes.containsKey("int[]")) {
			throw new DuplicateClassException("int[] cannot be used as a class name");
		}
		Classes.put("int[]", intArrayClass);
		
		MiniJavaClass boolClass = new MiniJavaClass(null,"boolean");
		if(Classes.containsKey("boolean")) {
			throw new DuplicateClassException("boolean cannot be used as a class name");
		}
		Classes.put("boolean", boolClass);
	}
	
	//
	// GJ Auto class visitors
	//
	@Override
	//TODO check if it extends?
	public String visit(NodeList n, String argu) throws VisitorException {
		String retString1=argu;
		for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
			String retString2=e.nextElement().accept(this,retString1);
			if(retString1==null) {
				retString1=retString2;
			}
			else if(retString2!=null && !retString1.equals(retString2)) {
				TypeMismatchException ex = new TypeMismatchException();
				throw ex;
			}
		}
		return retString1;
	}
	@Override
	public String visit(NodeListOptional n, String argu) throws VisitorException {
		if ( n.present() ) {
			String retString1=argu;
			for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
				String retString2=e.nextElement().accept(this,retString1);
				if(retString1==null) {
					retString1=retString2;
				}
				else if(retString2!=null && !retString1.equals(retString2)) {
					TypeMismatchException ex = new TypeMismatchException();
					throw ex;
				}
			}
			return retString1;
		}
		return null;
	}
	@Override
	public String visit(NodeOptional n, String argu) throws VisitorException {
		if ( n.present() ) {
			return n.node.accept(this,argu);
		}
		return null;
	}
	@Override
	public String visit(NodeSequence n, String argu) throws VisitorException {
		String retString1=argu;
		for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
			String retString2=e.nextElement().accept(this,retString1);
			if(retString1==null) {
				retString1=retString2;
			}
			else if(retString2!=null && !retString1.equals(retString2)) {
				TypeMismatchException ex = new TypeMismatchException();
				throw ex;
			}
		}
		return retString1;
	}
	@Override
	public String visit(NodeToken n, String argu) {
		return n.tokenImage;
	}

	//
	// User-generated visitor methods below
	//

	/**
	 * f0 -> MainClass()
	 * f1 -> ( TypeDeclaration() )*
	 * f2 -> <EOF>
	 * @throws VisitorException 
	 */
	@Override
	public String visit(Goal n, String argu) throws VisitorException {
		n.f0.accept(this,null);
		n.f1.accept(this,null);
		return null;
	}

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
	@Override
	public String visit(MainClass n, String argu) throws VisitorException {
		//Everything else has been checked. 
		//Change context to main body and start checking statements.
		MiniJavaClass mainClassContext = Classes.get(n.f1.f0.tokenImage);
		if(mainClassContext==null) {
			throw new UndeclaredClassException("Main class not found in class map. Check if you're running TypeDeclVisitor first before calling TypeCheckVisitor.");
		}
		MiniJavaMethod mainMethodContext = mainClassContext.Methods.get("main");
		if(mainMethodContext==null) {
			throw new UndeclaredMethodException("Main method not found in main class. Check if you're running TypeDeclVisitor first before calling TypeCheckVisitor.");
		}
		MiniJavaBody mainBodyContext = mainMethodContext.body;
		
		//Check all variable declarations for valid types
		n.f14.accept(this,null);
		
		//Check all statements for valid types
		currentBodyContext = mainBodyContext;
		n.f15.accept(this,null);
		
		//Reset context and exit
		currentBodyContext = null;
		return null;
	}

	/**
	 * f0 -> ClassDeclaration()
	 *       | ClassExtendsDeclaration()
	 * @throws VisitorException 
	 */
	@Override
	public String visit(TypeDeclaration n, String argu) throws VisitorException {
		n.f0.accept(this,null);
		return null;
	}

	/**
	 * f0 -> "class"
	 * f1 -> Identifier()
	 * f2 -> "{"
	 * f3 -> ( VarDeclaration() )*
	 * f4 -> ( MethodDeclaration() )*
	 * f5 -> "}"
	 * @throws VisitorException 
	 */
	@Override
	public String visit(ClassDeclaration n, String argu) throws VisitorException {
		MiniJavaClass thisClass = Classes.get(n.f1.f0.tokenImage);
		//Check all variable declarations for valid types
		n.f3.accept(this,null);
		
		//Check all method declarations for valid types
		currentClassContext = thisClass;
		n.f4.accept(this,null);
		
		currentClassContext  = null;
		return null;
	}

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
	@Override
	public String visit(ClassExtendsDeclaration n, String argu) throws VisitorException {
		MiniJavaClass thisClass = Classes.get(n.f1.f0.tokenImage);
		
		//Check all variable declarations for valid types
		n.f5.accept(this,null);
		
		//Check all method declarations for valid types
		currentClassContext = thisClass;
		n.f6.accept(this,null);
		
		currentClassContext  = null;
		return null;
	}

	/**
	 * f0 -> Type()
	 * f1 -> Identifier()
	 * f2 -> ";"
	 * @throws VisitorException 
	 */
	@Override
	public String visit(VarDeclaration n, String argu) throws VisitorException {
		//Check for valid type
		String varType = n.f0.accept(this,null);
		if(!Classes.containsKey(varType)) {
			throw new UndeclaredClassException("Line:"+n.f2.beginLine+":"+n.f2.beginColumn+": Class "+varType+" not declared");
		}
		return null;
	}

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
	@Override
	public String visit(MethodDeclaration n, String argu) throws VisitorException {
		//Check for valid return type
		String varType = n.f1.accept(this,null);
		if(!Classes.containsKey(varType)) {
			throw new UndeclaredClassException("Line:"+n.f0.beginLine+":"+n.f0.beginColumn+": Class "+varType+" not declared");
		}
		
		//Check for valid parameter type
		n.f4.accept(this,null);
		
		//Check for valid variable declaration types
		n.f7.accept(this,null);
		
		//Check statements for valid types
		MiniJavaMethod tempMethod = currentClassContext.Methods.get(n.f2.f0.tokenImage);
		currentBodyContext = tempMethod.body;
		n.f8.accept(this,null);
		
		//Check return statement
		n.f10.accept(this,varType);
		
		currentBodyContext = null;
		return null;
	}

	/**
	 * f0 -> FormalParameter()
	 * f1 -> FormalParameterTail()
	 * @throws VisitorException 
	 */
	@Override
	public String visit(FormalParameterList n, String argu) throws VisitorException {
		n.f0.accept(this,null);
		n.f1.accept(this,null);
		return null;
	}

	/**
	 * f0 -> Type()
	 * f1 -> Identifier()
	 * @throws VisitorException 
	 */
	@Override
	public String visit(FormalParameter n, String argu) throws VisitorException {
		//Check for valid type
		String varType = n.f0.accept(this,null);
		if(!Classes.containsKey(varType)) {
			throw new UndeclaredClassException("Line:"+n.f1.f0.beginLine+":"+n.f1.f0.beginColumn+": Class "+varType+" not declared");
		}
		return null;
	}

	/**
	 * f0 -> ( FormalParameterTerm() ) *
	 * @throws VisitorException 
	 */
	@Override
	public String visit(FormalParameterTail n, String argu) throws VisitorException {
		n.f0.accept(this,null);
		return null;
	}

	/**
	 * f0 -> ","
	 * f1 -> FormalParameter()
	 * @throws VisitorException 
	 */
	@Override
	public String visit(FormalParameterTerm n, String argu) throws VisitorException {
		n.f1.accept(this,null);
		return null;
	}

	/**
	 * f0 -> ArrayType()
	 *       | BooleanType()
	 *       | IntegerType()
	 *       | Identifier()
	 * @throws VisitorException 
	 */
	@Override
	public String visit(Type n, String argu) throws VisitorException {
		return n.f0.accept(this,null);
	}

	/**
	 * f0 -> "int"
	 * f1 -> "["
	 * f2 -> "]"
	 */
	@Override
	public String visit(ArrayType n, String argu) {
		return "int[]";
	}

	/**
	 * f0 -> "boolean"
	 */
	@Override
	public String visit(BooleanType n, String argu) {
		return "boolean";
	}

	/**
	 * f0 -> "int"
	 */
	@Override
	public String visit(IntegerType n, String argu) {
		return "int";
	}

	/**
	 * f0 -> Block()
	 *       | AssignmentStatement()
	 *       | ArrayAssignmentStatement()
	 *       | IfStatement()
	 *       | WhileStatement()
	 *       | PrintStatement()
	 * @throws VisitorException 
	 */
	@Override
	public String visit(Statement n, String argu) throws VisitorException {
		n.f0.accept(this,null);
		return null;
	}

	/**
	 * f0 -> "{"
	 * f1 -> ( Statement() )*
	 * f2 -> "}"
	 * @throws VisitorException 
	 */
	@Override
	public String visit(Block n, String argu) throws VisitorException {
		n.f1.accept(this,null);
		return null;
	}

	/**
	 * f0 -> Identifier()
	 * f1 -> "="
	 * f2 -> Expression()
	 * f3 -> ";"
	 * @throws VisitorException 
	 */
	@Override
	public String visit(AssignmentStatement n, String argu) throws VisitorException {
		String varName=n.f0.f0.tokenImage;
		//Search for the variable
		Context tempContext=currentBodyContext;
		while(tempContext!=null) {
			if(tempContext.Vars.containsKey(varName)) {
				break;
			}
			tempContext=tempContext.cparent;
		}
		if(tempContext==null) {
			UndeclaredVarException e = new UndeclaredVarException("Line:"+n.f1.beginLine+":"+n.f1.beginColumn+": Undeclared variable "+varName);
			throw e;
		}
		//If it exists, get its type
		String varType=tempContext.Vars.get(varName);
		//Check if type is valid
		if(!Classes.containsKey(varType)) {
			throw new UndeclaredClassException("Line:"+n.f1.beginLine+":"+n.f1.beginColumn+": Class "+varType+" not declared");
		}
		//Search the expression, making sure that the returned result is of the above type or extends the above type
		n.f2.accept(this,varType);
		return null;
	}

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
	@Override
	public String visit(ArrayAssignmentStatement n, String argu) throws VisitorException {
		String varName=n.f0.f0.tokenImage;
		//Search for the variable
		Context tempContext=currentBodyContext;
		while(tempContext!=null) {
			if(tempContext.Vars.containsKey(varName)) {
				break;
			}
			tempContext=tempContext.cparent;
		}
		if(tempContext==null) {
			UndeclaredVarException e = new UndeclaredVarException("Line:"+n.f1.beginLine+":"+n.f1.beginColumn+": Undeclared variable "+varName);
			throw e;
		}
		//If it exists, get its type
		String varType=tempContext.Vars.get(varName);
		//Check if type is int[]
		if(!varType.equals("int[]")) {
			throw new TypeMismatchException("Line:"+n.f1.beginLine+":"+n.f1.beginColumn+": "+varName+" is a variable of type "+varType+"\nIt cannot be used as an int[]");
		}
		//Check [expression], making sure it's int
		n.f2.accept(this,"int");
		//Check =expression, making sure it's int
		n.f5.accept(this,"int");		
		
		return null;
	}

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
	@Override
	public String visit(IfStatement n, String argu) throws VisitorException {
		//(expression()) needs to be boolean
		n.f2.accept(this,"boolean");
		n.f4.accept(this,null);
		n.f6.accept(this,null);
		return null;
	}

	/**
	 * f0 -> "while"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> Statement()
	 * @throws VisitorException 
	 */
	@Override
	public String visit(WhileStatement n, String argu) throws VisitorException {
		//(expression()) needs to be boolean
		n.f2.accept(this,"boolean");
		n.f4.accept(this,null);
		return null;
	}

	/**
	 * f0 -> "System.out.println"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> ";"
	 * @throws VisitorException 
	 */
	@Override
	public String visit(PrintStatement n, String argu) throws VisitorException {
		//Expression needs to be integer (not much else it can be)
		try{
			n.f2.accept(this,"boolean");
		}
		catch (TypeMismatchException ex) {
			n.f2.accept(this,"int");
		}
		return null;
	}

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
	@Override
	public String visit(Expression n, String argu) throws VisitorException {
		return n.f0.accept(this,argu);
	}

	/**
	 * f0 -> Clause()
	 * f1 -> "&&"
	 * f2 -> Clause()
	 * @throws VisitorException 
	 */
	@Override
	public String visit(AndExpression n, String argu) throws VisitorException {
		if(argu!=null&&!argu.equals("boolean")) {
			throw new TypeMismatchException("Line:"+n.f1.beginLine+":"+n.f1.beginColumn+": Expected "+argu+" but is boolean expression");
		}
		n.f0.accept(this,"boolean");
		n.f2.accept(this,"boolean");
		return "boolean";
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "<"
	 * f2 -> PrimaryExpression()
	 * @throws VisitorException 
	 */
	@Override
	public String visit(CompareExpression n, String argu) throws VisitorException {
		if(argu!=null&&!argu.equals("boolean")) {
			throw new TypeMismatchException("Line:"+n.f1.beginLine+":"+n.f1.beginColumn+": Expected "+argu+" but is boolean expression");
		}
		n.f0.accept(this,"int");
		n.f2.accept(this,"int");
		return "boolean";
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "+"
	 * f2 -> PrimaryExpression()
	 * @throws VisitorException 
	 */
	@Override
	public String visit(PlusExpression n, String argu) throws VisitorException {
		if(argu!=null&&!argu.equals("int")) {
			throw new TypeMismatchException("Line:"+n.f1.beginLine+":"+n.f1.beginColumn+": Expected "+argu+" but is int expression");
		}
		n.f0.accept(this,"int");
		n.f2.accept(this,"int");
		return "int";
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "-"
	 * f2 -> PrimaryExpression()
	 * @throws VisitorException 
	 */
	@Override
	public String visit(MinusExpression n, String argu) throws VisitorException {
		if(argu!=null&&!argu.equals("int")) {
			throw new TypeMismatchException("Line:"+n.f1.beginLine+":"+n.f1.beginColumn+": Expected "+argu+" but is int expression");
		}
		n.f0.accept(this,"int");
		n.f2.accept(this,"int");
		return "int";
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "*"
	 * f2 -> PrimaryExpression()
	 * @throws VisitorException 
	 */
	@Override
	public String visit(TimesExpression n, String argu) throws VisitorException {
		if(argu!=null&&!argu.equals("int")) {
			throw new TypeMismatchException("Line:"+n.f1.beginLine+":"+n.f1.beginColumn+": Expected "+argu+" but is int expression");
		}
		n.f0.accept(this,"int");
		n.f2.accept(this,"int");
		return "int";
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "["
	 * f2 -> PrimaryExpression()
	 * f3 -> "]"
	 * @throws VisitorException 
	 */
	@Override
	public String visit(ArrayLookup n, String argu) throws VisitorException {
		if(argu!=null&&!argu.equals("int")) {
			throw new TypeMismatchException("Line:"+n.f1.beginLine+":"+n.f1.beginColumn+": Expected "+argu+" but is int expression");
		}
		n.f0.accept(this,"int[]");
		n.f2.accept(this,"int");
		return "int";
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "."
	 * f2 -> "length"
	 * @throws VisitorException 
	 */
	@Override
	public String visit(ArrayLength n, String argu) throws VisitorException {
		if(argu!=null&&!argu.equals("int")) {
			throw new TypeMismatchException("Line:"+n.f1.beginLine+":"+n.f1.beginColumn+": Expected "+argu+" but is int expression");
		}
		n.f0.accept(this,"int[]");
		return "int";
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "."
	 * f2 -> Identifier()
	 * f3 -> "("
	 * f4 -> ( ExpressionList() )?
	 * f5 -> ")"
	 * @throws VisitorException 
	 */
	@Override
	public String visit(MessageSend n, String argu) throws VisitorException {
		//Get expression class name
		boolean prevRequest = requestType;
		requestType=true;
		String varType=n.f0.accept(this,null);
		requestType=prevRequest;
		//Check expression class exists
		if(!Classes.containsKey(varType)) {
			throw new UndeclaredClassException("Line:"+n.f1.beginLine+":"+n.f1.beginColumn+": Class "+varType+" not declared");
		}
		//Get expression class
		MiniJavaClass varClass=Classes.get(varType);
		//Get method name
		String methName=n.f2.f0.tokenImage;
		//Search for method
		while(varClass!=null) {
			if(varClass.Methods.containsKey(methName)) {
				break;
			}
			varClass=varClass.extended;
		}
		if(varClass==null) {
			throw new UndeclaredMethodException("Line:"+n.f1.beginLine+":"+n.f1.beginColumn+": "+varType+" has no method named "+methName);
		}
		//Get method
		MiniJavaMethod methClass = varClass.Methods.get(methName);
		//Get return type
		String retType = methClass.returnType;
		//Check for return type 
		if(argu!=null) {
			MiniJavaClass retClass = Classes.get(retType);
			while(retClass!=null) {
				if(retClass.name.equals(argu)) {
					break;
				}
				retClass=retClass.extended;
			}
			if(retClass==null) {
				throw new TypeMismatchException("Line:"+n.f1.beginLine+":"+n.f1.beginColumn+": Expected "+argu+" but is "+retType+" expression");
			}
		}
		//Also check if arguments match
		Set<Entry<String, String>> prevVars = Vars;
		Iterator<Entry<String, String>> prevIterVar = IterVar;
		Vars=(new LinkedHashMap<String,String>(methClass.Vars)).entrySet();
		IterVar=Vars.iterator();
		try {
			n.f4.accept(this,"arguements");
		}
		catch (WrongArgNumException exp) {
			throw new WrongArgNumException("Line:"+n.f1.beginLine+":"+n.f1.beginColumn+":There were more arguements provided than needed in method "+methName,exp);
		}
		//Check if there are less vars
		if(IterVar.hasNext()) {
			throw new WrongArgNumException("Line:"+n.f1.beginLine+":"+n.f1.beginColumn+":There were less arguements provided than needed in method "+methName);
		}
		
		Vars=prevVars;
		IterVar=prevIterVar;
		return retType;
	}

	/**
	 * f0 -> Expression()
	 * f1 -> ExpressionTail()
	 * @throws VisitorException 
	 */
	@Override
	public String visit(ExpressionList n, String argu) throws VisitorException {
		if(!IterVar.hasNext()) {
			throw new WrongArgNumException();
		}
		String varType = IterVar.next().getValue();
		n.f0.accept(this,varType);
		n.f1.accept(this,argu);
		return argu;
	}

	/**
	 * f0 -> ( ExpressionTerm() )
	 * @throws VisitorException *
	 */
	@Override
	public String visit(ExpressionTail n, String argu) throws VisitorException {
		return n.f0.accept(this,argu);
	}

	/**
	 * f0 -> ","
	 * f1 -> Expression()
	 * @throws VisitorException 
	 */
	@Override
	public String visit(ExpressionTerm n, String argu) throws VisitorException {
		if(!IterVar.hasNext()) {
			throw new WrongArgNumException();
		}
		String varType = IterVar.next().getValue();
		n.f0.accept(this,varType);
		return argu;
	}

	/**
	 * f0 -> NotExpression()
	 *       | PrimaryExpression()
	 * @throws VisitorException 
	 */
	@Override
	public String visit(Clause n, String argu) throws VisitorException {
		return n.f0.accept(this,argu);
	}

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
	@Override
	public String visit(PrimaryExpression n, String argu) throws VisitorException {
		return n.f0.accept(this,argu);
	}

	/**
	 * f0 -> <INTEGER_LITERAL>
	 * @throws VisitorException 
	 */
	@Override
	public String visit(IntegerLiteral n, String argu) throws VisitorException {
		if(argu!=null&&!argu.equals("int")) {
			throw new TypeMismatchException("Line:"+n.f0.beginLine+":"+n.f0.beginColumn+": Expected "+argu+" but is int expression");
		}
		return "int";
	}

	/**
	 * f0 -> "true"
	 * @throws VisitorException 
	 */
	@Override
	public String visit(TrueLiteral n, String argu) throws VisitorException {
		if(argu!=null&&!argu.equals("boolean")) {
			throw new TypeMismatchException("Line:"+n.f0.beginLine+":"+n.f0.beginColumn+": Expected "+argu+" but is boolean expression");
		}
		return "boolean";
	}

	/**
	 * f0 -> "false"
	 * @throws VisitorException 
	 */
	@Override
	public String visit(FalseLiteral n, String argu) throws VisitorException {
		if(argu!=null&&!argu.equals("boolean")) {
			throw new TypeMismatchException("Line:"+n.f0.beginLine+":"+n.f0.beginColumn+": Expected "+argu+" but is boolean expression");
		}
		return "boolean";
	}

	/**
	 * f0 -> <IDENTIFIER>
	 * @throws VisitorException 
	 */
	@Override
	public String visit(Identifier n, String argu) throws VisitorException {
		String varName = n.f0.tokenImage;
		
		//If this is not inside an expression
		if(currentBodyContext==null) {
			return varName;
		} 
		//TODO Make this return type?
		if(argu==null&& !requestType) {
			return varName;
		}
		
		//Else
		//Check context for var
		Context tempContext=currentBodyContext;
		while(tempContext!=null) {
			if(tempContext.Vars.containsKey(varName)) {
				break;
			}
			tempContext=tempContext.cparent;
		}
		if(tempContext==null) {
			throw new UndeclaredVarException("Line:"+n.f0.beginLine+":"+n.f0.beginColumn+": Variable "+varName+" has not been previously declared");
		}
		//Get var type
		String varType = tempContext.Vars.get(varName);
		//Check var type exists (should never happen, but just in case)
		if(!Classes.containsKey(varType)) {
			throw new UndeclaredClassException("Line:"+n.f0.beginLine+":"+n.f0.beginColumn+": Class "+varType+" has not been declared");
		}
		//See if varType class is of type argu or extends argu 
		if(argu!=null) {
			MiniJavaClass tempClass = Classes.get(varType);
			while(tempClass!=null) {
				if(tempClass.name.equals(argu)) {
					break;
				}
				tempClass=tempClass.extended;
			}
			if(tempClass==null) {
				throw new TypeMismatchException("Line:"+n.f0.beginLine+":"+n.f0.beginColumn+": Expected "+argu+" but "+varName+" is "+varType+" varible");
			}
		}
		
		return varType;
	}

	/**
	 * f0 -> "this"
	 * @throws VisitorException 
	 */
	@Override
	public String visit(ThisExpression n, String argu) throws VisitorException {
		MiniJavaClass tempClass = currentClassContext;

		//See if the current class is of type argu or extends argu
		if(argu!=null) {
			while(tempClass!=null) {
				if(tempClass.name.equals(argu)) {
					break;
				}
				tempClass=tempClass.extended;
			}
			if(tempClass==null) {
				throw new TypeMismatchException("Line:"+n.f0.beginLine+":"+n.f0.beginColumn+": Expected "+argu+" but is "+currentClassContext.name+" class");
			}
		}
		
		return tempClass.name;
	}

	/**
	 * f0 -> "new"
	 * f1 -> "int"
	 * f2 -> "["
	 * f3 -> Expression()
	 * f4 -> "]"
	 * @throws VisitorException 
	 */
	@Override
	public String visit(ArrayAllocationExpression n, String argu) throws VisitorException {
		if(argu!=null&&!argu.equals("int[]")) {
			throw new TypeMismatchException("Line:"+n.f0.beginLine+":"+n.f0.beginColumn+": Expected "+argu+" but is int[] allocator");
		}
		//Expression must be int
		n.f3.accept(this,"int");
		
		return "int[]";
	}

	/**
	 * f0 -> "new"
	 * f1 -> Identifier()
	 * f2 -> "("
	 * f3 -> ")"
	 * @throws VisitorException 
	 */
	@Override
	public String visit(AllocationExpression n, String argu) throws VisitorException {
		String varType=n.f1.f0.tokenImage;
		if(!Classes.containsKey(varType)) {
			throw new UndeclaredClassException("Line:"+n.f0.beginLine+":"+n.f0.beginColumn+": Class "+varType+" has not been declared");
		}
		
		//See if the created class is of type argu or extends argu
		if(argu!=null) {
			MiniJavaClass tempClass = Classes.get(varType);
			while(tempClass!=null) {
				if(tempClass.name.equals(argu)) {
					break;
				}
				tempClass=tempClass.extended;
			}
			if(tempClass==null) {
				throw new TypeMismatchException("Line:"+n.f0.beginLine+":"+n.f0.beginColumn+": Expected "+argu+" but is "+varType+" class");
			}
		}
		
		return varType;
	}

	/**
	 * f0 -> "!"
	 * f1 -> Clause()
	 * @throws VisitorException 
	 */
	@Override
	public String visit(NotExpression n, String argu) throws VisitorException {
		if(argu!=null&&!argu.equals("boolean")) {
			throw new TypeMismatchException("Line:"+n.f0.beginLine+":"+n.f0.beginColumn+": Expected "+argu+" but is boolean expression");
		}
		n.f1.accept(this,"boolean");
		return "boolean";
	}

	/**
	 * f0 -> "("
	 * f1 -> Expression()
	 * f2 -> ")"
	 * @throws VisitorException 
	 */
	@Override
	public String visit(BracketExpression n, String argu) throws VisitorException {
		return n.f1.accept(this,argu);
	}

}
