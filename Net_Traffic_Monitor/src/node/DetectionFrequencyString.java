/**
 * 
 */
package node;

/**
 * @author Parisbre56
 *
 */
public class DetectionFrequencyString extends DetectionFrequency {
	/** The pattern  that matched the detection event.
	 */
	private final StringPattern match;

	/** Frequency is initialized to 0.
	 * @param match1 The pattern that matched
	 * 
	 */
	public DetectionFrequencyString(StringPattern match1) {
		super();
		this.match=match1;
	}
	
	/** Creates a copy of the object. 
	 * Frequency is copied so that changes to the old object are not carried over to the new one.
	 * @param dF The detection event to copy
	 */
	public DetectionFrequencyString(DetectionFrequencyString dF) {
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

	/** If object is a DetectionFrequencyString object, this makes sure that the string pattern matches.<br>
	 * If object is a StringPattern object, this makes sure that the string pattern matches.<br>
	 * If object is a String, this makes sure that the string matches the one in the string pattern.<br>
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
		else if (object.getClass()==DetectionFrequencyString.class) {
			return ((DetectionFrequencyString) object).match.equals(this.match);
		}
		else if (object.getClass()==StringPattern.class) {
			return ((StringPattern) object).equals(this.match);
		}
		else if (object.getClass()==String.class) {
			return ((String) object).equals(this.match.pattern);
		}
		else {
			System.err.println("DEBUG: Super called for a DetectionFrequencyString object's equals method.");
			return super.equals(object);
		}
	}

	
}
