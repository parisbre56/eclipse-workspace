/**
 * 
 */
package node_main;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import exceptions.NTMonException;
import exceptions.NTMonIAddrException;

/**
 * REMEMBER! ALL INTERVALS ARE IN SECONDS!
 * @author parisbre56
 *
 */
public class Node_Main {
	static public ConfigClass configClass = null;
	
	static public AtomicBoolean identificationReady = null;
	static public AtomicBoolean identificationFailed = null;
	Exception identificationFailedReason = null;
	
	
	//Definitions
	private static final Integer defaultTimeout = 50;
	

	/**
	 * @throws NTMonException 
	 * 
	 */
	public Node_Main() throws NTMonException {
		//Create config class with default data
		configClass = new ConfigClass();
		
		//Create notification variables
		identificationReady = new AtomicBoolean(false);
		identificationFailed = new AtomicBoolean(false);
		
		// TODO Set up any data necessary
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//Start checking arguments and load configuration file
		useArgsForConfig(args);
		
		// TODO Check/collect data
		
		// Wait for the all clear from the identification thread.
		
		synchronized(identificationReady) {
			while(identificationReady.get()==false) {
				try {
					identificationReady.wait(defaultTimeout);
				} catch (InterruptedException e) {
					System.err.println("DEBUG: Interrupted while waiting for the identification thread.");
					e.printStackTrace();
				}
				if(identificationFailed.get()==true) {
					System.err.println("ERROR: Identification thread failed.");
					return;
				}
			}
		}
		
		// TODO Start data retriever thread once the above has started successfully 
		//	and make sure it stays running.
		// Have it retrieve the malicious IPs and strings periodically
		
		// TODO Start data sender thread once the above has started successfully and make sure it 
		//	sends statistics to the accumulator periodically.
		
		// TODO Start interface checker thread and make sure it stays running
		// Also make sure that it adds the necessary data for statistics
		// Also make sure that it removes that data if the interface is removed
		// Also make sure it starts a thread that monitors traffic for malicious stuff and updates the statistics
		//Make it into a separate class? Runnable?
		
		// TODO Make it periodically check that all threads are running normally?
		
	}

	/**
	 * Sets the configuration according to the arguments and the configuration file
	 * @param args
	 */
	private static void useArgsForConfig(String[] args) {
		List<String> argList = Arrays.asList(args);
		
		//Check for accumulator address flag
		String accumulatorAddress = null;
		if(argList.contains("-aa")) {
			try {
				accumulatorAddress = argList.get(argList.indexOf("-aa")+1);
			}
			catch (IndexOutOfBoundsException e) {
				System.err.println("ERROR: Expected accumulator address after '-aa' flag. "
						+ "The default will be used instead.");
			}
		}
		
		//Check for accumulator port flag
		String accumulatorPort = null;
		if(argList.contains("-ap")) {
			try {
				accumulatorPort = argList.get(argList.indexOf("-ap")+1);
			}
			catch (IndexOutOfBoundsException e) {
				System.err.println("ERROR: Expected accumulator port after '-ap' flag. "
						+ "The default will be used instead.");
			}
		}
		
		//If there is a no-config flag set, then stick with the defaults and ignore any config flags
		if(!argList.contains("-nc")) {
			
			//Check arguments for config file arguments.
			String configFileString=".config";
			if(argList.contains("-c")) {
				try {
					configFileString=argList.get(argList.indexOf("-c")+1);
				}
				catch (IndexOutOfBoundsException e) {
					System.err.println("ERROR: Expected config file location after '-c' flag. "
							+ "The default will be used instead.");
					configFileString=".config";
				}
			}
			
			//Check if config file exists and if not create it.
			File configFile = new File(configFileString);
			if(!configFile.exists()) {
				System.err.println("DEBUG: Config file \'"+configFileString+"\' does not exist, "
						+ "it will be created with the default settings.");
				//Set any data from flags
				setDataFromFlags(accumulatorAddress,accumulatorPort);
				configClass.createConfigFile(configFile);
			}
			//Else, load data from it
			else {
				System.err.println("DEBUG: Reading configuration data from "+configFileString);
				configClass.loadConfigFile(configFile);
				//Set any data from flags
				setDataFromFlags(accumulatorAddress,accumulatorPort);
			}
		}
		else {
			setDataFromFlags(accumulatorAddress,accumulatorPort);
		}
		
		// TODO Check arguments
	}

	/**
	 * Changes the configuration according to the flags set
	 * @param accumulatorAddress
	 */
	private static void setDataFromFlags(String accumulatorAddress, String accumulatorPort) {
		if(accumulatorAddress!=null) {
			try {
				configClass.setAccumulatorAddress(accumulatorAddress);
			}
			catch (NTMonIAddrException e) {
				System.err.println("ERROR: Unable to find an IP for host "+accumulatorAddress+" "
						+ "The default will be used instead.");
			}
		}
		
		if(accumulatorPort!=null) {
			try {
				Integer tempPort = Integer.decode(accumulatorPort);
				if (tempPort<0 || tempPort>65535) {
					System.err.println("ERROR: "+tempPort+" is not a valid port number. "
							+ "The default will be used instead.");
				}
				else {
					configClass.setAccumulatorPort(tempPort);
				}
			}
			catch (NumberFormatException e) {
				System.err.println("ERROR: \'"+accumulatorPort+"\' is not a valid port number.");
			}
		}
		
		// TODO Set config data
	}

}
