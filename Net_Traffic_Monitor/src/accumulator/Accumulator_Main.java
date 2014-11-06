/**
 * 
 */
package accumulator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import accumulator.threads.ConnectionProcessorThread;
import accumulator.threads.Accumulator_ShutdownHookThread;

/**
 * @author Parisbre56
 *
 */
public class Accumulator_Main {
	//Shared exit data
	/** Indicates that the program is in the process of exiting.
	 */
	static public final AtomicBoolean exiting = new AtomicBoolean(false);
	/** Indicates that the exiting variable has been set due to a fatal exception.
	 */
	public static final AtomicBoolean exitingException = new AtomicBoolean(false);
	
	/** Keeps a list of all the threads that ever ran in the system.<br>
	 * Threads should try to remove themselves at exit.<br>
	 */
	static public final CopyOnWriteArrayList<Thread> threads = new CopyOnWriteArrayList<>();
	/** Configuration data for the accumulator
	 */
	public static final Accumulator_ConfigClass accumulator_configClass = new Accumulator_ConfigClass();
	
	/** Data shared between threads: clients, malicious patterns, etc.
	 */
	public static final Accumulator_SharedMemory accumulator_SharedMemory = new Accumulator_SharedMemory();

	/**
	 * 
	 */
	public Accumulator_Main() {
		//Nothing to initialize
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//TODO Process flags here
		
		//TODO Load settings here
		
		//Add shutdown hook that waits for all operations to finish
		Runtime.getRuntime().addShutdownHook(new Thread(null, new Accumulator_ShutdownHookThread(), "Shutdown Hook Thread"));
		
		//TODO Set up the necessary data structures/sql connections here. Maybe start threads
		
		//Load dummy data
		//TODO replace with load from sql
		accumulator_SharedMemory.addMaliciousIP("8.8.8.8");
		accumulator_SharedMemory.addMaliciousIP("www.google.gr");
		accumulator_SharedMemory.addMaliciousString("[h-k]ello");
		accumulator_SharedMemory.addMaliciousString("http:*");
		
		//Put self in thread list
		Accumulator_Main.threads.add(Thread.currentThread());

		//Set up listen port.
		try(ServerSocket servSock = new ServerSocket()) {
			servSock.setReuseAddress(true);
			servSock.setSoTimeout(accumulator_configClass.getSocketTimeout()*1000);
			servSock.bind(new InetSocketAddress(accumulator_configClass.getAccumulatorPort()));
			
			//Start listening for connections
			int count = 0;
			while(exiting.get()==false) {
				try {
					@SuppressWarnings("resource") //The thread closes the connection
					Socket connection = servSock.accept();
					connection.setReuseAddress(true);
					//Start connection handler for this thread
					Thread thread = new Thread(null, new ConnectionProcessorThread(connection, count), "Connection "
							+ "Processor Thread "+Integer.toString(count));
					threads.add(thread);
					thread.start();
					++count;
				} catch (SocketTimeoutException e) {
					//Do nothing. The loop will check the exit condition
				} catch (IOException e) {
					System.err.println("ERROR: Unable to accept connection");
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			System.err.println("ERROR: Error while creating or closing server socket");
			e.printStackTrace();
		}
		
		//Remove thread from list, notify the shutdownHook(if it is active) and exit.
		Accumulator_Main.threads.remove(Thread.currentThread());
		synchronized(Accumulator_Main.threads) {
			Accumulator_Main.threads.notifyAll();
		}
		return;
	}

}
