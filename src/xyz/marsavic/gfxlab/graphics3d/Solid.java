package xyz.marsavic.gfxlab.graphics3d;


import xyz.marsavic.gfxlab.math.Affine3;
import xyz.marsavic.gfxlab.math.Vec3;

/** A solid: a part of the space, bounded by a surface. For each point it is determined whether it is inside the solid. */
public interface Solid {
	
	/**
	 * Returns the first hit of the ray into the surface of the solid, occurring strictly after the given time.
	 * If the ray misses the solid, the hit at infinity is returned.
	 * All hits along the same line should alternate between entering and exiting hits, meaning the dot product
	 * between the normal at the hit and line direction should alternate its sign. This should hold also for
	 * the hit at infinity.
	 */
	Hit firstHit(Ray ray, double afterTime);
	
	
	/** The first hit of the ray into the surface of the solid, occurring strictly after time 0. */
	default Hit firstHit(Ray ray) {
		return firstHit(ray, 0);
	}
	
	
	/** Whether there is a hit strictly after afterTime and before beforeTime. */
	default boolean hitBetween(Ray ray, double afterTime, double beforeTime) {
		double t = firstHit(ray, afterTime).t();
		return t < beforeTime;
	}
	
	
	/**
	 * Whether the point x is inside the solid. The first hit of any ray starting at x is an exiting hit iff x is inside.
	 */
	default boolean contains(Vec3 x) {
		return firstHit(Ray.pd(x, Vec3.EX)).n().x() > 0;
	}
	
	
	
}