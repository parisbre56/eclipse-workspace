/**
 * 
 */
package node.threads;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import node.Node_Main;

/** Simply sets the exit signal to true and waits for all registered threads to exit
 * @author Parisbre56
 *
 */
public class Node_ShutdownHookThread implements Runnable {

	/**
	 * 
	 */
	public Node_ShutdownHookThread() {
		//Nothing to initialize
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
	 * @param threads The list of threads to check. 
	 * No synchronization needed because we are iterating over this thread's personal copy of the list.
	 * @return True if a live thread is found in the list, false otherwise
	 */
	private static boolean containsLiveThreads(CopyOnWriteArrayList<Thread> threads) {
		for(Iterator<Thread> it = threads.iterator(); it.hasNext() ;) {
			Thread t = it.next();
			if(t.isAlive()) {
				return true;
			}
		}
		return false;
	}

}
