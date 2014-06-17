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
	
	//This one keeps the TEMP variables that hold the vTables for the classes. Remember to clear after use.
	LinkedHashMap<String,Integer> tempClassTable = new LinkedHashMap<String,Integer>();
	
	Writer output = null;
	Integer tempCounter = 0;
	Integer tabLevel = 0;
	Integer labelCounter = 0;
	
	/**
	 * @param vt 
	 * @param writer 
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
		//TODO make this write the TEMP associated with this variable? Or a begin/return/end statement for fields? Use argu to determine this? 
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
		n.f15.accept(this,null);
		--tabLevel;
		//Write main finish
		this.writeToOutputFirst("END\n\n");
		//Clear tempVars
		tempClassTable.clear();
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
		--tabLevel;
		//End method
		this.writeToOutputFirst("END \n\n");
		--tabLevel;
		//Remove method from context
		tempCounter=-1;
		currentBodyContext=null;
		tempClassTable.clear();
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
		this.writeToOutput("\n");
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
		Boolean isField=false;
		//If this is a field
		if(tempVar<0) {
			tempVar=currentClassContext.getArgNum(n.f0.f0.tokenImage);
			//Make tempVar be the location of the loaded array location
			isField=true;
			Integer arrayVar=tempCounter;
			tempCounter++;
			writeToOutputFirst("HLOAD TEMP "+arrayVar.toString()+" TEMP 0 "+tempVar.toString()+"\n");
			tempVar=arrayVar;
		}
		//Check for null
		writeToOutputFirst("CJUMP LT TEMP "+tempVar.toString()+" 1 L"+labelCounter.toString()+"\n");
		writeToOutputFirst("\tERROR\n");
		writeToOutputFirst("L"+labelCounter.toString()+"\n");
		++labelCounter;
		//Get Array size
		arraySizeTemp=tempCounter;
		tempCounter++;
		writeToOutputFirst("HLOAD TEMP "+arraySizeTemp.toString()+" TEMP "+tempVar.toString()+" 0\n");
		//Get Array address
		arrayLocationTemp=tempCounter;
		tempCounter++;
		writeToOutputFirst("HLOAD TEMP "+arrayLocationTemp.toString()+" TEMP "+tempVar.toString()+" 4\n");
		//Get Array offset
		arrayOffsetTemp=tempCounter;
		tempCounter++;
		writeToOutputFirst("MOVE TEMP "+arrayOffsetTemp.toString()+" ");
		n.f2.accept(this,null);
		writeToOutput("\n");
		//Check offset
		//First for offset less than 0
		writeToOutputFirst("CJUMP LT TEMP "+arrayOffsetTemp.toString()+" 0 L"+labelCounter.toString()+"\n");
		writeToOutputFirst("\tERROR\n");
		writeToOutputFirst("L"+labelCounter.toString()+"\n");
		++labelCounter;
		//Then for offset grater than or equal to size, which translates to offset greater than size-1 
		//which translates to size-1 less than offset
		writeToOutputFirst("CJUMP LT MINUS TEMP "+arraySizeTemp.toString()+" 1 TEMP "+arrayOffsetTemp.toString()+" L"+labelCounter.toString()+"\n");
		writeToOutputFirst("\tERROR\n");
		writeToOutputFirst("L"+labelCounter.toString()+"\n");
		++labelCounter;
		//Make offset a proper offset by multiplying by 4
		//Make array address point to array address+offset. That makes it point to where we want to write
		//Write to the array address
		writeToOutputFirst("HSTORE PLUS TEMP "+arrayLocationTemp.toString()+" TIMES TEMP "+arrayOffsetTemp.toString()+" 4 0 ");
		n.f5.accept(this,null);
		writeToOutput("\n");
		//We no longer need the tempVars, allow them to be used again
		--tempCounter;
		--tempCounter;
		--tempCounter;
		if(isField) {
			--tempCounter;
		}
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
		n.f4.accept(this,null);
		writeToOutput("\n");
		//jump to the end
		writeToOutputFirst("JUMP L"+currLabel.toString()+"ENDIF\n");
		//The write the else part label and the else part
		writeToOutputFirst("L"+currLabel.toString()+"ELSE \n");
		n.f6.accept(this,null);
		writeToOutput("\n");
		//Write the end label
		writeToOutputFirst("L"+currLabel.toString()+"ENDIF\n");
		//Noop to ensure endif label has a valid address
		writeToOutputFirst("NOOP\n");
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
		writeToOutputFirst("L"+currLabel.toString()+"WHILESTART\n");
		//Jump to end if expr false
		writeToOutputFirst("CJUMP ");
		n.f2.accept(this,null);
		writeToOutput(" L"+currLabel.toString()+"WHILEEND\n");
		//What the while loop will execute
		n.f4.accept(this,null);
		//Jump to start to check condition
		writeToOutput("\n");
		writeToOutputFirst("JUMP L"+currLabel.toString()+"WHILESTART\n");
		//End label to jump to on condition false
		writeToOutputFirst("L"+currLabel.toString()+"WHILEEND\n");
		//Noop to ensure endwhile label has a valid address
		writeToOutputFirst("NOOP\n");
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.PrintStatement, java.lang.Object)
	 */
	@Override
	public String visit(PrintStatement n, String argu) throws VisitorException {
		writeToOutputFirst("PRINT ");
		n.f2.accept(this,null);
		writeToOutput("\n");
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
		//Check that array has been allocated
		//Check for null
		writeToOutputFirst("CJUMP LT TEMP "+tempVar.toString()+" 1 L"+labelCounter.toString()+"\n");
		writeToOutputFirst("\tERROR\n");
		writeToOutputFirst("L"+labelCounter.toString()+"\n");
		++labelCounter;
		//Get Array size
		Integer arraySizeTemp=tempCounter;
		tempCounter++;
		writeToOutputFirst("HLOAD TEMP "+arraySizeTemp.toString()+" TEMP "+tempVar.toString()+" 0\n");
		//Get Array address
		Integer arrayLocationTemp=tempCounter;
		tempCounter++;
		writeToOutputFirst("HLOAD TEMP "+arrayLocationTemp.toString()+" TEMP "+tempVar.toString()+" 4\n");
		//Get Array offset
		Integer arrayOffsetTemp=tempCounter;
		tempCounter++;
		writeToOutputFirst("MOVE TEMP "+arrayOffsetTemp.toString()+" ");
		n.f2.accept(this,null);
		writeToOutput("\n");
		//Check offset
		//First for offset less than 0
		writeToOutputFirst("CJUMP LT TEMP "+arrayOffsetTemp.toString()+" 0 L"+labelCounter.toString()+"\n");
		writeToOutputFirst("\tERROR\n");
		writeToOutputFirst("L"+labelCounter.toString()+"\n");
		++labelCounter;
		//Then for offset grater than or equal to size, which translates to offset greater than size-1 
		//which translates to size-1 less than offset
		writeToOutputFirst("CJUMP LT MINUS TEMP "+arraySizeTemp.toString()+" 1 TEMP "+arrayOffsetTemp.toString()+" L"+labelCounter.toString()+"\n");
		writeToOutputFirst("\tERROR\n");
		writeToOutputFirst("L"+labelCounter.toString()+"\n");
		++labelCounter;
		//Make offset a proper offset by multiplying by 4
		//Make array address point to array address+offset. That makes it point to where we want to write
		//Load the requested value into the tempArray
		Integer retTemp=tempCounter;
		tempCounter++;
		writeToOutputFirst("HLOAD TEMP "+retTemp.toString()+" PLUS TEMP "+arrayLocationTemp.toString()+" TIMES TEMP "+arrayOffsetTemp.toString()+" 4 0 ");
		writeToOutput("\n");
		
		//Return the requested value and end
		writeToOutputFirst("RETURN TEMP "+retTemp.toString()+" \n");
		--tabLevel;
		writeToOutputFirst("END \n");
		
		//We no longer need the tempVars, allow them to be used again
		--tempCounter;
		--tempCounter;
		--tempCounter;
		--tempCounter;
		--tempCounter;
		
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
		//Check that array has been allocated
		//Check for null
		writeToOutputFirst("CJUMP LT TEMP "+tempVar.toString()+" 1 L"+labelCounter.toString()+"\n");
		writeToOutputFirst("\tERROR\n");
		writeToOutputFirst("L"+labelCounter.toString()+"\n");
		++labelCounter;
		//Get Array size
		Integer arraySizeTemp=tempCounter;
		tempCounter++;
		writeToOutputFirst("HLOAD TEMP "+arraySizeTemp.toString()+" TEMP "+tempVar.toString()+" 0\n");
		//Return the requested value and end
		writeToOutputFirst("RETURN TEMP "+arraySizeTemp.toString()+" \n");
		--tabLevel;
		writeToOutputFirst("END \n");
		
		//We no longer need the tempVars, allow them to be used again
		--tempCounter;
		--tempCounter;
		return null;
	}

	/* (non-Javadoc)
	 * @see visitor.GJVisitor#visit(syntaxtree.MessageSend, java.lang.Object)
	 */
	@Override
	public String visit(MessageSend n, String argu) throws VisitorException {
		// TODO Finish this
		//Get the location of the class
		Integer tempVar=tempCounter;
		++tempCounter;
		writeToOutputFirst("MOVE TEMP "+tempVar.toString()+" ");
		n.f0.accept(this,null);
		//Check for null
		writeToOutputFirst("CJUMP LT TEMP "+tempVar.toString()+" 1 L"+labelCounter.toString()+"\n");
		writeToOutputFirst("\tERROR\n");
		writeToOutputFirst("L"+labelCounter.toString()+"\n");
		++labelCounter;
		//Use the other visitor to determine object type and place in vTable
		MiniJavaClass callToClass = Classes.get(n.f0.accept(typeRetriever,null));
		MiniJavaMethod callToMethod = callToClass.Methods.get(n.f2.f0.tokenImage);
		Integer placeInVTable = callToMethod.position;
		//Get vTable
		Integer vTableTemp=tempCounter;
		++tempCounter;
		
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
