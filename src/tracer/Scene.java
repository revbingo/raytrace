package tracer;

import java.util.ArrayList;

import tracer.objects.Material;
import tracer.objects.Plane;
import tracer.objects.SceneObject;
import tracer.objects.Sphere;

public class Scene {

	private ArrayList<SceneObject> objects = new ArrayList<SceneObject>();

	public final V3 light = new V3();

	private SceneObject sphere1;
	
	private V3 move1;
	
	public Scene() {
		buildScene();
	}

	public void buildScene() {
		sphere1 = new Sphere(new V3(2, 0, 2), 2);
		sphere1.setMaterial(new Material(0xFF7FFF, 0.1));

		move1 = new V3(0.05, 0.07, 0);

		Plane floor = new Plane(new V3(), new V3(0, 0, 1));
		floor.setMin(new V3(-5, -5, -5));
		floor.setMax(new V3(5, 5, 5));

		objects.add(floor);
		objects.add(sphere1);
		
		light.set(-15, -3, 20);
	}

	public double findIntersection(TracerDataSet data) {
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
	
	public void animate() {
		sphere1.translate(move1);
		bounceBorder(sphere1.getPos(), move1, 2);
	}

	private void bounceBorder(V3 pos, V3 move, double rad) {
		if ((pos.x < -5 + rad && move.x < 0) || (pos.x > 5 - rad && move.x > 0)) {
			move.x = -move.x;
		}
		if ((pos.y < -5 + rad && move.y < 0) || (pos.y > 5 - rad && move.y > 0)) {
			move.y = -move.y;
		}
	}
}
