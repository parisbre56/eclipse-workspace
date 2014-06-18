import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import syntaxtree.Goal;
import visitor.PigletTranslationVisitor;
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
			//Check for non-java file
			if(!fileName.endsWith(".java")) {
				System.err.println("Skipping file "+fileName);
				continue;
			}
			System.out.println("Opening file: "+fileName);

			FileInputStream inputFileStream;
			try {
				inputFileStream = new FileInputStream(fileName);
			} catch (FileNotFoundException e) {
				System.err.println("ERROR: Unable to open file: "+fileName);
				System.err.println("ERROR: File does not exist.");
				continue;
			}
			
			MiniJavaParser p = new MiniJavaParser(inputFileStream);

			TypeDeclVisitor vd = new TypeDeclVisitor();
			
			Goal root;
			try {
				root = p.Goal();
			} catch (ParseException e1) {
				System.err.println("ERROR: Unable to parse file: "+fileName);
				System.err.println("ERROR: Stack trace follows:");
				e1.printStackTrace();
				try {
					inputFileStream.close();
				} catch (IOException e3) {
					System.err.println("ERROR: Unable to close file: "+fileName);
					e3.printStackTrace();
					continue;
				}
				continue;
			}
			
			try {
				inputFileStream.close();
			} catch (IOException e3) {
				System.err.println("ERROR: Unable to close file: "+fileName);
				e3.printStackTrace();
				continue;
			}
			
			try {
				root.accept(vd);
			}
			catch (VisitorException e) {
				System.err.println("ERROR: Exception during first pass (variable declaration) in file: "+fileName+"\nERROR: Stack trace follows:");
				e.printStackTrace();
				continue;
			}
			
			TypeCheckVisitor vt;
			
			try {
				vt = new TypeCheckVisitor(vd);
				root.accept(vt,null);
			}
			catch (VisitorException e) {
				System.err.println("ERROR: Exception during second pass (type checking) in file: "+fileName+"\nERROR: Stack trace follows:");
				e.printStackTrace();
				continue;
			}
			
			String outFileName;
			outFileName=fileName.substring(0, fileName.lastIndexOf('.'))+".pg";
			
			PrintWriter writer;
			try {
				writer = new PrintWriter(outFileName, "UTF-8");
			} catch (FileNotFoundException | UnsupportedEncodingException e1) {
				e1.printStackTrace();
				continue;
			}
			
			PigletTranslationVisitor ptv;
			
			try {
				ptv = new PigletTranslationVisitor(vt,writer);
				root.accept(ptv,null);
			} catch (VisitorException e) {
				System.err.println("ERROR: Exception during third pass (piglet translation) in file: "+fileName+"\nERROR: Stack trace follows:");
				e.printStackTrace();
				continue;
			}
			
			writer.close();
			
			System.out.println("Succesfuly processed file: "+fileName+"\n Output written in: "+outFileName);
			
		}
	}

}
