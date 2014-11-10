/**
 * 
 */
package node.threads;

import java.io.IOException;

import node.Node_Main;
import exceptions.NTMonException;
import exceptions.NTMonUnableToRefreshException;
import shared_data.StatusCode;

/**
 * @author Parisbre56
 *
 */
public class DataRetrieverThread implements Runnable {

	/**
	 * 
	 */
	public DataRetrieverThread() {
		//No initialization needed
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		//One command to give all the patterns, one command to give changes (add or remove).
		//Server keeps the difference
		
		//First time it is run, get all the data.
		//Keep trying until successful or until it is time to exit.
		while(Node_Main.exiting.get()==false) {
			synchronized(Node_Main.accumulatorConnection) {
				//Try to get the data
				try {
					Node_Main.refreshConnectionRequest();
					Node_Main.os.writeInt(StatusCode.ALL_DATA_REQUEST.ordinal());
					processIncomingData();
					break;
				} catch (IOException e) {
					System.err.println("ERROR: Exception during initial data retrieval.");
					e.printStackTrace();
				} catch (NTMonUnableToRefreshException e) {
					e.printStackTrace();
					return;
				} catch (NTMonException e) {
					System.err.println("ERROR: Unknown signal received.");
					e.printStackTrace();
				}
				//Clear input on failure
				try {
					Node_Main.skipInput();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		

		//In the loop, just ask for the difference. 
		//If the server sends a clear memory request instead of additions or removals, 
		//then clear the memory and ask for all the data again.
		while(Node_Main.exiting.get()==false) {
			//Wait refresh rate before retrying
			try {
				synchronized(Node_Main.exiting) {
					Node_Main.exiting.wait(Node_Main.node_ConfigClass.getRefreshRate()*1000);
				}
			} catch (InterruptedException e) {
				System.err.println("DEBUG: Interrupted while waiting for data retrieval.");
				e.printStackTrace();
			}
			if(Node_Main.exiting.get()==true) {
				break;
			}
			synchronized(Node_Main.accumulatorConnection) {
				try{
					Node_Main.refreshConnectionRequest();
					Node_Main.os.writeInt(StatusCode.DATA_DIFF_REQUEST.ordinal());
					processIncomingData();
				}
				catch (IOException e) {
					System.err.println("ERROR: Exception while trying to retrieve data. Will retry in "+
							Node_Main.node_ConfigClass.getRefreshRate()+" seconds.");
					e.printStackTrace();
				} catch (NTMonUnableToRefreshException e) {
					e.printStackTrace();
					return;
				} catch (NTMonException e) {
					System.err.println("ERROR: Unknown signal while trying to retrieve data. Will retry in "+
							Node_Main.node_ConfigClass.getRefreshRate()+" seconds.");
					e.printStackTrace();
				}
			}
		}
		
		//Remove self from thread list before exiting
		Node_Main.exitThread();
		return;
	}

	/** This processes data sent by the server after either a DATA_DIFF_REQUEST or an ALL_DATA_REQUEST.<br>
	 * Protocol is:<br>
	 * read some_DATA_REQUEST<br>
	 * while(response!=END_OF_DATA)<br>
	 * 		if (CLEAR_MEMORY_REQUEST)<br>
	 * 			clear memory<br>
	 * 		else (MALICIOUS_IP_ADDITION || MALICIOUS_STRING_ADDITION || MALICIOUS_IP_REMOVAL || MALICIOUS_STRING_REMOVAL)<br>
	 * 			read data length<br>
	 * 			read data and add them to memory<br>
	 * 		else<br>
	 * 			error<br>
	 * @throws IOException 
	 * @throws NTMonException 
	 */
	private static void processIncomingData() throws IOException, NTMonException {
		int response;
		while(true) {
			response=Node_Main.is.readInt();
			if(response==StatusCode.END_OF_DATA.ordinal()) {
				break;
			}
			else if(response==StatusCode.CLEAR_MEMORY_REQUEST.ordinal()) {
				Node_Main.node_SharedMemory.clearMPSMMemory();
			}
			else if(response==StatusCode.MALICIOUS_IP_ADDITION.ordinal()) {
				int length=Node_Main.is.readInt();
				byte[] bytes = new byte[length];
				Node_Main.is.readFully(bytes);
				String data=new String(bytes);
				Node_Main.node_SharedMemory.addIP(data);
			}
			else if(response==StatusCode.MALICIOUS_STRING_ADDITION.ordinal()) {
				int length=Node_Main.is.readInt();
				byte[] bytes = new byte[length];
				Node_Main.is.readFully(bytes);
				String data=new String(bytes);
				Node_Main.node_SharedMemory.addString(data);
			}
			else if(response==StatusCode.MALICIOUS_IP_REMOVAL.ordinal()) {
				int length=Node_Main.is.readInt();
				byte[] bytes = new byte[length];
				Node_Main.is.readFully(bytes);
				String data=new String(bytes);
				Node_Main.node_SharedMemory.removeIP(data);
			}
			else if(response==StatusCode.MALICIOUS_STRING_REMOVAL.ordinal()) {
				int length=Node_Main.is.readInt();
				byte[] bytes = new byte[length];
				Node_Main.is.readFully(bytes);
				String data=new String(bytes);
				Node_Main.node_SharedMemory.removeString(data);
			}
			else {
				throw new NTMonException("ERROR: Received signal "
						+Integer.toString(response)+":"
						+StatusCode.values()[response].toString()+" from the accumulator while "
						+ "trying to retrieve data.");
			}
		}
	}

}
