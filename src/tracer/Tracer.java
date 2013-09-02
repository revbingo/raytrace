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
	}

	public void createWorkers(int count) {
		for (int i = 0; i < count; i++) {
			workers.add(new WorkerThread(this));
		}

		for (WorkerThread worker : workers) {
			worker.start();
		}
	}

	public void calculateScene() {
		final int height = displayPanel.getHeight();
		final int width = displayPanel.getWidth();
		final int hh = height >> 1;

		final int stripe = height / workers.size() + 1;

		for (int i = 0; i < workers.size(); i++) {
			final int yStart = -hh + i * stripe;
			final int yEnd = Math.min(hh, yStart + stripe);

			WorkerThread worker = workers.get(i);
			worker.startRendering(yStart, yEnd, width);

			LockSupport.unpark(worker);
		}
	}

	public void workerDone() {
		workerLatch.countDown();
	}

	void calculateScene(int yStart, int yEnd, TracerDataSet data) {
		int width = data.linepix.length;
		int height = displayPanel.getHeight();

		final int hw = width >> 1;
		final int hh = height >> 1;

		for (int y = yEnd; y > yStart; y--) {
			data.lineV.set(view.look);
			data.lineV.add(view.vert, y);

			for (int x = -hw; x < hw; x++) {
				data.ray.set(data.lineV);
				data.ray.add(view.horz, x);

				data.p.set(view.camera);

				final int rgb = traceObjects(data);

				data.linepix[hw + x] = rgb;
			}
			displayPanel.setLine(hh - y, data.linepix);
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

	synchronized void nextFrame(Graphics gr) {
		workerLatch = new CountDownLatch(workers.size());
		displayPanel.paint(gr);
		calculateScene();
		try {
			workerLatch.await();
		} catch (InterruptedException e) {}
	}
}
