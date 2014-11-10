/**
 * 
 */
package node.threads;

import node.Node_Main;
import node.PacketInspectionHandler;
import node.PacketInspectionHandlerWithCheck;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.nio.JMemory;
import org.jnetpcap.packet.PcapPacket;

/** Checks all packets going through the interface
 * @author Parisbre56
 *
 */
public class PacketInspectorThread implements Runnable {

	PcapIf currIf;
	
	/**
	 * @param newIf The interface that this thread will monitor. The thread will exit once the interface no longer exists in the shared memory. 
	 * 
	 */
	public PacketInspectorThread(PcapIf newIf) {
		this.currIf=newIf;
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			StringBuilder errbuf = new StringBuilder();
			
			//Open monitor
			errbuf.setLength(0);
			Pcap pcap = Pcap.openLive(this.currIf.getName(), Node_Main.node_ConfigClass.getSnaplen(), 
					Pcap.MODE_PROMISCUOUS, Node_Main.node_ConfigClass.getSnapTimeout()*1000, errbuf );
			
			//Check for error. Remove interface from shared memory so that it is found next time. 
			if(pcap==null) {
				System.err.println("ERROR: Unable to listen on interface "+this.currIf.getName()+" Reason: "+errbuf);
				Node_Main.node_SharedMemory.removeInterface(this.currIf);
				Node_Main.exitThread();
				return;
			}
			
			try {
				//Check for warning
				if(errbuf.length()>0) {
					System.err.println("WARNING: Warning while opening interface for listening: "+errbuf);
				}
				
				//Test if interface is active
				int testRetVal;
				PcapPacket testPacket = new PcapPacket(JMemory.POINTER);
				testRetVal = pcap.nextEx(testPacket);
				//If all is OK, then process the packet normally and continue on with the loop
				if(testRetVal==Pcap.NEXT_EX_OK) {
					new PacketInspectionHandler(this.currIf).nextPacket(testPacket, Node_Main.node_SharedMemory);
				}
				//Else print the appropriate error and disconnect
				else { 
					if (testRetVal==Pcap.NEXT_EX_TIMEDOUT) {
						System.err.println("WARNING: Interface "+this.currIf.getName()+" timedout "
								+ "during initial testing. Will stop listening and try again later.");
					}
					else {
						System.err.println("WARNING: Interface "+this.currIf.getName()+" returned "+testRetVal+
								" during initial testing. Will stop listening and try again later.");
					}
					Node_Main.node_SharedMemory.removeInterface(this.currIf);
				}
				
				int retVal;
				if(Node_Main.node_ConfigClass.getPacketBatchSize()==Pcap.LOOP_INFINITE) {
					//Check for -1 or -2
					retVal=pcap.loop(Pcap.LOOP_INFINITE, new PacketInspectionHandlerWithCheck(pcap,this.currIf), Node_Main.node_SharedMemory);
					if(retVal!=Pcap.OK && retVal!=Pcap.ERROR_BREAK) {
						System.err.println("ERROR: "+Integer.toString(retVal)+" returned from pcap.loop method");
					}
				}
				else {
					//While the program is not exiting and the interface still exists
					while(Node_Main.exiting.get()==false && Node_Main.node_SharedMemory.containsPcapIf(this.currIf)) {
						PacketInspectionHandler inspectionHandler = new PacketInspectionHandler(this.currIf);
						retVal=pcap.loop(Node_Main.node_ConfigClass.getPacketBatchSize(), inspectionHandler, Node_Main.node_SharedMemory);
						if(retVal!=Pcap.OK && retVal!=Pcap.ERROR_BREAK) {
							System.err.println("ERROR: "+retVal+" returned from pcap.loop method. "
									+ "Stopping checker thread for interface "+this.currIf.getName());
							Node_Main.node_SharedMemory.removeInterface(this.currIf);
							break;
						}
					}
				}
			} finally {
				pcap.close();
			}
		} finally {
			//Remove self from thread list
			//TODO Node_Main.exitThread();
		}
		return;
	}

}
