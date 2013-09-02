package tracer;

import java.util.ArrayList;

import tracer.objects.InvisiSphere;
import tracer.objects.Material;
import tracer.objects.Plane;
import tracer.objects.SceneObject;
import tracer.objects.Sphere;

public class Scene {

	private ArrayList<SceneObject> objects = new ArrayList<SceneObject>();

	private SceneObject sphere1;
	private SceneObject sphere2;
	private SceneObject sphere3;
	
	private V3 move1;
	private V3 move2;
	private V3 move3;
	
	public final V3 camera = new V3();
	public final V3 lookAt = new V3();
	public final V3 look = new V3();
	public final V3 horz = new V3();
	public final V3 vert = new V3();

	public final V3 light = new V3();
	
	public Scene() {
		buildScene();
		calibrateView();
	}

	private void calibrateView() {
		camera.set(2, -10, 7);
		lookAt.set(0, 1, 0);

		light.set(-15, -3, 20);
		
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

		// sphere2 = new FastSphere(new V3(-3, 0, 1), 1);
		sphere2 = new InvisiSphere(new V3(-3, 0, 1), 1);
		sphere2.setMaterial(new Material(0, 1.0));

		// sphere3 = new FastSphere(new V3(-6, 6, 3), 1);
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

		sphere1.translate(move1);
		bounceBorder(sphere1.getPos(), move1, 2);

		sphere2.translate(move2);
		bounceBorder(sphere2.getPos(), move2, 1);

//		checkSphereCollision(sd);
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
		V3.put(dist);
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
}
