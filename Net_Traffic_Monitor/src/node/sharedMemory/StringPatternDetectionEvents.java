package node.sharedMemory;

/** A simple pair for storing detections of malicious patterns
 * @author Parisbre56
 */
public class StringPatternDetectionEvents {
	/** The pattern that was found
	 */
	public final StringPattern pattern;
	/** The number of times the pattern was found
	 */
	public final int matches;

	/** Creates a simple pair for storing malicious pattern match result and frequency
	 * @param patternString The pattern detected
	 * @param matchesNum The number of times the pattern was found
	 */
	public StringPatternDetectionEvents(StringPattern patternString, int matchesNum) {
		this.pattern=patternString;
		this.matches=matchesNum;
	}

}
