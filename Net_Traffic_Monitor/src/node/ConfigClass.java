/**
 * 
 */
package node;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;

import org.jnetpcap.Pcap;

import exceptions.NTMonConfigException;
import exceptions.NTMonException;
import exceptions.NTMonIAddrException;

/** Configuration class. All intervals are in seconds
 * @author Parisbre56
 *
 */
public class ConfigClass {
	//Values
	/** The client's id. Note that the Id might change while connecting.
	 */
	private String Id;
	/** The server's address
	 */
	private InetAddress accumulatorAddress;
	/** The server's port.
	 */
	private Integer Port;
	/** How often the client will communicate with the server. 
	 * How long until the server declares that the client has timed out.
	 */
	private Integer refreshRate;
	/** The number of bytes that will be captured. 
	 * An integer greater than or equal to 65535 should be sufficient. 
	 */
	private Integer snaplen;
	/** Timeout for packet retrieval. 0 means no timeout
	 */
	private Integer snapTimeout;
	/** Number of packets that will be processed in each loop before exit conditions are checked. 
	 * -1 means loop until exit conditions are met.
	 */
	private Integer packetBatchSize;
	/** How may reconnect attempts should be made before the client exits.
	 */
	private Integer reconnectAttempts;
	
	
	//Default values
	private static final String defaultAccumulatorIP = "127.0.0.1";
	private static final Integer defaultAccumulatorPort = 24242;
	private static final String defaultId = "Unknown";
	private static final Integer defaultRefreshRate = 50;
	private static final Integer defaultSnaplen = 65535;
	private static final Integer defaultSnapTimeout = 10;
	private static final Integer defaultPacketBatchSize = 50;
	private static final Integer defaultReconnectAttempts = 10;
	
	/**
	 * @throws NTMonException 
	 * 
	 */
	public ConfigClass() throws NTMonException {
		//Initialize accumulator address to default
		try {
			setAccumulatorAddress(InetAddress.getByName(defaultAccumulatorIP));
		} catch (UnknownHostException e) {//Should never happen
			System.err.println("ERROR: Cannot find host "+defaultAccumulatorIP); 
			e.printStackTrace();
			throw new NTMonIAddrException("Cannot find host "+defaultAccumulatorIP,e);
		}
		//Initialize accumulator port to default
		setAccumulatorPort(defaultAccumulatorPort);
		//Initialize system id to default
		setId(System.getenv("HOSTNAME"));
		if(getId() == null) {
			setId(System.getenv("computername"));
		}
		if(getId() == null) {
			setId(defaultId);
		}
		//Try to put the MAC address in the default id
		try {
			setId(getId()+NetworkInterface.getNetworkInterfaces().nextElement().getHardwareAddress().toString());
		} catch (Exception e) {
			System.err.println("DEBUG: Error while attempting to get MAC address.");
			e.printStackTrace();
		}
		//Initialize refresh rate to default
		setRefreshRate(defaultRefreshRate);
		
		//Initialize capture length to default
		setSnaplen(defaultSnaplen);
		
		//Initialize capture timeout to default
		setSnapTimeout(defaultSnapTimeout);
		
		///Initialize packet batch size to default
		setPacketBatchSize(defaultPacketBatchSize);
		
		//Initialize number of reconnect attempts to default
		setReconnectAttempts(defaultReconnectAttempts);
	}

	/**
	 * @return the accumulatorAddress
	 */
	public InetAddress getAccumulatorAddress() {
		return this.accumulatorAddress;
	}

	/** The server's address
	 * @param accumulatorAddress1 the accumulatorAddress to set
	 */
	public void setAccumulatorAddress(InetAddress accumulatorAddress1) {
		this.accumulatorAddress = accumulatorAddress1;
	}
	
	/** The server's address
	 * @param accumulatorAddress1 the accumulatorAddress to set. Can be hostname or textual representation of IP
	 * @throws NTMonIAddrException if no IP can be found for this host
	 */
	public void setAccumulatorAddress(String accumulatorAddress1) throws NTMonIAddrException {
		try {
			setAccumulatorAddress(InetAddress.getByName(accumulatorAddress1));
		} catch (UnknownHostException e) {//Should never happen
			System.err.println("ERROR: Cannot find host "+accumulatorAddress1); 
			e.printStackTrace();
			throw new NTMonIAddrException("Cannot find host "+accumulatorAddress1,e);
		}
	}

	/** The client's id. Note that the Id might change while connecting.
	 * @return the id
	 */
	public String getId() {
		return this.Id;
	}

	/** The client's id. Note that the Id might change while connecting.
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.Id = id;
	}

	/** The server's port
	 * @return the port
	 */
	public Integer getAccumulatorPort() {
		return this.Port;
	}

	/** The server's port
	 * @param port the port to set
	 * @throws NTMonConfigException if an invalid port was supplied
	 */
	public void setAccumulatorPort(Integer port) throws NTMonConfigException {
		if (port<0 || port>65535) {
			throw new NTMonConfigException(port.toString()+" is not a valid port number");
		}
		this.Port = port;
	}

	/** How often the client will communicate with the server. 
	 * How long until the server declares that the client has timed out.
	 * @return the refreshRate
	 */
	public Integer getRefreshRate() {
		return this.refreshRate;
	}

	/** How often the client will communicate with the server. 
	 * How long until the server declares that the client has timed out.
	 * @param refreshRate1 the refreshRate to set. Must be greater than 0
	 * @throws NTMonConfigException If refresh rate is less than or equal to zero
	 */
	public void setRefreshRate(Integer refreshRate1) throws NTMonConfigException {
		if(refreshRate1<=0) {
			throw new NTMonConfigException(refreshRate1.toString()+" is not a valid refresh rate");
		}
		this.refreshRate = refreshRate1;
	}

	/** The number of bytes that will be captured. 
	 * An integer greater than or equal to 65535 should be sufficient.
	 * @return the snaplen
	 */
	public Integer getSnaplen() {
		return this.snaplen;
	}

	/** The number of bytes that will be captured. 
	 * An integer greater than or equal to 65535 should be sufficient.
	 * @param snaplen1 the snaplen to set. Must be greater than 0
	 * @throws NTMonConfigException if snaplen is less than or equal to 0
	 */
	public void setSnaplen(Integer snaplen1) throws NTMonConfigException {
		if(snaplen1<=0) {
			throw new NTMonConfigException(snaplen1.toString()+" is not a valid number of bytes for capture");
		}
		this.snaplen = snaplen1;
	}

	/** Timeout for packet retrieval. 0 means no timeout
	 * @return the snapTimeout
	 */
	public Integer getSnapTimeout() {
		return this.snapTimeout;
	}

	/** Timeout for packet retrieval. 0 means no timeout
	 * @param snapTimeout1 the snapTimeout to set. Must be greater than or equal to 0
	 * @throws NTMonConfigException If snapTimeout is less than 0
	 */
	public void setSnapTimeout(Integer snapTimeout1) throws NTMonConfigException {
		if(snapTimeout1<0) {
			throw new NTMonConfigException(snapTimeout1.toString()+" is not a valid capture timeout");
		}
		this.snapTimeout = snapTimeout1;
	}

	/** Number of packets that will be processed in each loop before exit conditions are checked. 
	 * -1 means loop until exit conditions are met.
	 * @return the packetBatchSize
	 */
	public Integer getPacketBatchSize() {
		return this.packetBatchSize;
	}

	/** Number of packets that will be processed in each loop before exit conditions are checked. 
	 * This speeds up the processing of packets, since many packets are loaded to memory at once, instead of one at a time.
	 * Setting this to org.jnetpcap.Pcap.LOOP_INFINITE (-1) means loop until exit conditions are met.
	 * @param packetBatchSize1 the packetBatchSize to set. Must be greatar than 0 or equal to org.jnetpcap.Pcap.LOOP_INFINITE (-1) 
	 * @throws NTMonConfigException If packetBatchSize is less than or equal to zero and not equal to 
	 * org.jnetpcap.Pcap.LOOP_INFINITE (-1)
	 */
	public void setPacketBatchSize(Integer packetBatchSize1) throws NTMonConfigException {
		if(packetBatchSize1<=0 && packetBatchSize1!=Pcap.LOOP_INFINITE) {
			throw new NTMonConfigException(packetBatchSize1.toString()+" is not a valid packet batch size");
		}
		this.packetBatchSize = packetBatchSize1;
	}

	/**
	 * @return the reconnectAttempts
	 */
	public Integer getReconnectAttempts() {
		return this.reconnectAttempts;
	}

	/**
	 * @param reconnectAttempts1 the reconnectAttempts to set
	 */
	public void setReconnectAttempts(Integer reconnectAttempts1) {
		this.reconnectAttempts = reconnectAttempts1;
	}

	/** Writes the configuration to the provided file
	 * @param configFile The file the configuration will be written in. It will be created if it does not exist.
	 */
	public void createConfigFile(File configFile) {
		// TODO Auto-generated method stub
		
	}

	/** Loads the configuration from the provided file
	 * @param configFile The file the configuration will be loaded from.
	 */
	public void loadConfigFile(File configFile) {
		// TODO Auto-generated method stub
		
	}

}
