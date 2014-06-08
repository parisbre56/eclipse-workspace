package visitor;

import java.util.LinkedHashMap;

public class Context {
	Context cparent;
	LinkedHashMap<String,String> Vars = new LinkedHashMap<String,String>();
	
	public Context(Context cParent) {
		cparent = cParent;
	}
}
