/**
 * 
 */
package node.threads;

import java.util.LinkedList;

import node.Node_Main;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;

/**
 * @author Parisbre56
 *
 */
public class InterfaceCheckerThread implements Runnable {

	/**
	 * 
	 */
	public InterfaceCheckerThread() {
		//Nothing to initialize
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		LinkedList<PcapIf> newList = new LinkedList<>();
		StringBuilder errorStr = new StringBuilder(); 
		while(Node_Main.exiting.get()==false) {
			errorStr.setLength(0);
			newList.clear();
			if(Pcap.findAllDevs(newList, errorStr)!=Pcap.OK) {
				System.err.println("ERROR: Unable to retrieve interfaces. Reason: "+errorStr);
				continue;
			}
			//Start searching through the new list for additions
			for(PcapIf newIf : newList) {
				if(!Node_Main.node_SharedMemory.containsPcapIf(newIf)) {
					System.err.println("DEBUG: Found interface: "+newIf.getName()+":"+newIf.getDescription());
					Node_Main.node_SharedMemory.addInterface(newIf);
					//Start a new inspector thread for them. Inspector threads should stop once they have no data to process.
					Thread thread = new Thread(null, new PacketInspectorThread(newIf), newIf.getName()+" Packet Inspector");
					Node_Main.threads.add(thread);
					thread.start();
				}
			}
			//Start searching through the old list for deletions
			Node_Main.node_SharedMemory.removeNonexistentInterfaces(newList);
			//Wait for refresh rate
			try {
				synchronized(Node_Main.exiting) {
					Node_Main.exiting.wait(Node_Main.node_ConfigClass.getRefreshRate()*1000);
				}
			} catch (InterruptedException e) {
				System.err.println("DEBUG: Interrupted while waiting to send data.");
				e.printStackTrace();
			}
		}
		
		Node_Main.threads.remove(Thread.currentThread());
		synchronized(Node_Main.threads) {
			Node_Main.threads.notifyAll();
		}
		return;
	}

}
