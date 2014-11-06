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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isString ? 1231 : 1237);
		result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MaliciousPattern other = (MaliciousPattern) obj;
		if (isString != other.isString) {
			return false;
		}
		if (pattern == null) {
			if (other.pattern != null) {
				return false;
			}
		} else if (!pattern.equals(other.pattern)) {
			return false;
		}
		return true;
	}

}
