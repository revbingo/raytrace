/*
 * This file is part of the SimpleRay project
 * 
 * Published as part of the FreeMapper project:
 * http://sourceforge.net/projects/freemapper/
 * 
 * author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */
package tracer;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

/**
 * The actual ray tracer.
 * 
 * @author Hj. Malthaner
 */
public class Tracer {
	private final DisplayInterface displayPanel;

	private Scene scene;
	private View view;
	
	private final ArrayList<WorkerThread> workers;

	private CountDownLatch workerLatch;
	
	public Tracer(DisplayInterface panel, Scene scene, View view) {
		this.displayPanel = panel;
		this.workers = new ArrayList<WorkerThread>();
		this.scene = scene;
		this.view = view;
		
		createWorkers(16);
	}

	public void createWorkers(int count) {		
		final int height = displayPanel.getHeight();
		final int width = displayPanel.getWidth();
		final int halfHeight = height >> 1;

		final int workerStripe = height / count + 1;

		for (int i = 0; i < count; i++) {
			WorkerThread worker = new WorkerThread(this);
			workers.add(worker);

			//0,0 is in the centre
			final int yStart = -halfHeight + i * workerStripe;
			final int yEnd = Math.min(halfHeight, yStart + workerStripe);

			worker.setRenderingParameters(yStart, yEnd, width);
		}

		for (WorkerThread worker : workers) {
			worker.start();
		}
	}

	void calculateAndSetLineData(int yStart, int yEnd, TracerDataSet data) {
		int width = data.linepix.length;
		int height = displayPanel.getHeight();

		final int halfWidth = width >> 1;
		final int halfHeight = height >> 1;

		for (int y = yEnd; y > yStart; y--) {
			data.lineV.set(view.look);
			data.lineV.add(view.vert, y);

			for (int x = -halfWidth; x < halfWidth; x++) {
				data.ray.set(data.lineV);
				data.ray.add(view.horz, x);

				data.p.set(view.camera);

				final int rgb = traceObjects(data);

				data.linepix[halfWidth + x] = rgb;
			}
			displayPanel.setLine(halfHeight - y, data.linepix);
		}
	}

	private int traceObjects(TracerDataSet data) {
		boolean go;
		int brightness = 255;

		long objectRgb = -1;

		do {
			go = false;

			final double t = scene.findIntersection(data);

			if (data.bestObject != null) {
				final long color = data.bestObject.hit(data.p, data.ray, scene.light, t);

				if (color == -1L) {
					// mirror, p and v are set up by hit().
					go = true;
					brightness = brightness * 230 >> 8;
				} else {
					objectRgb = color;
				}
			}
		} while (go && brightness > 16);

		if (objectRgb == -1) {
			// Nothing hit

			data.ray.norm();
			final int tx = (int) (Textures.clouds.getWidth() * (data.ray.x + 1.0) * 0.5);
			final int ty = (int) (Textures.clouds.getHeight() * (data.ray.y + 1.0) * 0.5);

			objectRgb = RGB.spread(Textures.clouds.getRGB(tx, ty));
		} else {
			// shadows
			// need to calculate ray from data.p to light source

			data.ray.set(scene.light);
			data.ray.sub(data.p);

			scene.findIntersection(data);
			if (data.bestObject != null) {
				// shadow
				brightness = brightness * 160 >> 8;
			}
		}

		if (objectRgb == 0xFF00000000000000L) {
			return brightness >= 160 ? 0x00000000 : 0xFF000000;
		} else {
			return RGB.shadeAndCompact(objectRgb, brightness);
		}
	}

	synchronized void nextFrame() {
		try {
			workerLatch = new CountDownLatch(workers.size());
			for(WorkerThread worker : workers) {
				LockSupport.unpark(worker);
			}
			workerLatch.await();
		} catch (InterruptedException e) {}
	}
	
	public void workerDone() {
		workerLatch.countDown();
	}
}
