package xyz.marsavic.gfxlab.graphics3d.solids;

import xyz.marsavic.gfxlab.graphics3d.Hit;
import xyz.marsavic.gfxlab.graphics3d.Ray;
import xyz.marsavic.gfxlab.graphics3d.Solid;


/**
 * The solid, with a bounding ball containing it. The solid is tested only for rays that hit the bounding ball, which
 * is faster when the solid is complex (a group of many solids) and most rays miss it.
 */
public record Bounded(
		Solid solid,
		Ball bound
) implements Solid {

	@Override
	public Hit firstHit(Ray ray, double afterTime) {
		// The ray misses the ball after afterTime only if it is outside the ball at afterTime, and so outside the solid.
		if (bound.firstHit(ray, afterTime).t() == Double.POSITIVE_INFINITY) {
			return Hit.AtPositiveInfinity.axisAlignedGoingIn(ray.d());
		}
		return solid.firstHit(ray, afterTime);
	}

}
