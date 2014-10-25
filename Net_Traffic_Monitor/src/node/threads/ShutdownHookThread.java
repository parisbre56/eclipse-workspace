/**
 * 
 */
package node.threads;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import node.Node_Main;

/**
 * @author Parisbre56
 *
 */
public class ShutdownHookThread implements Runnable {

	/**
	 * 
	 */
	public ShutdownHookThread() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		//Set exiting to true and notify all other threads
		Node_Main.exiting.set(true);
		synchronized(Node_Main.exiting) {
			Node_Main.exiting.notifyAll();
		}
		
		//Before exiting, write the configuration file if the user asked for it, including any changes made
		if(Node_Main.argList.contains("-wc")) {
			Node_Main.configClass.createConfigFile(Node_Main.configFile);
		}

		//Wait for all other threads to exit
		while(containsLiveThreads(Node_Main.threads)) {
			try {
				synchronized(Node_Main.threads) {
					Node_Main.threads.wait(Node_Main.defaultTimeout*1000);
				}
			} catch (InterruptedException e) {
				System.err.println("DEBUG: Exit hook interrupted while waiting for other threads to exit.");
				e.printStackTrace();
			}
		}
	}

	/** Checks threads for live threads. Returns true if one is found
	 * @param threads
	 * @return True if a live thread is found in the list, false otherwise
	 */
	private boolean containsLiveThreads(CopyOnWriteArrayList<Thread> threads) {
		for(Iterator<Thread> it = threads.iterator(); it.hasNext() ;) {
			Thread t = it.next();
			if(t.isAlive()) {
				return true;
			}
		}
		return false;
	}

}
