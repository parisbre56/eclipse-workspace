/**
 * 
 */
package accumulator.threads;

import accumulator.Accumulator_Main;

/** Joins all threads registered in {@link Accumulator_Main}.threads before exiting
 * @author Parisbre56
 *
 */
public class Accumulator_ShutdownHookThread implements Runnable {

	/**
	 * 
	 */
	public Accumulator_ShutdownHookThread() {
		//Nothing to initialize
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		Accumulator_Main.exiting.set(true);
		
		// TODO Auto-generated method stub
		
	}

}
