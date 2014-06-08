import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import syntaxtree.Goal;
import visitor.TypeCheckVisitor;
import visitor.TypeDeclVisitor;
import visitor.VisitorException;

/**
 * 
 */

/**
 * @author Parisbre56
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for(String fileName : args) {
			System.out.println("Opening file: "+fileName);
			FileInputStream inputFile;
			try {
				inputFile = new FileInputStream(fileName);
			} catch (FileNotFoundException e) {
				System.err.println("ERROR: Unable to open file: "+fileName);
				System.err.println("ERROR: File does not exist.");
				continue;
			}
			
			MiniJavaParser p = new MiniJavaParser(inputFile);

			TypeDeclVisitor vd = new TypeDeclVisitor();
			
			Goal root;
			try {
				root = p.Goal();
			} catch (ParseException e1) {
				System.err.println("ERROR: Unable to parse file: "+fileName);
				System.err.println("ERROR: Stack trace follows:");
				e1.printStackTrace();
				continue;
			}
			
			try {
				root.accept(vd);
			}
			catch (VisitorException e) {
				System.err.println("ERROR: Exception during first pass in file: "+fileName+"\nERROR: Stack trace follows:");
				e.printStackTrace();
				continue;
			}
			
			TypeCheckVisitor vt;
			
			try {
				vt = new TypeCheckVisitor(vd);
				root.accept(vt,null);
			}
			catch (VisitorException e) {
				System.err.println("ERROR: Exception during second pass in file: "+fileName+"\nERROR: Stack trace follows:");
				e.printStackTrace();
				continue;
			}
			
			try {
				inputFile.close();
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			
			System.out.println("Succesfuly processed file: "+fileName);
		}
	}

}
