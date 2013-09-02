/*
 * This file is part of the SimpleRay project
 * 
 * Published as part of the FreeMapper project:
 * http://sourceforge.net/projects/freemapper/
 * 
 * author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */
package tracer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Sort of a texture cache.
 * 
 * @author Hj. Malthaner
 */
public class Textures {
	public static Texture sand;
	public static Texture clouds;
	public static Texture leafs[];

	public static void init() {
		final String path = "/tracer/resources/";
		leafs = new Texture[3];

		try {
			sand = new Texture(ImageIO.read(Class.class.getResource(path + "sand.jpg")));
			clouds = new Texture(ImageIO.read(Class.class.getResource(path + "clouds.jpg")));
			leafs[0] = new Texture(ImageIO.read(Class.class.getResource(path + "leaf_bw.png")));
			leafs[1] = new Texture(ImageIO.read(Class.class.getResource(path + "leaf_heart_bw.png")));
			leafs[2] = new Texture(ImageIO.read(Class.class.getResource(path + "petal_bw.png")));
		} catch (IOException ex) {
			Logger.getLogger(Textures.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
