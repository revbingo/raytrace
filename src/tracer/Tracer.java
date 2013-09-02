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
import tracer.objects.FastSphere;
import tracer.objects.InvisiSphere;
import tracer.objects.Material;
import tracer.objects.Plane;
import tracer.objects.SceneObject;
import tracer.objects.Sphere;

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

	private final ArrayList<SceneObject> objects;
	private final ArrayList<WorkerThread> workers;

	private volatile int doneCount;

	private int frame;

	// not good here
	private SceneObject sphere1;
	private SceneObject sphere2;
	private SceneObject sphere3;

	private V3 move1;
	private V3 move2;
	private V3 move3;

	public Tracer(DisplayInterface panel) {
		this.displayPanel = panel;
		this.objects = new ArrayList<SceneObject>();
		this.workers = new ArrayList<WorkerThread>();
		this.doneCount = 0;

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

	public void buildScene() {
		sphere1 = new Sphere(new V3(2, 0, 2), 2);
		sphere1.setMaterial(new Material(0xFF7FFF, 0.1));
		sphere1.scale(0.5, 0.5, 1.0);

//		sphere2 = new FastSphere(new V3(-3, 0, 1), 1);
		sphere2 = new InvisiSphere(new V3(-3, 0, 1), 1);
		sphere2.setMaterial(new Material(0, 1.0));

//		sphere3 = new FastSphere(new V3(-6, 6, 3), 1);
		sphere3 = new InvisiSphere(new V3(-3, 0, 1), 1);
		sphere3.setMaterial(new Material(0, 1.0));

		move1 = new V3(0.05, 0.07, 0);
		move2 = new V3(0.11, 0.07, 0);
		move3 = new V3(-0.03, 0, 0.02);

		Plane floor = new Plane(new V3(), new V3(0, 0, 1));
		floor.setMin(new V3(-5, -5, -5));
		floor.setMax(new V3(5, 5, 5));

		objects.add(floor);

		objects.add(sphere1);
		objects.add(sphere2);
		objects.add(sphere3);
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
		frame++;

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

			final double t = findIntersection(data);

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

			findIntersection(data);
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

	private double findIntersection(TracerDataSet data) {
		final double raylen2 = data.ray.length2();

		double bestT = Double.MAX_VALUE;
		data.bestObject = null;

		for (int i = 0; i < objects.size(); i++) {
			data.tmpP.set(data.p);
			data.tmpRay.set(data.ray);

			final SceneObject object = objects.get(i);
			final double t = object.trace(data.tmpP, data.tmpRay, raylen2);

			if (t >= 0 && t < bestT) {
				data.bestObject = object;
				bestT = t;
			}
		}

		return bestT;
	}

	public void moveSphere() {
		move3.z -= 0.002; // Gravity

		sphere3.translate(move3);

		if (sphere3.getPos().z <= 1 && move3.z < 0) {
			move3.z = -move3.z;
			move3.z += 0.002; // Gravity
		}
		if (sphere3.getPos().x <= -8 && move3.x < 0) {
			move3.x = -move3.x;
		}
		if (sphere3.getPos().x >= 8 && move3.x > 0) {
			move3.x = -move3.x;
		}

		double sd = 1; //0.75 + 0.25 * Math.sin(System.currentTimeMillis() / 100.0);
		sphere1.translate(move1);
		sphere1.scale(1, 1, 1.0);

		bounceBorder(sphere1.getPos(), move1, 2);

		sphere2.translate(move2);
		bounceBorder(sphere2.getPos(), move2, 1);

//		checkSphereCollision(sd);
//		V3.put(dist);   //cache collision
	}

	private V3 checkSphereCollision(double sd) {
		V3 dist = V3.make(sphere1.getPos());
		dist.sub(sphere2.getPos());

		double len = dist.length2();
		double r = 3;
		if (len <= r * r) {
			dist.z = 0;
			dist.norm();
			dist.add(sphere2.getPos());
			dist.z = 0;

			bounceSphere(dist, sphere1.getPos(), move1, 4.0);
			bounceSphere(dist, sphere2.getPos(), move2, 1.0);
		}
		return dist;
	}

	private void bounceBorder(V3 pos, V3 move, double rad) {
		if ((pos.x < -5 + rad && move.x < 0) || (pos.x > 5 - rad && move.x > 0)) {
			move.x = -move.x;
		}
		if ((pos.y < -5 + rad && move.y < 0) || (pos.y > 5 - rad && move.y > 0)) {
			move.y = -move.y;
		}
	}

	private void bounceSphere(V3 kiss, V3 center, V3 move, double r2) {
		double x = kiss.x - center.x;
		double y = kiss.y - center.y;

		double f = (move.x * x + move.y * y) / (x * x + y * y);

		x *= 2 * f;
		y *= 2 * f;

		move.x -= x;
		move.y -= y;
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
