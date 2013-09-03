/*
 * This file is part of the SimpleRay project
 * 
 * Published as part of the FreeMapper project:
 * http://sourceforge.net/projects/freemapper/
 * 
 * author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */

package tracer;

import java.util.concurrent.locks.LockSupport;

/**
 * A raytracing worker thread.
 * 
 * @author Hj. Malthaner
 */
public class WorkerThread extends Thread {
	private final Tracer tracer;
	private int yStart;
	private int yEnd;

	private final TracerDataSet tracerData;;

	public WorkerThread(Tracer tracer) {
		this.tracer = tracer;
		tracerData = new TracerDataSet();

		setDaemon(true);
	}

	public void setRenderingParameters(int yStart, int yEnd, int width) {
		this.yStart = yStart;
		this.yEnd = yEnd;

		tracerData.updateLinepix(width);
	}

	@Override
	public synchronized void run() {
		while (true) {
			LockSupport.park(this);

			tracer.calculateAndSetLineData(yStart, yEnd, tracerData);
			tracer.workerDone();
		}
	}
}
