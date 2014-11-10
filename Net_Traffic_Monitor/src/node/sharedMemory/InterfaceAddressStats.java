/**
 * 
 */
package node.sharedMemory;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

/** Contains a list of detection event frequencies for an address of an interface.
 * Null means no address.
 * @author Parisbre56
 *
 */
public class InterfaceAddressStats {
	/** The address the interface had during detection. Null means no address.
	 */
	private final InetAddress addr;
	
	/** Detection events
	 * (string detected (StringPattern), number of detections (AtomicInteger))
	 */
	private final CopyOnWriteArrayList<DetectionFrequencyString> detectionStringFrequencies;
	
	/** Detection events
	 * (IP detected (InetAddress), number of detections (AtomicInteger))
	 */
	private final CopyOnWriteArrayList<DetectionFrequencyString> detectionIpFrequencies;
	

	/** Creates a new InterfaceAddressStat object with an empty detection event list.
	 * @param addr2 The address the interface had during detection. Null means no address.
	 * 
	 */
	public InterfaceAddressStats(InetAddress addr2) {
		this.addr=addr2;
		this.detectionStringFrequencies = new CopyOnWriteArrayList<>();
		this.detectionIpFrequencies = new CopyOnWriteArrayList<>();
	}

	/** Creates a copy of the provided InterfaceAddressStats. 
	 * <br>It is a deep copy so that modifying one doesn't influence the other.
	 * @param ad The InterfaceAddressStats to copy.
	 */
	public InterfaceAddressStats(InterfaceAddressStats ad) {
		this.addr=ad.addr;
		
		this.detectionStringFrequencies = new CopyOnWriteArrayList<>();
		for(DetectionFrequencyString dF : ad.detectionStringFrequencies) {
			this.detectionStringFrequencies.add(new DetectionFrequencyString(dF));
		}
		
		this.detectionIpFrequencies = new CopyOnWriteArrayList<>();
		for(DetectionFrequencyString dF : ad.detectionIpFrequencies) {
			this.detectionIpFrequencies.add(new DetectionFrequencyString(dF));
		}
	}

	/** Adds the provided malicious IP detection event to the event list.<br>
	 * If the event entry does not exist, it will be created. Else it will simply be incremented.
	 * @param pat The malicious IP address that was detected.
	 */
	public void addIpEvent(StringPattern pat) {
		int index = -1;
		int tempIndex = 0;
		for(DetectionFrequencyString e : this.detectionIpFrequencies) {
			if(e.equals(pat)) {
				index=tempIndex;
				break;
			}
			++tempIndex;
		}
		if(index>=0) {
			this.detectionIpFrequencies.get(index).addDetection();
		}
		else {
			DetectionFrequencyString a = new DetectionFrequencyString(pat);
			this.detectionIpFrequencies.add(a);
			a.addDetection();
		}
	}

	/** If the events do not exist they will be created.
	 * @param collection The string patterns that should have their frequencies incremented
	 */
	public void addStringEventCollection(LinkedList<StringPatternDetectionEvents> collection) {
		for(StringPatternDetectionEvents pat : collection) {
			int index = -1;
			int tempIndex = 0;
			for(DetectionFrequencyString e : this.detectionStringFrequencies) {
				if(e.equals(pat.pattern)) {
					index=tempIndex;
					break;
				}
				++tempIndex;
			}
			if(index>=0) {
				this.detectionStringFrequencies.get(index).addDetection(pat.matches);
			}
			else {
				DetectionFrequencyString a = new DetectionFrequencyString(pat.pattern);
				this.detectionStringFrequencies.add(a);
				a.addDetection(pat.matches);
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.addr == null) ? 0 : this.addr.hashCode());
		return result;
	}

	/** Checks if the addresses match for InterfaceAddressStats and InetAddress arguments<br>
	 * Returns true for null arguments<br>
	 * Otherwise is the same as Object.equals
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		else if (obj == null) {
			return this.addr==null;
		}
		else if (obj instanceof InterfaceAddressStats) {
			if(((InterfaceAddressStats) obj).addr!=null) {
				return ((InterfaceAddressStats) obj).addr.equals(this.addr);
			}
			return this.addr==null;
		}
		else if (obj instanceof InetAddress) {
			return ((InetAddress) obj).equals(this.addr);
		}
		else {
			System.err.println("DEBUG: Super called for an InterfaceAddressStats object's equals method.");
			new Exception().printStackTrace();
			return super.equals(obj);
		}
	}

	/**
	 * @return The address of the interface the capture was made on. Null means no address available.
	 */
	public InetAddress getAddress() {
		return this.addr;
	}

	/**
	 * @return An iterator over all the ip events for this address of this interface
	 */
	public Iterator<DetectionFrequencyString> getIpEventIterator() {
		return this.detectionIpFrequencies.iterator();
	}

	/**
	 * @return An iterator over all the string pattern events for this address of this interface
	 */
	public Iterator<DetectionFrequencyString> getStringEventIterator() {
		return this.detectionStringFrequencies.iterator();
	}
}
