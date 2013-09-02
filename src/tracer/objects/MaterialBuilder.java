/*
 * This file is part of the SimpleRay project
 * 
 * Published as part of the FreeMapper project:
 * http://sourceforge.net/projects/freemapper/
 * 
 * author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */
package tracer.objects;

/**
 * 
 * @author Hj. Malthaner
 */
public class MaterialBuilder {
	public static MaterialBuilder begin() {
		return new MaterialBuilder();
	}

	private int color;
	private double reflection;
	private double ambient;
	private double diffuse;
	private double specular;
	private double roughness;

	private MaterialBuilder() {
		color = 0x7F7F7F;
		ambient = 0.2;
		diffuse = 0.8;
		specular = 0.2;
		roughness = 30;
		reflection = 0;
	}

	public MaterialBuilder color(int color) {
		this.color = color;
		return this;
	}

	public MaterialBuilder ambient(double ambient) {
		this.ambient = ambient;
		return this;
	}

	public MaterialBuilder diffuse(double diffuse) {
		this.diffuse = diffuse;
		return this;
	}

	public MaterialBuilder specular(double specular) {
		this.specular = specular;
		return this;
	}

	public MaterialBuilder roughness(double roughness) {
		this.roughness = roughness;
		return this;
	}

	public MaterialBuilder reflection(double reflection) {
		this.reflection = reflection;
		return this;
	}

	public Material end() {
		return new Material(color, ambient, diffuse, specular, roughness, reflection);
	}
}
