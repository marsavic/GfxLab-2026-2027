package xyz.marsavic.gfxlab.graphics3d;

import xyz.marsavic.gfxlab.math.Affine3;
import xyz.marsavic.gfxlab.math.Vec3;
import xyz.marsavic.utils.Hash;
import xyz.marsavic.utils.Hashable;


/** A ray with the origin p and the direction vector d: the points p + t d, for t >= 0. d need not be normalized. */
public record Ray(Vec3 p, Vec3 d) implements Hashable {
	private static final Hash HASH = new Hash(0x8B92A123F8234490L);
	
	
	/** The ray with the origin p and the direction d. */
	public static Ray pd(Vec3 p, Vec3 d) {
		return new Ray(p, d);
	}
	
	
	/** The ray with the origin p, passing through q at time 1. */
	public static Ray pq(Vec3 p, Vec3 q) {
		return pd(p, q.sub(p));
	}
	
	
	/** The point of the ray at time t, p + t d. */
	public Vec3 at(double t) {
		return p.add(d.mul(t));
	}
	
	
	/** The ray with the same direction, starting at the point at time t. */
	public Ray moveTo(double t) {
		return Ray.pd(at(t), d());
	}
	
	
	/** The time at which the ray (as a line) is closest to the point x. */
	public double timeClosestTo(Vec3 x) {
		return x.sub(p).dot(d) / d.lengthSquared();
	}
	
	
	/** The distance between the point x and the line of the ray. */
	public double distanceTo(Vec3 x) {
		return x.sub(p).rejection(d).length();
	}
	
	
	/** The ray with the same origin and the normalized direction. Times along it are distances from the origin. */
	public Ray normalized_() {
		return Ray.pd(p(), d().normalized_());
	}

	
	/** The ray transformed by the affine transformation t, so that the time of each point is preserved. */
	public Ray transformed(Affine3 t) {
		return new Ray(t.at(p), t.applyWithoutTranslationTo(d));
	}

	
	@Override
	public Hash hash(Hash hash) {
		return hash.add(p).add(d);
	}

	
	@Override
	public int hashCode() {
		return hashCode(HASH);		
	}
	
}