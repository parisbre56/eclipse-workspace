/**
 * 
 */
package node.sharedMemory;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.LinkedList;

import org.jnetpcap.PcapIf;

/** Guarantees that all updates will be synchronized. No need for external synchronization
 * @author Parisbre56
 *
 */
public class Node_SharedMemory {
	final private MPSM mpsm;
	final private SMPSM smpsm;

	/**
	 * 
	 */
	public Node_SharedMemory() {
		this.mpsm = new MPSM();
		this.smpsm = new SMPSM();
	}

	/** Copy constructor, copies all data so that they can be altered without changing the original
	 * @param node_SharedMemory The shared memory to copy
	 */
	public Node_SharedMemory(Node_SharedMemory node_SharedMemory) {
		this.mpsm=new MPSM(node_SharedMemory.mpsm);
		this.smpsm=new SMPSM(node_SharedMemory.smpsm);
	}

	/** If the malicious string already exists in memory it will simply be marked as active
	 * @param data The malicious String to add in the malicious string pattern memory.
	 */
	public void addString(String data) {
		this.mpsm.addString(data);
	}
	
	/** The string will not be actually removed to preserve statistics
	 * @param data The malicious string to mark as inactive
	 */
	public void removeString(String data) {
		this.mpsm.removeString(data);
	}
	
	/** If the malicious IP is already in the memory it will simply be marked as active
	 * @param data The malicious IP to mark as active. Can also be a hostname.
	 */
	public void addIP(String data) {
		this.mpsm.addIP(data);
	}

	/** The ip will not actually be removed to preserve statistics<br>
	 * Note that addresses are not translated into InetAddress objects until later, so you can have two names in memory
	 * poiting to the same IP address. In that case, only the first match will be used.
	 * @param data The malicious IP to add in the memory. Can also be a hostname.
	 */
	public void removeIP(String data) {
		this.mpsm.removeIP(data);
	}

	/** Marks all malicious patterns (IPs and Strings) as inactive
	 */
	public void clearMPSMMemory() {
		this.mpsm.setAllInactive();
	}

	/** If the interface is already in memory it will be marked as active
	 * @param newIf The interface to add in the memory
	 */
	public void addInterface(PcapIf newIf) {
		this.smpsm.addInterface(newIf);
	}

	/** Does not actually remove the interface to preserve statistics
	 * @param oldIf The interface to mark as inactive
	 */
	public void removeInterface(PcapIf oldIf) {
		this.smpsm.removeInterface(oldIf);
	}

	/** 
	 * @param newIf The interface to search for
	 * @return true if this contains an ACTIVE interface with the same name as newIf, else false
	 */
	public boolean containsPcapIf(PcapIf newIf) {
		return this.smpsm.containsActivePcapIf(newIf);
	}

	/**
	 * Checks the interface list in smpsm for active interfaces that are not in the list and marks them as inactive
	 * @param newList
	 */
	public void removeNonexistentInterfaces(LinkedList<PcapIf> newList) {
		this.smpsm.removeNonexistentInterfaces(newList);
	}

	/**
	 * Checks if addrSource or addrDest (or both) are in the malicious IP list. If yes, then an entry is made in the statistics.
	 * If both IPs are malicious, an entry is made for both.
	 * @param addrSource The source IP address
	 * @param addrDest The destination IP address
	 * @param currIf The interface this capture was made on. 
	 * Used to determine the interface's name and IP, which will be used to store the statistics if necessary.
	 */
	public void checkAndAddIPStatIfNecessary(InetAddress addrSource,
			InetAddress addrDest, PcapIf currIf) {
		StringPattern pat = this.mpsm.getMatchingIP(addrSource);
		if(pat!=null) {
			this.smpsm.addIpEvent(pat,currIf);
		}
		pat = this.mpsm.getMatchingIP(addrDest);
		if(pat!=null) {
			this.smpsm.addIpEvent(pat,currIf);
		}
	}

	/**
	 * Checks the payload string for malicious string patterns and adds them to the statistics if necessary.
	 * @param string The payload string to check for malicious string patterns
	 * @param currIf The interface this capture was made on. 
	 * Used to determine the interface's name and IP, which will be used to store the statistics if necessary.
	 */
	public void checkAndAddPayloadStatIfNecessary(String string, PcapIf currIf) {
		LinkedList<StringPatternDetectionEvents> collection = this.mpsm.getMatchingStringCollection(string);
		if(collection.size()>0) {
			this.smpsm.addStringEventCollection(collection,currIf);
		}
	}

	
	/**
	 * @return An iterator over the interface statistics contained in this memory
	 */
	public Iterator<InterfaceStatistics> getInterfacesIterator() {
		return this.smpsm.getInterfacesIterator();
	}

}
