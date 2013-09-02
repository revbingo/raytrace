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
import tracer.Texture;
import tracer.V3;

/**
 *
 * @author Hj. Malthaner
 */
public class TexturePatch extends AbstractSceneObject
{
    private final V3 pos;
    private final V3 forward;
    private final V3 right;

    private final double fLen;
    private final double rLen;
    
    private final Plane plane;
    private final Texture texture;
    
    public TexturePatch(V3 pos, V3 up, V3 forward, V3 right, Texture leaf)
    {
        super();
        this.texture = leaf;
        this.pos = new V3(pos);
        this.forward = new V3(forward);
        this.right = new V3(right);
        
        fLen = forward.length();
        rLen = right.length();
        
        this.forward.norm();
        this.right.norm();
        
        this.plane = new Plane(pos, up);
    }
    
    
    @Override
    public double trace(final V3 p, final V3 ray, final double raylen2)
    {
        final double t = plane.trace(p, ray, raylen2);
        
        if(t >= 0 && t < Double.MAX_VALUE)
        {
            p.add(ray, t);
            final int argb = getRGB(p);

            if((argb & 0xFF000000) == 0)
            {
                return Double.MAX_VALUE;
            }
        }
        
        return t;
    }

    @Override
    public long hit(V3 p, V3 ray, V3 light, double t)
    {
        p.add(ray, t*ALMOST_ONE);
        
        final int texBright = getRGB(p) & 0xFF;
        
        final V3 lv = new V3(light);
        lv.sub(p);        

        final int phongBright = phong(lv, plane.getNormal(), ray);
        
        return RGB.shade(material.color, (texBright + phongBright) / 2);
    }

    private int getRGB(final V3 p)
    {
        V3 hit = new V3(p);
        hit.sub(pos);
        
        final double s = V3.dot(forward, hit) / fLen;
        final double t = V3.dot(right, hit) / rLen;

        int argb = 0;
        
        if(s>=0 && t>=0 && s<=1 && t<=1)
        {
            argb = texture.getRGB(s, t);
        }
        
        return argb;
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
}
