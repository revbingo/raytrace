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
import java.util.concurrent.locks.LockSupport;

import tracer.objects.SceneObject;

/**
 * A raytracing worker thread.
 * 
 * @author Hj. Malthaner
 */
public class TracerThread extends Thread {
	private static final long REFLECTED = -1L;
	private final TracerCoordinator coordinator;
	private int yStart;
	private int yEnd;
	private int halfWidth;
	
	private Scene scene;
	private View view;
	
	private final TracerDataSet tracerData;

	public TracerThread(TracerCoordinator tracer, Scene scene, View view) {
		this.coordinator = tracer;
		this.scene = scene;
		this.view = view;
		
		tracerData = new TracerDataSet();

		setDaemon(true);
	}

	public void setRenderingParameters(int yStart, int yEnd, int width) {
		this.yStart = yStart;
		this.yEnd = yEnd;
		this.halfWidth = width >> 1;
		
		tracerData.linepix = new int[width];
	}

	@Override
	public synchronized void run() {
		while (true) {
			LockSupport.park(this);

			calculateAndSetLinetracerData();
			coordinator.workerDone(yStart, tracerData.linepix);
		}
	}
	
	void calculateAndSetLinetracerData() {
		V3 lineV = new V3();
		
		for (int y = yEnd; y > yStart; y--) {
			//a ray through the scan line - set once and then added to each time below
			lineV.set(view.cameraToLookAt);
			lineV.add(view.vert, y);

			for (int x = -halfWidth; x < halfWidth; x++) {
				//the actual ray through the view
				tracerData.currentRay.set(lineV);
				tracerData.currentRay.add(view.horz, x);

				tracerData.camera.set(view.camera);

				final int rgb = traceObjects(tracerData);

				tracerData.linepix[halfWidth + x] = rgb;
			}
			coordinator.lineReady(y, tracerData.linepix);
		}
	}

	private int traceObjects(TracerDataSet tracerData) {
		int brightness = 255;
		long objectRgb = -1;

		while (brightness > 16) {
			final double distanceToNearestObject = findIntersection(tracerData);

			//there was no object intersected
			if(tracerData.bestObject == null) break;
			
			final long color = tracerData.bestObject.hit(tracerData.camera, tracerData.currentRay, scene.light, distanceToNearestObject);

			if (color == REFLECTED) {
				//ray is slightly dimmed on reflection
				brightness = (int)(brightness * 0.9);
				
				tracerData.currentRay.norm();
				final int tx = (int) (Textures.clouds.getWidth() * (tracerData.currentRay.x + 1.0) * 0.5);
				final int ty = (int) (Textures.clouds.getHeight() * (tracerData.currentRay.y + 1.0) * 0.5);

				objectRgb = RGB.spread(Textures.clouds.getRGB(tx, ty));
				
			} else {
				objectRgb = color;
				// shadows
				// need to calculate ray from tracerData.p to light source

				tracerData.currentRay.set(scene.light);
				tracerData.currentRay.sub(tracerData.camera);

				findIntersection(tracerData);
				if (tracerData.bestObject != null) {
					// shadow
					brightness = brightness * 80 >> 8;
				}
				break;
			}
		}

		return RGB.shadeAndCompact(objectRgb, brightness);
	}
	
	private double findIntersection(TracerDataSet data) {
		final double raylen2 = data.currentRay.length2();

		double distanceToNearestObject = Double.MAX_VALUE;
		data.bestObject = null;

		ArrayList<SceneObject> objects = scene.getSceneObjects();
		for (int i = 0; i < objects.size(); i++) {

			final SceneObject object = objects.get(i);
			final double distanceToObject = object.distanceToIntersection(data.camera, data.currentRay, raylen2);

			if (distanceToObject >= 0 && distanceToObject < distanceToNearestObject) {
				data.bestObject = object;
				distanceToNearestObject = distanceToObject;
			}
		}

		return distanceToNearestObject;
	}
}
