/**
 * 
 */
package visitor;

import java.util.LinkedHashMap;

/**
 * @author Parisbre56
 *
 */
public class MiniJavaClass extends Context {
	String name;
	MiniJavaClass extended;
	LinkedHashMap<String,MiniJavaMethod> Methods = new LinkedHashMap<String,MiniJavaMethod>();
	
	public MiniJavaClass(MiniJavaClass cExtends, String cName) {
		super(cExtends);
		extended = cExtends;
		name=cName;
	}
}
