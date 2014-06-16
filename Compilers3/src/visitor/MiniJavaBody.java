/**
 * 
 */
package visitor;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * @author Parisbre56
 *
 */
public class MiniJavaBody extends Context {
	MiniJavaMethod parent; 
	
	/**
	 * @param cParent
	 */
	public MiniJavaBody(MiniJavaMethod cParent) {
		super(cParent);
		parent=cParent;
		cParent.body=this;
	}
	
	/** 
	 * 
	 * @return Returns the first temp variable that is available
	 */
	public Integer getTempNum() {
		 return parent.getArgNum()+Vars.size();
	}
	
	/** 
	 * 
	 * @param arg the variable we're searching for, either in the method body or the arguments
	 * @return the temp number of that variable so that you can find it via TEMP X, returns -1 if not found
	 */
	public Integer getArgNum(String arg) {
		Integer retInt=parent.getArgNum();
		Entry<String, String> tempEntry;
		if(arg.equals("this")) {
			return 0;
		}
		for(Iterator<Entry<String, String>> it = Vars.entrySet().iterator();it.hasNext();++retInt) {
			tempEntry = it.next();
			if(tempEntry.getKey().equals(arg)) {
				return retInt;
			}
		}
		return parent.getArgNum(arg);
	}
}
