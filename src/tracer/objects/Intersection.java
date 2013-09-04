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
 * The common volume of two scene objects
 * 
 * @author Hj. Malthaner
 */
public class Intersection extends AbstractSceneObject
{
    private final SceneObject one;
    private final SceneObject two;

    public Intersection(SceneObject one, SceneObject two)
    {
        this.one = one;
        this.two = two;
    }

    @Override
    public double distanceToIntersection(V3 camera, V3 ray, double raylen2)
    {
        double t1 = one.distanceToIntersection(camera, ray, raylen2);
        double t2 = two.distanceToIntersection(camera, ray, raylen2);
        
        if(t1 >= 0 && t1 < Double.MAX_VALUE &&
           t2 >= 0 && t2 < Double.MAX_VALUE)
        {
            if(t1 < t2)
            {
                return t2;
            }
            else
            {
                return t1;
            }
        }
            
        // Hajo: no hit
        return Double.MAX_VALUE;
    }

    @Override
    public long hit(V3 camera, V3 ray, V3 light, double t)
    {        
        double mt1 = one.distanceToIntersection(camera, ray, ray.length2());

        // System.err.println("Hit at mt=" + mt1);
        
        if(Math.abs(mt1 - t) < 0.00000000001)
        {
            return one.hit(camera, ray, light, t);
        }
        else
        {
            long color = two.hit(camera, ray, light, t);
            // System.err.println("Backside hit at t=" + t + " color=" + color);
            return color;
        }
    }

    @Override
    public void translate(V3 move)
    {
        one.translate(move);
        two.translate(move);
    }

    @Override
    public V3 getPos()
    {
        return one.getPos();
    }
}
