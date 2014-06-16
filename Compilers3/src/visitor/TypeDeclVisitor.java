/**
 * 
 */
package visitor;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

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

/**
 * @author Parisbre56
 *
 */

 
public class TypeDeclVisitor implements GJNoArguVisitor<String> {
	LinkedHashMap<String,MiniJavaClass> Classes = new LinkedHashMap<String,MiniJavaClass>();
	Context currentContext = null;
	
	@Override
	public String visit(NodeList n) throws VisitorException {
		String retString = new String();
		for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
			retString+=e.nextElement().accept(this)+"|";
		}
		return retString;
	}

	@Override
	public String visit(NodeListOptional n) throws VisitorException {
		if ( n.present() ) {
			String retString = new String();
			for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
				retString+=e.nextElement().accept(this)+"|";
			}
			return retString;
		}
		return null;
	}

	@Override
	public String visit(NodeOptional n) throws VisitorException {
		if ( n.present() ) {
			return n.node.accept(this);
		}
		return null;
	}
	
	@Override
	public String visit(NodeSequence n) throws VisitorException {
		String retString = new  String();
		for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
			retString+=e.nextElement().accept(this)+"|";
		}
		return retString;
	}
	
	@Override
	public String visit(NodeToken n) { 
		return n.tokenImage;
	}

	/**
	 * f0 -> MainClass()
	 * f1 -> ( TypeDeclaration() )*
	 * f2 -> EOF
	 * @throws VisitorException 
	 */
	@Override
	public String visit(Goal n) throws VisitorException {
		return n.f0.accept(this) + n.f1.accept(this);
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
	 * @return 
	 * @throws DuplicateClassException 
	 * @throws DuplicateMethodException 
	 * @throws VisitorException
	 */
	@Override
	public String visit(MainClass n) throws VisitorException {
		String retString = "main class|main method";
		//Create main class
		String className = n.f1.f0.tokenImage;
		if(Classes.containsKey(className)) {
			DuplicateClassException e = new DuplicateClassException("Line:"+n.f0.beginLine+":"+n.f0.beginColumn+": Duplicate class declaration: "+className);
			throw e;
		}
		MiniJavaClass mainClass = new MiniJavaClass(null,className); //Null means it doesn't extend anything
		Classes.put(className, mainClass);
		
		//Create main method
		String mainName = n.f6.tokenImage;
		MiniJavaMethod mainMethod = new MiniJavaMethod("void",mainClass,mainName);
		if(mainClass.Methods.containsKey(mainName)) {
			DuplicateMethodException e = new DuplicateMethodException("Line:"+n.f0.beginLine+":"+n.f0.beginColumn+": Duplicate method declaration: "+mainName+"\nIn class: "+className);
			throw e;
		}
		mainClass.Methods.put(mainName, mainMethod);
		MiniJavaBody mainBody = new MiniJavaBody(mainMethod);
		
		//Put args
		String argName = n.f11.f0.tokenImage;
		String argType = n.f8.tokenImage+n.f9.tokenImage+n.f10.tokenImage;
		if(mainMethod.Vars.containsKey(argName)) {
			DuplicateVarException e = new DuplicateVarException("Line:"+n.f0.beginLine+":"+n.f0.beginColumn+": Duplicate argument declaration: "+argName+"\nIn method: "+mainName+"\nIn class: "+className);
			throw e;
		}
		mainMethod.Vars.put(argName, argType);
		
		//Current context is main method body 
		currentContext=mainBody;
		
		//Process vars
		retString+=n.f14.accept(this)+"|";
		
		//Process statements
		retString+=n.f15.accept(this)+"|";
		
		//Current context is nothing
		currentContext=null;
		return retString;
	}

	/**
	 * f0 -> ClassDeclaration()
	 *       | ClassExtendsDeclaration()
	 * @return 
	 */
	@Override
	public String visit(TypeDeclaration n) throws VisitorException {
		return n.f0.accept(this);
	}

	/**
	 * f0 -> "class"
	 * f1 -> Identifier()
	 * f2 -> "{"
	 * f3 -> ( VarDeclaration() )*
	 * f4 -> ( MethodDeclaration() )*
	 * f5 -> "}"
	 * @return 
	 * @throws DuplicateClassException 
	 */
	@Override
	public String visit(ClassDeclaration n) throws VisitorException {
		//Create this class
		String retString = "class ";
		String className = n.f1.f0.tokenImage;
		if(Classes.containsKey(className)) {
			DuplicateClassException e = new DuplicateClassException("Line:"+n.f0.beginLine+":"+n.f0.beginColumn+": Duplicate class declaration:"+className);
			throw e;
		}
		retString+=className+"|";
		MiniJavaClass thisClass = new MiniJavaClass(null,className); //Null means it extends nothing
		Classes.put(className, thisClass);
		
		//Current context is this Class
		currentContext = thisClass;
		
		//Get fields
		retString+=n.f3.accept(this)+"|";
		
		//Get methods
		retString+=n.f4.accept(this)+"|";
		
		//Current context is nothing
		currentContext = null;
		return retString;
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
	 * @return 
	 * @throws DuplicateClassException 
	 */
	@Override
	public String visit(ClassExtendsDeclaration n) throws VisitorException {
		//Create this class
		String retString = "class ";
		String className = n.f1.f0.tokenImage;
		if(Classes.containsKey(className)) {
			DuplicateClassException e = new DuplicateClassException("Line:"+n.f0.beginLine+":"+n.f0.beginColumn+": Duplicate class declaration:"+className);
			throw e;
		}
		retString+=className+" extends ";
		String extendName = n.f3.f0.tokenImage;
		if(!Classes.containsKey(extendName)) {
			UndeclaredClassException e = new UndeclaredClassException("Line:"+n.f0.beginLine+":"+n.f0.beginColumn+": Class "+extendName+" needs to be declared before being extended by class " + className);
			throw e;
		}
		retString+=extendName+"|";
		MiniJavaClass thisClass = new MiniJavaClass(Classes.get(extendName),className); //Null means it extends nothing
		Classes.put(className, thisClass);
		
		//Current context is this Class
		currentContext = thisClass;
		
		//Get fields
		retString+=n.f5.accept(this)+"|";
		
		//Get methods
		retString+=n.f6.accept(this)+"|";
		
		//Current context is nothing
		currentContext = null;
		return retString;
	}

	/**
	 * f0 -> Type()
	 * f1 -> Identifier()
	 * f2 -> ";"
	 * @throws VisitorException 
	 */
	@Override
	public String visit(VarDeclaration n) throws VisitorException {
		String varName;
		String varType;
		varType=n.f0.accept(this);
		varName=n.f1.accept(this);
		if(currentContext.Vars.containsKey(varName)) {
			DuplicateVarException e = new DuplicateVarException("Line:"+n.f2.beginLine+":"+n.f2.beginColumn+": Duplicate variable declaration: "+varType+" "+varName+" and "+currentContext.Vars.get(varName)+" "+varName);
			throw e;
		}
		currentContext.Vars.put(varName, varType);
		return varType+" "+varName+"|";
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
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(MethodDeclaration n) throws VisitorException {
		String retString="method ";
			//We are certain this is going to be a MiniJavaClass because the parser will only allow method declarations in classes
		MiniJavaClass previousContext=(MiniJavaClass) currentContext;
		
		//Get method name and type
		String methType = n.f1.accept(this);
		String methName = n.f2.f0.tokenImage;
		
		//Get the variables
		MiniJavaMethod thisMethod = new MiniJavaMethod(methType, previousContext,methName);
		previousContext.Methods.put(methName, thisMethod);
		currentContext=thisMethod;
		retString+=n.f4.accept(this)+" ";
		
		
		//Keep checking all father contexts for similar methods. If there are discrepancies, complain
		MiniJavaClass tempContext=(MiniJavaClass) previousContext; 
		while(tempContext!=null) {
			if(tempContext.Methods.containsKey(methName)) {
				MiniJavaMethod tempMethod = tempContext.Methods.get(methName);
				if(!tempMethod.returnType.equals(methType) || !Arrays.deepEquals(tempMethod.Vars.entrySet().toArray(),thisMethod.Vars.entrySet().toArray())) {
					//Find class name. We are guaranteed that there is only one of each name
					//The cost of this doesn't matter since this code will only be called on errors
					String thisClassName=null;
					String parentClassName=null;
					for(Entry<String, MiniJavaClass> s : Classes.entrySet()) {
						if(s.getValue().equals(tempContext)) {
							parentClassName=s.getKey();
						}
						if(s.getValue().equals(previousContext)) {
							thisClassName=s.getKey();
						}
						if(thisClassName!=null&&parentClassName!=null) {
							break;
						}
					}
					MethodMismatchException e = new MethodMismatchException("Line:"+n.f0.beginLine+":"+n.f0.beginColumn+": Method "+methName +" declared in class "+ thisClassName +"does not match the one decalred in parent class " + parentClassName);
					throw e;
				}
			}
			tempContext=tempContext.extended;
		}
		
		
		retString += methType + " " + methName + "|";
		
		//Add local variables to method body
		MiniJavaBody thisBody = new MiniJavaBody(thisMethod);
		currentContext=thisBody;
		retString+="vars "+n.f7.accept(this)+"|";
		//Check statements
		retString+="statements "+n.f8.accept(this)+"|";
		//Check return statement
		retString+="returns "+n.f10.accept(this)+"|";
		
		//Return context to original
		currentContext=previousContext;
		return retString;
	}

	/**
	 * f0 -> FormalParameter()
	 * f1 -> FormalParameterTail()
	 * @throws VisitorException 
	 */
	@Override
	public String visit(FormalParameterList n) throws VisitorException {
		return n.f0.accept(this)+n.f1.accept(this);
	}

	/**
	 * f0 -> Type()
	 * f1 -> Identifier()
	 * @throws VisitorException 
	 */
	@Override
	public String visit(FormalParameter n) throws VisitorException {
		String varType = n.f0.accept(this);
		String varName = n.f1.accept(this);
		if(currentContext.Vars.containsKey(varName)) {
			DuplicateVarException e = new DuplicateVarException("Line:"+n.f1.f0.beginLine+":"+n.f1.f0.beginColumn+": Duplicate parameter declaration: "+varType+" "+varName+" and "+currentContext.Vars.get(varName)+" "+varName);
			throw e;
		}
		currentContext.Vars.put(varName, varType);
		return varType+" "+varName+"|";
	}

	/**
	 * f0 -> ( FormalParameterTerm() )
	 * @return 
	 * @throws VisitorException *
	 */
	@Override
	public String visit(FormalParameterTail n) throws VisitorException {
		return n.f0.accept(this);
	}

	/**
	 * f0 -> ","
	 * f1 -> FormalParameter()
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(FormalParameterTerm n) throws VisitorException {
		return n.f1.accept(this);
	}

	/**
	 * f0 -> ArrayType()
	 *       | BooleanType()
	 *       | IntegerType()
	 *       | Identifier()
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(Type n) throws VisitorException {
		return n.f0.accept(this);
	}

	/**
	 * f0 -> "int"
	 * f1 -> "["
	 * f2 -> "]"
	 * @return 
	 */
	@Override
	public String visit(ArrayType n) {
		return n.f0.tokenImage+n.f1.tokenImage+n.f2.tokenImage;
	}

	/**
	 * f0 -> "boolean"
	 * @return 
	 */
	@Override
	public String visit(BooleanType n) {
		return n.f0.tokenImage;
	}

	/**
	 * f0 -> "int"
	 * @return 
	 */
	@Override
	public String visit(IntegerType n) {
		return n.f0.tokenImage;
	}

	/**
	 * f0 -> Block()
	 *       | AssignmentStatement()
	 *       | ArrayAssignmentStatement()
	 *       | IfStatement()
	 *       | WhileStatement()
	 *       | PrintStatement()
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(Statement n) throws VisitorException {
		return n.f0.accept(this);
	}

	/**
	 * f0 -> "{"
	 * f1 -> ( Statement() )*
	 * f2 -> "}"
	 * @throws VisitorException 
	 */
	@Override
	public String visit(Block n) throws VisitorException {
		return n.f1.accept(this);
	}

	/**
	 * f0 -> Identifier()
	 * f1 -> "="
	 * f2 -> Expression()
	 * f3 -> ";"
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(AssignmentStatement n) throws VisitorException {
		String varName = n.f0.f0.tokenImage;
		Context tempContext=currentContext;
		//Check if var was previously declared
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
		String expr = n.f2.accept(this);
		return varName+" "+n.f1.tokenImage+" "+expr+" "+n.f3.tokenImage;
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
	public String visit(ArrayAssignmentStatement n) throws VisitorException {
		String varName = n.f0.f0.tokenImage;
		//Check if var was previously declared
		Context tempContext=currentContext;
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
		//Check if var type is correct
		if(!tempContext.Vars.get(varName).equals("int[]")) {
			TypeMismatchException e = new TypeMismatchException("Line:"+n.f1.beginLine+":"+n.f1.beginColumn+": Variable "+varName+" is of type "+tempContext.Vars.get(varName)+" but is used as int[]");
			throw e;
		}
		
		String indExpr = n.f2.accept(this);
		String expr = n.f5.accept(this);
		return varName+"["+indExpr+"]"+"="+expr+";|";
	}

	/**
	 * f0 -> "if"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> Statement()
	 * f5 -> "else"
	 * f6 -> Statement()
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(IfStatement n) throws VisitorException {
		return "if ( "+n.f2.accept(this)+" ) "+n.f4.accept(this)+" else "+n.f6.accept(this)+"|";
	}

	/**
	 * f0 -> "while"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> Statement()
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(WhileStatement n) throws VisitorException {
		return "while ( "+n.f2.accept(this)+" ) "+n.f4.accept(this)+"|";
	}

	/**
	 * f0 -> "System.out.println"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> ";"
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(PrintStatement n) throws VisitorException {
		return "System.out.println ( "+n.f2.accept(this)+" );|";
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
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(Expression n) throws VisitorException {
		return n.f0.accept(this);
	}

	/**
	 * f0 -> Clause()
	 * f1 -> "&&"
	 * f2 -> Clause()
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(AndExpression n) throws VisitorException {
		return n.f0.accept(this)+" && "+n.f2.accept(this)+"|";
	}
	
	/**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
	 * @return 
	 * @throws VisitorException 
     */
	@Override
	public String visit(CompareExpression n) throws VisitorException {
		return n.f0.accept(this)+" < "+n.f2.accept(this)+"|";
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "+"
	 * f2 -> PrimaryExpression()
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(PlusExpression n) throws VisitorException {
		return n.f0.accept(this)+" + "+n.f2.accept(this)+"|";
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "-"
	 * f2 -> PrimaryExpression()
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(MinusExpression n) throws VisitorException {
		return n.f0.accept(this)+" - "+n.f2.accept(this)+"|";
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "*"
	 * f2 -> PrimaryExpression()
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(TimesExpression n) throws VisitorException {
		return n.f0.accept(this)+" * "+n.f2.accept(this)+"|";
	}
	
	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "["
	 * f2 -> PrimaryExpression()
	 * f3 -> "]"
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(ArrayLookup n) throws VisitorException  {
		return n.f0.accept(this)+" [ "+n.f2.accept(this)+" ]|";
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "."
	 * f2 -> "length"
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(ArrayLength n) throws VisitorException {
		return n.f0.accept(this)+" . length|";
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "."
	 * f2 -> Identifier()
	 * f3 -> "("
	 * f4 -> ( ExpressionList() )?
	 * f5 -> ")"
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(MessageSend n) throws VisitorException {
		return n.f0.accept(this)+" . "+n.f2.accept(this)+" ( "+n.f4.accept(this)+" )|";
	}

	/**
	 * f0 -> Expression()
	 * f1 -> ExpressionTail()
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(ExpressionList n) throws VisitorException {
		return n.f0.accept(this)+" "+n.f1.accept(this)+"|";
	}

	/**
	 * f0 -> ( ExpressionTerm() )
	 * @return 
	 * @throws VisitorException *
	 */
	@Override
	public String visit(ExpressionTail n) throws VisitorException {
		return n.f0.accept(this);
	}

	/**
	 * f0 -> ","
	 * f1 -> Expression()
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(ExpressionTerm n) throws VisitorException {
		return ", "+n.f1.accept(this)+"|";
	}

	/**
	 * f0 -> NotExpression()
	 *       | PrimaryExpression()
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(Clause n) throws VisitorException {
		return n.f0.accept(this);
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
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(PrimaryExpression n) throws VisitorException {
		return n.f0.accept(this);
	}

	/**
	 * f0 -> <INTEGER_LITERAL>
	 * @return 
	 */
	@Override
	public String visit(IntegerLiteral n) {
		return n.f0.tokenImage;
	}

	/**
	 * f0 -> "true"
	 * @return 
	 */
	@Override
	public String visit(TrueLiteral n) {
		return "true";
	}

	/**
	 * f0 -> "false"
	 * @return 
	 */
	@Override
	public String visit(FalseLiteral n) {
		return "false";
	}

	/**
	 * f0 -> <IDENTIFIER>
	 * @return 
	 */
	@Override
	public String visit(Identifier n) {
		return n.f0.tokenImage;
	}

	/**
	 * f0 -> "this"
	 * @return 
	 */
	@Override
	public String visit(ThisExpression n) {
		return "this";
	}

	/**
	 * f0 -> "new"
	 * f1 -> "int"
	 * f2 -> "["
	 * f3 -> Expression()
	 * f4 -> "]"
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(ArrayAllocationExpression n) throws VisitorException {
		return "new int [ "+n.f3.accept(this)+" ]|";
	}

	/**
	 * f0 -> "new"
	 * f1 -> Identifier()
	 * f2 -> "("
	 * f3 -> ")"
	 * @return 
	 */
	@Override
	public String visit(AllocationExpression n) {
		return "new "+n.f1.f0.tokenImage+"()|";
	}

	/**
	 * f0 -> "!"
	 * f1 -> Clause()
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(NotExpression n) throws VisitorException {
		return "! "+n.f1.accept(this);
	}

	/**
	 * f0 -> "("
	 * f1 -> Expression()
	 * f2 -> ")"
	 * @return 
	 * @throws VisitorException 
	 */
	@Override
	public String visit(BracketExpression n) throws VisitorException {
		return "( "+n.f1.accept(this)+" )|";
	}
	
	/**
	 * @return 
	 */
	public LinkedHashMap<String, MiniJavaClass> getClassesMap() {
		return new LinkedHashMap<String, MiniJavaClass>(Classes);
	}

}
