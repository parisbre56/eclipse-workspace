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
	ALL_CLEAR, 
	NAME_REGISTRATION_REQUEST, 
	NAME_ALREADY_EXISTS, 
	SET_REFRESH_RATE, 
	EXIT_REQUEST, 
	ALL_DATA_REQUEST,
	DATA_DIFF_REQUEST, 
	END_OF_DATA, 
	CLEAR_MEMORY_REQUEST, 
	MALICIOUS_IP_ADDITION, 
	MALICIOUS_STRING_ADDITION, 
	MALICIOUS_IP_REMOVAL, 
	MALICIOUS_STRING_REMOVAL, 
	DATA_INCOMING_NOTIFICATION
}
