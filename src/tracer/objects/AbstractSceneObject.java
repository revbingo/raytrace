/*
 * This file is part of the SimpleRay project
 * 
 * Published as part of the FreeMapper project:
 * http://sourceforge.net/projects/freemapper/
 * 
 * author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */

package tracer.objects;

import tracer.V3;

/**
 * 
 * @author Hj. Malthaner
 */
public abstract class AbstractSceneObject implements SceneObject {
	protected final static double ALMOST_ONE = 1.0 - 1E-13;
	protected final static double ONE_PLUS = 1.0 + 1E-10;

	protected Material material;
	protected double scaleX;
	protected double scaleY;
	protected double scaleZ;

	protected AbstractSceneObject() {
		this.material = new Material(0x777777, 0);

		scaleX = 1.0;
		scaleY = 1.0;
		scaleZ = 1.0;
	}

	@Override
	public void setMaterial(Material material) {
		this.material = material;
	}

	@Override
	public void scale(double x, double y, double z) {
		// some day this should become matrix operations ...
		scaleX = 1 / x;
		scaleY = 1 / y;
		scaleZ = 1 / z;
	}

	public void reflect(V3 ray, final V3 normal) {
		final double x = normal.x * scaleX;
		final double y = normal.y * scaleY;
		final double z = normal.z * scaleZ;

		final double f = 2.0 * V3.dot(x, y, z, ray) / (x * x + y * y + z * z);

		ray.x -= x * f;
		ray.y -= y * f;
		ray.z -= z * f;
	}

	public void fastReflect(V3 ray, V3 normal, double normlen2) {
		final double f = 2.0 * V3.dot(normal, ray) / normlen2;

		final double x = normal.x * f;
		final double y = normal.y * f;
		final double z = normal.z * f;

		ray.x -= x;
		ray.y -= y;
		ray.z -= z;
	}

	public int phong(V3 light, V3 oNormal, V3 ray) {
		final V3 normal = V3.make(oNormal);
		normal.mul(scaleX, scaleY, scaleZ);

		final double d = V3.dot(light, normal);

		final int sum;

		if (d <= 0) {
			// Hajo: this is the shadow side
			double ambient = 255.0 * material.ambient;
			sum = Math.min(255, (int) (ambient));
		} else {
			final V3 reflect = new V3(light);
			reflect(reflect, normal);

			final double l1 = light.length2();
			final double l2 = normal.length2();

			double theta = V3.dot(reflect, ray) / Math.sqrt(reflect.length2() * ray.length2());

			double specular = 255.0 * material.specular * Math.pow(theta, material.roughness);

			double ambient = 255.0 * material.ambient;
			double diffuse = 255.0 * material.diffuse * d / Math.sqrt(l1 * l2);

			sum = Math.min(255, (int) (ambient + diffuse + specular));
		}

		V3.put(normal);

		return sum;
	}

	@Override
	abstract public double trace(V3 camera, V3 ray, double raylen2);

	@Override
	abstract public long hit(V3 camera, V3 ray, V3 light, double t);
}
