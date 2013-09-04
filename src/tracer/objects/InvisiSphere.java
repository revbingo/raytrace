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
 * Limited to simple round spheres, this is faster than the generic
 * sphere object, which also support scaling and rotation.
 * 
 * @author Hj. Malthaner
 */
public class InvisiSphere extends AbstractSceneObject
{
    private double r2;
    private V3 pos;
    
    public InvisiSphere(V3 pos, double rad)
    {
        super();
        
        this.pos = pos;
        r2 = rad*rad;
    }

    @Override
    public double distanceToIntersection(final V3 camera, final V3 ray, final double raylen2)
    {
//        final double a = camera.x - pos.x;
//        final double b = camera.y - pos.y;
//        final double c = camera.z - pos.z;
//        
//        final double e = V3.dot(a, b, c, ray);
//        
//        final double disk = e*e - raylen2*(a*a + b*b + c*c - r2);
//        
//        if(disk < 0)
//        {
//            return Double.MAX_VALUE;
//        }
//        else
//        {
//            final double t = (-e - Math.sqrt(disk));
//
//            if(t < 0.0001)
//            {
//                // intersection behind camera point
//                return Double.MAX_VALUE;                
//            }
//            
//            return t / raylen2;
//        }
    	
    	return Double.MAX_VALUE;
    }

    @Override
    public long hit(V3 p, V3 ray, V3 light, final double t)
    {
//        p.add(ray, t*ALMOST_ONE);
//  
//        V3 normal = V3.make(p);
//        normal.sub(pos);
//
//        final long color;
//        if(material.reflection > 0)
//        {
//            fastReflect(ray, normal, r2);
//
//            color = -1L;
//        }
//        else
//        {
//            V3 lv = new V3(light);
//            lv.sub(p);        
//            
//            final int phong = phong(lv, normal, ray);
//            color = RGB.shade(material.color, phong);
//        }
//        
//        V3.put(normal);
//        
//        return color;
    	
    	return -1L;
    }

    @Override
    public V3 getPos()
    {
        return pos;
    }
    
    public void setPos(V3 v)
    {
        pos.set(v);
    }

    @Override
    public void translate(V3 v)
    {
        pos.add(v);
    }
}
