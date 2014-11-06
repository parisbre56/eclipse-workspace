/**
 * 
 */
package node.sharedMemory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jnetpcap.PcapIf;

/**
 * Contains statistics for all interfaces and all patterns. <br>
 * Interfaces are marked as deactivated instead of being deleted, so that the statistics aren't lost. <br>
 * The structure is List of interfaces (Interface name, List of IPs for interface 
 * (IP, List of detection events for IP of interface)) <br>
 * @author Parisbre56
 *
 */
public class SMPSM {
	/**List with interfaces 
	 * (interface name strings, list of events for ips)
	 */
	final private CopyOnWriteArrayList<InterfaceStatistics> interfaceStatistics;

	/** Creates a new statistics memory block.
	 */
	public SMPSM() {
		this.interfaceStatistics = new CopyOnWriteArrayList<>();
	}

	/** Copy constructor, makes a new copy of the statistics block. 
	 * All the data are copied, which means that they can be safely modified.
	 * @param smpsm The statistics block to be copied.
	 */
	public SMPSM(SMPSM smpsm) {
		this.interfaceStatistics = new CopyOnWriteArrayList<>();
		for(InterfaceStatistics iS : smpsm.interfaceStatistics) {
			this.interfaceStatistics.add(new InterfaceStatistics(iS));
		}
	}

	/** Adds the interface if it doesn't exist or marks it as active if it does.
	 * @param newIf The interface to mark as active
	 */
	public void addInterface(PcapIf newIf) {
		int index = -1;
		int tempIndex = 0;
		for(InterfaceStatistics e : this.interfaceStatistics) {
			if(e.equals(newIf.getName())) {
				index=tempIndex;
				break;
			}
			++tempIndex;
		}
		if(index<0) {
			this.interfaceStatistics.add(new InterfaceStatistics(newIf.getName()));
		}
		else {
			this.interfaceStatistics.get(index).setActive();
		}
	}

	/** Marks an interface as inactive. Does not actually delete it so that the statistics are not removed.
	 * @param oldIf The interface to mark as inactive
	 */
	public void removeInterface(PcapIf oldIf) {
		int index = -1;
		int tempIndex = 0;
		for(InterfaceStatistics e : this.interfaceStatistics) {
			if(e.equals(oldIf.getName())) {
				index=tempIndex;
				break;
			}
			++tempIndex;
		}
		if(index>=0) {
			this.interfaceStatistics.get(index).setInactive();
		}
	}

	/** Searches for an interface and checks if it is marked as active
	 * @param newIf The interface to search for
	 * @return True if newIf exists and is marked as active, false otherwise
	 */
	public Boolean containsActivePcapIf(PcapIf newIf) {
		int index = -1;
		int tempIndex = 0;
		for(InterfaceStatistics e : this.interfaceStatistics) {
			if(e.equals(newIf.getName())) {
				index=tempIndex;
				break;
			}
			++tempIndex;
		}
		if(index>=0) {
			return this.interfaceStatistics.get(index).getActive();
		}
		return false;
	}

	/** Marks all interfaces not present in the provided list as inactive.
	 * They are not actually deleted to preserve statistics
	 * @param newList The list of active interfaces
	 */
	public void removeNonexistentInterfaces(LinkedList<PcapIf> newList) {
		for(InterfaceStatistics stat : this.interfaceStatistics) {
			Boolean found = false;
			for(PcapIf iF : newList) {
				if(stat.getName().equals(iF.getName())) {
					found=true;
					break;
				}
			}
			if(!found) {
				stat.setInactive();
			}
		}
		
	}

	/** Adds the ip detection event to memory.<br>
	 * If an entry for this ip does not exist, it will be created. 
	 * Else the frequency of the old entry's frequency will be incremented by one.
	 * @param pat The malicious ip
	 * @param currIf The interface this detection occurred on
	 */
	public void addIpEvent(StringPattern pat, PcapIf currIf) {
		int index = -1;
		int tempIndex = 0;
		for(InterfaceStatistics e : this.interfaceStatistics) {
			if(e.equals(currIf.getName())) {
				index=tempIndex;
				break;
			}
			++tempIndex;
		}
		if(index>=0) {
			this.interfaceStatistics.get(index).addIpEvent(pat,currIf);
		}
	}

	/** Adds a list of string patterns that had a match to this interface's statistics. <br>
	 * If an event does not exist, it will be created. Else the frequeny of preexisting events will be incremented by one.
	 * @param collection A list of malicious string pattern detections to add to the interface
	 * @param currIf The interface the capture was made on. Used to determine the Interface's current IP.
	 */
	public void addStringEventCollection(LinkedList<StringPattern> collection, PcapIf currIf) {
		int index = -1;
		int tempIndex = 0;
		for(InterfaceStatistics e : this.interfaceStatistics) {
			if(e.equals(currIf.getName())) {
				index=tempIndex;
				break;
			}
			++tempIndex;
		}
		if(index>=0) {
			this.interfaceStatistics.get(index).addStringEventCollection(collection,currIf);
		}	
	}

	/**
	 * @return An iterator over the interfaces contained in the memory
	 */
	public Iterator<InterfaceStatistics> getInterfacesIterator() {
		return this.interfaceStatistics.iterator();
	}

}
