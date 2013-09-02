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
 * The volume of one scene object minus another one.
 * 
 * @author Hj. Malthaner
 */
public class Difference extends AbstractSceneObject
{
    private final SceneObject subt;
    private final SceneObject minu;

    public Difference(SceneObject subt, SceneObject minu)
    {
        this.subt = subt;
        this.minu = minu;
    }

    @Override
    public double trace(V3 camera, V3 ray, double raylen2)
    {
        final double st1 = subt.trace(camera, ray, raylen2);
        double result = Double.MAX_VALUE;
        
        if(st1 > 0 && st1 < Double.MAX_VALUE)
        {
            final double mt1 = minu.trace(camera, ray, raylen2);
            
            if(st1 <= mt1)
            {
                result = st1;
            }
            else
            {
                final V3 p = V3.make(camera);
                p.add(ray, st1 * ONE_PLUS);
                
                final double mt2 = minu.trace(p, ray, raylen2);

                p.set(camera);
                p.add(ray, st1 * ONE_PLUS);

                final double st2 = subt.trace(p, ray, raylen2);
                
                if(mt2 <= st2)
                {
                    result = st1 + mt2;
                }
                
                V3.put(p);
            }
        }
        
        return result;
    }

    @Override
    public long hit(V3 camera, V3 ray, V3 light, double t)
    {        
        final double st1 = subt.trace(camera, ray, ray.length2());

        if(Math.abs(t - st1) < 0.000001)
        {
            return subt.hit(camera, ray, light, t);
        }
        else
        {
            final V3 revLight = V3.make(light);
            revLight.reverse();
            final long color = minu.hit(camera, ray, revLight, t);
            V3.put(revLight);
            return color;
        }
    }

    @Override
    public void translate(V3 move)
    {
        subt.translate(move);
        minu.translate(move);
    }

    @Override
    public V3 getPos()
    {
        return subt.getPos();
    }
}
