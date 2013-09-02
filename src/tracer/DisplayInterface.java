/*
 * This file is part of the SimpleRay project
 * 
 * Published as part of the FreeMapper project:
 * http://sourceforge.net/projects/freemapper/
 * 
 * author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */
package tracer;

import java.awt.Graphics;

/**
 * The tracer will use this to let a component display the 
 * calculated image.
 * 
 * @author Hj. Malthaner
 */
public interface DisplayInterface
{
    public int getHeight();
    public int getWidth();
 
    public void setline(int y, int[] linepix);

    public void paint(Graphics gr);
}
