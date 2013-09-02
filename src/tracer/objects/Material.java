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

/**
 * SceneObject material data.
 * 
 * @author Hj. Malthaner
 */
public class Material
{
    public final long color;
    public final double reflection;
    public final double ambient;
    public final double diffuse;
    public final double specular;
    public final double roughness;
    
    public Material(int color, double reflection)
    {
        this.color = RGB.spread(color);
        this.reflection = reflection;
        
        this.ambient = 0.2;
        this.diffuse = 0.85;
        this.specular = 0.2;
        this.roughness = 10.0;
    }

    Material(int color, double ambient, double diffuse, double specular, double roughness, double reflection)
    {
        this.color = RGB.spread(color);
        
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.roughness = roughness;       

        this.reflection = reflection;
    }
}
