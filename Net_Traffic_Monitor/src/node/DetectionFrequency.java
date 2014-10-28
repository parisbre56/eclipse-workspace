/**
 * 
 */
package node;

import java.util.concurrent.atomic.AtomicInteger;

/**
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
	
	public DetectionFrequency(DetectionFrequency dF) {
		this.frequency=new AtomicInteger(dF.frequency.get());
	}
	
	/** Increment the detection frequency by one for this event
	 */
	public void addDetection() {
		this.frequency.incrementAndGet();
	}
	
	/**
	 * @return The number of detections for this event
	 */
	public Integer getFrequency() {
		return this.frequency.get();
	}

}
