/**
 * 
 */
package visitor;

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

}
