/*
 * This file is part of the SimpleRay project
 * 
 * Published as part of the FreeMapper project:
 * http://sourceforge.net/projects/freemapper/
 * 
 * author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */
package tracer;

import tracer.objects.SceneObject;

/**
 * Data set for a raytracing thread.
 * 
 * @author Hj. Malthaner
 */
public class TracerDataSet {
	public final V3 currentRay = new V3();
	public final V3 camera = new V3();
	public final V3 tmpCurrentRay = new V3();
	public final V3 tmpCamera = new V3();
	public final V3 lineV = new V3();
	public final V3 hit = new V3();

	public int[] linepix;

	public SceneObject bestObject;

	final void updateLinepix(int width) {
		if (linepix == null || linepix.length != width) {
			linepix = new int[width];
		}
	}
}
