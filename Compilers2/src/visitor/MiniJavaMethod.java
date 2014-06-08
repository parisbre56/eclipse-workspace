package visitor;

public class MiniJavaMethod extends Context {
	String returnType;
	String name;
	MiniJavaBody body;
	
	public MiniJavaMethod(String retType,MiniJavaClass cParent,String cName) {
		super(cParent);
		name=cName;
		returnType=retType;
	}
}
