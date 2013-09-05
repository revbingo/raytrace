/*
 * This file is part of the SimpleRay project
 * 
 * Published as part of the FreeMapper project:
 * http://sourceforge.net/projects/freemapper/
 * 
 * author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */

package tracer.objects;

import tracer.RGB;
import tracer.V3;

/**
 * 
 * @author Hj. Malthaner
 */
public class Sphere extends AbstractSceneObject {
	private double radiusSquared;
	private V3 pos;

	public Sphere(V3 pos, double rad) {
		super();

		this.pos = pos;
		radiusSquared = rad * rad;
	}

	@Override
	public double distanceToIntersection(final V3 camera, final V3 ray, final double raylen2) {
		double px = (camera.x - pos.x);
		double py = (camera.y - pos.y);
		double pz = (camera.z - pos.z);
		
		V3 cameraToObjectPosition = new V3(px, py, pz);

		final double projection = V3.dot(cameraToObjectPosition, ray);
		
		final double projectionSquared = projection * projection;
		
		final double lengthOfRaySquared = ray.length2();
		final double lengthOfCameraToObjectSquared = cameraToObjectPosition.length2();

		final double distanceToEdgeOfObject = lengthOfCameraToObjectSquared - radiusSquared;
		
		final double disk = projectionSquared - lengthOfRaySquared * (distanceToEdgeOfObject);

		if (disk < 0) {
			// intersection behind camera point
			return Double.MAX_VALUE;
		} else {
			//potentially intersect in two places, front and back
			final double root = Math.sqrt(disk);
			final double t1 = (-projection - root);
			final double t2 = (-projection + root);

			//assume it's the front 
			double t = t1;

			//but if that's behind us, we're in the middle of the sphere!
			if (t1 < 0) {
				t = t2;
			}

			return t / lengthOfRaySquared;
		}
	}

	@Override
	public long hit(V3 camera, V3 ray, V3 light, final double t) {
		camera.add(ray, t * ALMOST_ONE);

		V3 normal = V3.make(camera).sub(pos);

		final long color;
		if (material.reflection > 0) {
			reflect(ray, normal);

			color = -1L;
		} else {
			V3 lv = new V3(light);
			lv.sub(camera);

			final int phong = phong(lv, normal, ray);
			color = RGB.shade(material.color, phong);
		}

		V3.put(normal);

		return color;
	}

	@Override
	public V3 getPos() {
		return pos;
	}

	@Override
	public void translate(V3 v) {
		pos.add(v);
	}
}
