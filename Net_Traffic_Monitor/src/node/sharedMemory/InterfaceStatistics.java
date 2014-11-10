/**
 * 
 */
package node.sharedMemory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jnetpcap.PcapAddr;
import org.jnetpcap.PcapIf;
import org.jnetpcap.PcapSockAddr;

/**
 * @author Parisbre56
 *
 */
public class InterfaceStatistics {
	/** 
	 * The name of the interface
	 */
	private final String interfaceName;
	/**List with detection events for each ip of interface 
	 * (interface ip during detection, detection list) 
	 */
	private final CopyOnWriteArrayList<InterfaceAddressStats> interfaceAdressesStats;
	/** Whether or not this interface is marked as active
	 */
	private final AtomicBoolean isActive;

	/** Creates an empty statistic entry for this interface 
	 * @param name The name of the interface
	 */
	public InterfaceStatistics(String name) {
		this.interfaceName = new String(name);
		this.interfaceAdressesStats = new CopyOnWriteArrayList<>();
		this.isActive = new AtomicBoolean(true);
	}

	/** Copies this statistic entry for this interface<br>
	 * A deep copy is made so that this can be modified without changing the original.
	 * @param iS
	 */
	public InterfaceStatistics(InterfaceStatistics iS) {
		this.interfaceName = new String(iS.interfaceName);
		
		this.isActive = new AtomicBoolean(iS.isActive.get());
		
		this.interfaceAdressesStats = new CopyOnWriteArrayList<>();
		for(InterfaceAddressStats ad : iS.interfaceAdressesStats) {
			this.interfaceAdressesStats.add(new InterfaceAddressStats(ad));
		}
	}

	/** Marks the interface as active
	 */
	public void setActive() {
		this.isActive.lazySet(true);
	}

	/** Marks the interface as inactive
	 */
	public void setInactive() {
		this.isActive.lazySet(false);
	}

	/**
	 * @return True if the interface is marked as active, false otherwise
	 */
	public Boolean getActive() {
		return this.isActive.get();
	}

	/**
	 * @return The name of the interface
	 */
	public String getName() {
		return this.interfaceName;
	}

	/** Adds the detection of the provided IP.<br>
	 * If an entry already exists, its frequency will be incremented by one, else one will be created.
	 * @param pat The malicious IP.
	 * @param currIf The interface this capture was made on. Used to find the interface's IP.
	 */
	public void addIpEvent(StringPattern pat, PcapIf currIf) {
		//First search for the interface's address. First one found should do. If no suitable address is found, null is used.
		InetAddress addr=null;
		for(PcapAddr ad : currIf.getAddresses()) {
			if(ad.getAddr().getFamily()==PcapSockAddr.AF_INET || ad.getAddr().getFamily()==PcapSockAddr.AF_INET6) {
				try {
					addr = InetAddress.getByAddress(ad.getAddr().getData());
					break;
				} catch (UnknownHostException e) {
					System.err.println("ERROR: Unable to determine interface "+currIf.getName()+" address. "
							+ "No address will be supplied for this detection event.");
					e.printStackTrace();
				}
			}
		}
		
		
		//Then search for the appropriate entry for that address (or create it if it doesn't exist) and insert it.
		InterfaceAddressStats stat = new InterfaceAddressStats(addr);
		int index = -1;
		int tempIndex = 0;
		for(InterfaceAddressStats e : this.interfaceAdressesStats) {
			if(e.equals(stat)) {
				index=tempIndex;
				break;
			}
			++tempIndex;
		}
		if(index>=0) {
			this.interfaceAdressesStats.get(index).addIpEvent(pat);
		}
		else {
			this.interfaceAdressesStats.add(stat);
			stat.addIpEvent(pat);
		}
	}

	/** Adds a list of string patterns that had a match to this interface's statistics. <br>
	 * If an event does not exist, it will be created. Else the frequeny of preexisting events will be incremented by one.
	 * @param collection A list of malicious string pattern detections to add to the interface
	 * @param currIf The interface the capture was made on. Used to determine the Interface's current IP.
	 */
	public void addStringEventCollection(LinkedList<StringPatternDetectionEvents> collection,
			PcapIf currIf) {
		//First search for the interface's address. First one found should do. If no suitable address is found, null is used.
		InetAddress addr=null;
		for(PcapAddr ad : currIf.getAddresses()) {
			if(ad.getAddr().getFamily()==PcapSockAddr.AF_INET || ad.getAddr().getFamily()==PcapSockAddr.AF_INET6) {
				try {
					addr = InetAddress.getByAddress(ad.getAddr().getData());
					break;
				} catch (UnknownHostException e) {
					System.err.println("ERROR: Unable to determine interface "+currIf.getName()+" address. "
							+ "No address will be supplied for this detection event.");
					e.printStackTrace();
				}
			}
		}
		
		//Then search for the appropriate entry for that address (or create it if it doesn't exist) and insert it.
		InterfaceAddressStats stat = new InterfaceAddressStats(addr);
		int index = -1;
		int tempIndex = 0;
		for(InterfaceAddressStats e : this.interfaceAdressesStats) {
			if(e.equals(stat)) {
				index=tempIndex;
				break;
			}
			++tempIndex;
		}
		if(index>=0) {
			this.interfaceAdressesStats.get(index).addStringEventCollection(collection);
		}
		else {
			this.interfaceAdressesStats.add(stat);
			stat.addStringEventCollection(collection);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.interfaceName == null) ? 0 : this.interfaceName.hashCode());
		return result;
	}

	/** If the object is an InterfaceStatistics, then this returns true if their names match.<br>
	 * If the object is a String, then this returns true if the String matches the name of this interface.<br>
	 * Otherwise acts the same as Object.equals
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		else if (obj == null) {
			return false;
		}
		else if (obj instanceof InterfaceStatistics) {
			return ((InterfaceStatistics) obj).interfaceName.equals(this.interfaceName);
		}
		else if (obj instanceof String) {
			return ((String) obj).equals(this.interfaceName);
		}
		else {
			System.err.println("DEBUG: Super called for an InterfaceStatistics object's equals method.");
			new Exception().printStackTrace();
			return super.equals(obj);
		}
	}

	/**
	 * @return An iterator over all the addresses for this interface. Remember to check for name==null
	 */
	public Iterator<InterfaceAddressStats> getAddressIterator() {
		return this.interfaceAdressesStats.iterator();
	}
	
	
}
