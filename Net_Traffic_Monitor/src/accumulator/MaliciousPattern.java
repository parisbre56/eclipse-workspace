package accumulator;

/** A malicious pattern, be it IP or String
 * @author Parisbre56
 *
 */
public class MaliciousPattern {
	private final String pattern;
	private final boolean isString;

	/**
	 * @param pattern1 The string that represents this pattern, be it IP or String
	 * @param isString1 True if this represents a String pattern, false if this represents an IP pattern
	 */
	public MaliciousPattern(String pattern1, boolean isString1) {
		this.pattern = new String(pattern1);
		this.isString = isString1;
	}

	/**
	 * @return True if this represents a String pattern, false if this represents an IP pattern
	 */
	public boolean isString() {
		return this.isString;
	}

	/**
	 * @return A copy of the string that represents this pattern, be it IP or String
	 */
	public String getPattern() {
		return new String(this.pattern);
	}

}
