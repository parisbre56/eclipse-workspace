/**
 * 
 */
package node.sharedMemory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Parisbre56
 *
 */
public class StringPattern {
	/** The malicious pattern.
	 */
	public String pattern;
	/** Whether or not this pattern is active.
	 */
	public AtomicBoolean active;

	/**
	 * @param pattern1 The pattern to create
	 */
	public StringPattern(String pattern1) {
		this.pattern=new String(pattern1);
		this.active=new AtomicBoolean(true);
	}
	/**
	 * Copy constructor. Creates a deep copy of the original
	 * @param stringPattern The pattern to copy
	 */
	public StringPattern(StringPattern stringPattern) {
		this.pattern=new String(stringPattern.pattern);
		this.active=new AtomicBoolean(stringPattern.active.get());
	}
	
	/** 
	 * If the object is a StringPattern, it checks if the pattern of the two StringPatterns strings match<br>
	 * If the object is a String, it checks if the pattern in StringPattern matches the string<br>
	 * If the object is an InetAddress, it checks if any of the IP addresses 
	 * 		associated with this pattern match the provided IP address. Note that
	 * 		this is a best effort, so false might also be returned be returned if 
	 * 		the ip addresses associated with this pattern cannot be retrieved for some reason<br>
	 * Else acts the same as java.lang.Object.equals
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		else if(object.getClass()==StringPattern.class) {
			return this.pattern.equals(((StringPattern) object).pattern);
		}
		else if (object.getClass()==String.class) {
			return this.pattern.equals(object);
		}
		else if (object.getClass()==InetAddress.class) {
			try {
				return Arrays.asList(InetAddress.getAllByName(this.pattern)).contains(object);
			} catch (UnknownHostException e) {
				System.err.println("ERROR: Cannot find ip of host "+this.pattern+" during equals operation");
				e.printStackTrace();
				return false;
			}
		}
		else {
			System.err.println("DEBUG: Super called for a StringPattern object's equals method.");
			return super.equals(object);
		}
	}
	
	/** Returns the hash value of the string contained within this object
	 */
	@Override
	public int hashCode() {
		return this.pattern.hashCode(); 
	}

	/** Marks this pattern as active
	 */
	public void setActive() {
		//Since this is a singe writer implementation, 
		//lazySet should make this faster (assuming the server does not send inconsistent requests)
		this.active.lazySet(true);
	}

	/** Marks that pattern as inactive
	 */
	public void setInactive() {
		//Since this is a singe writer implementation, 
		//lazySet should make this faster (assuming the server does not send inconsistent requests)
		this.active.lazySet(false);
	}
}
