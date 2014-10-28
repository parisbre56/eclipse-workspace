/**
 * 
 */
package node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import shared_data.StatusCode;
import node.sharedMemory.SharedMemory;
import node.threads.DataRetrieverThread;
import node.threads.DataSenderThread;
import node.threads.IdentificationThread;
import node.threads.InterfaceCheckerThread;
import node.threads.ShutdownHookThread;
import exceptions.NTMonException;
import exceptions.NTMonIAddrException;
import exceptions.NTMonUnableToRefreshException;

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
	/** Indicates that the program is in the process of exiting.
	 */
	static public AtomicBoolean exiting = null;
	/** Indicates that the exiting variable has been set due to a fatal exception.
	 */
	private static AtomicBoolean exitingException = null;
	
	//Shared thread data
	/** Keeps a list of all the threads that ever ran in the system.<br>
	 * Threads should try to remove themselves at exit.<br>
	 * Periodic checks should be made if possible to ensure that any dead threads are removed to save memory.
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
				e.printStackTrace();
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
			for(Integer i = 0 ; i<configClass.getReconnectAttempts() ; ++i)
			{
				System.err.println("DEBUG: Attempting to recconect.");
				try {
					Node_Main.accumulatorConnection = new Socket(Node_Main.configClass.getAccumulatorAddress(),
							Node_Main.configClass.getAccumulatorPort());
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
						+configClass.getReconnectAttempts()+") exceeded. Exiting...");
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
		os.writeInt(configClass.getId().getBytes().length);
		os.write(configClass.getId().getBytes());
		//Read response
		Integer response = is.readInt();
		
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
