/**
 * 
 */
package node;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Properties;

import org.jnetpcap.Pcap;

import exceptions.NTMonConfigException;
import exceptions.NTMonException;
import exceptions.NTMonIAddrException;

/** Configuration class. Container class. All intervals are in seconds
 * @author Parisbre56
 *
 */
public class Node_ConfigClass {
	//Values
	/** The client's id. Note that the Id might change while connecting.
	 */
	private String Id;
	/** The server's address
	 */
	private InetAddress accumulatorAddress;
	/** The server's port. Must be greater than 0 and less than or equal to 65535
	 */
	private int accumulatorPort;
	/** How often the client will communicate with the server. 
	 * How long until the server declares that the client has timed out.
	 */
	private int refreshRate;
	/** The number of bytes that will be captured. 
	 * An integer greater than or equal to 65535 should be sufficient. 
	 */
	private int snaplen;
	/** Timeout for packet retrieval. 0 means no timeout
	 */
	private int snapTimeout;
	/** Number of packets that will be processed in each loop before exit conditions are checked. 
	 * -1 means loop until exit conditions are met.
	 */
	private int packetBatchSize;
	/** How may reconnect attempts should be made before the client fails without chance of recovery.
	 */
	private int reconnectAttempts;
	
	
	//Default values
	private static final String defaultAccumulatorIP = "127.0.0.1";
	private static final int defaultAccumulatorPort = 24242;
	private static final String defaultId = "Unknown";
	private static final int defaultRefreshRate = 50;
	private static final int defaultSnaplen = 65535;
	private static final int defaultSnapTimeout = 10;
	private static final int defaultPacketBatchSize = 50;
	private static final int defaultReconnectAttempts = 10;
	
	private static final String Id_String = "ID";
	private static final String accumulatorAddress_String = "ACCUMULATOR_ADDRESS";
	private static final String packetBatchSize_String = "PACKET_BATCH_SIZE";
	private static final String accumulatorPort_String = "ACCUMULATOR_PORT";
	private static final String reconnectAttempts_String = "RECONNECT_ATTEMPTS";
	private static final String refreshRate_String = "REFRESH_RATE";
	private static final String snaplen_String = "SNAP_LENGTH";
	private static final String snapTimeout_String = "SNAP_TIMEOUT";
	
	/**
	 * @throws NTMonException 
	 * 
	 */
	public Node_ConfigClass() {
		//Initialize accumulator address to default
		try {
			setAccumulatorAddress(InetAddress.getByName(defaultAccumulatorIP));
		} catch (UnknownHostException e) {//Should never happen
			System.err.println("ERROR: Cannot find default host "+defaultAccumulatorIP); 
			e.printStackTrace();
			//throw new NTMonIAddrException("Cannot find default host "+defaultAccumulatorIP,e);
		}
		//Initialize accumulator port to default
		try {
			setAccumulatorPort(defaultAccumulatorPort);
		} catch (NTMonConfigException e1) {
			System.err.println("ERROR: Cannot set the default host port "+Integer.toString(defaultAccumulatorPort));
			e1.printStackTrace();
		}
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
			byte [] mac = null;
			for(Enumeration<NetworkInterface> netIfIt = NetworkInterface.getNetworkInterfaces(); 
					netIfIt.hasMoreElements();) {
				mac = netIfIt.nextElement().getHardwareAddress();
				if(mac!=null && mac.length>0) {
					break;
				}
			}
			if(mac!=null) {
				StringBuilder sb = new StringBuilder(18);
			    for (byte b : mac) {
			        if (sb.length() > 0) {
			            sb.append(':');
			        }
			        System.err.println(String.format("%02x", b));
			        sb.append(String.format("%02x", b));
			    }
				setId(getId()+sb.toString());
			}
		} catch (Exception e) {
			System.err.println("DEBUG: Error while attempting to get MAC address.");
			e.printStackTrace();
		}
		//Initialize refresh rate to default
		try {
			setRefreshRate(defaultRefreshRate);
		} catch (NTMonConfigException e) {
			System.err.println("ERROR: Cannot set default refresh rate "+Integer.toString(defaultRefreshRate));
			e.printStackTrace();
		}
		
		//Initialize capture length to default
		try {
			setSnaplen(defaultSnaplen);
		} catch (NTMonConfigException e) {
			System.err.println("ERROR: Cannot set the default snaplen "+Integer.toString(defaultSnaplen));
			e.printStackTrace();
		}
		
		//Initialize capture timeout to default
		try {
			setSnapTimeout(defaultSnapTimeout);
		} catch (NTMonConfigException e) {
			System.err.println("ERROR: Cannot set the default snap timeout "+Integer.toString(defaultSnapTimeout));
			e.printStackTrace();
		}
		
		///Initialize packet batch size to default
		try {
			setPacketBatchSize(defaultPacketBatchSize);
		} catch (NTMonConfigException e) {
			System.err.println("ERROR: Cannot set the default packet batch size "+Integer.toString(defaultPacketBatchSize));
			e.printStackTrace();
		}
		
		//Initialize number of reconnect attempts to default
		try {
			setReconnectAttempts(defaultReconnectAttempts);
		} catch (NTMonConfigException e) {
			System.err.println("ERROR: Cannot set the default number of reconnect attempts "+Integer.toString(defaultReconnectAttempts));
			e.printStackTrace();
		}
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
	public int getAccumulatorPort() {
		return this.accumulatorPort;
	}

	/** The server's port
	 * @param port the port to set
	 * @throws NTMonConfigException if an invalid port was supplied
	 */
	public void setAccumulatorPort(int port) throws NTMonConfigException {
		if (port<0 || port>65535) {
			throw new NTMonConfigException(Integer.toString(port)+" is not a valid port number");
		}
		this.accumulatorPort = port;
	}

	/** How often the client will communicate with the server. 
	 * How long until the server declares that the client has timed out.
	 * @return the refreshRate
	 */
	public int getRefreshRate() {
		return this.refreshRate;
	}

	/** How often the client will communicate with the server. 
	 * How long until the server declares that the client has timed out.
	 * @param refreshRate1 the refreshRate to set. Must be greater than 0
	 * @throws NTMonConfigException If refresh rate is less than or equal to zero
	 */
	public void setRefreshRate(int refreshRate1) throws NTMonConfigException {
		if(refreshRate1<=0) {
			throw new NTMonConfigException(Integer.toString(refreshRate1)+" is not a valid refresh rate");
		}
		this.refreshRate = refreshRate1;
	}

	/** The number of bytes that will be captured. 
	 * An integer greater than or equal to 65535 should be sufficient.
	 * @return the snaplen
	 */
	public int getSnaplen() {
		return this.snaplen;
	}

	/** The number of bytes that will be captured. 
	 * An integer greater than or equal to 65535 should be sufficient.
	 * @param snaplen1 the snaplen to set. Must be greater than 0
	 * @throws NTMonConfigException if snaplen is less than or equal to 0
	 */
	public void setSnaplen(int snaplen1) throws NTMonConfigException {
		if(snaplen1<=0) {
			throw new NTMonConfigException(Integer.toString(snaplen1)+" is not a valid number of bytes for capture");
		}
		this.snaplen = snaplen1;
	}

	/** Timeout for packet retrieval. 0 means no timeout
	 * @return the snapTimeout
	 */
	public int getSnapTimeout() {
		return this.snapTimeout;
	}

	/** Timeout for packet retrieval. 0 means no timeout
	 * @param snapTimeout1 the snapTimeout to set. Must be greater than or equal to 0
	 * @throws NTMonConfigException If snapTimeout is less than 0
	 */
	public void setSnapTimeout(int snapTimeout1) throws NTMonConfigException {
		if(snapTimeout1<0) {
			throw new NTMonConfigException(Integer.toString(snapTimeout1)+" is not a valid capture timeout");
		}
		this.snapTimeout = snapTimeout1;
	}

	/** Number of packets that will be processed in each loop before exit conditions are checked. 
	 * -1 means loop until exit conditions are met.
	 * @return the packetBatchSize
	 */
	public int getPacketBatchSize() {
		return this.packetBatchSize;
	}

	/** Number of packets that will be processed in each loop before exit conditions are checked. 
	 * This speeds up the processing of packets, since many packets are loaded to memory at once, instead of one at a time.
	 * Setting this to org.jnetpcap.Pcap.LOOP_INFINITE (-1) means loop until exit conditions are met.
	 * @param packetBatchSize1 the packetBatchSize to set. Must be greatar than 0 or equal to org.jnetpcap.Pcap.LOOP_INFINITE (-1) 
	 * @throws NTMonConfigException If packetBatchSize is less than or equal to zero and not equal to 
	 * org.jnetpcap.Pcap.LOOP_INFINITE (-1)
	 */
	public void setPacketBatchSize(int packetBatchSize1) throws NTMonConfigException {
		if(packetBatchSize1<=0 && packetBatchSize1!=Pcap.LOOP_INFINITE) {
			throw new NTMonConfigException(Integer.toString(packetBatchSize1)+" is not a valid packet batch size");
		}
		this.packetBatchSize = packetBatchSize1;
	}

	/**
	 * @return the reconnectAttempts
	 */
	public int getReconnectAttempts() {
		return this.reconnectAttempts;
	}

	/**
	 * @param reconnectAttempts1 the reconnectAttempts to set
	 * @throws NTMonConfigException If the provided parameter was less than 0
	 */
	public void setReconnectAttempts(int reconnectAttempts1) throws NTMonConfigException {
		if(reconnectAttempts1<0) {
			throw new NTMonConfigException(Integer.toString(reconnectAttempts1)+" is not a valid reconnect attempts number");
		}
		this.reconnectAttempts = reconnectAttempts1;
	}

	/** Writes the configuration to the provided file
	 * @param configFile The file the configuration will be written in. It will be created if it does not exist.
	 * @throws IOException If it was unable to write to file
	 */
	public void createConfigFile(File configFile) throws IOException {
		Properties props = new Properties();
		
		//Set values
		props.setProperty(packetBatchSize_String, Integer.toString(getPacketBatchSize()));
		props.setProperty(Id_String, getId());
		props.setProperty(accumulatorAddress_String, getAccumulatorAddress().getHostName());
		props.setProperty(accumulatorPort_String, Integer.toString(getAccumulatorPort()));
		props.setProperty(refreshRate_String, Integer.toString(getRefreshRate()));
		props.setProperty(snapTimeout_String, Integer.toString(getSnapTimeout()));
		props.setProperty(snaplen_String, Integer.toString(getSnaplen()));
		props.setProperty(reconnectAttempts_String, Integer.toString(getReconnectAttempts()));
		
		//Save the properties file (try with resource)
		try (FileWriter writer = new FileWriter(configFile)) {
			props.store(writer, "Net Traffic Monitor Settings\n"+
					Id_String+": The client's id. Note that the Id might change while connecting.\n"+
					accumulatorAddress_String+": The server's address\n"+
					accumulatorPort_String+": The server's port. Must be greater than 0 and less than or equal to 65535\n"+
					refreshRate_String+":  How often the client will communicate with the server. "
							+ "How long until the server declares that the client has timed out.\n"+
					snaplen_String+": The number of bytes that will be captured. "
							+ "An integer greater than or equal to 65535 should be sufficient.\n"+
					snapTimeout_String+": Timeout for packet retrieval. 0 means no timeout.\n"+
					packetBatchSize_String+": Number of packets that will be processed in each loop "
							+ "before exit conditions are checked. -1 means loop until exit conditions are met.\n"+
					reconnectAttempts_String+": How may reconnect attempts should be made before the client fails "
							+ "without chance of recovery.\n"
							+ "============================#");
			writer.close();
		}
	}

	/** Loads the configuration from the provided file
	 * @param configFile The file the configuration will be loaded from.
	 * @throws IOException If there was a problem with reading the file
	 */
	public void loadConfigFile(File configFile) throws IOException {
		Properties props = new Properties();

		//Load the properties file (try with resource)
		try(FileReader reader = new FileReader(configFile)) {
			props.load(reader);
			reader.close();
		
			//Pass the input to the appropriate vars.
			String valString;
			
			//Id
			valString=props.getProperty(Id_String);
			if(valString!=null) {
				this.setId(valString);
			}
			
			//accumulatorAddress
			valString=props.getProperty(accumulatorAddress_String);
			if(valString!=null) {
				InetAddress tempAddr = null;
				try {
					tempAddr = InetAddress.getByName(valString);
				} catch (UnknownHostException e) {
					System.err.println("ERROR: Unable to resolve hostname for "+valString+
							" while reading the config file. The default will be used instead.");
					e.printStackTrace();
				}
				if(tempAddr!=null) {
					this.accumulatorAddress=tempAddr;
				}
			}
			
			//packetBatchSize
			valString=props.getProperty(packetBatchSize_String);
			if(valString!=null) {
				Integer tempInt= null;
				try {
					tempInt = Integer.decode(valString);
				}
				catch (NumberFormatException e) {
					System.err.println("ERROR: Invalid number in config file. "
							+valString+" is not a valid number.");
					e.printStackTrace();
				}
				if(tempInt!=null) {
					try {
						this.setPacketBatchSize(tempInt);
					} catch (NTMonConfigException e) {
						System.err.println("ERROR: Config file error");
						e.printStackTrace();
					}
				}
			}
			
			//Port
			valString=props.getProperty(accumulatorPort_String);
			if(valString!=null) {
				Integer tempInt= null;
				try {
					tempInt = Integer.decode(valString);
				}
				catch (NumberFormatException e) {
					System.err.println("ERROR: Invalid number in config file. "
							+valString+" is not a valid number.");
					e.printStackTrace();
				}
				if(tempInt!=null) {
					try {
						setAccumulatorPort(tempInt);
					} catch (NTMonConfigException e) {
						System.err.println("ERROR: Config file error");
						e.printStackTrace();
					}
				}
			}
			
			//reconnectAttempts
			valString=props.getProperty(reconnectAttempts_String);
			if(valString!=null) {
				Integer tempInt= null;
				try {
					tempInt = Integer.decode(valString);
				}
				catch (NumberFormatException e) {
					System.err.println("ERROR: Invalid number in config file. "
							+valString+" is not a valid number.");
					e.printStackTrace();
				}
				if(tempInt!=null) {
					try {
						this.setReconnectAttempts(tempInt);
					} catch (NTMonConfigException e) {
						System.err.println("ERROR: Config file error");
						e.printStackTrace();
					}
				}
			}
			
			//refreshRate
			valString=props.getProperty(refreshRate_String);
			if(valString!=null) {
				Integer tempInt= null;
				try {
					tempInt = Integer.decode(valString);
				}
				catch (NumberFormatException e) {
					System.err.println("ERROR: Invalid number in config file. "
							+valString+" is not a valid number.");
					e.printStackTrace();
				}
				if(tempInt!=null) {
					try {
						this.setRefreshRate(tempInt);
					} catch (NTMonConfigException e) {
						System.err.println("ERROR: Config file error");
						e.printStackTrace();
					}
				}
			}
			
			//snaplen
			valString=props.getProperty(snaplen_String);
			if(valString!=null) {
				Integer tempInt= null;
				try {
					tempInt = Integer.decode(valString);
				}
				catch (NumberFormatException e) {
					System.err.println("ERROR: Invalid number in config file. "
							+valString+" is not a valid number.");
					e.printStackTrace();
				}
				if(tempInt!=null) {
					try {
						this.setSnaplen(tempInt);
					} catch (NTMonConfigException e) {
						System.err.println("ERROR: Config file error");
						e.printStackTrace();
					}
				}
			}
			
			//snapTimeout
			valString=props.getProperty(snapTimeout_String);
			if(valString!=null) {
				Integer tempInt= null;
				try {
					tempInt = Integer.decode(valString);
				}
				catch (NumberFormatException e) {
					System.err.println("ERROR: Invalid number in config file. "
							+valString+" is not a valid number.");
					e.printStackTrace();
				}
				if(tempInt!=null) {
					try {
						this.setSnapTimeout(tempInt);
					} catch (NTMonConfigException e) {
						System.err.println("ERROR: Config file error");
						e.printStackTrace();
					}
				}
			}
		}
	}
}
