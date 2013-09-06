/*
 * This file is part of the SimpleRay project
 * 
 * Published as part of the FreeMapper project:
 * http://sourceforge.net/projects/freemapper/
 * 
 * author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */
package tracer;

/**
 * RGB long <> int representation conversion routines. Fake SIMD instructions,
 * using 64 bit longs.
 * 
 * @author Hj. Malthaner
 */
public class RGB {
	public static long spread(int r, int g, int b) {
		return ((long) r << 32) | (g << 16) | b;
	}

	public static int compact(long rgb) {
		return 0xFF000000 | (((int) (rgb >> 16)) & 0xFF0000) | (((int) rgb >> 8) & 0xFF00) | ((int) rgb & 0xFF);
	}

	static int shadeAndCompact(long lrgb, int brightness) {
		return compact(shade(lrgb, brightness));
	}

	public static long shade(long lrgb, int light) {
		final long c = (lrgb * light) >> 8;
		return (c & 0xFF00FF00FFL);
	}

	public static long spread(int rgb) {
		return ((((long) rgb) << 16) & 0xFF00000000L) | ((rgb << 8) & 0xFF0000) | (rgb & 0xFF);
	}
}
