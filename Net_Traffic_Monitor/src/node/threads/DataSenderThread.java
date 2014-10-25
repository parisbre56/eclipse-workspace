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
		// TODO Auto-generated constructor stub
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
	 * send DATA_INCOMING_NOTIFICATION<br>
	 * for each interface<br>
	 *		send INTERFACE_DECLARATION<br>
	 *		send name<br>
	 *		send ip<br>
	 *		for each malicious ip or string in MPSM<br>
	 *			send MALICIOUS_IP/STRING_ACTIVITY<br>
	 *			send ip/string<br>
	 *			send frequency<br>
	 * 	send END_OF_DATA
	 * @throws IOException
	 * @throws NTMonException
	 */
	private void sendData() throws IOException, NTMonException {
		Node_Main.os.writeInt(StatusCode.DATA_INCOMING_NOTIFICATION.ordinal());
		//TODO finish this
	}

}
