/**
 * 
 */
package node.threads;

import node.Node_Main;
import node.PacketInspectionHandler;
import node.PacketInspectionHandlerWithCheck;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;

/**
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
		currIf=newIf;
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		StringBuilder errbuf = new StringBuilder();
		
		//Open monitor
		errbuf.setLength(0);
		Pcap pcap = Pcap.openLive(currIf.getName(), Node_Main.configClass.getSnaplen(), 
				Pcap.MODE_PROMISCUOUS, Node_Main.configClass.getSnapTimeout()*1000, errbuf );
		
		//Check for error. Remove interface from shared memory so that it is found next time. 
		if(pcap==null) {
			System.err.println("ERROR: Unable to listen on interface "+currIf.getName()+" Reason: "+errbuf);
			Node_Main.sharedMemory.removeInterface(currIf);
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
		
		Integer retVal;
		if(Node_Main.configClass.getPacketBatchSize()==Pcap.LOOP_INFINITE) {
			//Check for -1 or -2
			retVal=pcap.loop(Pcap.LOOP_INFINITE, new PacketInspectionHandlerWithCheck(pcap,currIf), Node_Main.sharedMemory);
			if(retVal!=Pcap.OK && retVal!=Pcap.ERROR_BREAK) {
				System.err.println("ERROR: "+retVal.toString()+" returned from pcap.loop method");
			}
		}
		else {
			//While the program is not exiting and the interface still exists
			while(Node_Main.exiting.get()!=false && Node_Main.sharedMemory.containsPcapIf(currIf)) {
				retVal=pcap.loop(Node_Main.configClass.getPacketBatchSize(), new PacketInspectionHandler(currIf), Node_Main.sharedMemory);
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
