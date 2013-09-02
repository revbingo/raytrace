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
import tracer.Textures;
import tracer.V3;

/**
 *
 * @author Hj. Malthaner
 */
public class Plane extends AbstractSceneObject
{
    private final V3 pos;
    private final V3 normal;
    
    private V3 min;
    private V3 max;
    
    private final long [] checker;
    private final long outside;

    
    public Plane(V3 pos, V3 normal)
    {
        super();
        
        this.pos = new V3(pos);
        this.normal = new V3(normal);
        this.normal.norm();
        
        checker = new long [2];
        checker[0] = RGB.spread(255, 160, 0);
        checker[1] = RGB.spread(128, 80, 0);
    
        outside = RGB.spread(48, 64, 20);
    }
    
    public void setMin(V3 min)
    {
        this.min = min;
    }

    public void setMax(V3 max)
    {
        this.max = max;
    }
    
    @Override
    public double trace(final V3 camera, final V3 ray, double raylen2)
    {
        final double d = V3.dot(ray, normal);
        
        if(d != 0)
        {
            final double x = pos.x - camera.x;
            final double y = pos.y - camera.y;
            final double z = pos.z - camera.z;

            return V3.dot(x,y,z, normal) / d;
        }
        
        return Double.MAX_VALUE;
    }

    @Override
    public long hit(V3 p, V3 ray, V3 light, double t)
    {
        p.add(ray, t*ALMOST_ONE);
        
        if((min == null || (p.x >= min.x && p.y >= min.y && p.z >= min.z)) &&
           (max == null || (p.x <= max.x && p.y <= max.y && p.z <= max.z)))
        {
            final int px = (int)(p.x+1024);
            final int py = (int)(p.y+1024);
            final int f = px + py & 1;
            
            if(f == 0)
            {
                double fractX = p.x - Math.floor(p.x);
                double fractY = p.y - Math.floor(p.y);
                
                final int tx = (int)(Textures.sand.getWidth() * fractX);
                final int ty = (int)(Textures.sand.getHeight() * fractY);
                
                return RGB.spread(Textures.sand.getRGB(tx, ty));
            }
            else
            {
                return checker[f];
            }
        }
        else
        {
            return outside;
        }
    }

    @Override
    public void translate(V3 move)
    {
        pos.add(move);
    }

    @Override
    public V3 getPos()
    {
        return pos;
    }

    V3 getNormal()
    {
        return normal;
    }
}
