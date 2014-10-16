/**
 * 
 */
package node_main;

import java.io.IOException;
import java.net.Socket;

/**
 * @author Parisbre56
 *
 */
public class IdentificationThread implements Runnable {

	/**
	 * 
	 */
	public IdentificationThread() {
		// TODO Initialize any needed data
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			Socket connection = new Socket(Node_Main.configClass.getAccumulatorAddress(),
					Node_Main.configClass.getAccumulatorPort());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// TODO Start identification thread and make sure it stays running.
		// This one connects with the accumulator and registers this. Check for error or duplicate.
		//		If duplicate start incrementing and retrying (e.g. Unknown-1) until successful. 
		//		Be sure to update configClass with the new name.
		// Renew session every time data is sent, otherwise it expires. Accumulator keeps timeout 
		//	for each node and waits a bit longer (say, renewal time + a minute). Timeout is essentially the 
		//	same as offline
		// Notify accumulator when exiting. Accumulator has an online, offline or timeout enumeration for nodes.

	}

}
