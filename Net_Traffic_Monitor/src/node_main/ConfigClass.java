/**
 * 
 */
package node_main;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import exceptions.NTMonException;
import exceptions.NTMonIAddrException;

/**
 * @author Parisbre56
 *
 */
public class ConfigClass {
	//Values
	private InetAddress accumulatorAddress;
	private String Id;
	private Integer Port;
	
	
	//Default values
	private static final String defaultAccumulatorIP = "127.0.0.1";
	private static final Integer defaultAccumulatorPort = 24242;
	private static final String defaultId = "Unknown";
	
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
		// TODO Initialize data to defaults
	}

	/**
	 * @return the accumulatorAddress
	 */
	public InetAddress getAccumulatorAddress() {
		return accumulatorAddress;
	}

	/**
	 * @param accumulatorAddress the accumulatorAddress to set
	 */
	public void setAccumulatorAddress(InetAddress accumulatorAddress) {
		this.accumulatorAddress = accumulatorAddress;
	}
	
	/**
	 * @param accumulatorAddress the accumulatorAddress to set
	 * @throws NTMonIAddrException if no IP can be found for this host
	 */
	public void setAccumulatorAddress(String accumulatorAddress) throws NTMonIAddrException {
		try {
			setAccumulatorAddress(InetAddress.getByName(accumulatorAddress));
		} catch (UnknownHostException e) {//Should never happen
			System.err.println("ERROR: Cannot find host "+accumulatorAddress); 
			e.printStackTrace();
			throw new NTMonIAddrException("Cannot find host "+accumulatorAddress,e);
		}
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return Id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		Id = id;
	}

	/**
	 * @return the port
	 */
	public Integer getAccumulatorPort() {
		return Port;
	}

	/**
	 * @param port the port to set
	 */
	public void setAccumulatorPort(Integer port) {
		Port = port;
	}

	public void createConfigFile(File configFile) {
		// TODO Auto-generated method stub
		
	}

	public void loadConfigFile(File configFile) {
		// TODO Auto-generated method stub
		
	}

}
