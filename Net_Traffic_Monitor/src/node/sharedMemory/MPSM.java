/**
 * 
 */
package node.sharedMemory;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

/** Holds all malicious IP patterns and malicious String patterns. Fully synchronized, no need for manual synchronization.
 * @author Parisbre56
 *
 */
public class MPSM {
	private CopyOnWriteArrayList<StringPattern> stringPatterns;
	private CopyOnWriteArrayList<StringPattern> ipPatterns;
	
	/** Contains the malicious IPs and string patterns.
	 * Removing a malicious string or IP also removes the statistics for it
	 */
	public MPSM() {
		this.stringPatterns = new CopyOnWriteArrayList<>();
		this.ipPatterns = new CopyOnWriteArrayList<>();
	}

	/** Creates a deep copy of the provided parameter
	 * @param mpsm
	 */
	public MPSM(MPSM mpsm) {
		this.stringPatterns=new CopyOnWriteArrayList<>();
		for (StringPattern sP : mpsm.stringPatterns) {
			this.stringPatterns.add(new StringPattern(sP));
		}
		
		this.ipPatterns=new CopyOnWriteArrayList<>();
		for (StringPattern sP : mpsm.ipPatterns) {
			this.ipPatterns.add(new StringPattern(sP));
		}
	}

	/**
	 * @param data The malicious string pattern to add to memory. If it already exists, it just sets it as active.
	 */
	public void addString(String data) {
		//synchronized(stringPatterns) {
			int index = -1;
			int tempIndex = 0;
			for(StringPattern e : this.stringPatterns) {
				if(e.equals(data)) {
					index=tempIndex;
					break;
				}
				++tempIndex;
			}
			if(index<0) {
				this.stringPatterns.add(new StringPattern(data));
			}
			else {
				this.stringPatterns.get(index).setActive();
			}
		//}
	}
	
	/**
	 * Sets it as inactive. Does not actually delete it so that they can be used for statistics
	 * @param data The string to mark as inactive
	 */
	public void removeString(String data) {
		//synchronized(stringPatterns) {
			int index = -1;
			int tempIndex = 0;
			for(StringPattern e : this.stringPatterns) {
				if(e.equals(data)) {
					index=tempIndex;
					break;
				}
				++tempIndex;
			}
			if(index>=0) {
				this.stringPatterns.get(index).setInactive();
			}
		//}
	}
	
	/**
	 * @param data The ip to mark as active.
	 */
	public void addIP(String data) {
		//synchronized(ipPatterns) {
			int index = -1;
			int tempIndex = 0;
			for(StringPattern e : this.ipPatterns) {
				if(e.equals(data)) {
					index=tempIndex;
					break;
				}
				++tempIndex;
			}
			if(index<0) {
				this.ipPatterns.add(new StringPattern(data));
			}
			else {
				this.ipPatterns.get(index).setActive();
			}
		//}
	}

	/**
	 * Sets it as inactive. Does not actually delete it so that they can be used for statistics
	 * @param data The IP to set as inactive.
	 */
	public void removeIP(String data) {
		//synchronized(ipPatterns) {
			int index = -1;
			int tempIndex = 0;
			for(StringPattern e : this.ipPatterns) {
				if(e.equals(data)) {
					index=tempIndex;
					break;
				}
				++tempIndex;
			}
			if(index>=0) {
				this.ipPatterns.get(index).setInactive();
			}
		//}
	}

	/**
	 * Sets all as inactive. Does not actually delete them so that they can be used for statistics
	 */
	public void setAllInactive() {
		for(StringPattern p : this.stringPatterns) {
			p.setInactive();
		}
		for(StringPattern p : this.ipPatterns) {
			p.setInactive();
		}
	}

	/**
	 * @param addrSource The address to search for
	 * @return True if the memory contains the address, else false.
	 */
	public Boolean containsIP(InetAddress addrSource) {
		for(StringPattern e : this.ipPatterns) {
			if(e.equals(addrSource)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks all patterns and returns a collection of all regular expression patterns that had a match in the provided string
	 * @param string The string containing the data that will be checked for matches
	 * @return a collection of all regular expression patterns that had at least one match in the provided string.
	 */
	public LinkedList<StringPattern> getMatchingStringCollection(String string) {
		LinkedList<StringPattern> retVal = new LinkedList<>();
		for(StringPattern p : this.stringPatterns) {
			//If something in the string matches this pattern, add this pattern to the list
			if(Pattern.compile(p.pattern).matcher(string).find()) {
				retVal.add(p);
			}
		}
		return retVal;
	}

	/**
	 * @param addrSource The IP pattern to search for
	 * @return The malicious IP pattern matching this address, else null
	 */
	public StringPattern getMatchingIP(InetAddress addrSource) {
		int index = -1;
		int tempIndex = 0;
		for(StringPattern e : this.ipPatterns) {
			if(e.equals(addrSource)) {
				index=tempIndex;
				break;
			}
			++tempIndex;
		}
		if(index>=0) {
			return this.ipPatterns.get(index);
		}
		return null;
	}

}
