/**
 * 
 */
package node.threads;

import java.io.IOException;

import node.Node_Main;
import node.SharedMemory;
import shared_data.StatusCode;
import exceptions.NTMonException;

/**
 * @author Parisbre56
 *
 */
public class DataSenderThread implements Runnable {
	SharedMemory localCopy = null;

	/**
	 * 
	 */
	public DataSenderThread() {
		//Nothing to initialize
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		//Create memory as per specifications
		Node_Main.sharedMemory = new SharedMemory();
		Node_Main.senderReady.set(true);
		synchronized(Node_Main.senderReady) {
			Node_Main.senderReady.notify();
		}
		
		while(Node_Main.exiting.get()==false) {
			//Wait refresh rate before retrying
			try {
				synchronized(Node_Main.exiting) {
					Node_Main.exiting.wait(Node_Main.configClass.getRefreshRate()*1000);
				}
			} catch (InterruptedException e) {
				System.err.println("DEBUG: Interrupted while waiting to send data.");
				e.printStackTrace();
			}
			if(Node_Main.exiting.get()==true) {
				break;
			}
			synchronized(Node_Main.accumulatorConnection) {
				//Get a copy of SMPSM
				//TODO change this to make it use a cheaper data structure?
				localCopy = new SharedMemory(Node_Main.sharedMemory);
				try{
					Node_Main.refreshConnectionRequest();
					sendData();
				}
				catch (IOException e) {
					System.err.println("ERROR: Exception while trying to send data. Will retry in "+
							Node_Main.configClass.getRefreshRate()+" seconds.");
					e.printStackTrace();
				} catch (NTMonException e) {
					System.err.println("ERROR: Unknown signal while trying to send data. Will retry in "+
							Node_Main.configClass.getRefreshRate()+" seconds.");
					e.printStackTrace();
				}
			}
		}
		
		Node_Main.threads.remove(Thread.currentThread());
		synchronized(Node_Main.threads) {
			Node_Main.threads.notifyAll();
		}
		return;
	}

	/** Protocol is:
	 *	send DATA_INCOMING_NOTIFICATION<br>
	 *	for each interface<br>
	 *		-send INTERFACE_DECLARATION<br>
	 *		-send active/inactive (boolean)<br>
	 *		-send name (size int,string as byte array)<br>
	 *		-for each ip this interface has had a detection event for<br>
	 *			--send INTERFACE_IP_DECLARATION<br>
	 *			--send interface ip (size int, ip(NOT STRING) as byte array)<br>
	 *			--for each malicious ip or string for this ip of this interface that is active<br>
	 *				---send MALICIOUS_IP/STRING_ACTIVITY<br>
	 *				---send ip/string (size int,string as byte array)<br>
	 *				---send frequency<br>
	 *	send END_OF_DATA
	 * @throws IOException
	 * @throws NTMonException
	 */
	private void sendData() throws IOException, NTMonException {
		Node_Main.os.writeInt(StatusCode.DATA_INCOMING_NOTIFICATION.ordinal());
		//this.localCopy
		//TODO finish this
	}

}
