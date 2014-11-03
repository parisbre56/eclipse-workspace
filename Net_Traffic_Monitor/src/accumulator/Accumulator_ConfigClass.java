/**
 * 
 */
package accumulator;

import exceptions.NTMonConfigException;

/**
 * @author Parisbre56
 *
 */
public class Accumulator_ConfigClass {

	private int accumulatorPort;
	private int socketTimeout;
	private int timeoutTimes;
	
	private static final int defaultAccumulatorPort = 24242;
	private static final int defaultSocketTimeout = 10;
	private static final int defaultTimeoutTimes = 3;
	
	/**
	 * 
	 */
	public Accumulator_ConfigClass() {
		try {
			setAccumulatorPort(defaultAccumulatorPort);
		} catch (NTMonConfigException e) {
			System.err.println("ERROR: Exception while setting Accumulator_ConfigClass default values");
			e.printStackTrace();
		}
		try {
			setSocketTimeout(defaultSocketTimeout);
		} catch (NTMonConfigException e) {
			System.err.println("ERROR: Exception while setting Accumulator_ConfigClass default values");
			e.printStackTrace();
		}
		try {
			setTimeoutTimes(defaultTimeoutTimes);
		}
		catch (NTMonConfigException e) {
			System.err.println("ERROR: Exception while setting Accumulator_ConfigClass default values");
			e.printStackTrace();
		}
	}

	/**
	 * @return The port the accumulator will listen in for new incoming connections
	 */
	public int getAccumulatorPort() {
		return this.accumulatorPort;
	}
	
	/**
	 * @param port The port the accumulator will listen in for new incoming connections. 
	 * Must be greater than or equal to 0 and less than or equal 65535
	 * @throws NTMonConfigException If the port is not within acceptable limits
	 */
	public void setAccumulatorPort(int port) throws NTMonConfigException {
		if (port<0 || port>65535) {
			throw new NTMonConfigException(Integer.toString(port)+" is not a valid port number");
		}
		this.accumulatorPort=port;
	}

	/**
	 * @return How long a socket should wait for an incoming connection in seconds.
	 */
	public int getSocketTimeout() {
		return this.socketTimeout;
	}
	
	/**
	 * @param time How long a socket will wait for an incoming connection. 
	 * Must be greater than 0 so that the process will periodically check for the exit condition.
	 * @throws NTMonConfigException If a value less than or equal to 0 was given.
	 */
	public void setSocketTimeout(int time) throws NTMonConfigException {
		if(time<=0) {
			throw new NTMonConfigException(Integer.toString(time)+" is not a valid socket timeout");
		}
		this.socketTimeout=time;
	}

	/**
	 * @return The number of times a connection will timeout before it is marked as disconnected
	 */
	public int getTimeoutTimes() {
		return this.timeoutTimes;
	}

	/**
	 * @param timeoutTimes1 The number of times a connection will timeout before it is marked as disconnected
	 * @throws NTMonConfigException If a value less than 0 was given
	 */
	public void setTimeoutTimes(int timeoutTimes1) throws NTMonConfigException {
		if(timeoutTimes1<0) {
			throw new NTMonConfigException(Integer.toString(timeoutTimes1)+" is not a valid number of timeouts");
		}
		this.timeoutTimes=timeoutTimes1;
	}
}
