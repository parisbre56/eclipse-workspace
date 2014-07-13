import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;

import syntaxtree.Goal;
import visitor.BlockCreatorVisitor;
import visitor.ProcedureData;

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
			//Check for non-spiglet file
			if(!fileName.endsWith(".spg")) {
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
			
			SpigletParser p = new SpigletParser(inputFileStream);
			
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
			
			String outFileName;
			outFileName=fileName.substring(0, fileName.lastIndexOf('.'))+".kg";
			
			BlockCreatorVisitor bcv = new BlockCreatorVisitor();
			
			root.accept(bcv);			
			
			findLiveness(bcv.procData);
			
			System.out.println("\tSuccesfuly processed file: "+fileName+"\n\tOutput written in: "+outFileName);
			
		}

	}

	/**
	 * 
	 * @param procData The list of procedures for which we need to find the liveness of their variables
	 */
	private static void findLiveness(LinkedHashMap<String, ProcedureData> procData) {
		// TODO Auto-generated method stub
		
	}

}
