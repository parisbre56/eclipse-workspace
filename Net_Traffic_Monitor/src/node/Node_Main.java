/**
 * 
 */
package node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import node.threads.DataRetrieverThread;
import node.threads.DataSenderThread;
import node.threads.IdentificationThread;
import node.threads.InterfaceCheckerThread;
import node.threads.ShutdownHookThread;
import exceptions.NTMonException;
import exceptions.NTMonIAddrException;

/**
 * REMEMBER! ALL INTERVALS ARE IN SECONDS!
 * @author parisbre56
 *
 */
public class Node_Main {
	//Shared data
	static public ConfigClass configClass = null;
	
	//Shared data for the identification thread
	static public AtomicBoolean identificationReady = null;
	static public AtomicBoolean identificationFailed = null;
	static public Exception identificationFailedReason = null;
	
	//Shared data for the data sender thread
	public static AtomicBoolean senderReady = null;
	public static AtomicBoolean senderFailed = null;
	static public Exception senderFailedReason = null;
	
	//Shared exit data
	static public AtomicBoolean exiting = null;
	
	//Shared thread data
	/** Keeps a list of all the threads that ever ran in the system.<br>
	 * Threads should try to remove themselves at exit.<br>
	 * Periodic checks should be made if possible to ensure that any dead threads are removed to save memory.
	 */
	static public CopyOnWriteArrayList<Thread> threads = null;
	
	//Shared connection data
	/** Remember that all uses of this object should be synchronized. <br>
	 * Remember to send a refresh connection request before each communication attempt
	 * to ensure that the server knows with which client it is talking to.
	 */
	public static Socket accumulatorConnection = null;
	public static DataOutputStream os = null;
	public static DataInputStream is = null;
	
	//Shared arguments
	static public List<String> argList = null;
	
	//Shared config file location
	public static File configFile = null;

	//(Statistic) Malicious Pattern Shared Memory
	public static SharedMemory sharedMemory = null;
	
	//Definitions
	public static final Integer defaultTimeout = 50;
	public static final int maxRetries = 3;
	

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
		
		senderReady = new AtomicBoolean(false);
		senderFailed = new AtomicBoolean(false);
		
		exiting = new AtomicBoolean(false);
		
		threads = new CopyOnWriteArrayList<Thread>();
		
		// TODO Set up any data necessary
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//Start checking arguments and load configuration file
		useArgsForConfig(args);
		
		//Add shutdown hook that waits for all operations to finish
		Runtime.getRuntime().addShutdownHook(new Thread(null, new ShutdownHookThread(), "Shutdown Hook Thread"));
		
		//Start the identification thread.
		threads.add(new Thread(null, new IdentificationThread(), "Identification Thread"));
		threads.get(threads.size()-1).run();
		
		// Wait for the all clear from the identification thread.
		while(identificationReady.get()==false) {
			synchronized(identificationReady) {
				try {
					identificationReady.wait(defaultTimeout*1000);
				} catch (InterruptedException e) {
					System.err.println("DEBUG: Interrupted while waiting for the identification thread.");
					e.printStackTrace();
				}
				if(identificationFailed.get()==true) {
					System.err.println("ERROR: Identification thread failed. Exiting...");
					exiting.set(true);
					exiting.notifyAll();
					return;
				}
			}
		}
		
		//Start the data sender thread
		threads.add(new Thread(null, new DataSenderThread(), "Data Sender Thread"));
		threads.get(threads.size()-1).run();
		
		//Wait for the all clear from the data sender thread
		while(senderReady.get()==false) {
			synchronized(senderReady) {
				try {
					senderReady.wait(defaultTimeout*1000);
				} catch (InterruptedException e) {
					System.err.println("DEBUG: Interrupted while waiting for the identification thread.");
					e.printStackTrace();
				}
				if(senderFailed.get()==true) {
					System.err.println("ERROR: Identification thread failed. Exiting...");
					exiting.set(true);
					exiting.notifyAll();
					return;
				}
			}
		}
		
		//Start the data retriever thread
		threads.add(new Thread(null, new DataRetrieverThread(), "Data Retriever Thread"));
		threads.get(threads.size()-1).run();
		
		//Start the interface checker thread.
		threads.add(new Thread(null, new InterfaceCheckerThread(), "Interface Checker Thread"));
		threads.get(threads.size()-1).run();
		
		//Add self to thread list
		threads.add(Thread.currentThread());
		
		// TODO Make this periodically check that all threads are running normally?
		
		Node_Main.threads.remove(Thread.currentThread());
		synchronized(Node_Main.threads) {
			Node_Main.threads.notifyAll();
		}
		return;
	}

	/**
	 * Sets the configuration according to the arguments and the configuration file
	 * @param args
	 */
	private static void useArgsForConfig(String[] args) {
		argList = Arrays.asList(args);
		
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

	public static void refreshConnectionRequest() {
		//TODO refresh connection
		//Handle connection closed
		
	}
	
	

}
