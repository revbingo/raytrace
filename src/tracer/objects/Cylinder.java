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
public class Cylinder extends AbstractSceneObject {
	private V3 pos1;
	private V3 pos2;
	private final V3 direction;

	private final double r2;
	private final double len2;

	private Plane cap1;
	private Plane cap2;

	public Cylinder(V3 pos1, V3 pos2, double rad) {
		super();

		this.pos1 = pos1;
		this.pos2 = pos2;

		direction = new V3(pos2);
		direction.sub(pos1);

		r2 = rad * rad;
		len2 = direction.length2();

		cap1 = new Plane(pos1, direction);

		cap2 = new Plane(pos2, direction);
	}

	@Override
	public double trace(final V3 p, final V3 ray, final double raylen2) {
		V3 Y = V3.cross(ray, direction);

		final double a = 2 * V3.dot(Y, Y);

		if (a == 0) {
			// parallel, no intersection
			return Double.MAX_VALUE;
		}

		V3 X = V3.cross(p.x - pos1.x, p.y - pos1.y, p.z - pos1.z, direction);

		double d = r2 * len2;
		double b = 2 * V3.dot(X, Y);
		double c = 2 * (V3.dot(X, X) - d);

		// at^2 + bt + c = 0
		double disk = b * b - a * c;

		if (disk < 0) {
			// passes too far, no intersection
			return Double.MAX_VALUE;
		}

		double root = Math.sqrt(disk);

		// t = (-b +- sqrt(b*b - 4ac)) / 2a;

		double t1 = (-b + root) / a;
		double t2 = (-b - root) / a;

		double t = t2;

		if (t1 < t2 && t1 >= 0) {
			t = t1;
		}

		V3 tmp = X; // new V3(); -- re-use X object, save garbage

		if (t >= 0) {
			// test cyl height
			tmp.set(p);
			tmp.add(ray, t);
			tmp.sub(pos1);

			double height = V3.dot(direction, tmp);
			if (height > len2 || height < 0) {
				// too high or too low
				// test caps now
				double pt = testCaps(tmp, p, ray, raylen2);
				if (pt >= 0 && pt < Double.MAX_VALUE) {
					return pt;
				}
			} else {
				// cylinder body hit
				return t;
			}
		}

		return Double.MAX_VALUE;
	}

	private double testCaps(V3 tmp, V3 p, V3 ray, double raylen2) {
		double pt1 = cap1.trace(p, ray, raylen2);
		if (pt1 < Double.MAX_VALUE) {
			tmp.set(p);
			tmp.add(ray, pt1);
			tmp.sub(pos1);

			double dist = tmp.length2();

			if (dist > r2) {
				// no good, outside cylinder
				pt1 = Double.MAX_VALUE;
			}
		}

		double pt2 = cap2.trace(p, ray, raylen2);
		if (pt2 < Double.MAX_VALUE) {
			tmp.set(p);
			tmp.add(ray, pt2);
			tmp.sub(pos2);

			double dist = tmp.length2();

			if (dist > r2) {
				// no good, outside cylinder
				pt2 = Double.MAX_VALUE;
			}
		}

		return Math.min(pt1, pt2);
	}

	@Override
	public long hit(V3 p, V3 ray, V3 light, final double t) {
		p.add(ray, t * ALMOST_ONE);

		V3 tmp = new V3(p);
		tmp.sub(pos1);

		double a = V3.dot(tmp, direction) / len2;

		// calculate normal, store in tmp
		if (a <= 0) {
			tmp.set(direction);
		} else if (a >= 1) {
			tmp.set(direction);
		} else {

			tmp.set(p);
			tmp.add(direction, -a);
			tmp.sub(pos1);
		}

		if (material.reflection > 0) {
			reflect(ray, tmp);
			return -1L;
		} else {
			V3 lv = new V3(light);
			lv.sub(p);

			int phong = phong(lv, tmp, ray);
			return RGB.shade(material.color, phong);
		}
	}

	@Override
	public V3 getPos() {
		return pos1;
	}

	public void setPos(V3 v) {
		pos1.set(v);
	}

	@Override
	public void translate(V3 v) {
		pos1.add(v);
		pos2.add(v);
	}
}
