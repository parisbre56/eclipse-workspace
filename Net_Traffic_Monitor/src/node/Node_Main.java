/**
 * 
 */
package node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import shared_data.StatusCode;
import node.sharedMemory.Node_SharedMemory;
import node.threads.DataRetrieverThread;
import node.threads.DataSenderThread;
import node.threads.IdentificationThread;
import node.threads.InterfaceCheckerThread;
import node.threads.Node_ShutdownHookThread;
import exceptions.NTMonIAddrException;
import exceptions.NTMonUnableToRefreshException;

/** Starts the main threads and handles command line arguments.<br> 
 * Also holds a static method for refreshing the connection to the server.
 * REMEMBER! ALL INTERVALS ARE IN SECONDS!
 * @author parisbre56
 *
 */
public class Node_Main {
	//Shared data
	/** Holds all configuration info set either via flags or via the config file
	 */
	static public Node_ConfigClass node_ConfigClass = null;
	
	//Shared data for the identification thread
	/** Set to true and signaled with notify 
	 * to indicate that the identification thread has finished connecting to the server.
	 */
	static public AtomicBoolean identificationReady = null;
	/** Set to true and signaled with notify
	 * to indicate that the identification thread failed with no chance of recovery.
	 */
	static public AtomicBoolean identificationFailed = null;
	/** The reason the identification thread failed if possible, else null
	 */
	static public Exception identificationFailedReason = null;
	
	//Shared data for the data sender thread
	/** Set to true and signaled with notify 
	 * to indicate that the data sender thread has finished setting up the shared memory
	 */
	public static AtomicBoolean senderReady = null;
	/** Set to true to indicate that the sender thread failed with no chance of recovery
	 */
	public static AtomicBoolean senderFailed = null;
	/** The reason the sender failed if possible, else null
	 */
	static public Exception senderFailedReason = null;
	
	//Shared exit data
	/** Indicates that the program is in the process of exiting.
	 */
	static public AtomicBoolean exiting = null;
	/** Indicates that the exiting variable has been set due to a fatal exception.
	 */
	private static AtomicBoolean exitingException = null;
	
	//Shared thread data
	/** Keeps a list of all the threads that ever ran in the system.<br>
	 * Threads should try to remove themselves at exit.<br>
	 */
	static public CopyOnWriteArrayList<Thread> threads = null;
	
	//Shared connection data
	/** The connection to the server.<br>
	 * Remember that all uses of this object should be synchronized. <br>
	 * Remember to send a refresh connection request before each communication attempt
	 * to ensure that the server knows with which client it is talking to.
	 */
	public static Socket accumulatorConnection = null;
	/** Output stream for the server.<br> Make sure to synchronize on accumulatorConnection.
	 */
	public static DataOutputStream os = null;
	/** Input stream for the server.<br> Make sure to synchronize on accumulatorConnection.
	 */
	public static DataInputStream is = null;
	
	//Shared arguments
	/** The arguments of this program given as a list
	 */
	static public List<String> argList = null;
	
	//Shared config file location
	/** The configuration file location
	 */
	public static File configFile = null;

	//(Statistic) Malicious Pattern Shared Memory
	/** (Statistic) Malicious Pattern Shared Memory, set up by the sender thread
	 */
	public static Node_SharedMemory node_SharedMemory = null;
	
	//Definitions
	/** How many seconds a wait on a synchronized object will last
	 * before the condition is checked manually
	 */
	public static final int defaultTimeout = 50;
	/** Default number of retries for some I/O and other operations.
	 */
	//public static final int maxRetries = 3;
	

	/**
	 * 
	 */
	public Node_Main() {
		//Create config class with default data
		node_ConfigClass = new Node_ConfigClass();
		
		//Create notification variables
		identificationReady = new AtomicBoolean(false);
		identificationFailed = new AtomicBoolean(false);
		
		senderReady = new AtomicBoolean(false);
		senderFailed = new AtomicBoolean(false);
		
		exiting = new AtomicBoolean(false);
		exitingException = new AtomicBoolean(false);
		
		threads = new CopyOnWriteArrayList<>();
		
		// TODO Set up any data necessary
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//Start checking arguments and load configuration file
		useArgsForConfig(args);
		
		//Add shutdown hook that waits for all operations to finish
		Runtime.getRuntime().addShutdownHook(new Thread(null, new Node_ShutdownHookThread(), "Shutdown Hook Thread"));
		
		//Start the identification thread.
		threads.add(new Thread(null, new IdentificationThread(), "Identification Thread"));
		threads.get(threads.size()-1).start();
		
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
		threads.get(threads.size()-1).start();
		
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
		threads.get(threads.size()-1).start();
		
		//Start the interface checker thread.
		threads.add(new Thread(null, new InterfaceCheckerThread(), "Interface Checker Thread"));
		threads.get(threads.size()-1).start();
		
		//Add self to thread list
		threads.add(Thread.currentThread());
		
		// TODO Make this periodically check that all threads are running normally?
		
		//Before exiting, write the configuration file if the user asked for it, including any changes made
		if(Node_Main.argList.contains("-wc")) {
			try {
				Node_Main.node_ConfigClass.createConfigFile(Node_Main.configFile);
			} catch (IOException e) {
				System.err.println("ERROR: Unable to save config file to "+Node_Main.configFile.getAbsolutePath());
				e.printStackTrace();
			}
		}
		
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
			String configFileString="Client.config";
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
			File configFile1 = new File(configFileString);
			if(!configFile1.exists()) {
				System.err.println("DEBUG: Config file \'"+configFileString+"\' does not exist, "
						+ "it will be created with the default settings.");
				//Set any data from flags
				setDataFromFlags(accumulatorAddress,accumulatorPort);
				try {
					node_ConfigClass.createConfigFile(configFile1);
				} catch (IOException e) {
					System.err.println("ERROR: Unable to write config file "+configFile1.getAbsolutePath());
					e.printStackTrace();
				}
			}
			//Else, load data from it
			else {
				System.err.println("DEBUG: Reading configuration data from "+configFileString);
				try {
					node_ConfigClass.loadConfigFile(configFile1);
				} catch (FileNotFoundException e) {
					//Should never happen, since we have checked that the file exists.
					System.err.println("ERROR: The configuration file provided does not exist.");
					e.printStackTrace();
				} catch (IOException e) {
					System.err.println("ERROR: Error while reading from the configuration file. The defaults will be used instead.");
					e.printStackTrace();
				}
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
				node_ConfigClass.setAccumulatorAddress(accumulatorAddress);
			}
			catch (NTMonIAddrException e) {
				e.printStackTrace();
				System.err.println("ERROR: Unable to find an IP for host "+accumulatorAddress+" "
						+ "The default will be used instead.");
			}
		}
		
		if(accumulatorPort!=null) {
			try {
				int tempPort = Integer.decode(accumulatorPort);
				if (tempPort<0 || tempPort>65535) {
					System.err.println("ERROR: "+Integer.toString(tempPort)+" is not a valid port number. "
							+ "The default will be used instead.");
				}
				else {
					node_ConfigClass.setAccumulatorPort(tempPort);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				System.err.println("ERROR: \'"+accumulatorPort+"\' is not a valid port number. "
						+ "The default will be used instead.");
			}
		}
		
		// TODO Set config data
	}

	/** Sends a refresh request to the server, to ensure that everything is OK with the connection.<br> 
	 * If there is a fatal error, the program starts its exit sequence and throws NTMonUnableToRefreshException!!!<br>
	 * If the connection is closed, an attempt will be made to reopen it. 
	 * If it is unable to do so, it will initiate the exit sequence.<br>
	 * Used by all threads before communication. <br>
	 * NOT synchronized as the caller should had already done that.<br>
	 * @throws NTMonUnableToRefreshException If there was a fatal problem with the connection. The program will no EXIT.
	 * @throws IOException If there was a non-fatal communication exception
	 */
	public static void refreshConnectionRequest() throws NTMonUnableToRefreshException, IOException {
		//No reason to continue if exiting due to an error.
		if(exitingException.get()) {
			throw new NTMonUnableToRefreshException();
		}
		//First checks whether or not the connection is open
		if(accumulatorConnection.isClosed() ||
				accumulatorConnection.isInputShutdown()||accumulatorConnection.isOutputShutdown()) {
			System.err.println("ERROR: Connection closed.");
			Boolean success=false;
			for(int i = 0 ; i<node_ConfigClass.getReconnectAttempts() ; ++i)
			{
				System.err.println("DEBUG: Attempting to recconect.");
				try {
					Node_Main.accumulatorConnection = new Socket(Node_Main.node_ConfigClass.getAccumulatorAddress(),
							Node_Main.node_ConfigClass.getAccumulatorPort());
				} catch (IOException e) {
					System.err.println("ERROR: Unable to reconnect to server.");
					e.printStackTrace();
					continue;
				}
				try {
					Node_Main.os = new DataOutputStream(Node_Main.accumulatorConnection.getOutputStream());
					Node_Main.is = new DataInputStream(Node_Main.accumulatorConnection.getInputStream());
				} catch (IOException e) {
					System.err.println("ERROR: Unable to open data stream to server.");
					e.printStackTrace();
					continue;
				}
				if(accumulatorConnection.isClosed() ||
						accumulatorConnection.isInputShutdown()||accumulatorConnection.isOutputShutdown()) {
					continue;
				}
				success=true;
				break;
			}
			//If there was a problem with reconnecting
			if(!success) {
				System.err.println("ERROR: Unable to reconnect. Max retries ("
						+node_ConfigClass.getReconnectAttempts()+") exceeded. Exiting...");
				exitingException.set(true);
				exiting.set(true);
				synchronized(exiting) {
					exiting.notifyAll();
				}
				throw new NTMonUnableToRefreshException();
			}
		}
		//Try to clear input stream
		try {
			Node_Main.is.skip(Long.MAX_VALUE);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Send refresh request
		os.writeInt(StatusCode.REFRESH_REQUEST.ordinal());
		os.writeInt(node_ConfigClass.getId().getBytes().length);
		os.write(node_ConfigClass.getId().getBytes());
		//Read response
		int response = is.readInt();
		
		if(response!=StatusCode.ALL_CLEAR.ordinal()) {
			System.err.println("ERROR: Refresh connection attempt failed. "
					+ "Server gave response status code "+StatusCode.values()[response].toString());
			System.err.println("ERROR: Exiting...");
			exitingException.set(true);
			exiting.set(true);
			synchronized(exiting) {
				exiting.notifyAll();
			}
			throw new NTMonUnableToRefreshException();
		}
	}
	
	

}
