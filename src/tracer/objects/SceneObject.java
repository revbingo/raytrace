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
 * Scene object abstract base class.
 * 
 * @author Hj. Malthaner
 */
public interface SceneObject
{
    public void setMaterial(Material material);
    
    public double trace(V3 camera, V3 ray, double raylen2);
    public long hit(V3 camera, V3 ray, V3 light, double t);

    
    public void scale(double x, double y, double z);
    
    public void translate(V3 move);

    public V3 getPos();
}
