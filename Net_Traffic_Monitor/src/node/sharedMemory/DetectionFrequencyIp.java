/**
 * 
 */
package node.sharedMemory;

import java.net.InetAddress;

/**
 * @author Parisbre56
 *
 */
public class DetectionFrequencyIp extends DetectionFrequency {
	/** The pattern  that matched the detection event.
	 */
	private final InetAddress match;

	/** Frequency is initialized to 0
	 * @param match1 The pattern that matched
	 * 
	 */
	public DetectionFrequencyIp(InetAddress match1) {
		super();
		this.match=match1;
	}
	
	/** Creates a copy of the object. 
	 * Frequency is copied so that changes to the old object are not carried over to the new one.
	 * @param dF The detection event to copy
	 */
	public DetectionFrequencyIp(DetectionFrequencyIp dF) {
		super(dF);
		this.match=dF.match;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.match == null) ? 0 : this.match.hashCode());
		return result;
	}

	/** If object is a DetectionFrequencyIp object, this makes sure that the addresses match<br>
	 * If object is an InetAddress, this makes sure that the address matches the detected address<br>
	 * Else this behaves the same as Object.equals.<br>
	 */
	@Override
	public boolean equals (Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		else if (object.getClass()==DetectionFrequencyIp.class) {
			return ((DetectionFrequencyIp) object).match.equals(this.match);
		}
		else if (object.getClass()==InetAddress.class) {
			return ((InetAddress) object).equals(this.match);
		}
		else {
			System.err.println("DEBUG: Super called for a DetectionFrequencyIp object's equals method.");
			return super.equals(object);
		}
	}

}
