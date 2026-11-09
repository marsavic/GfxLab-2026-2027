package xyz.marsavic.gfxlab.graphics3d.cameras;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.graphics3d.Camera;
import xyz.marsavic.gfxlab.graphics3d.Ray;
import xyz.marsavic.gfxlab.math.Vec3;
import xyz.marsavic.utils.Numeric;


public record Perspective(
	double k
) implements Camera {
	
	/** The default camera looks in the direction of +y, with x to the right and z up (the sensor position (x, y) maps to the direction (x, 1, y)). */
	public static final Perspective DEFAULT = new Perspective(1);
	
	public static Perspective fov(double angle) {
		return new Perspective(Numeric.tanT(angle / 2));
	}

	
	@Override
	public Ray exitingRay(Vector p) {
		return Ray.pd(Vec3.ZERO, Vec3.yp(1, p.mul(k)));
	}
}
