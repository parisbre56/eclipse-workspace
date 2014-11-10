/**
 * 
 */
package node.sharedMemory;

import java.util.concurrent.atomic.AtomicInteger;

/** Just holds an atomic integer for the frequency
 * @author Parisbre56
 *
 */
public class DetectionFrequency {
	/** How many times this has been detected.
	 */
	private AtomicInteger frequency;

	/** Creates a new detection frequency object
	 */
	public DetectionFrequency() {
		this.frequency=new AtomicInteger(0);
	}
	
	/** Copy constructor, just creates a new atomic integer for frequency.
	 * @param dF The detection frequency to copy
	 */
	public DetectionFrequency(DetectionFrequency dF) {
		this.frequency=new AtomicInteger(dF.frequency.get());
	}
	
	/** Increment the detection frequency by the given value for this event
	 * @param matches How many times this pattern was detected
	 */
	public void addDetection(int matches) {
		this.frequency.addAndGet(matches);
	}
	
	/** Increment the detection frequency by one for this event
	 */
	public void addDetection() {
		this.frequency.incrementAndGet();
	}
	
	/**
	 * @return The number of detections for this event
	 */
	public int getFrequency() {
		return this.frequency.get();
	}

}
