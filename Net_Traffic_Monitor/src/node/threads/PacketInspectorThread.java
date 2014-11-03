/**
 * 
 */
package node.threads;

import node.Node_Main;
import node.PacketInspectionHandler;
import node.PacketInspectionHandlerWithCheck;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;

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
		StringBuilder errbuf = new StringBuilder();
		
		//Open monitor
		errbuf.setLength(0);
		Pcap pcap = Pcap.openLive(this.currIf.getName(), Node_Main.node_ConfigClass.getSnaplen(), 
				Pcap.MODE_PROMISCUOUS, Node_Main.node_ConfigClass.getSnapTimeout()*1000, errbuf );
		
		//Check for error. Remove interface from shared memory so that it is found next time. 
		if(pcap==null) {
			System.err.println("ERROR: Unable to listen on interface "+this.currIf.getName()+" Reason: "+errbuf);
			Node_Main.node_SharedMemory.removeInterface(this.currIf);
			Node_Main.threads.remove(Thread.currentThread());
			synchronized(Node_Main.threads) {
				Node_Main.threads.notifyAll();
			}
			return;
		}
		
		//Check for warning
		if(errbuf.length()>0) {
			System.err.println("WARNING: Warning while opening interface for listening: "+errbuf);
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
			while(Node_Main.exiting.get()!=false && Node_Main.node_SharedMemory.containsPcapIf(this.currIf)) {
				retVal=pcap.loop(Node_Main.node_ConfigClass.getPacketBatchSize(), new PacketInspectionHandler(this.currIf), Node_Main.node_SharedMemory);
				if(retVal!=Pcap.OK && retVal!=Pcap.ERROR_BREAK) {
					System.err.println("ERROR: "+retVal+" returned from pcap.loop method");
				}
			}
		}
		
		//Remove self from thread list and exit
		pcap.close();
		Node_Main.threads.remove(Thread.currentThread());
		synchronized(Node_Main.threads) {
			Node_Main.threads.notifyAll();
		}
		return;
	}

}
