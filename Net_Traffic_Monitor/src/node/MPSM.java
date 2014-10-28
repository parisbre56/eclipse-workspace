/**
 * 
 */
package node;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

/**
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
		this.stringPatterns = new CopyOnWriteArrayList<StringPattern>();
		this.ipPatterns = new CopyOnWriteArrayList<StringPattern>();
	}

	public MPSM(MPSM mpsm) {
		this.stringPatterns=new CopyOnWriteArrayList<StringPattern>();
		for (StringPattern sP : mpsm.stringPatterns) {
			this.stringPatterns.add(new StringPattern(sP));
		}
		
		this.ipPatterns=new CopyOnWriteArrayList<StringPattern>();
		for (StringPattern sP : mpsm.ipPatterns) {
			this.ipPatterns.add(new StringPattern(sP));
		}
	}

	public void addString(String data) {
		//synchronized(stringPatterns) {
			Integer index = this.stringPatterns.indexOf(data);
			if(index<0) {
				this.stringPatterns.add(new StringPattern(data,true));
			}
			else {
				this.stringPatterns.get(index).setActive();
			}
		//}
	}
	
	/**
	 * Sets it as inactive. Does not actually delete it so that they can be used for statistics
	 */
	public void removeString(String data) {
		//synchronized(stringPatterns) {
			Integer index = this.stringPatterns.indexOf(data);
			if(index>=0) {
				this.stringPatterns.get(index).setInactive();
			}
		//}
	}
	
	public void addIP(String data) {
		//synchronized(ipPatterns) {
			Integer index = this.ipPatterns.indexOf(data);
			if(index<0) {
				this.ipPatterns.add(new StringPattern(data,true));
			}
			else {
				this.ipPatterns.get(index).setActive();
			}
		//}
	}

	/**
	 * Sets it as inactive. Does not actually delete it so that they can be used for statistics
	 */
	public void removeIP(String data) {
		//synchronized(ipPatterns) {
			Integer index = this.ipPatterns.indexOf(data);
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

	public Boolean containsIP(InetAddress addrSource) {
		if(this.ipPatterns.contains(addrSource)) {
			return true;
		}
		else {
			return false;
		}
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

	public StringPattern getMatchingIP(InetAddress addrSource) {
		Integer index = this.ipPatterns.indexOf(addrSource);
		if(index>=0) {
			return this.ipPatterns.get(index);
		}
		return null;
	}

}
