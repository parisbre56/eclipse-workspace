/**
 * 
 */
package node.threads;

import org.jnetpcap.PcapIf;

/**
 * @author Parisbre56
 *
 */
public class PacketInspectorThread implements Runnable {

	
	
	/**
	 * @param newIf The interface that this thread will monitor. The thread will exit once the interface no longer exists in the shared memory. 
	 * 
	 */
	public PacketInspectorThread(PcapIf newIf) {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
