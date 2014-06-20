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
	
	//Use this to check the type of an expression.
	TypeCheckVisitor typeRetriever = null;
	
	Writer output = null;
	Integer tempCounter = 0;
	Integer tabLevel = 0;
	Integer labelCounter = 0;
	
	/**
	 * @param vt is the visitor used for typechecking the same file. It is needed to determine what class a method call belongs to
	 * @param writer used to output the translated and partially formatted piglet code
	 * 
	 */
	public PigletTranslationVisitor(TypeCheckVisitor vt, Writer writer) {
		typeRetriever = vt;
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
		//Each class is a temp var pointing to its allocated space, except for ints and bools.
		//int_arrays are a class with a size of 2*4, 
		//position 1 is an int, the array's length, 
		//position two points to the array of size equal to position 1
		
		//(each field is 4 bytes long, since it's either an int or a pointer)
		//Structure of a class will be:
		//Pointer to vTable (int pointing to a table with size=num_of_methods*4)
		//Fields used by extended
		//Fields used by this one
		//So total size will be:(1+num_of_fields_in_extended+num_of_fields_in_this_one)*4 
		
		//And every time we want to access a field we will ofsset it. 
		//So if our class map tells us that this is field 2 but the extended have 2 other fields,
		//then this field is accessed in address (2+2)*4 //numbering starts from 1 based because position 0 is the vTable
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
		//Visit main class
		n.f0.accept(this,null);
		//and all class declarations
		n.f1.accept(this,null);
		writeToOutput("\n\n");
		//Writes constructors for each class and an array constructor
		writeClassConstructors();
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
		//Write a very simple main segment that only instantiates the main class and calls the main method
		writeToOutputFirst("MAIN \n");
		++tabLevel;
		//Create a new main class 
		Integer mainClassTemp=tempCounter;
		++tempCounter;
		writeToOutputFirst("MOVE TEMP "+mainClassTemp.toString()+" CALL "+currentClassContext.constructorFunctionName()+" () \n");
		//load its vTable
		Integer vTableTemp=tempCounter;
		++tempCounter;
		writeToOutputFirst("HLOAD TEMP "+vTableTemp.toString()+" TEMP "+mainClassTemp.toString()+" 0 \n");
		Integer mainMethodTemp=tempCounter;
		++tempCounter;
		//Load the location of the main method
		writeToOutputFirst("HLOAD TEMP "+mainMethodTemp.toString()+" TEMP "+vTableTemp.toString()+" "+currentBodyContext.parent.getPosition().toString()+" \n");
		//Call the main method
		Integer retExitCode=tempCounter;
		++tempCounter;
		writeToOutputFirst("MOVE TEMP "+retExitCode.toString()+" CALL TEMP "+mainMethodTemp.toString()+" (TEMP "+mainClassTemp.toString()+" 0) \n");
		//Display exit code
		//writeToOutputFirst("PRINT TEMP "+retExitCode.toString()+" \n");
		--tabLevel;
		writeToOutputFirst("END \n\n");
		//Write main start
		tempCounter=currentBodyContext.getTempNum();
		this.writeToOutputFirst(currentBodyContext.parent.pigletMethodName()+" [2] \n");
		++tabLevel;
		writeToOutputFirst("BEGIN \n");
		++tabLevel;
		//Write main method body (statements only, we don't care about declarations)
		n.f15.accept(this,null);
		//Return 0 for success (not much choice here)
		writeToOutputFirst("RETURN 0 \n");
		--tabLevel;
		//Write main finish
		this.writeToOutputFirst("END \n\n");
		--tabLevel;
		//Clear tempVars
		currentClassContext=null;
		currentBodyContext=null;
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
		//Current context is class with the name of this class
		currentClassContext=Classes.get(n.f1.f0.tokenImage);
		//We don't care about the vars, since we've already seen them in the previous visitor.
		//Get the methods and write them.
		n.f4.accept(this,null);
		//Remove class from context
		currentClassContext=null;
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ClassExtendsDeclaration, java.lang.Object)
	 */
	@Override
	public String visit(ClassExtendsDeclaration n, String argu) throws VisitorException {
		//Current context is class with the name of this class
		currentClassContext=Classes.get(n.f1.f0.tokenImage);
		//We don't care about the vars and the extension, since we've already seen them in the previous visitor.
		//Get the methods and write them.
		n.f6.accept(this,null);
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
		//Get context
		currentBodyContext=currentClassContext.Methods.get(n.f2.f0.tokenImage).body;
		//Get the first available tempVar
		tempCounter=currentBodyContext.getTempNum();
		//Write the method name for the piglet
		this.writeToOutputFirst(currentBodyContext.parent.pigletMethodName()+" ["+currentBodyContext.parent.getArgNum().toString()+"] \n");
		++tabLevel;
		//Begin method
		this.writeToOutputFirst("BEGIN \n");
		++tabLevel;
		//We only care about the statements and the return
		n.f8.accept(this,null);
		this.writeToOutputFirst("RETURN ");
		n.f10.accept(this,null);
		writeToOutput(" \n");
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
		n.f0.accept(this,null);
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Block, java.lang.Object)
	 */
	@Override
	public String visit(Block n, String argu) throws VisitorException {
		n.f1.accept(this,null);
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.AssignmentStatement, java.lang.Object)
	 */
	@Override
	public String visit(AssignmentStatement n, String argu) throws VisitorException {
		Integer tempVar=currentBodyContext.getArgNum(n.f0.f0.tokenImage);
		if(tempVar<0) {
			tempVar=currentClassContext.getArgNum(n.f0.f0.tokenImage);
			this.writeToOutputFirst("HSTORE TEMP 0 "+tempVar.toString()+" ");
		}
		else {
			this.writeToOutputFirst("MOVE TEMP "+tempVar.toString()+" ");
		}
		n.f2.accept(this,null);
		this.writeToOutput(" \n");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ArrayAssignmentStatement, java.lang.Object)
	 */
	@Override
	public String visit(ArrayAssignmentStatement n, String argu) throws VisitorException {
		Integer tempVar=currentBodyContext.getArgNum(n.f0.f0.tokenImage);
		Integer arraySizeTemp=-1;
		Integer arrayLocationTemp=-1;
		Integer arrayOffsetTemp=-1;
		//If this is a field
		if(tempVar<0) {
			tempVar=currentClassContext.getArgNum(n.f0.f0.tokenImage);
			//Make tempVar be the location of the loaded array location
			Integer arrayVar=tempCounter;
			tempCounter++;
			writeToOutputFirst("HLOAD TEMP "+arrayVar.toString()+" TEMP 0 "+tempVar.toString()+" \n");
			tempVar=arrayVar;
		}
		//Check for null
		writeToOutputFirst("CJUMP LT TEMP "+tempVar.toString()+" 1 L"+labelCounter.toString()+"NULL \n");
		writeToOutputFirst("\tERROR \n");
		writeToOutputFirst("L"+labelCounter.toString()+"NULL \n");
		++labelCounter;
		//Get Array size
		arraySizeTemp=tempCounter;
		tempCounter++;
		writeToOutputFirst("HLOAD TEMP "+arraySizeTemp.toString()+" TEMP "+tempVar.toString()+" 0 \n");
		//Get Array address
		arrayLocationTemp=tempCounter;
		tempCounter++;
		writeToOutputFirst("HLOAD TEMP "+arrayLocationTemp.toString()+" TEMP "+tempVar.toString()+" 4 \n");
		//Get Array offset
		arrayOffsetTemp=tempCounter;
		tempCounter++;
		writeToOutputFirst("MOVE TEMP "+arrayOffsetTemp.toString()+" ");
		n.f2.accept(this,null);
		writeToOutput(" \n");
		//Check offset
		//First for offset less than 0
		writeToOutputFirst("CJUMP LT TEMP "+arrayOffsetTemp.toString()+" 0 L"+labelCounter.toString()+"OFFSLTZERO \n");
		writeToOutputFirst("\tERROR\n");
		writeToOutputFirst("L"+labelCounter.toString()+"OFFSLTZERO \n");
		++labelCounter;
		//Then for offset grater than or equal to size, which translates to offset greater than size-1 
		//which translates to size-1 less than offset
		writeToOutputFirst("CJUMP LT MINUS TEMP "+arraySizeTemp.toString()+" 1 TEMP "+arrayOffsetTemp.toString()+" L"+labelCounter.toString()+"OFFSGTSIZE \n");
		writeToOutputFirst("\tERROR \n");
		writeToOutputFirst("L"+labelCounter.toString()+"OFFSGTSIZE \n");
		++labelCounter;
		//Make offset a proper offset by multiplying by 4
		//Make array address point to array address+offset. That makes it point to where we want to write
		//Write to the array address
		writeToOutputFirst("HSTORE PLUS TEMP "+arrayLocationTemp.toString()+" TIMES TEMP "+arrayOffsetTemp.toString()+" 4 0 ");
		n.f5.accept(this,null);
		writeToOutput(" \n");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.IfStatement, java.lang.Object)
	 */
	@Override
	public String visit(IfStatement n, String argu) throws VisitorException {
		Integer currLabel=labelCounter;
		++labelCounter;
		//If the expression (f2) is true (expression returns 1) then continue into the then part, else jump to the else part
		writeToOutputFirst("CJUMP ");
		n.f2.accept(this,null);
		writeToOutput(" L"+currLabel.toString()+"ELSE \n");
		//Write the then part
		++tabLevel;
		n.f4.accept(this,null);
		--tabLevel;
		//jump to the end
		writeToOutputFirst("JUMP L"+currLabel.toString()+"ENDIF \n");
		//The write the else part label and the else part
		writeToOutputFirst("L"+currLabel.toString()+"ELSE \n");
		++tabLevel;
		n.f6.accept(this,null);
		//Noop to ensure else label has a valid address
		writeToOutputFirst("NOOP \n");
		--tabLevel;
		//Write the end label
		writeToOutputFirst("L"+currLabel.toString()+"ENDIF \n");
		//Noop to ensure endif label has a valid address
		writeToOutputFirst("NOOP \n");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.WhileStatement, java.lang.Object)
	 */
	@Override
	public String visit(WhileStatement n, String argu) throws VisitorException {
		//LWHILESTART
		//CJUMP EXPRESSION LWHILEEND
		//	STUFF
		//JUMP LWHILESTART
		//LWHILEEND
		Integer currLabel=labelCounter;
		++labelCounter;
		//Start label to return to so that you can check condition
		writeToOutputFirst("L"+currLabel.toString()+"WHILESTART \n");
		//Jump to end if expr false
		writeToOutputFirst("CJUMP ");
		n.f2.accept(this,null);
		writeToOutput(" L"+currLabel.toString()+"WHILEEND \n");
		//What the while loop will execute
		++tabLevel;
		n.f4.accept(this,null);
		writeToOutput(" \n");
		--tabLevel;
		//Jump to start to check condition
		writeToOutputFirst("JUMP L"+currLabel.toString()+"WHILESTART \n");
		//End label to jump to on condition false
		writeToOutputFirst("L"+currLabel.toString()+"WHILEEND \n");
		//Noop to ensure endwhile label has a valid address
		writeToOutputFirst("NOOP \n");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.PrintStatement, java.lang.Object)
	 */
	@Override
	public String visit(PrintStatement n, String argu) throws VisitorException {
		writeToOutputFirst("PRINT ");
		n.f2.accept(this,null);
		writeToOutput(" \n");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Expression, java.lang.Object)
	 */
	@Override
	public String visit(Expression n, String argu) throws VisitorException {
		n.f0.accept(this,null);
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.AndExpression, java.lang.Object)
	 */
	@Override
	public String visit(AndExpression n, String argu) throws VisitorException {
		//AND is simply multiplication, because 1*1=1 and 1*0=0 and 0*0=0
		writeToOutput("TIMES ");
		n.f0.accept(this,null);
		writeToOutput(" ");
		n.f2.accept(this,null);
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.CompareExpression, java.lang.Object)
	 */
	@Override
	public String visit(CompareExpression n, String argu) throws VisitorException {
		writeToOutput("LT ");
		n.f0.accept(this,null);
		writeToOutput(" ");
		n.f2.accept(this,null);
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.PlusExpression, java.lang.Object)
	 */
	@Override
	public String visit(PlusExpression n, String argu) throws VisitorException {
		writeToOutput("PLUS ");
		n.f0.accept(this,null);
		writeToOutput(" ");
		n.f2.accept(this,null);
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.MinusExpression, java.lang.Object)
	 */
	@Override
	public String visit(MinusExpression n, String argu) throws VisitorException {
		writeToOutput("MINUS ");
		n.f0.accept(this,null);
		writeToOutput(" ");
		n.f2.accept(this,null);
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.TimesExpression, java.lang.Object)
	 */
	@Override
	public String visit(TimesExpression n, String argu) throws VisitorException {
		writeToOutput("TIMES ");
		n.f0.accept(this,null);
		writeToOutput(" ");
		n.f2.accept(this,null);
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ArrayLookup, java.lang.Object)
	 */
	@Override
	public String visit(ArrayLookup n, String argu) throws VisitorException {
		//Begin segment, requested value returned at end
		writeToOutput("BEGIN \n");
		++tabLevel;
		//Get the location of the array
		Integer tempVar=tempCounter;
		++tempCounter;
		writeToOutputFirst("MOVE TEMP "+tempVar+" ");
		n.f0.accept(this,null);
		writeToOutput(" \n");
		//Check that array has been allocated
		//Check for null
		writeToOutputFirst("CJUMP LT TEMP "+tempVar.toString()+" 1 L"+labelCounter.toString()+"NULL \n");
		writeToOutputFirst("\tERROR \n");
		writeToOutputFirst("L"+labelCounter.toString()+"NULL \n");
		++labelCounter;
		//Get Array size
		Integer arraySizeTemp=tempCounter;
		tempCounter++;
		writeToOutputFirst("HLOAD TEMP "+arraySizeTemp.toString()+" TEMP "+tempVar.toString()+" 0 \n");
		//Get Array address
		Integer arrayLocationTemp=tempCounter;
		tempCounter++;
		writeToOutputFirst("HLOAD TEMP "+arrayLocationTemp.toString()+" TEMP "+tempVar.toString()+" 4 \n");
		//Get Array offset
		Integer arrayOffsetTemp=tempCounter;
		tempCounter++;
		writeToOutputFirst("MOVE TEMP "+arrayOffsetTemp.toString()+" ");
		n.f2.accept(this,null);
		writeToOutput(" \n");
		//Check offset
		//First for offset less than 0
		writeToOutputFirst("CJUMP LT TEMP "+arrayOffsetTemp.toString()+" 0 L"+labelCounter.toString()+"OFFSLTZERO \n");
		writeToOutputFirst("\tERROR \n");
		writeToOutputFirst("L"+labelCounter.toString()+"OFFSLTZERO \n");
		++labelCounter;
		//Then for offset grater than or equal to size, which translates to offset greater than size-1 
		//which translates to size-1 less than offset
		writeToOutputFirst("CJUMP LT MINUS TEMP "+arraySizeTemp.toString()+" 1 TEMP "+arrayOffsetTemp.toString()+" L"+labelCounter.toString()+"OFFSGTSIZE \n");
		writeToOutputFirst("\tERROR \n");
		writeToOutputFirst("L"+labelCounter.toString()+"OFFSGTSIZE \n");
		++labelCounter;
		//Make offset a proper offset by multiplying by 4
		//Make array address point to array address+offset. That makes it point to where we want to write
		//Load the requested value into the tempArray
		Integer retTemp=tempCounter;
		tempCounter++;
		writeToOutputFirst("HLOAD TEMP "+retTemp.toString()+" PLUS TEMP "+arrayLocationTemp.toString()+" TIMES TEMP "+arrayOffsetTemp.toString()+" 4 0 \n");
		
		//Return the requested value and end
		writeToOutputFirst("RETURN TEMP "+retTemp.toString()+" \n");
		--tabLevel;
		writeToOutputFirst("END ");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ArrayLength, java.lang.Object)
	 */
	@Override
	public String visit(ArrayLength n, String argu) throws VisitorException {
		//Begin segment, requested value returned at end
		writeToOutput("BEGIN \n");
		++tabLevel;
		//Get the location of the array
		Integer tempVar=tempCounter;
		++tempCounter;
		writeToOutputFirst("MOVE TEMP "+tempVar.toString()+" ");
		n.f0.accept(this,null);
		writeToOutput(" \n");
		//Check that array has been allocated
		//Check for null
		writeToOutputFirst("CJUMP LT TEMP "+tempVar.toString()+" 1 L"+labelCounter.toString()+"NULL \n");
		writeToOutputFirst("\tERROR \n");
		writeToOutputFirst("L"+labelCounter.toString()+"NULL \n");
		++labelCounter;
		//Get Array size
		Integer arraySizeTemp=tempCounter;
		tempCounter++;
		writeToOutputFirst("HLOAD TEMP "+arraySizeTemp.toString()+" TEMP "+tempVar.toString()+" 0 \n");
		//Return the requested value and end
		writeToOutputFirst("RETURN TEMP "+arraySizeTemp.toString()+" \n");
		--tabLevel;
		writeToOutputFirst("END ");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.MessageSend, java.lang.Object)
	 */
	@Override
	public String visit(MessageSend n, String argu) throws VisitorException {
		//Begin new segment, result of function call returned at end
		writeToOutput("BEGIN \n");
		++tabLevel;
		//Get the location of the class
		Integer tempVar=tempCounter;
		++tempCounter;
		writeToOutputFirst("MOVE TEMP "+tempVar.toString()+" ");
		n.f0.accept(this,null);
		writeToOutput(" \n");
		//Check for null
		writeToOutputFirst("CJUMP LT TEMP "+tempVar.toString()+" 1 L"+labelCounter.toString()+"NULL \n");
		writeToOutputFirst("\tERROR \n");
		writeToOutputFirst("L"+labelCounter.toString()+"NULL \n");
		++labelCounter;
		//Use the other visitor to determine object type and place in vTable
		typeRetriever.currentClassContext=currentClassContext;
		typeRetriever.currentBodyContext=currentBodyContext;
		typeRetriever.requestType=true;
		MiniJavaClass callToClass = Classes.get(n.f0.accept(typeRetriever,null));
		typeRetriever.requestType=false;
		typeRetriever.currentClassContext=null;
		typeRetriever.currentBodyContext=null;
		MiniJavaMethod callToMethod = callToClass.Methods.get(n.f2.f0.tokenImage);
		Integer placeInVTable = callToMethod.getPosition();
		//Get vTable
		Integer vTableTemp=tempCounter;
		++tempCounter;
		writeToOutputFirst("HLOAD TEMP "+vTableTemp.toString()+" TEMP "+tempVar.toString()+" 0 \n");
		//Get the method location
		Integer methodTemp=tempCounter;
		++tempCounter;
		writeToOutputFirst("HLOAD TEMP "+methodTemp.toString()+" TEMP "+vTableTemp.toString()+" "+placeInVTable.toString()+" \n");
		//Return call to method
		writeToOutputFirst("RETURN CALL TEMP "+methodTemp.toString()+" (TEMP "+tempVar.toString());
		//Get args
		n.f4.accept(this,null);
		writeToOutput(") \n");
		--tabLevel;
		writeToOutputFirst("END ");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ExpressionList, java.lang.Object)
	 */
	@Override
	public String visit(ExpressionList n, String argu) throws VisitorException {
		writeToOutput(" "); //A space following TEMP 0
		n.f0.accept(this,null);
		n.f1.accept(this,null);
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ExpressionTail, java.lang.Object)
	 */
	@Override
	public String visit(ExpressionTail n, String argu) throws VisitorException {
		n.f0.accept(this,null);
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ExpressionTerm, java.lang.Object)
	 */
	@Override
	public String visit(ExpressionTerm n, String argu) throws VisitorException {
		writeToOutput(" "); //Space replaces comma for method call arguments
		n.f1.accept(this,null);
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Clause, java.lang.Object)
	 */
	@Override
	public String visit(Clause n, String argu) throws VisitorException {
		n.f0.accept(this,null);
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.PrimaryExpression, java.lang.Object)
	 */
	@Override
	public String visit(PrimaryExpression n, String argu) throws VisitorException {
		n.f0.accept(this,null);
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.IntegerLiteral, java.lang.Object)
	 */
	@Override
	public String visit(IntegerLiteral n, String argu) throws VisitorException {
		writeToOutput(n.f0.tokenImage); //For integers just write the integer
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.TrueLiteral, java.lang.Object)
	 */
	@Override
	public String visit(TrueLiteral n, String argu) throws VisitorException {
		writeToOutput("1"); //True is 1 and only 1 (due to the way && is evaluated and the way LT and CJUMP works)
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.FalseLiteral, java.lang.Object)
	 */
	@Override
	public String visit(FalseLiteral n, String argu) throws VisitorException {
		writeToOutput("0"); //False is 0 and only 0 (due to the way && is evaluated and the way LT and CJUMP works)
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.Identifier, java.lang.Object)
	 */
	@Override
	public String visit(Identifier n, String argu) throws VisitorException {
		//If this is local to the method, print the tempVar associated with it. Else make a begin/return/end that returns the requested field
		//We are certain that this will only get called during an expression evaluation
		Integer tempVar=currentBodyContext.getArgNum(n.f0.tokenImage);
		if(tempVar<0) {
			tempVar=currentClassContext.getArgNum(n.f0.tokenImage);
			Integer retVarTemp=tempCounter;
			++tempCounter;
			writeToOutput("BEGIN \n");
			++tabLevel;
			writeToOutputFirst("HLOAD TEMP "+retVarTemp.toString()+" TEMP 0 "+tempVar.toString()+" \n");
			writeToOutputFirst("RETURN TEMP "+retVarTemp.toString()+" \n");
			--tabLevel;
			writeToOutputFirst("END ");

		}
		else {
			this.writeToOutput("TEMP "+tempVar.toString());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ThisExpression, java.lang.Object)
	 */
	@Override
	public String visit(ThisExpression n, String argu) throws VisitorException {
		writeToOutput("TEMP 0");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.ArrayAllocationExpression, java.lang.Object)
	 */
	@Override
	public String visit(ArrayAllocationExpression n, String argu) throws VisitorException {
		//Call the array constructor with the array size as argument that will return the allocated array
		writeToOutput("CALL "+Classes.get("int[]").constructorFunctionName()+" (");
		n.f3.accept(this,null);
		writeToOutput(")");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.AllocationExpression, java.lang.Object)
	 */
	@Override
	public String visit(AllocationExpression n, String argu) throws VisitorException {
		//Call to class constructor that will return the allocated class
		MiniJavaClass toAllocate = Classes.get(n.f1.f0.tokenImage);
		writeToOutput("CALL "+toAllocate.constructorFunctionName()+" ()");//constructors get no arguments
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.NotExpression, java.lang.Object)
	 */
	@Override
	public String visit(NotExpression n, String argu) throws VisitorException {
		Integer retTempVar=tempCounter;
		++tempCounter;
		writeToOutput("BEGIN \n");
		++tabLevel;
		//If this is 1 (true), return 0 (false), else got to label false, where you'll return true
		writeToOutputFirst("CJUMP ");
		n.f1.accept(this,null);
		writeToOutput(" L"+labelCounter.toString()+"FALSE \n");
		//If true (1 moves on to the next command rather than jumping) return 0 (false)
		writeToOutputFirst("MOVE TEMP "+retTempVar.toString()+" 0 \n");
		//Go to end so that you can return false
		writeToOutputFirst("JUMP L"+labelCounter.toString()+"END \n");
		//Else if false
		writeToOutputFirst("L"+labelCounter.toString()+"FALSE \n");
		//Return true
		writeToOutputFirst("MOVE TEMP "+retTempVar.toString()+" 1 \n");
		//This is the end
		writeToOutputFirst("L"+labelCounter.toString()+"END \n");
		//Noop because of grammar restrictions
		writeToOutputFirst("NOOP \n");
		//Return the negated value
		writeToOutputFirst("RETURN TEMP "+retTempVar.toString()+" \n");
		++labelCounter;
		--tabLevel;
		writeToOutputFirst("END ");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.BracketExpression, java.lang.Object)
	 */
	@Override
	public String visit(BracketExpression n, String argu) throws VisitorException {
		n.f1.accept(this,null);
		return null;
	}
	
	/** 
	 * For each class in the Classes map except int, int[] and boolean, writes a constructor function that can create new instances of each class.<br>
	 * Constructor function names are created in the following way {Class name}Constructor_ (no spaces or other divider). This ensures that all function
	 * names will be unique, since all function names related to methods contain _{UNIQUEMETHODIDENTIFIER} at the end.
	 * 
	 * This will be of great help later, because it will make it easier to give each class instance a single vTable
	 * 
	 * @throws VisitorException 
	 * @see {@link visitor.MiniJavaMethod#pigletMethodName()}, {@link visitor.MiniJavaClass#constructorFunctionName()}
	 */
	private void writeClassConstructors() throws VisitorException {
		//writes functions that can create instances of each class
		for(Iterator<Entry<String, MiniJavaClass>> itC=Classes.entrySet().iterator();itC.hasNext();) {
			Entry<String, MiniJavaClass> currEntry = itC.next();
			MiniJavaClass toAllocate = currEntry.getValue();
			String conName = currEntry.getValue().constructorFunctionName();
			//If this doesn't need a constructor, continue to the next one
			if(conName==null||conName.isEmpty()) {
				continue;
			}
			//If this is the array constructor
			else if(toAllocate.name.equals("int[]")) { 
				//remember you need temp 0 for size
				tempCounter=1;
				writeToOutputFirst(conName+" [1] \n");
				++tabLevel;
				//Allocate array. Return allocated array
				writeToOutputFirst("BEGIN \n");
				++tabLevel;
				//Check for size less than zero
				writeToOutputFirst("CJUMP LT TEMP 0 0 L"+labelCounter.toString()+"OFFSLTZERO \n");
				writeToOutputFirst("\tERROR \n");
				writeToOutputFirst("L"+labelCounter.toString()+"OFFSLTZERO \n");
				++labelCounter;
				//Allocate array class in arrayVar (one position for length var and one for the pointer to the actual array)
				Integer arrayVar=tempCounter;
				++tempCounter;
				writeToOutputFirst("MOVE TEMP "+arrayVar.toString()+" HALLOCATE "+Classes.get("int[]").instanceSizeRequirment().toString()+" \n");
				//Put size in the first position
				writeToOutputFirst("HSTORE TEMP "+arrayVar.toString()+" 0 TEMP 0 \n");
				//Allocate an array at the second position with size equal to sizeVar
				writeToOutputFirst("HSTORE TEMP "+arrayVar.toString()+" 4 HALLOCATE TIMES 4 TEMP 0 \n");
				//Return the array and end
				writeToOutputFirst("RETURN TEMP "+arrayVar.toString()+" \n");
				--tabLevel;
				writeToOutputFirst("END \n\n");
				--tabLevel;
			}
			//If this is a normal class constructor
			else {
				//This has no args so tempCounter is 0
				tempCounter=0;
				writeToOutputFirst(conName+" [0] \n");
				++tabLevel;
				//Begin
				writeToOutputFirst("BEGIN \n");
				++tabLevel;
				//This will be the returned instance
				Integer retVar=tempCounter;
				tempCounter++;
				//Allocate space for this instance
				writeToOutputFirst("MOVE TEMP "+retVar.toString()+" HALLOCATE "+toAllocate.instanceSizeRequirment().toString()+" \n");
				//Store the vtable in it
				writeToOutputFirst("HSTORE TEMP "+retVar.toString()+" 0 BEGIN \n");
				++tabLevel;
				//This will hold the vTable
				Integer retVtable=tempCounter;
				++tempCounter;
				//Allocate space for the vTable
				writeToOutputFirst("MOVE TEMP "+retVtable.toString()+" HALLOCATE "+toAllocate.vTableSizeRequirement().toString()+" \n");
				//Put a pointer to each function
				for(Iterator<Entry<String, MiniJavaMethod>> itM = toAllocate.Methods.entrySet().iterator();itM.hasNext();) {
					Entry<String, MiniJavaMethod> tempEntry = itM.next();
					writeToOutputFirst("HSTORE TEMP "+retVtable.toString()+" "+tempEntry.getValue().getPosition()+" "+tempEntry.getValue().pigletMethodName()+" \n");
				}
				//Return the vTable and end
				writeToOutputFirst("RETURN TEMP "+retVtable.toString()+" \n");
				--tabLevel;
				writeToOutputFirst("END \n");
				//Return the instance and end
				writeToOutputFirst("RETURN TEMP "+retVar.toString()+" \n");
				--tabLevel;
				writeToOutputFirst("END \n\n");
				--tabLevel;
			}
		}
	}
	
	/** 
	 * Writes tabs in front of the string according to tabLevel and then writes the string
	 * It also transforms IOExceptions into VisitorIOExceptions and puts the causing IOException in it as cause
	 * @param string to write
	 * @throws VisitorIOException which contains an IOException as its cause
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
	 * Writes the string
	 * This transforms IOExceptions into VisitorIOExceptions and puts the causing IOException in it as cause
	 * @param string to write
	 * @throws VisitorIOException which contains an IOException as its cause 
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