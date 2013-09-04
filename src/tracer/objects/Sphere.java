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
	private double r2;
	private V3 pos;

	public Sphere(V3 pos, double rad) {
		super();

		this.pos = pos;
		r2 = rad * rad;
	}

	@Override
	public double trace(final V3 camera, final V3 ray, final double raylen2) {
		double px = (camera.x - pos.x) * scaleX;
		double py = (camera.y - pos.y) * scaleY;
		double pz = (camera.z - pos.z) * scaleZ;

		double rx = ray.x * scaleX;
		double ry = ray.y * scaleY;
		double rz = ray.z * scaleZ;

		final double e = px * rx + py * ry + pz * rz;
		final double scaledLength2 = rx * rx + ry * ry + rz * rz;
		final double disk = e * e - scaledLength2 * (px * px + py * py + pz * pz - r2);

		if (disk < 0) {
			// intersection behind camera point
			return Double.MAX_VALUE;
		} else {
			final double root = Math.sqrt(disk);
			final double t1 = (-e - root);
			final double t2 = (-e + root);

			double t = t1;

			if (t1 < 0) {
				t = t2;
			}

			return t / scaledLength2;
		}
	}

	@Override
	public long hit(V3 camera, V3 ray, V3 light, final double t) {
		camera.add(ray, t * ALMOST_ONE);

		V3 normal = V3.make(camera);
		normal.sub(pos);

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

	public void setPos(V3 v) {
		pos.set(v);
	}

	@Override
	public void translate(V3 v) {
		pos.add(v);
	}
}
