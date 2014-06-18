package visitor;

import java.util.Iterator;
import java.util.Map.Entry;

public class MiniJavaMethod extends Context {
	static Integer nextUniqueID = 0;
	/** Used to differentiate methods that might end up having the same name by ensuring they all have
	 *  a unique postfix
	 */
	Integer uniqueMethodID;
	String returnType;
	String name;
	MiniJavaBody body;
	/** Set this to its position in the vTable, child method contructor sarches for similar key in this class' fathers, 
	 *  if found puts same value, if not found takes this class' counter and increments it
	 */
	Integer position;
	
	public MiniJavaMethod(String retType,MiniJavaClass cParent,String cName) {
		super(cParent);
		name=cName;
		returnType=retType;
		uniqueMethodID=nextUniqueID;
		++nextUniqueID;
		
		
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
	
	/** 
	 * 
	 * @return The label this method will be given when translated to a piglet function.<br>
	 * It is in the form of {CLASS NAME}_{METHOD NAME}_{UNIQUEMETHODID}<br>
	 * Unique method Id ensures that there won't be any functions with the same name
	 */
	public String pigletMethodName() {
		return ((MiniJavaClass)(this.cparent)).name+"_"+this.name+"_"+uniqueMethodID.toString();
	}
	
	/**
	 * 
	 * @return Returns the number of arguments this one has, including the this argument (temp 0)
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

	/** 
	 * 
	 * @return (This method's position in the vTable) * 4
	 */
	public Integer getPosition() {
		return this.position*4;
	}
}
