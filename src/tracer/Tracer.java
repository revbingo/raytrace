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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The actual ray tracer.
 * 
 * @author Hj. Malthaner
 */
public class Tracer {
	private final DisplayInterface displayPanel;

	private final V3 camera;
	private final V3 lookAt;
	private final V3 look;
	private final V3 horz;
	private final V3 vert;

	private final V3 light;

	private final ArrayList<WorkerThread> workers;

	private volatile int doneCount;

	private Scene scene;

	public Tracer(DisplayInterface panel, Scene scene) {
		this.displayPanel = panel;
		this.workers = new ArrayList<WorkerThread>();
		this.doneCount = 0;
		this.scene = scene;

		camera = new V3();
		lookAt = new V3();

		look = new V3();
		light = new V3();

		horz = new V3();
		vert = new V3();

		camera.set(2, -10, 7);
		lookAt.set(0, 1, 0);

		light.set(-15, -3, 20);

		setup();
	}

	public void createWorkers(int count) {
		for (int i = 0; i < count; i++) {
			workers.add(new WorkerThread(this, i));
		}

		for (WorkerThread worker : workers) {
			worker.start();
		}
	}

	public void setCamera(V3 camera) {
		this.camera.set(camera);
	}

	public void setLight(V3 light) {
		this.light.set(light);
	}

	public void setLookAt(V3 lookAt) {
		this.lookAt.set(lookAt);
		setup();
	}

	private void setup() {
		look.set(lookAt);
		look.sub(camera);

		horz.set(look.y, -look.x, 0);
		vert.set(V3.cross(horz, look));

		horz.norm();
		vert.norm();

		horz.mul(0.018);
		vert.mul(0.018);
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

			synchronized (worker) {
				worker.notify();
			}
		}
	}

	public synchronized void workerDone() {
		doneCount++;

		if (doneCount == workers.size()) {
			notify();
			// System.err.println("notify sent, count=" + doneCount);
		}

		// System.err.println("done count=" + doneCount);
	}

	private synchronized void waitForSceneFinish() {
		try {
			// System.err.println("waiting for workers on frame=" + frame);

			wait();

			doneCount = 0;

			// System.err.println("------------- end ---------------");
		} catch (InterruptedException ex) {
			Logger.getLogger(Tracer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	void calculateScene(int yStart, int yEnd, TracerDataSet data) {
		int width = data.linepix.length;
		int height = displayPanel.getHeight();

		final int hw = width >> 1;
		final int hh = height >> 1;

		for (int y = yEnd; y > yStart; y--) {
			data.lineV.set(look);
			data.lineV.add(vert, y);

			for (int x = -hw; x < hw; x++) {
				data.ray.set(data.lineV);
				data.ray.add(horz, x);

				data.p.set(camera);

				final int rgb = traceObjects(data);

				data.linepix[hw + x] = rgb;
			}
			displayPanel.setline(hh - y, data.linepix);
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
				final long color = data.bestObject.hit(data.p, data.ray, light, t);

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

			data.ray.set(light);
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
		calculateScene();
		displayPanel.paint(gr);
		waitForSceneFinish();
	}

	public synchronized void calculateOneFrame() {
		calculateScene();
		waitForSceneFinish();
	}

	public void dispose() {
		for (WorkerThread worker : workers) {
			worker.quit();
		}
	}
}
