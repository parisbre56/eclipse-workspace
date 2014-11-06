/**
 * 
 */
package accumulator.threads;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedList;

import exceptions.NTMonException;
import exceptions.NTMonOutOfBoundsException;
import shared_data.StatusCode;
import accumulator.Accumulator_Main;
import accumulator.MaliciousPatternEvent;

/**
 * @author Parisbre56
 *
 */
public class ConnectionProcessorThread implements Runnable {

	private final Socket conn;
	private final int count;
	private String connID;

	/**
	 * @param connection The connection to process requests from
	 * @param count1 The identifying number of this connection
	 */
	public ConnectionProcessorThread(Socket connection, int count1) {
		this.conn = connection;
		this.count = count1;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		DataInputStream is = null;
		DataOutputStream os = null;
		int timeoutCount = 0;
		//TODO better error handling?
		try {
			//Socket timeout also used as default refresh rate
			this.conn.setSoTimeout((Accumulator_Main.accumulator_configClass.getSocketTimeout()+1)*1000);
			//Initialize object input and output streams
			is = new DataInputStream(this.conn.getInputStream());
			os = new DataOutputStream(this.conn.getOutputStream());
			
			//Start processing incoming data
			boolean disconnected = false;
			while(Accumulator_Main.exiting.get()==false&&disconnected  == false) {
				//Get signal
				int inputInt;
				try {
					inputInt = is.readInt();
					timeoutCount=0;
				}
				catch (SocketTimeoutException e) {
					//If refresh rate amount of seconds passes, mark it as timed out
					System.err.println("DEBUG: Connection "+Integer.toString(this.count)+" timed out");
					if(this.connID!=null) {
						Accumulator_Main.accumulator_SharedMemory.setTimedOut(this.connID);
					}
					++timeoutCount;
					//If it has timed out a set number of times, mark it as disconnected (if it hasn't
					//connected to any other threads) and stop this thread
					if(timeoutCount>Accumulator_Main.accumulator_configClass.getTimeoutTimes()) {
						if(this.connID!=null) {
							Accumulator_Main.accumulator_SharedMemory.setDisconnectedIfTimedOut(this.connID);
						}
						break;
					}
					continue;
				}
				StatusCode inputSignal = null;
				inputSignal = getStatusCodeFromInt(inputInt);
				switch(inputSignal) {
				case ALL_DATA_REQUEST:
					System.err.println("DEBUG: Start of ALL_DATA_REQUEST: "+
							Integer.toString(this.count)+":"+this.connID);
					sendDataDiff(Accumulator_Main.accumulator_SharedMemory.refillDataQueue(this.connID),os);
					System.err.println("DEBUG: End of ALL_DATA_REQUEST: "+
							Integer.toString(this.count)+":"+this.connID);
					break;
				case DATA_DIFF_REQUEST:
					System.err.println("DEBUG: Start of DATA_DIFF_REQUEST: "+
							Integer.toString(this.count)+":"+this.connID);
					sendDataDiff(Accumulator_Main.accumulator_SharedMemory.getDataQueue(this.connID),os);
					System.err.println("DEBUG: End of DATA_DIFF_REQUEST: "+
							Integer.toString(this.count)+":"+this.connID);
					break;
				case DATA_INCOMING_NOTIFICATION:
					System.err.println("DEBUG: Start of incoming data from connection "+this.count+":"+this.connID);
					boolean stateInterface = true;
					inputInt=is.readInt();
					inputSignal = getStatusCodeFromInt(inputInt);
					while(stateInterface) {
						if(inputSignal==StatusCode.END_OF_DATA) {
							stateInterface=false;
							continue;
						}
						else if(inputSignal==StatusCode.INTERFACE_DECLARATION) {
							String interfaceName = getString(is, os);
							boolean interfaceIsActive = is.readBoolean();
							stateInterface = false;
							boolean stateInterfaceIp = true;
							inputInt=is.readInt();
							inputSignal = getStatusCodeFromInt(inputInt);
							while(stateInterfaceIp) {
								if(inputSignal==StatusCode.END_OF_DATA) {
									stateInterfaceIp=false;
									continue;
								}
								else if(inputSignal==StatusCode.INTERFACE_DECLARATION) {
									stateInterfaceIp=false;
									stateInterface=true;
									continue;
								}
								else if(inputSignal==StatusCode.INTERFACE_ADDRESS_DECLARATION) {
									InetAddress interfaceAddress = getAddress(is, os);
									stateInterfaceIp = false;
									boolean stateDeclaration= true;
									inputInt=is.readInt();
									inputSignal = getStatusCodeFromInt(inputInt);
									while (stateDeclaration) {
										if(inputSignal==StatusCode.END_OF_DATA) {
											stateDeclaration=false;
											continue;
										}
										else if(inputSignal==StatusCode.INTERFACE_DECLARATION) {
											stateDeclaration=false;
											stateInterface=true;
											continue;
										}
										else if(inputSignal==StatusCode.INTERFACE_ADDRESS_DECLARATION) {
											stateDeclaration=false;
											stateInterfaceIp=true;
											continue;
										}
										else if(inputSignal==StatusCode.MALICIOUS_IP_ACTIVITY) {
											String patternName = getString(is, os);
											int frequency = is.readInt();
											//TODO insert detection event into SQL database insertion queue
											System.err.println("DEBUG: Detection event: |Int: "+interfaceName+
													" |On: "+interfaceIsActive+
													" |Ad: "+interfaceAddress+
													" |Pat: "+patternName+" |Type: IP |Freq: "+Integer.toString(frequency));
											inputInt=is.readInt();
											inputSignal = getStatusCodeFromInt(inputInt);
											continue;
										}
										else if(inputSignal==StatusCode.MALICIOUS_STRING_ACTIVITY) {
											String patternName = getString(is, os);
											int frequency = is.readInt();
											//TODO insert detection event into SQL database insertion queue
											System.err.println("DEBUG: Detection event: |Int: "+interfaceName+
													" |On: "+interfaceIsActive+
													" |Ad: "+interfaceAddress+
													" |Pat: "+patternName+" |Type: String |Freq: "+Integer.toString(frequency));
											inputInt=is.readInt();
											inputSignal = getStatusCodeFromInt(inputInt);
											continue;
										}
										else {
											throw new NTMonException("Unhandled signal "+Integer.toString(inputInt)+
													":"+inputSignal.toString()+
													" received in connection "+Integer.toString(this.count)+
													" malicious detection data state");
										}
									}
									continue;
								}
								else {
									throw new NTMonException("Unhandled signal "+Integer.toString(inputInt)+
											":"+inputSignal.toString()+
											" received in connection "+Integer.toString(this.count)+
											" interface address data state");
								}
							}
							continue;
						}
						else {
							throw new NTMonException("Unhandled signal "+Integer.toString(inputInt)+
									":"+inputSignal.toString()+
									" received in connection "+Integer.toString(this.count)+
									" interface data state");
						}
					}
					System.err.println("DEBUG: End of incoming data from connection "+this.count+":"+this.connID);
					//TODO proccess incoming data
					break;
				case NAME_REGISTRATION_REQUEST:
					String recID = getString(is, os);
					//If this name is not the one associated with this connection and is an active name, then
					//the new connection can not get that id
					if(!recID.equals(this.connID) && Accumulator_Main.accumulator_SharedMemory.containsActiveID(recID)) {
						System.err.println("DEBUG: Name "+recID+" is already registered");
						os.writeInt(StatusCode.NAME_ALREADY_EXISTS.ordinal());
					}
					else {
						if(Accumulator_Main.accumulator_SharedMemory.addConnection(recID)) {
							System.err.println("DEBUG: Registered "+recID);
							//Mark old connection as timed out
							if(this.connID!=null) {
								Accumulator_Main.accumulator_SharedMemory.setTimedOut(this.connID);
							}
							//Update info
							this.connID=recID;
							this.conn.setSoTimeout(Accumulator_Main.accumulator_SharedMemory.getRefreshRate(this.connID)*1000);
							os.writeInt(StatusCode.ALL_CLEAR.ordinal());
						}
						else {
							System.err.println("DEBUG: Name "+recID+" is already registered");
							os.writeInt(StatusCode.NAME_ALREADY_EXISTS.ordinal());
						}
					}
					break;
				case REFRESH_REQUEST:
					recID = getString(is, os);
					//If this is the current ID, make sure it is marked as active
					if(recID.equals(this.connID)) {
						System.err.println("DEBUG: Normal refresh for "+recID);
						Accumulator_Main.accumulator_SharedMemory.addConnection(this.connID);
						os.writeInt(StatusCode.ALL_CLEAR.ordinal());
					}
					else {
						//If this isn't the current ID and is a valid ID, 
						//make it the current ID and mark the old one as disconnected
						if(Accumulator_Main.accumulator_SharedMemory.containsNotDisconnectedID(recID)) {
							if(this.connID!=null) {
								Accumulator_Main.accumulator_SharedMemory.setTimedOut(this.connID);
							}
							System.err.println("DEBUG: Refresh: Connection chenging from "+this.count+":"+this.connID+" to "+recID);
							this.connID=recID;
							Accumulator_Main.accumulator_SharedMemory.addConnection(this.connID);
							this.conn.setSoTimeout(Accumulator_Main.accumulator_SharedMemory.getRefreshRate(this.connID)*1000);
							os.writeInt(StatusCode.ALL_CLEAR.ordinal());
						}
						//Else, return an error response
						else {
							//Since we know it is neither active nor timed out, it is disconnected
							if(Accumulator_Main.accumulator_SharedMemory.containsID(recID)) {
								System.err.println("DEBUG: Refresh: Refresh failed for disconnected client "+recID);
								os.writeInt(StatusCode.CLIENT_DISCONNECTED.ordinal());
							}
							else {
								System.err.println("DEBUG: Refresh: Refresh failed for nonexistent client "+recID);
								os.writeInt(StatusCode.NO_SUCH_CLIENT.ordinal());
							}
						}
					}
					break;
				case SET_REFRESH_RATE:
					int tempInt=is.readInt();
					//If there is a problem
					if(tempInt<=0) {
						System.err.println("ERROR: Connection "+Integer.toString(this.count)+" received invalid"
								+ "refresh rate "+tempInt+". The previous setting will remain.");
						os.writeInt(StatusCode.VALUE_OUT_OF_BOUNDS.ordinal());
					}
					//If all ok, send the all clear and update the refresh rate
					else {
						System.err.println("DEBUG: Setting refresh rate to "+Integer.toString(tempInt)+
								" seconds for connection "+Integer.toString(this.count));
						os.writeInt(StatusCode.ALL_CLEAR.ordinal());
						this.conn.setSoTimeout(tempInt*1000);
						Accumulator_Main.accumulator_SharedMemory.setRefreshRate(this.connID,tempInt);
					}
					break;
				case EXIT_REQUEST:
					if(Accumulator_Main.accumulator_SharedMemory.setDisconnectedIfNotDisconnected(this.connID)) {
						System.err.println("DEBUG: Disconnected: "+this.count+":"+this.connID);
						os.writeInt(StatusCode.ALL_CLEAR.ordinal());
					}
					else {
						System.err.println("DEBUG: Disconnected: No such client: "+this.count+":"+this.connID);
						os.writeInt(StatusCode.NO_SUCH_CLIENT.ordinal());
					}
					disconnected=true;
					break;
				case CLEAR_MEMORY_REQUEST:
				case ALL_CLEAR:
				case END_OF_DATA:
				case INTERFACE_ADDRESS_DECLARATION:
				case INTERFACE_DECLARATION:
				case MALICIOUS_IP_ACTIVITY:
				case MALICIOUS_IP_ADDITION:
				case MALICIOUS_IP_REMOVAL:
				case MALICIOUS_STRING_ACTIVITY:
				case MALICIOUS_STRING_ADDITION:
				case MALICIOUS_STRING_REMOVAL:
				case NAME_ALREADY_EXISTS:
				case NO_SUCH_CLIENT:
				case VALUE_OUT_OF_BOUNDS:
				case CLIENT_DISCONNECTED:
				default:
					throw new NTMonException("Unhandled signal "+Integer.toString(inputInt)+
							":"+inputSignal.toString()+
							" received in connection "+Integer.toString(this.count)+
							" initial state");
				}
			}
		} catch (IOException e) {
			System.err.println("ERROR: Unable to process connection "+Integer.toString(this.count)+".");
			if(this.connID!=null) {
				Accumulator_Main.accumulator_SharedMemory.setTimedOut(this.connID);
			}
			e.printStackTrace();
		} catch (NTMonException e) {
			System.err.println("ERROR: Problem when processing connection "+Integer.toString(this.count));
			if(this.connID!=null) {
				Accumulator_Main.accumulator_SharedMemory.setTimedOut(this.connID);
			}
			e.printStackTrace();
		}
		//Close connection, remove self from thread list and exit
		finally {
			try {
				if(is!=null) {
					is.close();
				}
				if(os!=null) {
					os.close();
				}
				this.conn.close();
			} catch (IOException e) {
				System.err.println("ERROR: Exception while closing connection");
				e.printStackTrace();
			}
			Accumulator_Main.threads.remove(Thread.currentThread());
			synchronized(Accumulator_Main.threads) {
				Accumulator_Main.threads.notifyAll();
			}
		}
		return;
	}

	/**
	 * @param is Used to read the string
	 * @param os Used to send an error code
	 * @return The string read from the input stream
	 * @throws IOException if there was a problem with the connection
	 * @throws NTMonOutOfBoundsException if the string length was less than or equal to 0
	 */
	private static String getString(DataInputStream is, DataOutputStream os)
			throws IOException, NTMonOutOfBoundsException {
		int stringLength = is.readInt();
		if(stringLength<=0) {
			os.writeInt(StatusCode.VALUE_OUT_OF_BOUNDS.ordinal());
			throw new NTMonOutOfBoundsException("The incoming string length "+stringLength+" was invalid");
		}
		byte[] name = new byte[stringLength];
		is.readFully(name);
		String recID = new String(name);
		return recID;
	}
	
	/**
	 * @param is Used to read the address
	 * @param os Used to send an error code
	 * @return The address read from the input stream
	 * @throws IOException if there was a problem with the connection
	 * @throws NTMonOutOfBoundsException if the address length was less than or equal to 0 or 
	 * if it didn't match a valid address type
	 */
	private static InetAddress getAddress(DataInputStream is, DataOutputStream os) 
			throws IOException, NTMonOutOfBoundsException {
		int addrLength = is.readInt();
		//0 means no address for this capture
		if(addrLength==0) {
			return null;
		}
		if(addrLength<0) {
			os.writeInt(StatusCode.VALUE_OUT_OF_BOUNDS.ordinal());
			throw new NTMonOutOfBoundsException("The incoming address length "+addrLength+" was invalid");
		}
		byte[] addr = new byte[addrLength];
		is.readFully(addr);
		try {
			InetAddress recAd = InetAddress.getByAddress(addr);
			return recAd;
		} catch (UnknownHostException e) {
			os.writeInt(StatusCode.VALUE_OUT_OF_BOUNDS.ordinal());
			throw new NTMonOutOfBoundsException("The incoming address length "+addrLength+" was invalid",e);
		}
	}

	/**
	 * @param inputInt The integer to decode
	 * @return The {@link StatusCode} interpretation of the given integer
	 * @throws NTMonException if the given integer was not a valid {@link StatusCode}
	 */
	private StatusCode getStatusCodeFromInt(int inputInt)
			throws NTMonException {
		if(inputInt>=0 && inputInt<StatusCode.values().length) {
			return StatusCode.values()[inputInt];
		}
		throw new NTMonOutOfBoundsException("Unknown signal "+Integer.toString(inputInt)+
				" received in connection "+Integer.toString(this.count));
	}

	/** Sends all events from the provided list
	 * @param linkedList The list to take events from
	 * @param os The output stream to write events in
	 * @throws IOException If there was a problem with the connection
	 */
	private static void sendDataDiff(LinkedList<MaliciousPatternEvent> linkedList, DataOutputStream os) throws IOException {
		for(MaliciousPatternEvent e : linkedList) {
			if(e.isAddition()) {
				if(e.isString()) {
					os.writeInt(StatusCode.MALICIOUS_STRING_ADDITION.ordinal());
					sendPattern(e,os);
				}
				else {
					os.writeInt(StatusCode.MALICIOUS_IP_ADDITION.ordinal());
					sendPattern(e,os);
				}
			}
			else {
				if(e.isString()) {
					os.writeInt(StatusCode.MALICIOUS_STRING_REMOVAL.ordinal());
					sendPattern(e,os);
				}
				else {
					os.writeInt(StatusCode.MALICIOUS_IP_REMOVAL.ordinal());
					sendPattern(e,os);
				}
			}
		}
		os.writeInt(StatusCode.END_OF_DATA.ordinal());
	}

	/**
	 * @param e The event with the pattern we must to send
	 * @param os The output stream to write the pattern's String in.
	 * @throws IOException If a problem occurred with the connection
	 */
	private static void sendPattern(MaliciousPatternEvent e, DataOutputStream os) throws IOException {
		byte[] toSend = e.getPattern().getBytes();
		os.writeInt(toSend.length);
		os.write(toSend);
	}

}
