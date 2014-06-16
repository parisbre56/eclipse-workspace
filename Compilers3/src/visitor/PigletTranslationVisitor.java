/**
 * 
 */
package visitor;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Iterator;
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
public class PigletTranslationVisitor implements GJVisitor<String, String> {
	LinkedHashMap<String,MiniJavaClass> Classes = null;
	MiniJavaBody currentBodyContext = null;
	MiniJavaClass currentClassContext = null;
	LinkedHashMap<String,Integer> tempVarName = new LinkedHashMap<String,Integer>(); //This one keeps the number of the tempVariables. Remember to clear after use.
	LinkedHashMap<String,Integer> tempClassName = new LinkedHashMap<String,Integer>(); //This one keeps the vTables for the classes. Remember to clear after use.
	
	Writer output = null;
	Integer tempCounter = 0;
	Integer tabLevel = 0;
	
	/**
	 * @param vt 
	 * @param writer 
	 * 
	 */
	public PigletTranslationVisitor(TypeCheckVisitor vt, Writer writer) {
		Classes = vt.getClassesMap();
		output = writer;
		MiniJavaClass tempClass = null;
		MiniJavaClass currentClass = null;
		
		//For all classes
		Entry<String, MiniJavaClass> currentEntryC = null;
		Entry<String, MiniJavaMethod> currentEntryM = null;
		for(Iterator<Entry<String, MiniJavaClass>> Cit = Classes.entrySet().iterator();Cit.hasNext();) {
			currentEntryC=Cit.next();
			currentClass=currentEntryC.getValue();
			//Put its parents methods in it
			//For each class this extends
			for(tempClass=currentClass.extended;tempClass!=null;tempClass=tempClass.extended) {
				//For each method in this tempClass
				for(Iterator<Entry<String, MiniJavaMethod>> Mit = tempClass.Methods.entrySet().iterator();Mit.hasNext();) {
					currentEntryM = Mit.next();
					//If its child doesn't have this method
					if(!currentClass.Methods.containsKey(currentEntryM.getKey())) {
						//Put it in
						currentClass.Methods.put(currentEntryM.getKey(), currentEntryM.getValue());
					}
				}
			}
			
		}
		//TODO
		//Each class is a temp var pointing to its allocated space, except for ints and bools.
		//int_arrays are a class with a size of 2*4, 
		//position 1 is an int, the array's length, 
		//position two points to the array of size equal to position 1
		
		//(each field is 4 bytes long, since it's either an int or a pointer)
		//Structure of a class will be:
		//Pointer to vTable (TEMP var pointing to a table with size=num_of_methods*4)
		//Fields used by extended
		//Fields used by this one
		//So total size will be:(1+num_of_fields_in_extended+num_of_fields_in_this_one)*4 
		
		//And every time we want to access a field we will ofsset it. 
		//So if our map tells us that this is field num 2 but the extended have 2 other fields,
		//then this field is accessed in address (2+2)*4
		
		//Methods have 
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.NodeList, java.lang.Object)
	 */
	@Override
	public String visit(NodeList n, String argu) throws VisitorException {
		for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
			e.nextElement().accept(this,null);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.NodeListOptional, java.lang.Object)
	 */
	@Override
	public String visit(NodeListOptional n, String argu) throws VisitorException {
		if (n.present()) {
			for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
				e.nextElement().accept(this,null);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.NodeOptional, java.lang.Object)
	 */
	@Override
	public String visit(NodeOptional n, String argu) throws VisitorException {
		if ( n.present() ) {
			n.node.accept(this,null);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.NodeSequence, java.lang.Object)
	 */
	@Override
	public String visit(NodeSequence n, String argu) throws VisitorException {
		for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
			e.nextElement().accept(this,null);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.NodeToken, java.lang.Object)
	 */
	@Override
	public String visit(NodeToken n, String argu) {
		return n.tokenImage;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Goal, java.lang.Object)
	 */
	@Override
	public String visit(Goal n, String argu) throws VisitorException {
		n.f0.accept(this,null);
		n.f1.accept(this,null);
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.MainClass, java.lang.Object)
	 */
	@Override
	public String visit(MainClass n, String argu) throws VisitorException {
		//Current context is main class, main method
		currentClassContext=Classes.get(n.f1.f0.tokenImage);
		currentBodyContext=currentClassContext.Methods.get("main").body;
		//Write main start
		this.writeToOutputFirst("MAIN\n");
		++tabLevel;
		//Write main method body (declarations and body)
		n.f14.accept(this,null);
		n.f15.accept(this,null);
		--tabLevel;
		//Write main finish
		this.writeToOutputFirst("END\n\n");
		//Clear tempVars
		tempVarName.clear();
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.TypeDeclaration, java.lang.Object)
	 */
	@Override
	public String visit(TypeDeclaration n, String argu) throws VisitorException {
		n.f0.accept(this,null);
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ClassDeclaration, java.lang.Object)
	 */
	@Override
	public String visit(ClassDeclaration n, String argu) throws VisitorException {
		currentClassContext=Classes.get(n.f1.f0.tokenImage);
		//We don't care about the vars, since we've already seen them.
		//Get the methods and write them.
		n.f4.accept(this,null);
		//Remove method from context
		currentClassContext=null;
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ClassExtendsDeclaration, java.lang.Object)
	 */
	@Override
	public String visit(ClassExtendsDeclaration n, String argu) throws VisitorException {
		currentClassContext=Classes.get(n.f1.f0.tokenImage);
		//We don't care about the vars and the extension, since we've already seen them.
		//Get the methods and write them.
		n.f4.accept(this,null);
		//Remove class from context
		currentClassContext=null;
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.VarDeclaration, java.lang.Object)
	 */
	@Override
	public String visit(VarDeclaration n, String argu) throws VisitorException {
		//Will never get here
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.MethodDeclaration, java.lang.Object)
	 */
	@Override
	public String visit(MethodDeclaration n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		//Get context
		currentBodyContext=currentClassContext.Methods.get(n.f2.f0.tokenImage).body;
		//Get the first available tempVar
		tempCounter=currentBodyContext.getTempNum();
		//Write the method name for the piglet
		this.writeToOutputFirst(currentBodyContext.parent.pigletMethodName()+" ["+currentBodyContext.parent.getArgNum().toString()+"] \n");
		++tabLevel;
		this.writeToOutputFirst("BEGIN \n");
		++tabLevel;
		//We only care about the statements and the return
		n.f8.accept(this,null);
		this.writeToOutputFirst("RETURN ");
		n.f10.accept(this,null);
		--tabLevel;
		//End method
		this.writeToOutputFirst("END \n\n");
		--tabLevel;
		//Remove method from context
		tempCounter=-1;
		currentBodyContext=null;
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.FormalParameterList, java.lang.Object)
	 */
	@Override
	public String visit(FormalParameterList n, String argu) throws VisitorException {
		//We don't care about this
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.FormalParameter, java.lang.Object)
	 */
	@Override
	public String visit(FormalParameter n, String argu) throws VisitorException {
		//We don't care about this
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.FormalParameterTail, java.lang.Object)
	 */
	@Override
	public String visit(FormalParameterTail n, String argu) throws VisitorException {
		//We don't care about this
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.FormalParameterTerm, java.lang.Object)
	 */
	@Override
	public String visit(FormalParameterTerm n, String argu) throws VisitorException {
		//We don't care about this
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Type, java.lang.Object)
	 */
	@Override
	public String visit(Type n, String argu) throws VisitorException {
		//We don't care about this
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ArrayType, java.lang.Object)
	 */
	@Override
	public String visit(ArrayType n, String argu) {
		//We don't care about this
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.BooleanType, java.lang.Object)
	 */
	@Override
	public String visit(BooleanType n, String argu) {
		//We don't care about this
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.IntegerType, java.lang.Object)
	 */
	@Override
	public String visit(IntegerType n, String argu) {
		//We don't care about this
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Statement, java.lang.Object)
	 */
	@Override
	public String visit(Statement n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Block, java.lang.Object)
	 */
	@Override
	public String visit(Block n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.AssignmentStatement, java.lang.Object)
	 */
	@Override
	public String visit(AssignmentStatement n, String argu)
			throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ArrayAssignmentStatement, java.lang.Object)
	 */
	@Override
	public String visit(ArrayAssignmentStatement n, String argu)
			throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.IfStatement, java.lang.Object)
	 */
	@Override
	public String visit(IfStatement n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.WhileStatement, java.lang.Object)
	 */
	@Override
	public String visit(WhileStatement n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.PrintStatement, java.lang.Object)
	 */
	@Override
	public String visit(PrintStatement n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Expression, java.lang.Object)
	 */
	@Override
	public String visit(Expression n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.AndExpression, java.lang.Object)
	 */
	@Override
	public String visit(AndExpression n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.CompareExpression, java.lang.Object)
	 */
	@Override
	public String visit(CompareExpression n, String argu)
			throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.PlusExpression, java.lang.Object)
	 */
	@Override
	public String visit(PlusExpression n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.MinusExpression, java.lang.Object)
	 */
	@Override
	public String visit(MinusExpression n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.TimesExpression, java.lang.Object)
	 */
	@Override
	public String visit(TimesExpression n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ArrayLookup, java.lang.Object)
	 */
	@Override
	public String visit(ArrayLookup n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ArrayLength, java.lang.Object)
	 */
	@Override
	public String visit(ArrayLength n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.MessageSend, java.lang.Object)
	 */
	@Override
	public String visit(MessageSend n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ExpressionList, java.lang.Object)
	 */
	@Override
	public String visit(ExpressionList n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ExpressionTail, java.lang.Object)
	 */
	@Override
	public String visit(ExpressionTail n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ExpressionTerm, java.lang.Object)
	 */
	@Override
	public String visit(ExpressionTerm n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Clause, java.lang.Object)
	 */
	@Override
	public String visit(Clause n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.PrimaryExpression, java.lang.Object)
	 */
	@Override
	public String visit(PrimaryExpression n, String argu)
			throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.IntegerLiteral, java.lang.Object)
	 */
	@Override
	public String visit(IntegerLiteral n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.TrueLiteral, java.lang.Object)
	 */
	@Override
	public String visit(TrueLiteral n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.FalseLiteral, java.lang.Object)
	 */
	@Override
	public String visit(FalseLiteral n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Identifier, java.lang.Object)
	 */
	@Override
	public String visit(Identifier n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ThisExpression, java.lang.Object)
	 */
	@Override
	public String visit(ThisExpression n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ArrayAllocationExpression, java.lang.Object)
	 */
	@Override
	public String visit(ArrayAllocationExpression n, String argu)
			throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.AllocationExpression, java.lang.Object)
	 */
	@Override
	public String visit(AllocationExpression n, String argu)
			throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.NotExpression, java.lang.Object)
	 */
	@Override
	public String visit(NotExpression n, String argu) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.BracketExpression, java.lang.Object)
	 */
	@Override
	public String visit(BracketExpression n, String argu)
			throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}
	
	/** 
	 * Writes tabs in front of the string according to tabLevel and then writes the string
	 * It also transforms IOExceptions into VisitorIOExceptions and puts the causing IOException in it as cause
	 * @param string
	 * @throws VisitorIOException 
	 */
	private void writeToOutputFirst(String string) throws VisitorIOException {
		try {
			for(int i=0;i<tabLevel;++i) {
				output.write("\t");
			}
			output.write(string);
		} catch (IOException e) {
			e.printStackTrace();
			throw new VisitorIOException(e);
		}
	}
	
	/** 
	 * This transforms IOExceptions into VisitorIOExceptions and puts the causing IOException in it as cause
	 * @param string
	 * @throws VisitorIOException 
	 */
	private void writeToOutput(String string) throws VisitorIOException {
		try {
			output.write(string);
		} catch (IOException e) {
			e.printStackTrace();
			throw new VisitorIOException(e);
		}
	}

}
