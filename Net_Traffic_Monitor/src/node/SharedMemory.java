/**
 * 
 */
package node;

import java.util.LinkedList;

import org.jnetpcap.PcapIf;

/**
 * @author Parisbre56
 *
 */
public class SharedMemory {
	private static MPSM mpsm = null;
	private static SMPSM smpsm = null;

	/**
	 * 
	 */
	public SharedMemory() {
		mpsm = new MPSM();
		smpsm = new SMPSM();
	}

	/** Copy constructor, makes sure to make copies synchronized
	 * @param sharedMemory
	 */
	public SharedMemory(SharedMemory sharedMemory) {
		// TODO Auto-generated constructor stub
	}

	public void addString(String data) {
		// TODO Auto-generated method stub
		
	}

	public void removeIP(String data) {
		// TODO Auto-generated method stub
		
	}

	public void removeString(String data) {
		// TODO Auto-generated method stub
		
	}

	public void addIP(String data) {
		// TODO Auto-generated method stub
		
	}

	public void clearMPSMMemory() {
		// TODO Auto-generated method stub
		
	}

	public void addInterface(PcapIf newIf) {
		// TODO Auto-generated method stub
		
	}

	public void removeInterface(PcapIf oldIf) {
		// TODO Auto-generated method stub
		
	}

	public boolean containsPcapIf(PcapIf newIf) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeNonexistentInterfaces(LinkedList<PcapIf> newList) {
		// TODO Auto-generated method stub
		
	}

}
