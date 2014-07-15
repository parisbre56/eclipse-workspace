import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map.Entry;

import syntaxtree.Goal;
import syntaxtree.Label;
import syntaxtree.SimpleExp;
import visitor.BlockCreatorVisitor;
import visitor.CodeBlockSpiglet;
import visitor.ProcedureData;
import visitor.ReturnExpFinder;
import visitor.StatementFinder;
import visitor.VariableData;
import visitor.VariableData.RegisterAs;

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
				System.err.println("Skipping file: "+fileName);
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
			
			PrintWriter writer;
			try {
				writer = new PrintWriter(outFileName, "UTF-8");
			} catch (FileNotFoundException | UnsupportedEncodingException e1) {
				System.err.println("ERROR: Unable to open file:"+outFileName);
				e1.printStackTrace();
				continue;
			}
			
			BlockCreatorVisitor bcv = new BlockCreatorVisitor();
			
			//Create blocks
			root.accept(bcv);			
			
			//Make all blocks hold their live vars
			findLiveness(bcv.procData);
			
			//Assign registers or stack space to live vars
			assignRegisters(bcv.procData);
			
			//Translate to kanga and write output
			kangaTranslation(bcv.procData,writer);
			
			writer.close();
			
			System.out.println("\tSuccesfuly processed file: "+fileName+"\n\tOutput written in: "+outFileName);
			
		}

	}

	/**
	 * Translates the blocks of each procedure to kanga and writes the output to the writer
	 * @param procData Contains the procedures we need to translate
	 * @param writer Output will be written here
	 */
	private static void kangaTranslation(
			LinkedHashMap<String, ProcedureData> procData, PrintWriter writer) {
		//For each procedure
		for(Entry<String, ProcedureData> procEntry : procData.entrySet()){
			ProcedureData proc = procEntry.getValue();
			EnumSet<RegisterAs> toStore=proc.usedSxRegisters();
			//Label "[" ArgNum "]" "[" StackUse "]" "[" MaxCalledVars "]"
			writer.println(proc.name+" ["+proc.args+"] ["+(proc.stack+toStore.size())+"] ["+proc.maxCalledVars+"] ");
			//Save sigma registers we are going to be using
			Integer stackPos=proc.stack;
			for(RegisterAs regS : toStore) {
				writer.println("\tASTORE SPILLEDARG "+stackPos+" "+regS.toString()+" ");
				++stackPos;
			}
			//For each var used as argument, pass it to its assigned var
			for(Integer i = 0;i<proc.args;++i) {
				//If this var is not in the set of vars for this procedure
				//(probably because it is not used)
				//simply ignore it
				if(!proc.varData.containsKey(i.toString())) {
					continue;
				}
				VariableData var = proc.varData.get(i.toString());
				
				if(var.regAs==RegisterAs.NotYetAssigned) {
					//If this var has not been assigned a register
					//then it is never used and we should ignore it
					continue;
				}
				
				if(i<4) {
					RegisterAs argReg = RegisterAs.values()[RegisterAs.a0.ordinal()+i];
					if(var.regAs==RegisterAs.Stack) {
						//Take it from the register and put it in the appropriate stack position
						writer.println("\tASTORE SPILLEDARG "+var.stackPos+" "+argReg.toString()+" ");
					}
					else {
						//Take it from the register and put it in the appropriate register
						writer.println("\tMOVE "+var.regAs.toString()+" "+argReg.toString()+" ");
					}
				}
				else {
					if(var.regAs==RegisterAs.Stack) {
						//Take it from the stack and put it in the appropriate stack position
						writer.println("\tALOAD "+RegisterAs.v0.toString()+" SPILLEDARG "+i+" ");
						writer.println("\tASTORE SPILLEDARG "+var.stackPos+" "+RegisterAs.v0.toString()+" ");
					}
					else {
						//Take it from the stack and put it in the appropriate register
						writer.println("\tALOAD "+var.regAs.toString()+" SPILLEDARG "+i+" ");
					}
				}
			}
			//For each block of this procedure
			for(CodeBlockSpiglet blk : proc.blockData) {
				switch(blk.stType) {
				//Just write the label in the beginning of the line
				case Label:
					Label lbl = (Label) blk.statement;
					writer.println(lbl.nodeToken.tokenImage+" ");
					break;
				//RETURN SimpleExp
				//SimpleExp =  	Temp||IntegerLiteral||Label
				case Return:
					SimpleExp smplExp = (SimpleExp) blk.statement;
					//The simple expression finder will print the appropriate data
					smplExp.accept(new ReturnExpFinder(blk,writer));
					break;
				default:
					if(blk.stType==null) {
						System.err.println("ERROR: Null value in blockType enumeration.");
						System.exit(1);
					}
					//Thr statement writer will process all other statements
					blk.statement.accept(new StatementFinder(blk,writer));
					break;
				}
			}
			
			//Restore sigma registers we have used to their original value
			stackPos=proc.stack;
			for(RegisterAs regS : toStore) {
				writer.println("\tALOAD "+regS.toString()+" SPILLEDARG "+stackPos+" ");
				++stackPos;
			}
			
			writer.println("END \\\\End of function "+proc.name+"\n");
		}
	}

	/**
	 * Assigns registers for all the procedures in procData
	 * @param procData The procedures for which we need to assign the registers
	 */
	private static void assignRegisters(
			LinkedHashMap<String, ProcedureData> procData) {
		//For each procedure
		for(Entry<String, ProcedureData> procEntry : procData.entrySet()){
			ProcedureData proc = procEntry.getValue();
			//Assign registers to vars
			while(!proc.AssignVarsToRegisters()) {
				
			}
		}
	}

	/**
	 * 
	 * @param procData The list of procedures for which we need to find the liveness of their variables
	 */
	private static void findLiveness(LinkedHashMap<String, ProcedureData> procData) {
		//For each procedure
		for(Entry<String, ProcedureData> procEntry : procData.entrySet()){
			ProcedureData proc = procEntry.getValue();
			Boolean changeOccurred=true;
			//Begin fixpoint (keep looping until no more changes occur)
			while(changeOccurred) {
				changeOccurred=false;
				//For each codeBlock (from the end)
				CodeBlockSpiglet blk;
				for(ListIterator<CodeBlockSpiglet> itBlk = proc.blockData.listIterator(proc.blockData.size())
						;itBlk.hasPrevious();) {
					blk = itBlk.previous();
					//Update its liveness
					if(blk.updateLiveness()) {
						changeOccurred=true;
					}
				}
			}
		}
		
	}

}
