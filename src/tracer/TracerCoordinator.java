/*
 * This file is part of the SimpleRay project
 * 
 * Published as part of the FreeMapper project:
 * http://sourceforge.net/projects/freemapper/
 * 
 * author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */
package tracer;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

/**
 * The actual ray tracer.
 * 
 * @author Hj. Malthaner
 */
public class TracerCoordinator {
	private final DisplayInterface displayPanel;

	private Scene scene;
	private View view;
	
	private final ArrayList<TracerThread> workers;
	
	private int displayBase;

	private CountDownLatch workerLatch;
	
	public TracerCoordinator(DisplayInterface panel, Scene scene, View view) {
		this.displayPanel = panel;
		this.workers = new ArrayList<TracerThread>();
		this.scene = scene;
		this.view = view;
		
		createWorkers(16);
	}

	public void createWorkers(int count) {		
		final int height = displayPanel.getHeight();
		final int width = displayPanel.getWidth();
		final int halfHeight = height >> 1;
		
		this.displayBase = halfHeight;

		final int workerStripe = height / count + 1;

		for (int i = 0; i < count; i++) {
			TracerThread worker = new TracerThread(this, scene, view);
			workers.add(worker);

			//0,0 is in the centre, so offset by halfHeight
			final int yStart = -halfHeight + i * workerStripe;
			final int yEnd = Math.min(halfHeight, yStart + workerStripe);

			worker.setRenderingParameters(yStart, yEnd, width);
			worker.start();
		}
	}

	public void nextFrame() {
		synchronized(view) {
			try {
				workerLatch = new CountDownLatch(workers.size());
				for(TracerThread worker : workers) {
					LockSupport.unpark(worker);
				}
				workerLatch.await();
			} catch (InterruptedException e) {}
		}
	}

	public void workerDone(int yStart, int[] lineData) {
		workerLatch.countDown();
	}
	
	public void lineReady(int yStart, int[] lineData) {
		displayPanel.setLine(this.displayBase - yStart, lineData);
	}
}
