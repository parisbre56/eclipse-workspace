/**
 * 
 */
package shared_data;

/**
 * @author Parisbre56
 * The ordinal of the status codes are exchanged between the various servers and clients to indicate various things<br>
 * TO ENSURE COMPATIBILITY BETWEEN OLDER AND NEWER VERSIONS, NEW STATUS CODES MUST ALWAYS BE PUT AT THE END OF THIS LIST,
 * OTHERWISE THEY WILL CHANGE THE ORDINAL OF OLDER STATUS CODES.
 */
public enum StatusCode {
	/** No error
	 */
	ALL_CLEAR, 
	/** Signals that the client wants to register its id.<br> 
	 * Followed by string length and the id string transmitted as a byte array.
	 */
	NAME_REGISTRATION_REQUEST, 
	/** Sent by the server to inform the client that a client with that name has already been registered.
	 */
	NAME_ALREADY_EXISTS, 
	/** Sent by the client during registration to inform the server about 
	 * the MAXIMUM time it is going to wait before refresh requests.<br>
	 * Followed by an integer that represents the time in seconds. Should be greater than 0.<br>
	 * Note that refresh requests may be sent more often than that.<br>
	 * The server will consider the client to have timed out and essentially be 
	 * disconnected some time after REFRESH_RATE seconds pass. The client can reconnect with either a 
	 * NAME_REGISTRATION_REQUEST or a REFRESH_REQUEST.<br>
	 */
	SET_REFRESH_RATE, 
	/** Informs the server that the client is exiting and should be marked as disconnected.<br>
	 * Note that clients marked as disconnected can not reconnect via refresh requests,
	 */
	EXIT_REQUEST, 
	/** Request all the malicious patterns from the server, 
	 * regardless of whether or not the server has sent them before.
	 */
	ALL_DATA_REQUEST,
	/** Request only the additions and deletions that should be made to the malicious pattern memory.
	 */
	DATA_DIFF_REQUEST, 
	/** Sent to indicate that no more data is incoming.
	 */
	END_OF_DATA, 
	/** Sent by the server to ask the client to mark all patterns in its memory as inactive.
	 */
	CLEAR_MEMORY_REQUEST, 
	/** Sent by the server to indicate that the ip that follows should be added to the memory.<br>
	 * Followed by string length and the ip string transmitted as byte array.
	 */
	MALICIOUS_IP_ADDITION,
	/** Sent by the server to indicate that the pattern that follows should be added to the memory.<br>
	 * Followed by string length and the pattern string transmitted as byte array.
	 */
	MALICIOUS_STRING_ADDITION, 
	/** Sent by the server to indicate that the ip that follows should be marked as inactive.<br>
	 * Followed by string length and the ip string transmitted as byte array.
	 */
	MALICIOUS_IP_REMOVAL,
	/** Sent by the server to indicate that the pattern that follows should be marked as inactive.<br>
	 * Followed by string length and the pattern string transmitted as byte array.
	 */
	MALICIOUS_STRING_REMOVAL,
	/** Sent by the client to inform the server that it is going to start sending statistics data.
	 */
	DATA_INCOMING_NOTIFICATION,
	/** Sent by the client to test whether or not the connection is active and to inform the server that it is still active.<br>
	 * The client will make an attempt to establish a new connection if the old one has failed.<br>
	 * Followed by the string length and the id string transmitted as a byte array.<br>
	 * Usually sent before any communication with the server after flushing the incoming part of the stream.<br>
	 */
	REFRESH_REQUEST,
	/** Sent by the server to inform a client that it made a refresh request for a client that doesn't exist
	 */
	NO_SUCH_CLIENT, 
	/** Sent by the server to inform a client that it made a refresh request for a client that has been marked as disconnected
	 */
	CLIENT_DISCONNECTED,
	/** Sent to declare a new interface as part of sending statistics data<br>
	 * Usually followed by the interface name in the form of byte array length followed by the byte array 
	 * and after that a boolean showing whether or not this interface is currently active <br>
	 * After that a loop should send all addresses for this interface
	 */
	INTERFACE_DECLARATION, 
	/** Sent to declare a new interface address as part of sending interface statistics data<br>
	 * Usually followed by the interface address in the form of the length an IP byte array (NOT A STRING ONE!!) 
	 * followed by the array itself. A size of zero indicates null, which means the capture was made with no known
	 * interface address.<br>
	 * Followed by string pattern or ip pattern malicious activity declarations
	 */
	INTERFACE_ADDRESS_DECLARATION,
	/** Sent to declare a new malicious ip activity as part of sending interface address statistics<br>
	 * Usually followed by the ip string in the form of byte array size and then the byte array itself and then
	 * the frequency as an integer.<br>
	 */
	MALICIOUS_IP_ACTIVITY,
	/** Sent to declare a new malicious string activity as part of sending interface address statistics<br>
	 * Usually followed by the string pattern in the form of a byte array suze and then the byte array itself and then
	 * the frequency as an integer<br> 
	 */
	MALICIOUS_STRING_ACTIVITY,
	/** Sent to indicate that the provided data was out of bounds.<br>
	 * During a set refresh rate request, this might indicate that the sent refresh rate was less than or equal to zero.
	 */
	VALUE_OUT_OF_BOUNDS
}
