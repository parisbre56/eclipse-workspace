/**
 * 
 */
package visitor;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * @author Parisbre56
 *
 */
public class MiniJavaClass extends Context {
	String name;
	MiniJavaClass extended;
	LinkedHashMap<String,MiniJavaMethod> Methods = new LinkedHashMap<String,MiniJavaMethod>();
	//Points to the first available method number
	Integer methodCounter=0;
	
	public MiniJavaClass(MiniJavaClass cExtends, String cName) {
		super(cExtends);
		extended = cExtends;
		name=cName;
		if(cExtends!=null) {
			methodCounter=cExtends.methodCounter;
		}
	}
	
	/**
	 * 
	 * @return The number of fields in the classes this one extends.
	 */
	public Integer fieldsInExtended() {
		Integer retVal=0;
		for(MiniJavaClass tempClass = extended;tempClass!=null;tempClass=tempClass.extended) {
			retVal+=tempClass.Vars.hashCode();
		}
		return retVal;
	}
	
	public Integer instanceSizeRequirment() {
		if(name.equals("int")) {
			return -1; //Means this is a simple TEMP var
		}
		else if (name.equals("boolean")) {
			return -2; //Means this is a simple TEMP var
		}
		else if (name.equals("int[]")) {
			return 2*4;
		}
		else {
			return (this.fieldsInExtended()+this.Vars.size()+1)*4; //+1 for the vTable
		}
	}
	
	public Integer vTableSizeRequirement() {
		return this.Methods.size()*4;
	}
	
	/** 
	 * 
	 * @param arg is the field we're searching for either in this class' fields or in the classes it extends
	 * @return the argument number * 4 so that it can be found via<br>
	 * HLOAD {TEMP X to store the result} {ADRESS OF CLASS} getArgNum({ARG NAME}) or<br>
	 * HSTORE {ADRESS OF CLASS} getArgNum({ARG NAME}) {WHAT WE WANT TO STORE IN IT}
	 */
	public Integer getArgNum(String arg) {
		for(MiniJavaClass currentClass=this;currentClass!=null;currentClass=currentClass.extended) {
			Integer retInt=currentClass.fieldsInExtended()+1;//+1 for vTable
			Entry<String, String> tempEntry;
			for(Iterator<Entry<String, String>> it = Vars.entrySet().iterator();it.hasNext();++retInt) {
				tempEntry = it.next();
				if(tempEntry.getKey().equals(arg)) {
					return retInt*4;
				}
			}
		}
		return -1;
	}
	
	/** 
	 * 
	 * @return the name of the function used to construct instances of this class<br>
	 * This is usually {Class name}Constructor_<br>
	 * Classes that have no constructor (int and bool) return null<br>
	 * int[] returns the special constructor intConstructor_
	 */
	public String constructorFunctionName() {
		//Check for classes that should not have a constructor (int, bool)
		if(name.equals("int")||name.equals("boolean")) {
			return null;
		}
		//Check for special case int[]
		if(name.equals("int[]")) {
			return "intConstructor_";
		}
		return name+"Constructor_";
	}
}
