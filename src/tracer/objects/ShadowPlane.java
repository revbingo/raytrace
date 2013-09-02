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
 * A plane which is transparent but will carry shadows.
 * 
 * @author Hj. Malthaner
 */
public class ShadowPlane extends Plane
{
    public ShadowPlane(V3 pos, V3 normal)
    {
        super(pos, normal);
    }
    
    @Override
    public long hit(V3 p, V3 ray, V3 light, double t)
    {
        p.add(ray, t*ALMOST_ONE);
    
        // fully transparent black
        return 0xFF00000000000000L;
    }
}
