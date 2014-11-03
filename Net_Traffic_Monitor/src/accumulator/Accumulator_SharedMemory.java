package accumulator;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** Shared memory for the accumulator. Contains data about connections and malicious patterns
 * @author Parisbre56
 *
 */
public class Accumulator_SharedMemory {
	/** Holds all connection data. Key is client ID, object holds all additional data
	 */
	private final ConcurrentHashMap<String,ConnectionData> connectionData;
	
	private final CopyOnWriteArrayList<MaliciousPattern> maliciousPatterns;

	/** An empty shared memory object initialized with the default values
	 */
	public Accumulator_SharedMemory() {
		this.connectionData = new ConcurrentHashMap<>(20,0.75f,2);
		this.maliciousPatterns = new CopyOnWriteArrayList<>();
	}

	/** Tests whether or not a connection exists with that ID and if yes, returns whether or not the connection is active
	 * @param recID The ID to search for
	 * @return True if an active connection with that ID is in the memory, false otherwise 
	 */
	public boolean containsActiveID(String recID) {
		ConnectionData connInfo = this.connectionData.get(recID);
		if(connInfo!=null) {
			return connInfo.isActive();
		}
		return false;
	}

	/** Adds the connection to the list of active connections or marks it as active if it already exists. 
	 * If an active connection with that ID already exists, this does nothing and returns false
	 * @param recID The connection id to insert
	 * @return Returns true if the operation was completed successfully, false otherwise
	 */
	public boolean addConnection(String recID) {
		ConnectionData data = this.connectionData.putIfAbsent(recID, new ConnectionData());
		if(data==null) {
			return true;
		}
		if(data.isActive()) {
			return false;
		}
		return data.setActive(); 
	}

	/** Sets the refresh rate of a connection
	 * @param connID The ID of the connection to modify 
	 * @param tempInt The refresh rate to set for the connection
	 */
	public void setRefreshRate(String connID, int tempInt) {
		ConnectionData data = this.connectionData.get(connID);
		if(data!=null) {
			data.setTimeout(tempInt);
		}
	}

	/**
	 * @param connID The connection for which to retrieve the refrsh rate
	 * @return The refresh rate of the connection requested, in seconds
	 */
	public int getRefreshRate(String connID) {
		ConnectionData data = this.connectionData.get(connID);
		if(data!=null) {
			return data.getTimeout();
		}
		return Accumulator_Main.accumulator_configClass.getSocketTimeout()+1;
	}

	/** If this connection is active, it is marked as timed out. Else, nothing happens
	 * @param connID The connection to mark as timed out
	 */
	public void setTimedOut(String connID) {
		ConnectionData data = this.connectionData.get(connID);
		if(data!=null) {
			data.setTimedOut();
		}
	}

	/**
	 * @param recID The connection to search for
	 * @return True if a connection with this ID exists in memory and it its state is set to anything other than disconnected,
	 * false otherwise
	 */
	public boolean containsNotDisconnectedID(String recID) {
		ConnectionData data = this.connectionData.get(recID);
		if(data!=null) {
			return !data.isDisconnected();
		}
		return false;
	}

	/** 
	 * @param recID The ID of the connection to search for
	 * @return True if the connection with that ID is in memory, else false
	 */
	public boolean containsID(String recID) {
		return this.connectionData.containsKey(recID);
	}

	/** Empties the diff data queue for the connection and fills it with all known malicious patterns
	 * @param connID The ID of the connection for which to empty the queue.
	 * @return A shallow copy of the data queue. Null if the id was not found
	 */
	public LinkedList<MaliciousPatternEvent> refillDataQueue(String connID) {
		ConnectionData data = this.connectionData.get(connID);
		if(data!=null) {
			return data.refillDataQueue(this.maliciousPatterns.iterator());
		}
		return null;
	}

	/** Makes a shallow copy of the data queue and then empties it
	 * @param connID The id of the connection to search for.
	 * @return A shallow copy of the data queue. Null if the id was not found
	 */
	public LinkedList<MaliciousPatternEvent> getDataQueue(String connID) {
		ConnectionData data = this.connectionData.get(connID);
		if(data!=null) {
			return data.getDataQueueCopy();
		}
		return null;
	}

	public void setDisconnectedIfTimedOut(String connID) {
		ConnectionData data = this.connectionData.get(connID);
		if(data!=null) {
			data.setDisconnectedIfTimedOut();
		}
	}

}
