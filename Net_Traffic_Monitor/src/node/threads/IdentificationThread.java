/**
 * 
 */
package node.threads;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import node.Node_Main;
import exceptions.NTMonException;
import exceptions.NTMonUnableToRefreshException;
import shared_data.StatusCode;

/** First sends an identification request. Keeps trying until it finds an identity it can register with.<br>
 * Then sends the refresh rate.<br>
 * Finally waits until all other threads (except this one) have exited to send the exit signal to the server.<br>
 * @author Parisbre56
 *
 */
public class IdentificationThread implements Runnable {

	// Start identification thread and make sure it stays running.
	// This one connects with the accumulator and registers this. Check for error or duplicate.
	//		If duplicate start incrementing and retrying (e.g. Unknown-1) until successful. 
	//		Be sure to update configClass with the new name.
	// Renew session every time data is sent, otherwise it expires. Accumulator keeps timeout 
	//	for each node and waits a bit longer (say, renewal time + a minute). Timeout is essentially the 
	//	same as offline
	// Notify accumulator when exiting. Accumulator has an online, offline or timeout enumeration for nodes.
	// Sleep and wait for the exit signal. When the exit signal is received, send the appropriate signal to the 
	
	/**
	 * 
	 */
	public IdentificationThread() {
		//No initialization needed
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		//We are certain that only this thread can access the socket at this time, 
		//so no need to concern ourselves with concurrency
		try {
			Node_Main.accumulatorConnection = new Socket();
			Node_Main.accumulatorConnection.setReuseAddress(true);
			Node_Main.accumulatorConnection.setSoTimeout(Node_Main.node_ConfigClass.getRefreshRate()*1000);
			Node_Main.accumulatorConnection.connect(new InetSocketAddress(Node_Main.node_ConfigClass.getAccumulatorAddress(), 
					Node_Main.node_ConfigClass.getAccumulatorPort()), 
					Node_Main.node_ConfigClass.getRefreshRate()*1000);
		} catch (IOException e) {
			System.err.println("ERROR: Unable to establish connection to the accumulator.");
			e.printStackTrace();
			Node_Main.identificationFailedReason=e;
			Node_Main.identificationFailed.set(true);
			synchronized(Node_Main.identificationReady) {
				Node_Main.identificationReady.notifyAll();
			}
			Node_Main.threads.remove(Thread.currentThread());
			synchronized(Node_Main.threads) {
				Node_Main.threads.notifyAll();
			}
			return;
		}
		
		//Send the request to register to the server. Keeps trying until it succeeds or fails horribly.
		/* Protocol is:
		 * Send registration request
		 * Send name size
		 * Send name
		 * Get response for success/fail
		 * If successful, send additional data
		 */
		try {
			int nameConflictCounter=0;
			Node_Main.os = new DataOutputStream(Node_Main.accumulatorConnection.getOutputStream());
			Node_Main.is = new DataInputStream(Node_Main.accumulatorConnection.getInputStream());
			while(true) {
			//Send registration request
				Node_Main.os.writeInt(StatusCode.NAME_REGISTRATION_REQUEST.ordinal());
				//Create name
				String tempName;
				if(nameConflictCounter>0) {
					tempName=Node_Main.node_ConfigClass.getId()+"-"+Integer.toString(nameConflictCounter);
				}
				else {
					tempName=Node_Main.node_ConfigClass.getId();
				}
				//Send name size
				Node_Main.os.writeInt(tempName.getBytes().length);
				//Send name
				Node_Main.os.write(tempName.getBytes());
				//Read response
				int response = Node_Main.is.readInt();
				if(response != StatusCode.ALL_CLEAR.ordinal()) {
					//If the name is taken, retry with a different name
					if(response == StatusCode.NAME_ALREADY_EXISTS.ordinal()) {
						System.err.println("DEBUG: \'"+tempName+"\' already registered. "
								+ "Retrying to connect with a different name.");
						nameConflictCounter+=1;
						continue;
					}
					//Else if there was some kind of other problem we can't handle, fail
					System.err.println("ERROR: Received signal "+Integer.toString(response)+":"
							+StatusCode.values()[response].toString()+" from the accumulator while "
									+ "trying to register.");
					Node_Main.identificationFailedReason=new NTMonException("ERROR: Received signal "
							+Integer.toString(response)+":"
							+StatusCode.values()[response].toString()+" from the accumulator while "
							+ "trying to register.");
					Node_Main.identificationFailed.set(true);
					synchronized(Node_Main.identificationReady) {
						Node_Main.identificationReady.notifyAll();
					}
					Node_Main.accumulatorConnection.close();
					Node_Main.threads.remove(Thread.currentThread());
					synchronized(Node_Main.threads) {
						Node_Main.threads.notifyAll();
					}
					return;
				}
				//If successful, change the Id in the settings and continue out of the loop
				Node_Main.node_ConfigClass.setId(tempName);
				break;
			}
		} catch (IOException e) {
			System.err.println("ERROR: Exception while trying to register with the accumulator.");
			e.printStackTrace();
			Node_Main.identificationFailedReason=e;
			Node_Main.identificationFailed.set(true);
			synchronized(Node_Main.identificationReady) {
				Node_Main.identificationReady.notifyAll();
			}
			try {
				Node_Main.accumulatorConnection.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			Node_Main.threads.remove(Thread.currentThread());
			synchronized(Node_Main.threads) {
				Node_Main.threads.notifyAll();
			}
			return;
		}
		
		//Notify the main process that we are registered and ready to continue.
		Node_Main.identificationReady.set(true);
		synchronized(Node_Main.identificationReady) {
			Node_Main.identificationReady.notifyAll();
		}
		
		//Send the refresh rate to the server (concurrency becomes an issue, must synchronize)
		int retries = 0;
		while(true) {
			++retries;
			try{
				synchronized(Node_Main.accumulatorConnection) {
					Node_Main.refreshConnectionRequest();
					Node_Main.os.writeInt(StatusCode.SET_REFRESH_RATE.ordinal());
					Node_Main.os.writeInt(Node_Main.node_ConfigClass.getRefreshRate());
					int response = Node_Main.is.readInt();
					if(response==StatusCode.ALL_CLEAR.ordinal()) {
						break;
					}
					//Else if there was a problem
					System.err.println("ERROR: Received signal "
							+Integer.toString(response)+":"
							+StatusCode.values()[response].toString()+" from the accumulator while "
							+ "trying to send refresh rate.");
				}
			}
			catch (IOException e) {
				System.err.println("ERROR: Exception while trying to send the refresh rate to the accumulator.");
				e.printStackTrace();
			} catch (NTMonUnableToRefreshException e) {
				System.err.println("ERROR: Fatal exception occured. Giving up on sending refresh rate.");
				e.printStackTrace();
				break;
			}
			//Try to clear input stream
			try {
				Node_Main.is.skip(Long.MAX_VALUE);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(retries<Node_Main.node_ConfigClass.getReconnectAttempts()) {
				System.err.println("ERROR: Will retry to send refresh rate.");
			}
			else {
				System.err.println("ERROR: Max retries exceeded. Giving up on sending refresh rate.");
				break;
			}
		}
		
		//Wait for the exit signal
		while(Node_Main.exiting.get()==false) {
			synchronized(Node_Main.exiting) {
				try {
					Node_Main.exiting.wait(Node_Main.defaultTimeout*1000);
				} catch (InterruptedException e) {
					System.err.println("DEBUG: Interrupted while waiting for the exit signal.");
					e.printStackTrace();
				}
			}
		}
		
		//When the exit signal is received, join all other threads
		//to ensure that all data is written first
		//then send the appropriate message to the server and exit.
		for(Thread toJoin : Node_Main.threads) {
			if(toJoin.equals(Thread.currentThread())) {
				Boolean joined=false;
				while(joined==false) {
					try {
						toJoin.join();
						joined=true;
					} catch (InterruptedException e) {
						System.err.println("DEBUG: Interrupted while waiting for the other threads to exit.");
						e.printStackTrace();
					}
				}
			}
		}
		
		//Send the exit signal to the server. 
		//Even though this should be the only thread alive at this point, keep it synchronized, just in case.
		synchronized(Node_Main.accumulatorConnection) {
			retries=0;
			while(true) {
				try {
					Node_Main.refreshConnectionRequest();
					Node_Main.os.writeInt(StatusCode.EXIT_REQUEST.ordinal());
					int response = Node_Main.is.readInt();
					if(response!=StatusCode.ALL_CLEAR.ordinal()) {
						System.err.println("DEBUG: Received signal "
								+Integer.toString(response)+":"
								+StatusCode.values()[response].toString()+" from the accumulator while "
								+ "trying to send exit signal to it.");
					}
					break;
				}
				catch (IOException e) {
					++retries;
					System.err.println("ERROR: Exception while trying to send exit signal to accumulator.");
					e.printStackTrace();
					if(retries<Node_Main.node_ConfigClass.getReconnectAttempts()) {
						System.err.println("ERROR: Will retry to send exit signal.");
					}
					else {
						System.err.println("ERROR: Max retries exceeded. Giving up on sending exit signal.");
						break;
					}
				} catch (NTMonUnableToRefreshException e) {
					System.err.println("ERROR: Fatal exception occured. Giving up on sending exit signal.");
					e.printStackTrace();
					break;
				}
			}
		}
		
		//Close connections and exit
		try {
			Node_Main.is.close();
			Node_Main.os.close();
			Node_Main.accumulatorConnection.close();
		} catch (IOException e) {
			System.err.println("ERROR: Unable to close connection to accumulator.");
			e.printStackTrace();
		}
		
		//Remove self from thread list before exiting
		Node_Main.threads.remove(Thread.currentThread());
		synchronized(Node_Main.threads) {
			Node_Main.threads.notifyAll();
		}
		return;
	}

}
