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
	private SceneObject sphere2;
	
	private V3 move1;
	
	public Scene() {
		buildScene();
	}

	public void buildScene() {
		sphere1 = new Sphere(new V3(2, 0, 2), 3);
		sphere1.setMaterial(new Material(0xFF7FFF, 0));

		sphere2 = new Sphere(new V3(2, 0, 5), 1);
		sphere2.setMaterial(new Material(0xFF7FFF, 1));
		
		move1 = new V3(0.05, 0.07, 0);

		Plane floor = new Plane(new V3(), new V3(0, 0, 1));
		floor.setMin(new V3(-5, -5, -5));
		floor.setMax(new V3(5, 5, 5));

		objects.add(floor);
		objects.add(sphere1);
		objects.add(sphere2);
		
		light.set(-15, -3, 20);
	}

	public void animate() {
		sphere1.translate(move1);
		bounceBorder(sphere1.getPos(), move1, 2);
	}
	
	public ArrayList<SceneObject> getSceneObjects() {
		return objects;
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
