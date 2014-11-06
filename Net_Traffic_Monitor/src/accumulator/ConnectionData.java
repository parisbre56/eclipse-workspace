package accumulator;

import java.util.Iterator;
import java.util.LinkedList;

/** Holds data for a client connection. Fully concurrent, no synchronization required
 * @author Parisbre56
 *
 */
public class ConnectionData {
	private enum ConnectionState {
		/** The connection is marked as active
		 */
		ACTIVE,
		/** The connection is marked as timed out
		 */
		TIMED_OUT,
		/** The connection is marked as inactive
		 */
		DISCONNECTED
	}
	
	private ConnectionState connectionState;
	private volatile int timeout;
	private final LinkedList<MaliciousPatternEvent> dataQueue;

	/** Creates a connection at the default state (Disconnected) and with the default timeout 
	 */
	public ConnectionData() {
		this.connectionState = ConnectionState.DISCONNECTED;
		this.timeout = Accumulator_Main.accumulator_configClass.getSocketTimeout()+1;
		this.dataQueue = new LinkedList<>();
	}

	/**
	 * @return True if this connection is active, else false
	 */
	public boolean isActive() {
		synchronized(this.connectionState) {
			switch(this.connectionState) {
			case ACTIVE:
				return true;
			case DISCONNECTED:
			case TIMED_OUT:
			default:
				return false;
			}
		}
	}

	/**
	 * @return True if it was set to active succesfully, false otherwise
	 */
	public boolean setActive() {
		synchronized(this.connectionState) {
			switch(this.connectionState) {
			case ACTIVE:
				return false;
			case DISCONNECTED:
			case TIMED_OUT:
			default:
				this.connectionState=ConnectionState.ACTIVE;
				return true;
			}
		}
	}

	/** If this connection is active, it is marked as timed out
	 */
	public void setTimedOut() {
		synchronized(this.connectionState) {
			switch(this.connectionState) {
			case ACTIVE:
				this.connectionState=ConnectionState.TIMED_OUT;
				break;
			case DISCONNECTED:
			case TIMED_OUT:
			default:
				return;
			}
		}
	}

	/**
	 * @return The timeout for this connection.
	 */
	public int getTimeout() {
		return this.timeout;
	}

	/** Sets timeout to the provided value
	 * @param tempInt
	 */
	public void setTimeout(int tempInt) {
		this.timeout=tempInt;		
	}

	/**
	 * @return True if this connection is in the Disconnected state (NOT timedOut), false otherwise
	 */
	public boolean isDisconnected() {
		synchronized(this.connectionState) {
			switch(this.connectionState) {
			case DISCONNECTED:
				return true;
			case ACTIVE:
			case TIMED_OUT:
			default:
				return false;
			}
		}
	}

	/** Empties the incoming data queue and refills it with the provided collection
	 * @param iterator  An iterator over the provided collection's data
	 * @return A shallow copy of the data queue. The original is cleared of entries
	 */
	public LinkedList<MaliciousPatternEvent> refillDataQueue(Iterator<MaliciousPattern> iterator) {
		synchronized(this.dataQueue) {
			this.dataQueue.clear();
			while(iterator.hasNext()) {
				this.dataQueue.add(new MaliciousPatternEvent(iterator.next(),true));
			}
			return this.getDataQueueCopy();
		}
	}

	/**
	 * @return A shallow copy of the data queue. The original is cleared of entries
	 */
	public LinkedList<MaliciousPatternEvent> getDataQueueCopy() {
		LinkedList<MaliciousPatternEvent> retval;
		synchronized(this.dataQueue) {
			retval = new LinkedList<>(this.dataQueue);
			this.dataQueue.clear();
		}
		return retval;
	}

	/** Marks this as disconnected if this is set as timed out, else does nothing
	 */
	public void setDisconnectedIfTimedOut() {
		synchronized(this.connectionState) {
			switch(this.connectionState) {
			case TIMED_OUT:
				this.connectionState=ConnectionState.DISCONNECTED;
				break;
			case DISCONNECTED:
			case ACTIVE:
			default:
				return;
			}
		}
	}

	/** Inserts the pattern addition event in the event queue to be transferred to the client 
	 * @param pattern The pattern to insert in the queue
	 */
	public void patternAddedNotification(MaliciousPattern pattern) {
		synchronized (this.dataQueue) {
			this.dataQueue.add(new MaliciousPatternEvent(pattern, true));
		} 
	}

	/**
	 * @return True if this connection was not marked as disconnected before and is marked as disconnected now, false otherwise
	 */
	public boolean setDisconnectedIfNotDisconnected() {
		synchronized(this.connectionState) {
			switch(this.connectionState) {
			case DISCONNECTED:
				return false;
			case ACTIVE:
			case TIMED_OUT:
			default:
				this.connectionState=ConnectionState.DISCONNECTED;
				return true;
			}
		}
	}

}
