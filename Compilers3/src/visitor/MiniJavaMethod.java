package visitor;

import java.util.Iterator;
import java.util.Map.Entry;

public class MiniJavaMethod extends Context {
	String returnType;
	String name;
	MiniJavaBody body;
	Integer position; //TODO set this to its position in the vTable, contructor sarches for similar key in father, if found puts same value, if not found takes this class' counter and increments it
	
	public MiniJavaMethod(String retType,MiniJavaClass cParent,String cName) {
		super(cParent);
		name=cName;
		returnType=retType;
		for(MiniJavaClass it = cParent.extended;it!=null;it= it.extended) {
			if(it.Methods.containsKey(cName)) {
				position=it.Methods.get(cName).position;
				return;
			}
		}
		//Else, if it wasn't found in the fathers
		position=cParent.methodCounter;
		cParent.methodCounter=cParent.methodCounter+1;
	}
	
	public String pigletMethodName() {
		if(name.equals("main")) {
			return "MAIN";
		}
		else {
			return ((MiniJavaClass)(this.cparent)).name+"_"+this.name;
		}
	}
	
	/**
	 * 
	 * @return Returns the number of arguments this one has, including temp 0
	 */
	public Integer getArgNum() {
		return this.Vars.size()+1; //Plus one because of "this"
	}
	
	/**
	 * 
	 * @param arg is the name of the argument variable we're searching for in the arguments
	 * @return the temp number of this argument variable so you can find it via TEMP X, returns -1 if not found
	 */
	public Integer getArgNum(String arg) {
		Integer retInt=1;
		Entry<String, String> tempEntry;
		for(Iterator<Entry<String, String>> it = Vars.entrySet().iterator();it.hasNext();++retInt) {
			tempEntry = it.next();
			if(tempEntry.getKey().equals(arg)) {
				return retInt;
			}
		}
		return -1;
	}
}
