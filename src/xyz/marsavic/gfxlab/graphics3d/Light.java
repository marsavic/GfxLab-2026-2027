package xyz.marsavic.gfxlab.graphics3d;

import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.math.Vec3;

/** A point light at the position p, with the color (intensity) c. */
public record Light(Vec3 p, Color c) {
	
	/** A point light at p with the color c. */
	public static Light pc(Vec3 p, Color c) {
		return new Light(p, c);
	}
	
	/** A white point light at p. */
	public static Light p(Vec3 p) {
		return pc(p, Color.WHITE);
	}
	
}
