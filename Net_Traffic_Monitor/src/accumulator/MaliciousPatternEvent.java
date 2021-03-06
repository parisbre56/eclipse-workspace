package accumulator;

/** An event representing the addition or the deletion of a malicious pattern
 * @author Parisbre56
 *
 */
public class MaliciousPatternEvent {
	private final MaliciousPattern pattern;
	private final boolean isAddition; 

	/**
	 * @param pattern1 The malicious pattern, be it string or IP
	 * @param isAddition1 True if this is an addition event, false if this is a deletion event
	 */
	public MaliciousPatternEvent(MaliciousPattern pattern1, boolean isAddition1) {
		this.pattern=pattern1;
		this.isAddition=isAddition1;
	}

	/**
	 * @return True if this pattern represents a String, false if it represents an IP
	 */
	public boolean isString() {
		return this.pattern.isString();
	}

	/**
	 * @return True if this is an addition, false if this is a deletion
	 */
	public boolean isAddition() {
		return this.isAddition;
	}

	/**
	 * @return The string representing this pattern, be it IP or String
	 */
	public String getPattern() {
		return this.pattern.getPattern();
	}
}
