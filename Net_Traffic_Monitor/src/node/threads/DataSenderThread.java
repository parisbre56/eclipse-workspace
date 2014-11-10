/**
 * 
 */
package node.threads;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;

import node.Node_Main;
import node.sharedMemory.DetectionFrequencyString;
import node.sharedMemory.InterfaceAddressStats;
import node.sharedMemory.InterfaceStatistics;
import node.sharedMemory.Node_SharedMemory;
import shared_data.StatusCode;
import exceptions.NTMonUnableToRefreshException;

/**
 * @author Parisbre56
 *
 */
public class DataSenderThread implements Runnable {
	Node_SharedMemory localCopy = null;

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
		Node_Main.node_SharedMemory = new Node_SharedMemory();
		Node_Main.senderReady.set(true);
		synchronized(Node_Main.senderReady) {
			Node_Main.senderReady.notifyAll();
		}
		
		while(Node_Main.exiting.get()==false) {
			//Wait refresh rate before retrying
			try {
				synchronized(Node_Main.exiting) {
					Node_Main.exiting.wait(Node_Main.node_ConfigClass.getRefreshRate()*1000);
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
				this.localCopy = new Node_SharedMemory(Node_Main.node_SharedMemory);
				try{
					//Refresh connection and send data
					Node_Main.refreshConnectionRequest();
					sendData();
				}
				catch (IOException e) {
					System.err.println("ERROR: Exception while trying to send data. Will retry in "+
							Node_Main.node_ConfigClass.getRefreshRate()+" seconds.");
					e.printStackTrace();
				} catch (NTMonUnableToRefreshException e) {
					e.printStackTrace();
					return;
				} /*catch (NTMonException e) {
					System.err.println("ERROR: Unknown signal while trying to send data. Will retry in "+
							Node_Main.configClass.getRefreshRate()+" seconds.");
					e.printStackTrace();
				}*/
			}
		}
		
		Node_Main.exitThread();
		return;
	}

	/** Protocol is:
	 *	send DATA_INCOMING_NOTIFICATION<br>
	 *	for each interface<br>
	 *		-send INTERFACE_DECLARATION<br>
	 *		-send name (size int,string as byte array)<br>
	 *		-send active/inactive (boolean)<br>
	 *		-for each ip this interface has had a detection event for<br>
	 *			--send INTERFACE_ADDRESS_DECLARATION<br>
	 *			--send interface ip (size int, ip(NOT STRING) as byte array) | 0 size means null<br>
	 *			--for each malicious ip or string for this ip of this interface that is active<br>
	 *				---send MALICIOUS_IP/STRING_ACTIVITY<br>
	 *				---send ip/string (size int,string as byte array)<br>
	 *				---send frequency<br>
	 *	send END_OF_DATA
	 * @throws IOException
	 */
	private void sendData() throws IOException/*, NTMonException*/ {
		Node_Main.os.writeInt(StatusCode.DATA_INCOMING_NOTIFICATION.ordinal());
		
		//For all interfaces
		Iterator<InterfaceStatistics> itIn = this.localCopy.getInterfacesIterator();
		while(itIn.hasNext()) {
			InterfaceStatistics inStats = itIn.next();
			
			//Declare interface
			Node_Main.os.writeInt(StatusCode.INTERFACE_DECLARATION.ordinal());
			Node_Main.os.writeInt(inStats.getName().getBytes().length);
			Node_Main.os.write(inStats.getName().getBytes());
			Node_Main.os.writeBoolean(inStats.getActive());
			
			//For all addresses
			Iterator<InterfaceAddressStats> itInAd = inStats.getAddressIterator();
			while(itInAd.hasNext()) {
				InterfaceAddressStats inAdStats = itInAd.next();
				
				//Declare address
				Node_Main.os.writeInt(StatusCode.INTERFACE_ADDRESS_DECLARATION.ordinal());
				InetAddress inetAddr = inAdStats.getAddress();
				if(inetAddr==null) {
					Node_Main.os.writeInt(0);
				}
				else {
					Node_Main.os.writeInt(inetAddr.getAddress().length);
					Node_Main.os.write(inetAddr.getAddress());
				}
				
				//For all ip detection events for this address that are marked as active
				Iterator<DetectionFrequencyString> itEvent = inAdStats.getIpEventIterator();
				while(itEvent.hasNext()) {
					DetectionFrequencyString detIp = itEvent.next();
					if(detIp.patternIsActive()) {
						//Send the event and its frequency
						Node_Main.os.writeInt(StatusCode.MALICIOUS_IP_ACTIVITY.ordinal());
						Node_Main.os.writeInt(detIp.getPatternString().getBytes().length);
						Node_Main.os.write(detIp.getPatternString().getBytes());
						Node_Main.os.writeInt(detIp.getFrequency());
					}
				}
				
				//For all string detection events for this address that are marked as active
				itEvent = inAdStats.getStringEventIterator();
				while(itEvent.hasNext()) {
					DetectionFrequencyString detString = itEvent.next();
					if(detString.patternIsActive()) {
						//Send the event and its frequency
						Node_Main.os.writeInt(StatusCode.MALICIOUS_STRING_ACTIVITY.ordinal());
						Node_Main.os.writeInt(detString.getPatternString().getBytes().length);
						Node_Main.os.write(detString.getPatternString().getBytes());
						Node_Main.os.writeInt(detString.getFrequency());
					}
				}
			}
		}
		//No more data incoming
		Node_Main.os.writeInt(StatusCode.END_OF_DATA.ordinal());
	}

}
